package com.systmeo.wallet.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.Minecraft;

public class PacketSyncBalance implements IMessage {
    public int balance;

    public PacketSyncBalance() {}
    public PacketSyncBalance(int balance) {
        this.balance = balance;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.balance = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(balance);
    }

    public static class Handler implements IMessageHandler<PacketSyncBalance, IMessage> {
        @Override
        public IMessage onMessage(PacketSyncBalance message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                EntityPlayer player = Minecraft.getMinecraft().player;
                if (player != null) {
                    // Optionally update client-side cache or GUI
                }
            });
            return null;
        }
    }
}
