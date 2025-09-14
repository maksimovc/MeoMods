package com.systmeo.wallet;

import com.systmeo.wallet.network.PacketHandler;
import com.systmeo.wallet.network.PacketSyncBalance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WalletManager {
    private static Map<UUID, Integer> balances = new HashMap<>();

    public static int getBalance(EntityPlayer player) {
        return balances.getOrDefault(player.getUniqueID(), 0);
    }

    public static void setBalance(EntityPlayer player, int amount) {
        balances.put(player.getUniqueID(), amount);
        save(player);
        if (!player.world.isRemote && player instanceof EntityPlayerMP) {
            PacketHandler.INSTANCE.sendTo(new PacketSyncBalance(amount), (EntityPlayerMP) player);
        }
    }

    public static void addBalance(EntityPlayer player, int amount) {
        setBalance(player, getBalance(player) + amount);
    }

    public static boolean removeBalance(EntityPlayer player, int amount) {
        int current = getBalance(player);
        if (current >= amount) {
            setBalance(player, current - amount);
            return true;
        }
        return false;
    }

    public static void save(EntityPlayer player) {
        NBTTagCompound persistent = player.getEntityData();
        NBTTagCompound data;
        if (!persistent.hasKey(EntityPlayer.PERSISTED_NBT_TAG)) {
            data = new NBTTagCompound();
            persistent.setTag(EntityPlayer.PERSISTED_NBT_TAG, data);
        } else {
            data = persistent.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        }
        data.setInteger("wallet_balance", getBalance(player));
    }

    public static void load(EntityPlayer player) {
        NBTTagCompound persistent = player.getEntityData();
        if (persistent.hasKey(EntityPlayer.PERSISTED_NBT_TAG)) {
            NBTTagCompound data = persistent.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
            if (data.hasKey("wallet_balance")) {
                balances.put(player.getUniqueID(), data.getInteger("wallet_balance"));
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        load(event.player);
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        save(event.player);
    }
}
