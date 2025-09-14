package com.meoworld.meoregion;

import com.meoworld.meoregion.commands.RegionCommand;
import com.meoworld.meoregion.economy.EconomyManager;
import com.meoworld.meoregion.gui.GuiListener;
import com.meoworld.meoregion.listeners.ChatInputListener;
import com.meoworld.meoregion.listeners.RegionListener;
import com.meoworld.meoregion.listeners.SignListener;
import com.meoworld.meoregion.managers.*;
import com.meoworld.meoregion.tasks.GridTask;
import com.meoworld.meoregion.tasks.RentalTask;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.logging.Level;

public class MeoRegion extends JavaPlugin {

    private RegionManager regionManager;
    private DataManager dataManager;
    private EconomyManager economyManager;
    private TemporaryFlagManager temporaryFlagManager;
    private MessageManager messageManager;
    private ChangeLogManager changeLogManager;
    private ChatInputManager chatInputManager;
    private TransactionManager transactionManager;
    private RegionCommand regionCommand;

    private BukkitTask autosaveTask;
    private BukkitTask backupTask;
    private BukkitTask rentalTask;

    @Override
    public void onEnable() {
        try {
            saveDefaultConfig();

            this.messageManager = new MessageManager(this);
            this.dataManager = new DataManager(this);
            this.regionManager = new RegionManager(this);
            this.economyManager = new EconomyManager(this);
            this.changeLogManager = new ChangeLogManager(this);
            this.chatInputManager = new ChatInputManager(this);
            this.temporaryFlagManager = new TemporaryFlagManager(this);
            this.transactionManager = new TransactionManager(this);

            regionManager.loadRegions(dataManager.loadRegions());
            regionManager.setupGlobalRegions();

            this.regionCommand = new RegionCommand(this);
            if (getCommand("rg") != null) {
                getCommand("rg").setExecutor(regionCommand);
                getCommand("rg").setTabCompleter(regionCommand);
            } else {
                getLogger().warning("Command 'rg' not defined in plugin.yml; skipping command registration.");
            }

            getServer().getPluginManager().registerEvents(new RegionListener(this), this);
            getServer().getPluginManager().registerEvents(new GuiListener(this, regionCommand), this);
            getServer().getPluginManager().registerEvents(new ChatInputListener(this), this);
            getServer().getPluginManager().registerEvents(new SignListener(this), this);

            new GridTask(this).runTaskTimer(this, 0L, 20L);
            scheduleTasks();

            getLogger().info("MeoRegion v1.0.0 has been enabled!");
        } catch (Throwable t) {
            getLogger().severe("An error occurred while enabling MeoRegion. Plugin will be disabled.");
            getLogger().log(Level.SEVERE, "Error enabling plugin", t);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        try {
            if (autosaveTask != null) autosaveTask.cancel();
            if (backupTask != null) backupTask.cancel();
            if (rentalTask != null) rentalTask.cancel();

            if (this.regionManager != null && this.dataManager != null) {
                getLogger().info("Saving regions before disabling...");
                saveRegions(false);

                if (getConfig().getBoolean("backups.backup-on-disable", false)) {
                    getLogger().info("Performing final backup...");
                    dataManager.runBackup();
                }
            }

            if (this.dataManager != null) {
                dataManager.close();
            }
        } catch (Throwable t) {
            getLogger().log(Level.SEVERE, "An error occurred while disabling MeoRegion.", t);
        }
        getLogger().info("MeoRegion v1.0.0 has been disabled!");
    }

    public void reload() {
        reloadConfig();
        messageManager.loadMessages();

        if (autosaveTask != null) autosaveTask.cancel();
        if (backupTask != null) backupTask.cancel();
        if (rentalTask != null) rentalTask.cancel();
        scheduleTasks();

        try {
            regionManager.loadRegions(dataManager.loadRegions());
            regionManager.setupGlobalRegions();
            getLogger().info("MeoRegion has been reloaded successfully.");
        } catch (Throwable t) {
            getLogger().log(Level.SEVERE, "An error occurred during region data reload. The plugin may be in an unstable state.", t);
        }
    }

    private void scheduleTasks() {
        long autosaveInterval = getConfig().getLong("performance.autosave-interval-minutes", 5) * 20 * 60;
        if (autosaveInterval > 0) {
            this.autosaveTask = getServer().getScheduler().runTaskTimerAsynchronously(this, () -> saveRegions(true), autosaveInterval, autosaveInterval);
        }

        long backupInterval = getConfig().getLong("backups.interval-minutes", 360) * 20 * 60;
        if (getConfig().getBoolean("backups.enabled", true) && backupInterval > 0) {
            this.backupTask = getServer().getScheduler().runTaskTimerAsynchronously(this, dataManager::runBackup, backupInterval, backupInterval);
        }

        if (economyManager != null && economyManager.isEconomyEnabled()) {
            long rentalInterval = getConfig().getLong("economy.rental-check-interval-seconds", 60) * 20;
            if (rentalInterval > 0) {
                this.rentalTask = getServer().getScheduler().runTaskTimer(this, new RentalTask(this), rentalInterval, rentalInterval);
            }
        }
    }

    public void saveRegions(boolean async) {
        if (regionManager == null || dataManager == null) return;
        if (async) {
            getServer().getScheduler().runTaskAsynchronously(this, () -> {
                dataManager.saveRegions(regionManager.getRegions());
                getLogger().info("Regions saved asynchronously.");
            });
        } else {
            dataManager.saveRegions(regionManager.getRegions());
            getLogger().info("Regions saved synchronously.");
        }
    }

    //<editor-fold desc="Manager Getters">
    public RegionManager getRegionManager() { return regionManager; }
    public EconomyManager getEconomyManager() { return economyManager; }
    public TemporaryFlagManager getTemporaryFlagManager() { return temporaryFlagManager; }
    public MessageManager getMessageManager() { return messageManager; }
    public ChangeLogManager getChangeLogManager() { return changeLogManager; }
    public ChatInputManager getChatInputManager() { return chatInputManager; }
    public TransactionManager getTransactionManager() { return transactionManager; }
    public RegionCommand getRegionCommand() { return regionCommand; }
    //</editor-fold>
}
