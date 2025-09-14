package com.systmeo.permissions.proxy;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(net.minecraftforge.fml.common.event.FMLPreInitializationEvent event) {
        super.preInit(event);
        // register client-side packet handlers
        com.systmeo.permissions.network.PacketHandler.registerClientPackets();
    }
}
