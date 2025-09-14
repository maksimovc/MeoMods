package com.systmeo.wallet.network;

import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import com.systmeo.wallet.Wallet;

public class PacketHandler {
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Wallet.MODID);

    // Server-safe: register server-side handlers here if any in future
    public static void registerServerPackets() {
        INSTANCE.registerMessage(PacketSyncBalance.Handler.class, PacketSyncBalance.class, 0, Side.CLIENT);
    }
}
