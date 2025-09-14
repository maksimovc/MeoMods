package com.systmeo.permissions.network;

import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import com.systmeo.permissions.Permissions;

public class PacketHandler {
    public static SimpleNetworkWrapper INSTANCE;

    public static void initChannel() {
        if (INSTANCE == null) {
            INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Permissions.MODID);
        }
    }

    // server-safe registration (no client-only handlers)
    public static void registerPackets() {
        initChannel();
        // server side packet registration (if any) can go here
    }

    // client-side only packet registrations
    public static void registerClientPackets() {
        if (INSTANCE == null) initChannel();
        INSTANCE.registerMessage(PacketSyncGroup.Handler.class, PacketSyncGroup.class, 0, Side.CLIENT);
    }
}
