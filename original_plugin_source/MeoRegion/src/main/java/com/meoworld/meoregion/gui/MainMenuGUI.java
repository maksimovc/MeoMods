package com.meoworld.meoregion.gui;

import com.meoworld.meoregion.MeoRegion;
import com.meoworld.meoregion.data.Region;
import com.meoworld.meoregion.managers.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;

public class MainMenuGUI implements InventoryHolder {

    private final Inventory inventory;
    private final Region region;
    private final MessageManager messageManager;

    public MainMenuGUI(MeoRegion plugin, Region region) {
        this.region = region;
        this.messageManager = plugin.getMessageManager();

        String title = messageManager.getMessage("region-menu.title").replace("{region}", region.getName());

        this.inventory = Bukkit.createInventory(this, 27, title);
        initializeItems();
    }

    private void initializeItems() {
        ItemStack filler = createGuiItem(Material.STAINED_GLASS_PANE, (short) 7, " ");
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }

        inventory.setItem(11, createGuiItem(Material.BEACON, messageManager.getMessage("region-menu.flags-button"), messageManager.getMessage("region-menu.flags-lore")));
        inventory.setItem(13, createGuiItem(Material.SKULL_ITEM, (short) 3, messageManager.getMessage("region-menu.members-button"), messageManager.getMessage("region-menu.members-lore")));
        inventory.setItem(15, createGuiItem(Material.GOLD_INGOT, messageManager.getMessage("region-menu.economy-button"), messageManager.getMessage("region-menu.economy-lore")));
        inventory.setItem(17, createGuiItem(Material.QUARTZ_BLOCK, messageManager.getMessage("region-menu.subchunks-button"), messageManager.getMessage("region-menu.subchunks-lore")));
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
        return createGuiItem(material, (short) 0, name, lore);
    }

    private ItemStack createGuiItem(Material material, short durability, String name, String... lore) {
        ItemStack item = new ItemStack(material, 1, durability);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    public Region getRegion() {
        return region;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}