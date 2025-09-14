package com.systmeo.cosmetics;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = Cosmetics.MODID, name = Cosmetics.NAME, version = Cosmetics.VERSION)
public class Cosmetics {
    public static final String MODID = "cosmetics";
    public static final String NAME = "Cosmetics [MEO]";
    public static final String VERSION = "1.0.0-1.12.2";

    @Mod.Instance
    public static Cosmetics instance;
    public static Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        instance = this;
        logger = event.getModLog();
        logger.info(NAME + " is loading!");
        // Ініціалізація декоративних предметів
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Реєстрація предметів
        logger.info(NAME + " initialized.");
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        logger.info("Cosmetics server starting");
        // Додати команди для косметики
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
        logger.info("Cosmetics server stopping");
        // Зберегти дані
    }
}