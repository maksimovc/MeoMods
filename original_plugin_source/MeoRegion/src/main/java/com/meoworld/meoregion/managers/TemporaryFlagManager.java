package com.meoworld.meoregion.managers;

import com.meoworld.meoregion.MeoRegion;
import com.meoworld.meoregion.data.Region;
import com.meoworld.meoregion.util.Flag;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ConcurrentLinkedQueue;

public class TemporaryFlagManager {

    private final MeoRegion plugin;
    private final RegionManager regionManager;
    private final ChangeLogManager changeLogManager;
    private final ConcurrentLinkedQueue<TemporaryFlag> activeTempFlags = new ConcurrentLinkedQueue<>();

    public TemporaryFlagManager(MeoRegion plugin) {
        this.plugin = plugin;
        this.regionManager = plugin.getRegionManager();
        this.changeLogManager = plugin.getChangeLogManager();
        startExpirationTask();
    }

    public void addTemporaryFlag(Region region, Flag flag, Object newValue, long durationMillis) {
        long expirationTime = System.currentTimeMillis() + durationMillis;

        activeTempFlags.removeIf(tf -> tf.getRegionName().equals(region.getName()) && tf.getFlag() == flag);

        Object originalValue = region.getFlags().get(flag.getName());

        TemporaryFlag tempFlag = new TemporaryFlag(region.getName(), flag, originalValue, expirationTime);
        activeTempFlags.add(tempFlag);

        region.getFlags().put(flag.getName(), newValue);
    }

    private void checkAndRevertExpiredFlags() {
        long currentTime = System.currentTimeMillis();
        for (TemporaryFlag tempFlag : activeTempFlags) {
            if (currentTime >= tempFlag.getExpirationTime()) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    revertFlag(tempFlag);
                    activeTempFlags.remove(tempFlag);
                });
            }
        }
    }

    private void revertFlag(TemporaryFlag tempFlag) {
        Region region = regionManager.getRegionByName(tempFlag.getRegionName());
        if (region == null) return;

        Object originalValue = tempFlag.getOriginalValue();
        String flagName = tempFlag.getFlag().getName();

        if (originalValue != null) {
            region.getFlags().put(flagName, originalValue);
        } else {
            region.getFlags().remove(flagName);
        }

        changeLogManager.logChange("SYSTEM", "TEMP_FLAG_EXPIRED", region.getName() + "(" + flagName + ")");
    }

    private void startExpirationTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                checkAndRevertExpiredFlags();
            }
        }.runTaskTimerAsynchronously(plugin, 100L, 20L);
    }

    private static class TemporaryFlag {
        private final String regionName;
        private final Flag flag;
        private final Object originalValue;
        private final long expirationTime;

        public TemporaryFlag(String regionName, Flag flag, Object originalValue, long expirationTime) {
            this.regionName = regionName;
            this.flag = flag;
            this.originalValue = originalValue;
            this.expirationTime = expirationTime;
        }

        public String getRegionName() { return regionName; }
        public Flag getFlag() { return flag; }
        public Object getOriginalValue() { return originalValue; }
        public long getExpirationTime() { return expirationTime; }
    }
}
