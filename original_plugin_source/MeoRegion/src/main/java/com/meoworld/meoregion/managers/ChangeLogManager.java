package com.meoworld.meoregion.managers;

import com.meoworld.meoregion.MeoRegion;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChangeLogManager {
    private final MeoRegion plugin;
    private final boolean enabled;
    private final File logFile;
    private final long maxLogSize;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public ChangeLogManager(MeoRegion plugin) {
        this.plugin = plugin;
        this.enabled = plugin.getConfig().getBoolean("changelog.enabled", true);

        if (!this.enabled) {
            this.logFile = null;
            this.maxLogSize = 0;
            return;
        }

        this.logFile = new File(plugin.getDataFolder(), "changelog.log");
        this.maxLogSize = plugin.getConfig().getLong("changelog.rotate-size-mb", 5) * 1024 * 1024;

        try {
            File parent = logFile.getParentFile();
            if (parent != null && !parent.exists()) {
                if (!parent.mkdirs()) {
                    plugin.getLogger().warning("Could not create changelog directory: " + parent.getAbsolutePath());
                }
            }
            if (!logFile.exists()) {
                if (!logFile.createNewFile()) {
                    plugin.getLogger().warning("Could not create new changelog file: " + logFile.getAbsolutePath());
                }
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not create changelog file", e);
        }
    }

    public void logChange(String actor, String action, String regionName) {
        if (!enabled) {
            return;
        }

        String line = String.format("[%s] [%s] %s: %s", sdf.format(new Date()), actor == null ? "SYSTEM" : actor, action, regionName == null ? "" : regionName);

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            synchronized (this) {
                try {
                    if (maxLogSize > 0 && logFile.length() > maxLogSize) {
                        rotateLog();
                    }
                    try (FileWriter fw = new FileWriter(logFile, true)) {
                        fw.write(line + System.lineSeparator());
                    }
                } catch (IOException e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to write to changelog", e);
                }
            }
        });
    }

    private void rotateLog() throws IOException {
        File oldLog = new File(plugin.getDataFolder(), "changelog.log.old");
        if (oldLog.exists()) {
            if (!oldLog.delete()) {
                plugin.getLogger().warning("Could not delete old changelog file: " + oldLog.getAbsolutePath());
            }
        }

        if (logFile.exists()) {
            Files.move(logFile.toPath(), oldLog.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        String line = String.format("[%s] [SYSTEM] Log file rotated due to size limit.", sdf.format(new Date()));
        try (FileWriter fw = new FileWriter(logFile, false)) {
            fw.write(line + System.lineSeparator());
        }
    }

    public List<String> getChanges(String regionName, int page) {
        if (!enabled || !logFile.exists()) {
            return Collections.emptyList();
        }

        List<String> allLines;
        try (Stream<String> lines = Files.lines(logFile.toPath())) {
            allLines = lines.collect(Collectors.toList());
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not read changelog file", e);
            return Collections.emptyList();
        }

        String filter = ": " + regionName;
        List<String> matchingLines = allLines.stream()
                .filter(line -> line.endsWith(filter))
                .collect(Collectors.toList());

        Collections.reverse(matchingLines);

        int linesPerPage = 10;
        int startIndex = (page - 1) * linesPerPage;
        if (startIndex >= matchingLines.size()) {
            return Collections.emptyList();
        }

        int endIndex = Math.min(startIndex + linesPerPage, matchingLines.size());
        return matchingLines.subList(startIndex, endIndex);
    }
}
