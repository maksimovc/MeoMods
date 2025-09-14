package com.systmeo.wallet;

import com.systmeo.wallet.network.PacketHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.Logger;

@Mod(modid = Wallet.MODID, name = Wallet.NAME, version = Wallet.VERSION)
public class Wallet {
    public static final String MODID = "wallet";
    public static final String NAME = "Wallet [MEO]";
    public static final String VERSION = "1.0.0-1.12.2";

    @Mod.Instance
    public static Wallet instance;
    public static Logger logger;

    @SidedProxy(clientSide = "com.systmeo.wallet.proxy.ClientProxy", serverSide = "com.systmeo.wallet.proxy.CommonProxy")
    public static com.systmeo.wallet.proxy.CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        instance = this;
        logger = event.getModLog();
        logger.info(NAME + " is loading!");
        MinecraftForge.EVENT_BUS.register(new WalletManager());
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
        PacketHandler.registerServerPackets();
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        logger.info("Wallet server starting");
        event.registerServerCommand(new com.systmeo.wallet.commands.CommandWallet());
        proxy.serverStarting(event);
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
        logger.info("Wallet server stopping");
        proxy.serverStopping(event);
    }
}
