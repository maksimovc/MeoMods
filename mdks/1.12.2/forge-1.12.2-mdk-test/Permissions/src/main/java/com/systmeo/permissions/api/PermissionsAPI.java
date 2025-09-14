package com.systmeo.permissions.api;

import net.minecraft.entity.player.EntityPlayer;
import com.systmeo.permissions.PermissionsManager;

/**
 * Permissions API для використання іншими модами
 */
public class PermissionsAPI {

    /**
     * Перевіряє, чи має гравець вказане право
     * @param player Гравець
     * @param permission Право для перевірки
     * @return true, якщо гравець має право
     */
    public static boolean hasPermission(EntityPlayer player, String permission) {
        return PermissionsManager.hasPermission(player, permission);
    }

    /**
     * Встановлює основну групу для гравця
     * @param player Гравець
     * @param groupName Назва групи
     */
    public static void setGroup(EntityPlayer player, String groupName) {
        PermissionsManager.setGroup(player, groupName);
    }

    /**
     * Додає право до групи
     * @param groupName Назва групи
     * @param permission Право
     */
    public static void addPermission(String groupName, String permission) {
        PermissionsManager.addPermission(groupName, permission);
    }

    /**
     * Видаляє право з групи
     * @param groupName Назва групи
     * @param permission Право
     */
    public static void removePermission(String groupName, String permission) {
        PermissionsManager.removePermission(groupName, permission);
    }

    /**
     * Створює нову групу
     * @param groupName Назва групи
     */
    public static void createGroup(String groupName) {
        PermissionsManager.ensureLoaded();
        PermissionsManager.getPermissionManager().createGroup(groupName);
        PermissionsManager.saveAll();
    }

    /**
     * Видаляє групу
     * @param groupName Назва групи
     */
    public static void deleteGroup(String groupName) {
        PermissionsManager.ensureLoaded();
        PermissionsManager.getPermissionManager().deleteGroup(groupName);
        PermissionsManager.saveAll();
    }

    /**
     * Перевіряє, чи існує група
     * @param groupName Назва групи
     * @return true, якщо група існує
     */
    public static boolean groupExists(String groupName) {
        PermissionsManager.ensureLoaded();
        return PermissionsManager.getPermissionManager().getGroup(groupName) != null;
    }

    /**
     * Отримує основну групу гравця
     * @param player Гравець
     * @return Назва основної групи або null
     */
    public static String getPlayerGroup(EntityPlayer player) {
        PermissionsManager.ensureLoaded();
        com.systmeo.permissions.data.User user = PermissionsManager.getPermissionManager().getOrCreateUser(player.getUniqueID());
        java.util.Set<com.systmeo.permissions.data.GroupNode> groups = user.getGroups();
        if (!groups.isEmpty()) {
            return groups.iterator().next().getGroupName();
        }
        return null;
    }
}