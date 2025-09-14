package com.systmeo.permissions.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import com.systmeo.permissions.Permissions;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.Minecraft;

public class PacketSyncGroup implements IMessage {
    public String group;

    public PacketSyncGroup() {}
    public PacketSyncGroup(String group) {
        this.group = group;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int len = buf.readInt();
        this.group = new String(buf.readBytes(len).array());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        byte[] arr = group.getBytes();
        buf.writeInt(arr.length);
        buf.writeBytes(arr);
    }

    public static class Handler implements IMessageHandler<PacketSyncGroup, IMessage> {
        @Override
        public IMessage onMessage(PacketSyncGroup message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                EntityPlayer player = Minecraft.getMinecraft().player;
                // Optionally update client-side cache or GUI
            });
            return null;
        }
    }
}
