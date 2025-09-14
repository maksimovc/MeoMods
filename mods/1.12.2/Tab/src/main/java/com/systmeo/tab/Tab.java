package com.systmeo.tab;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tab.MODID, name = Tab.NAME, version = Tab.VERSION)
public class Tab {
    public static final String MODID = "tab";
    public static final String NAME = "Tab [MEO]";
    public static final String VERSION = "1.0.0-1.12.2";

    @Mod.Instance
    public static Tab instance;
    public static Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        instance = this;
        logger = event.getModLog();
        logger.info(NAME + " is loading!");
        // Ініціалізація налаштувань TAB списку
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Інтеграція з PermissionsAPI
        logger.info(NAME + " initialized.");
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        logger.info("Tab server starting");
        // Додати команди для TAB
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
        logger.info("Tab server stopping");
        // Зберегти налаштування TAB
    }
}