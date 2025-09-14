package com.systmeo.alerts;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = Alerts.MODID, name = Alerts.NAME, version = Alerts.VERSION)
public class Alerts {
    public static final String MODID = "alerts";
    public static final String NAME = "Alerts [MEO]";
    public static final String VERSION = "1.0.0-1.12.2";

    @Mod.Instance
    public static Alerts instance;
    public static Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        instance = this;
        logger = event.getModLog();
        logger.info(NAME + " is loading!");
        // Тут можна ініціалізувати конфігурацію або менеджери
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Реєстрація подій або команд
        logger.info(NAME + " initialized.");
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        logger.info("Alerts server starting");
        // Додати команди тут
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
        logger.info("Alerts server stopping");
        // Зберегти дані тут
    }
}