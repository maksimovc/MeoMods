package com.systmeo.permissions;

import com.systmeo.permissions.network.PacketHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = Permissions.MODID, name = Permissions.NAME, version = Permissions.VERSION)
public class Permissions {
    public static final String MODID = "permissions";
    public static final String NAME = "Permissions [MEO]";
    public static final String VERSION = "1.0.0-1.12.2";

    @Mod.Instance
    public static Permissions instance;
    public static Logger logger;

    @SidedProxy(clientSide = "com.systmeo.permissions.proxy.ClientProxy", serverSide = "com.systmeo.permissions.proxy.CommonProxy")
    public static com.systmeo.permissions.proxy.CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        instance = this;
        logger = event.getModLog();
        logger.info(NAME + " is loading!");
        MinecraftForge.EVENT_BUS.register(new PermissionsManager());
        PacketHandler.registerPackets();
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        logger.info("Permissions server starting");
        proxy.serverStarting(event);
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
        logger.info("Permissions server stopping");
        proxy.serverStopping(event);
    }
}
