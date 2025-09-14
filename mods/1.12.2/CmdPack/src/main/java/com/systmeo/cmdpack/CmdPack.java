package com.systmeo.cmdpack;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = CmdPack.MODID, name = CmdPack.NAME, version = CmdPack.VERSION)
public class CmdPack {
    public static final String MODID = "cmdpack";
    public static final String NAME = "CmdPack [MEO]";
    public static final String VERSION = "1.0.0-1.12.2";

    @Mod.Instance
    public static CmdPack instance;
    public static Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        instance = this;
        logger = event.getModLog();
        logger.info(NAME + " is loading!");
        // Ініціалізація набору команд
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Реєстрація команд
        logger.info(NAME + " initialized.");
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        logger.info("CmdPack server starting");
        // Додати команди тут
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
        logger.info("CmdPack server stopping");
        // Зберегти налаштування
    }
}