package com.systmeo.chunkguard.listeners;

import com.systmeo.chunkguard.ChunkGuard;
import com.systmeo.chunkguard.data.Region;
import com.systmeo.chunkguard.data.Role;
import com.systmeo.chunkguard.util.Flag;
import com.systmeo.permissions.PermissionsAPI;
import net.minecraft.block.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ProtectionListener {

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        EntityPlayer player = event.getPlayer();
        if (player == null || player.isCreative()) return;

        // Check if player has admin bypass permission
        if (PermissionsAPI.hasPermission(player, "chunkguard.admin.bypass")) {
            return;
        }

        Region region = ChunkGuard.getRegionManager().getHighestPriorityRegionAt(event.getPos(), event.getWorld());
        // Якщо ми не в приватному регіоні (тобто в глобальному), то виходимо, нічого не робимо
        if (region == null || region.getName().startsWith("__global_")) {
            return;
        }

        // Якщо ми в приватному регіоні, перевіряємо, чи має гравець дозвіл на руйнування
        if (!region.hasPermission(player.getUniqueID(), Role.Permission.BUILD)) {
            event.setCanceled(true);
            sendDenyMessage(player, region, "У вас немає дозволу руйнувати блоки в цьому регіоні!");
        }
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.PlaceEvent event) {
        EntityPlayer player = event.getPlayer();
        if (player == null || player.isCreative()) return;

        // Check if player has admin bypass permission
        if (PermissionsAPI.hasPermission(player, "chunkguard.admin.bypass")) {
            return;
        }

        Region region = ChunkGuard.getRegionManager().getHighestPriorityRegionAt(event.getPos(), event.getWorld());
        if (region == null || region.getName().startsWith("__global_")) {
            return;
        }

        if (!region.hasPermission(player.getUniqueID(), Role.Permission.BUILD)) {
            event.setCanceled(true);
            sendDenyMessage(player, region, "У вас немає дозволу ставити блоки в цьому регіоні!");
        }
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player == null || player.isCreative()) return;

        // Check if player has admin bypass permission
        if (PermissionsAPI.hasPermission(player, "chunkguard.admin.bypass")) {
            return;
        }

        Region region = ChunkGuard.getRegionManager().getHighestPriorityRegionAt(event.getPos(), event.getWorld());
        if (region == null || region.getName().startsWith("__global_")) {
            return;
        }

        // Перевіряємо дозвіл на загальну взаємодію
        if (!region.hasPermission(player.getUniqueID(), Role.Permission.INTERACT)) {
            event.setCanceled(true);
            sendDenyMessage(player, region, "У вас немає дозволу на взаємодію в цьому регіоні!");
        }
    }

    @SubscribeEvent
    public void onPlayerPVP(LivingAttackEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer) || !(event.getSource().getTrueSource() instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer victim = (EntityPlayer) event.getEntityLiving();
        EntityPlayer attacker = (EntityPlayer) event.getSource().getTrueSource();

        // Check if attacker has admin bypass permission
        if (PermissionsAPI.hasPermission(attacker, "chunkguard.admin.bypass")) {
            return;
        }

        // Для PvP використовуємо прапори, оскільки це стосується всіх у регіоні
        boolean isPvpAllowed = ChunkGuard.getRegionManager().getFlagValueBoolean(attacker, Flag.PVP, victim.getPosition(), victim.world);

        if (!isPvpAllowed) {
            event.setCanceled(true);
            attacker.sendMessage(new TextComponentString(TextFormatting.RED + "PvP заборонено в цьому регіоні!"));
        }
    }

    private void sendDenyMessage(EntityPlayer player, Region region, String defaultMessage) {
        String denyMessage = ChunkGuard.getRegionManager().getFlagValueString(player, Flag.DENY_MESSAGE, player.getPosition(), player.world);

        if (!denyMessage.equalsIgnoreCase("none")) {
            player.sendMessage(new TextComponentString(TextFormatting.RED + denyMessage));
        } else {
            player.sendMessage(new TextComponentString(TextFormatting.RED + defaultMessage));
        }
    }
}
