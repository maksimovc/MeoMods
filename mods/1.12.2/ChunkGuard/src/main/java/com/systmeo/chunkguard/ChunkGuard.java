package com.systmeo.chunkguard;

import com.systmeo.chunkguard.commands.CommandChunkGuard;
import com.systmeo.chunkguard.listeners.ProtectionListener;
import com.systmeo.chunkguard.managers.DataManager;
import com.systmeo.chunkguard.managers.RegionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.Logger;

@Mod(modid = ChunkGuard.MODID, name = ChunkGuard.NAME, version = ChunkGuard.VERSION)
public class ChunkGuard {
    public static final String MODID = "chunkguard";
    public static final String NAME = "ChunkGuard [MEO]";
    public static final String VERSION = "1.0.0";

    public static Logger logger;
    private static ChunkGuard instance;
    private static RegionManager regionManager;
    private static DataManager dataManager;

    public ChunkGuard() {
        instance = this;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        logger.info(NAME + " is loading!");

        dataManager = new DataManager();
        regionManager = new RegionManager(this);
        regionManager.loadRegions(dataManager.loadRegions());
        logger.info("Loaded " + regionManager.getRegions().size() + " regions.");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new ProtectionListener());
        logger.info(NAME + " protection listener registered.");
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        logger.info("Server is starting, registering commands and setting up global regions for " + NAME);
        event.registerServerCommand(new CommandChunkGuard());
        regionManager.setupGlobalRegions();
        logger.info("Commands and global regions for " + NAME + " have been set up.");
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
        logger.info("Server is stopping, saving regions for " + NAME);
        dataManager.saveRegions(regionManager.getRegions());
        logger.info("Regions for " + NAME + " have been saved.");
    }

    public static RegionManager getRegionManager() {
        return regionManager;
    }

    public static DataManager getDataManager() {
        return dataManager;
    }

    public static ChunkGuard getInstance() {
        return instance;
    }
}
