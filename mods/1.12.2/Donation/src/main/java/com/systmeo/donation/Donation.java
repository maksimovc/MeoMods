package com.systmeo.donation;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = Donation.MODID, name = Donation.NAME, version = Donation.VERSION)
public class Donation {
    public static final String MODID = "donation";
    public static final String NAME = "Donation [MEO]";
    public static final String VERSION = "1.0.0-1.12.2";

    @Mod.Instance
    public static Donation instance;
    public static Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        instance = this;
        logger = event.getModLog();
        logger.info(NAME + " is loading!");
        // Ініціалізація системи донатів
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Інтеграція з WalletAPI
        logger.info(NAME + " initialized.");
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        logger.info("Donation server starting");
        // Додати команди для донатів
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
        logger.info("Donation server stopping");
        // Зберегти дані донатів
    }
}