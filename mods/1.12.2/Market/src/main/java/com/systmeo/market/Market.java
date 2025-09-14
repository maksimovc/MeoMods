package com.systmeo.market;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = Market.MODID, name = Market.NAME, version = Market.VERSION)
public class Market {
    public static final String MODID = "market";
    public static final String NAME = "Market [MEO]";
    public static final String VERSION = "1.0.0-1.12.2";

    @Mod.Instance
    public static Market instance;
    public static Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        instance = this;
        logger = event.getModLog();
        logger.info(NAME + " is loading!");
        // Ініціалізація системи аукціонів
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Інтеграція з WalletAPI
        logger.info(NAME + " initialized.");
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        logger.info("Market server starting");
        // Додати команди для ринку
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
        logger.info("Market server stopping");
        // Зберегти дані аукціонів
    }
}