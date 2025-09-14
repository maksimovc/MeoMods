package com.systmeo.permissions;

import com.systmeo.permissions.managers.DataManager;
import com.systmeo.permissions.managers.PermissionManager;
import com.systmeo.permissions.data.User;
import com.systmeo.permissions.network.PacketHandler;
import com.systmeo.permissions.network.PacketSyncGroup;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Facade that bridges commands/events to the managers.PermissionManager and managers.DataManager.
 */
public class PermissionsManager {

    private static final PermissionManager permissionManager = new PermissionManager();
    
    /**
     * Gets the permission manager instance
     * @return The permission manager
     */
    public static PermissionManager getPermissionManager() {
        return permissionManager;
    }
    private static final DataManager dataManager = new DataManager();
    private static boolean dataLoaded = false;
    public static synchronized void ensureLoaded() {
        if (dataLoaded) return;
        Map<String, com.systmeo.permissions.data.Group> groups = dataManager.loadGroups();
        Map<UUID, com.systmeo.permissions.data.User> users = dataManager.loadUsers();
        Map<String, com.systmeo.permissions.data.Track> tracks = dataManager.loadTracks();
        if (groups != null) permissionManager.getGroups().putAll(groups);
        if (users != null) permissionManager.getUsers().putAll(users);
        if (tracks != null) permissionManager.getTracks().putAll(tracks);
        dataLoaded = true;
    }

    public static String getGroup(EntityPlayer player) {
        ensureLoaded();
        UUID uuid = player.getUniqueID();
        Optional<com.systmeo.permissions.data.Group> g = permissionManager.getPrimaryGroup(uuid);
        return g.map(com.systmeo.permissions.data.Group::getName).orElse("default");
    }

    public static void setGroup(EntityPlayer player, String group) {
        ensureLoaded();
        UUID uuid = player.getUniqueID();
        com.systmeo.permissions.data.User user = permissionManager.getOrCreateUser(uuid);
        // remove existing groups
        user.getGroups().forEach(n -> user.removeGroup(n.getGroupName()));
        user.addGroup(group, 0);
        permissionManager.invalidateCache(uuid);
        save(player);
        saveAll();
        if (!player.world.isRemote && player instanceof EntityPlayerMP) {
            PacketHandler.INSTANCE.sendTo(new PacketSyncGroup(group), (EntityPlayerMP) player);
        }
    }

    public static void addPermission(String group, String permission) {
        ensureLoaded();
        com.systmeo.permissions.data.Group g = permissionManager.createGroup(group);
        g.addPermission(permission, 0);
        permissionManager.invalidateAllCaches();
        saveAll();
    }

    public static void removePermission(String group, String permission) {
        ensureLoaded();
        com.systmeo.permissions.data.Group g = permissionManager.getGroup(group);
        if (g != null) {
            g.removePermission(permission);
            permissionManager.invalidateAllCaches();
            saveAll();
        }
    }

    public static boolean hasPermission(EntityPlayer player, String permission) {
        ensureLoaded();
        return permissionManager.hasPermission(player.getUniqueID(), permission);
    }

    public static void save(EntityPlayer player) {
        // store player's primary group to persistent NBT for compatibility
        UUID uuid = player.getUniqueID();
        Optional<com.systmeo.permissions.data.Group> g = permissionManager.getPrimaryGroup(uuid);
        String groupName = g.map(com.systmeo.permissions.data.Group::getName).orElse("default");

        NBTTagCompound persistent = player.getEntityData();
        NBTTagCompound data;
        if (!persistent.hasKey(EntityPlayer.PERSISTED_NBT_TAG)) {
            data = new NBTTagCompound();
            persistent.setTag(EntityPlayer.PERSISTED_NBT_TAG, data);
        } else {
            data = persistent.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        }
        data.setString("permissions_group", groupName);
    }

    public static void load(EntityPlayer player) {
        ensureLoaded();
        NBTTagCompound persistent = player.getEntityData();
        if (persistent.hasKey(EntityPlayer.PERSISTED_NBT_TAG)) {
            NBTTagCompound data = persistent.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
            if (data.hasKey("permissions_group")) {
                String group = data.getString("permissions_group");
                UUID uuid = player.getUniqueID();
                com.systmeo.permissions.data.User user = permissionManager.getOrCreateUser(uuid);
                // ensure the group is present on the user
                user.getGroups().forEach(n -> user.removeGroup(n.getGroupName()));
                user.addGroup(group, 0);
                permissionManager.invalidateCache(uuid);
                // send sync to client
                if (!player.world.isRemote && player instanceof EntityPlayerMP) {
                    PacketHandler.INSTANCE.sendTo(new PacketSyncGroup(group), (EntityPlayerMP) player);
                }
            }
        }
    }

    public static void saveAll() {
        ensureLoaded();
        dataManager.saveGroups(permissionManager.getGroups());
        dataManager.saveUsers(permissionManager.getUsers());
        dataManager.saveTracks(permissionManager.getTracks());
    }

    public static void loadAll() {
        ensureLoaded();
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        // load global data on first login
        ensureLoaded();
        load(event.player);
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        save(event.player);
        saveAll();
    }
}
