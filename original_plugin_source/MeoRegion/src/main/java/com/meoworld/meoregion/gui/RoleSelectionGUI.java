package com.meoworld.meoregion.gui;

import com.meoworld.meoregion.MeoRegion;
import com.meoworld.meoregion.data.Region;
import com.meoworld.meoregion.data.Role;
import com.meoworld.meoregion.managers.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

public class RoleSelectionGUI implements InventoryHolder {

    private final Inventory inventory;
    private final Region region;
    private final UUID targetPlayer;
    private final MessageManager messageManager;
    private final InventoryHolder parent;

    public RoleSelectionGUI(MeoRegion plugin, Region region, UUID targetPlayer, InventoryHolder parent) {
        this.region = region;
        this.targetPlayer = targetPlayer;
        this.messageManager = plugin.getMessageManager();
        this.parent = parent;
        OfflinePlayer player = Bukkit.getOfflinePlayer(targetPlayer);
        String title = messageManager.getMessage("role-selection-gui.title").replace("{player}", player.getName());
        this.inventory = Bukkit.createInventory(this, 9, title);
        initializeItems();
    }

    private void initializeItems() {
        inventory.setItem(0, createRoleItem(Role.CO_OWNER, Material.DIAMOND_BLOCK));
        inventory.setItem(1, createRoleItem(Role.MANAGER, Material.EMERALD_BLOCK));
        inventory.setItem(2, createRoleItem(Role.BUILDER, Material.IRON_BLOCK));
        inventory.setItem(3, createRoleItem(Role.MEMBER, Material.GOLD_BLOCK));

        ItemStack removeItem = new ItemStack(Material.BARRIER);
        ItemMeta removeMeta = removeItem.getItemMeta();
        if (removeMeta != null) {
            removeMeta.setDisplayName(messageManager.getMessage("role-selection-gui.remove-from-region"));
            removeMeta.setLore(Arrays.asList(
                messageManager.getMessage("role-selection-gui.remove-lore1"), 
                messageManager.getMessage("role-selection-gui.remove-lore2")
            ));
            removeItem.setItemMeta(removeMeta);
        }
        inventory.setItem(8, removeItem);
    }

    private ItemStack createRoleItem(Role role, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(role.getName());
            meta.setLore(Collections.singletonList(messageManager.getMessage("role-selection-gui.assign-role-lore")));
            item.setItemMeta(meta);
        }
        return item;
    }

    public Region getRegion() {
        return region;
    }

    public UUID getTargetPlayer() {
        return targetPlayer;
    }

    public InventoryHolder getParent() {
        return parent;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}