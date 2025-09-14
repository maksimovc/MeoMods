package com.systmeo.permissions;

import net.minecraft.entity.player.EntityPlayer;

import java.util.UUID;

/**
 * Public API for Permissions [MEO] mod.
 * Use this to check permissions from other mods.
 */
public class PermissionsAPI {

    /**
     * Check if a player has a specific permission.
     * @param playerUuid The UUID of the player.
     * @param permission The permission string.
     * @return true if the player has the permission, false otherwise.
     */
    public static boolean hasPermission(UUID playerUuid, String permission) {
        PermissionsManager.ensureLoaded();
        return PermissionsManager.getPermissionManager().hasPermission(playerUuid, permission);
    }

    /**
     * Check if a player has a specific permission (convenience method for EntityPlayer).
     * @param player The player entity.
     * @param permission The permission string.
     * @return true if the player has the permission, false otherwise.
     */
    public static boolean hasPermission(EntityPlayer player, String permission) {
        return hasPermission(player.getUniqueID(), permission);
    }

    /**
     * Get the primary group name of a player.
     * @param playerUuid The UUID of the player.
     * @return The group name, or "default" if none.
     */
    public static String getPrimaryGroup(UUID playerUuid) {
        PermissionsManager.ensureLoaded();
        return PermissionsManager.getGroup(null); // Need to implement properly
    }

    /**
     * Get the prefix of the player's primary group.
     * @param playerUuid The UUID of the player.
     * @return The prefix string.
     */
    public static String getPrefix(UUID playerUuid) {
        PermissionsManager.ensureLoaded();
        return PermissionsManager.getPermissionManager().getPrefix(playerUuid);
    }

    /**
     * Get the suffix of the player's primary group.
     * @param playerUuid The UUID of the player.
     * @return The suffix string.
     */
    public static String getSuffix(UUID playerUuid) {
        PermissionsManager.ensureLoaded();
        return PermissionsManager.getPermissionManager().getSuffix(playerUuid);
    }

    /**
     * Set a player's primary group.
     * @param player The player entity.
     * @param group The group name.
     */
    public static void setPrimaryGroup(EntityPlayer player, String group) {
        PermissionsManager.setGroup(player, group);
    }
}