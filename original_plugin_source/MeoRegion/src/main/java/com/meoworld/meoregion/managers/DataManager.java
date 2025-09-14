package com.meoworld.meoregion.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.meoworld.meoregion.MeoRegion;
import com.meoworld.meoregion.data.Region;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class DataManager {

    private final MeoRegion plugin;
    private final File regionsFile;
    private final File regionsTempFile;
    private final File regionsBackupFile;
    private final File backupDir;
    private final Gson gson;

    private boolean useSQLite;
    private Connection sqliteConnection;

    public DataManager(MeoRegion plugin) {
        this.plugin = plugin;
        this.regionsFile = new File(plugin.getDataFolder(), "regions.json");
        this.regionsTempFile = new File(plugin.getDataFolder(), "regions.json.tmp");
        this.regionsBackupFile = new File(plugin.getDataFolder(), "regions.json.bak");
        this.backupDir = new File(plugin.getDataFolder(), "backups");
        if (!backupDir.exists() && !backupDir.mkdirs()) {
            plugin.getLogger().warning("Could not create backup directory: " + backupDir.getAbsolutePath());
        }

        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .enableComplexMapKeySerialization()
                .create();

        String storageType = plugin.getConfig().getString("storage.type", "json");
        this.useSQLite = "sqlite".equalsIgnoreCase(storageType);
        if (useSQLite) {
            try {
                initSQLite();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to initialize SQLite storage. Falling back to JSON.", e);
                this.useSQLite = false;
            }
        }
    }

    private void initSQLite() throws SQLException {
        File dbFile = new File(plugin.getDataFolder(), "regions.db");
        if (!dbFile.getParentFile().exists() && !dbFile.getParentFile().mkdirs()) {
            plugin.getLogger().warning("Could not create parent directory for DB: " + dbFile.getParent());
        }

        this.sqliteConnection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
        try (Statement stmt = sqliteConnection.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS regions_data (key TEXT PRIMARY KEY, json_data TEXT, updated_at INTEGER)");
        }
        plugin.getLogger().info("Initialized SQLite storage for regions: " + dbFile.getAbsolutePath());
    }

    public Map<String, Region> loadRegions() {
        if (useSQLite) {
            if (sqliteConnection != null) {
                return loadRegionsFromSQLite();
            }
            plugin.getLogger().severe("SQLite is enabled, but the connection is not available. Cannot load regions.");
            return new ConcurrentHashMap<>();
        }
        return loadRegionsFromJson();
    }

    private Map<String, Region> loadRegionsFromJson() {
        File fileToLoad = regionsFile;
        if (!fileToLoad.exists()) {
            plugin.getLogger().warning("regions.json not found. Trying to load from backup (regions.json.bak)...");
            fileToLoad = regionsBackupFile;
            if (!fileToLoad.exists()) {
                plugin.getLogger().info("Backup file not found. Starting with an empty region list.");
                return new ConcurrentHashMap<>();
            }
        }

        try (Reader reader = new FileReader(fileToLoad)) {
            Type type = new TypeToken<ConcurrentHashMap<String, Region>>() {}.getType();
            Map<String, Region> loadedRegions = gson.fromJson(reader, type);
            if (loadedRegions == null) {
                plugin.getLogger().severe("Failed to parse " + fileToLoad.getName() + "! It might be corrupted or empty. Starting fresh.");
                return new ConcurrentHashMap<>();
            }
            plugin.getLogger().info("Successfully loaded " + loadedRegions.size() + " regions from " + fileToLoad.getName());
            return loadedRegions;
        } catch (IOException | JsonSyntaxException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not load regions from " + fileToLoad.getName() + "!", e);
            if (fileToLoad.equals(regionsFile) && regionsBackupFile.exists()) {
                plugin.getLogger().info("Attempting to load from regions.json.bak as a fallback...");
                return loadRegionsFromBackup();
            }
            return new ConcurrentHashMap<>();
        }
    }

    private Map<String, Region> loadRegionsFromBackup() {
        try (Reader reader = new FileReader(regionsBackupFile)) {
            Type type = new TypeToken<ConcurrentHashMap<String, Region>>() {}.getType();
            Map<String, Region> loadedRegions = gson.fromJson(reader, type);
            if (loadedRegions == null) return new ConcurrentHashMap<>();
            plugin.getLogger().info("Successfully loaded " + loadedRegions.size() + " regions from backup file.");
            return loadedRegions;
        } catch (IOException | JsonSyntaxException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not load regions from backup file!", e);
            return new ConcurrentHashMap<>();
        }
    }

    private Map<String, Region> loadRegionsFromSQLite() {
        String json;
        try {
            json = getRegionsJsonFromSQLite();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fatal error reading regions from SQLite storage.", e);
            return new ConcurrentHashMap<>();
        }

        if (json == null || json.isEmpty()) {
            plugin.getLogger().info("No regions found in SQLite. Starting with an empty list.");
            return new ConcurrentHashMap<>();
        }

        try {
            Type type = new TypeToken<ConcurrentHashMap<String, Region>>() {}.getType();
            Map<String, Region> loadedRegions = gson.fromJson(json, type);
            if (loadedRegions == null) {
                plugin.getLogger().severe("Failed to parse regions JSON from SQLite. Data may be corrupt. Starting fresh.");
                return new ConcurrentHashMap<>();
            }
            plugin.getLogger().info("Successfully loaded " + loadedRegions.size() + " regions from SQLite.");
            return loadedRegions;
        } catch (JsonSyntaxException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to parse regions JSON from SQLite. Data is corrupt.", e);
            return new ConcurrentHashMap<>();
        }
    }

    public synchronized void saveRegions(Map<String, Region> regions) {
        if (useSQLite) {
            if (sqliteConnection != null) {
                saveRegionsToSQLite(regions);
            }
            return;
        }
        saveRegionsToJson(regions);
    }

    private void saveRegionsToJson(Map<String, Region> regions) {
        try (Writer writer = new FileWriter(regionsTempFile)) {
            gson.toJson(regions, writer);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not write regions to temporary file! Save aborted.", e);
            return;
        }

        try {
            if (regionsFile.exists()) {
                Files.move(regionsFile.toPath(), regionsBackupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            Files.move(regionsTempFile.toPath(), regionsFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not perform atomic file rename for safe save! Data may be in temp file.", e);
        }
    }

    private void saveRegionsToSQLite(Map<String, Region> regions) {
        String json = gson.toJson(regions);
        try (PreparedStatement ps = sqliteConnection.prepareStatement("INSERT OR REPLACE INTO regions_data(key, json_data, updated_at) VALUES(?,?,?)")) {
            ps.setString(1, "regions");
            ps.setString(2, json);
            ps.setLong(3, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save regions to SQLite storage", e);
        }
    }

    public void runBackup() {
        if (!plugin.getConfig().getBoolean("backups.enabled", true)) return;

        String date = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new java.util.Date());
        File backupFile = new File(backupDir, "regions-" + date + ".json.bak");

        try {
            boolean success = false;
            if (useSQLite && sqliteConnection != null) {
                String json = getRegionsJsonFromSQLite();
                if (json != null && !json.isEmpty()) {
                    try (Writer writer = new FileWriter(backupFile)) {
                        writer.write(json);
                        success = true;
                    }
                }
            } else if (regionsFile.exists()) {
                Files.copy(regionsFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                success = true;
            }

            if (success) {
                plugin.getLogger().info("Successfully created region backup: " + backupFile.getName());
                cleanupOldBackups();
            }
        } catch (IOException | SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to create region backup!", e);
        }
    }

    private String getRegionsJsonFromSQLite() throws SQLException {
        try (PreparedStatement ps = sqliteConnection.prepareStatement("SELECT json_data FROM regions_data WHERE key = ?")) {
            ps.setString(1, "regions");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return null;
    }

    private void cleanupOldBackups() {
        int maxBackups = plugin.getConfig().getInt("backups.max-backups-to-keep", 10);
        if (maxBackups <= 0) return;

        File[] backups = backupDir.listFiles((dir, name) -> name.endsWith(".json.bak"));
        if (backups != null && backups.length > maxBackups) {
            Arrays.sort(backups, Comparator.comparingLong(File::lastModified));
            int backupsToDelete = backups.length - maxBackups;
            for (int i = 0; i < backupsToDelete; i++) {
                if (backups[i].delete()) {
                    plugin.getLogger().info("Deleted old backup: " + backups[i].getName());
                }
            }
        }
    }

    /**
     * These methods form the basis for a potential future feature allowing admins to manage backups.
     * They are not currently used by any command, but are kept for future development.
     */
    @SuppressWarnings({"unused"})
    public List<File> listBackups() {
        File[] files = backupDir.listFiles((dir, name) -> name.endsWith(".json.bak"));
        if (files == null) return new ArrayList<>();
        List<File> backupFiles = new ArrayList<>(Arrays.asList(files));
        backupFiles.sort(Comparator.comparingLong(File::lastModified).reversed());
        return backupFiles;
    }

    @SuppressWarnings({"unused"})
    public Region loadRegionFromBackup(String regionName, File backupFile) {
        if (!backupFile.exists()) return null;
        try (Reader reader = new FileReader(backupFile)) {
            Type type = new TypeToken<ConcurrentHashMap<String, Region>>() {}.getType();
            Map<String, Region> backupRegions = gson.fromJson(reader, type);
            return backupRegions != null ? backupRegions.get(regionName.toLowerCase()) : null;
        } catch (IOException | JsonSyntaxException e) {
            plugin.getLogger().log(Level.WARNING, "Could not read backup file: " + backupFile.getName(), e);
            return null;
        }
    }

    public void close() {
        if (sqliteConnection != null) {
            try {
                if (!sqliteConnection.isClosed()) {
                    sqliteConnection.close();
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to close SQLite connection", e);
            }
        }
    }
}
