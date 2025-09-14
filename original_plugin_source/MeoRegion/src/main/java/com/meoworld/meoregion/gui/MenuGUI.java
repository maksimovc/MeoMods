package com.meoworld.meoregion.gui;

import com.meoworld.meoregion.MeoRegion;
import com.meoworld.meoregion.managers.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class MenuGUI implements InventoryHolder {
    private final Inventory inventory;
    private final MessageManager messageManager;

    public MenuGUI(MeoRegion plugin) {
        this.messageManager = plugin.getMessageManager();
        String title = messageManager.getMessage("main-menu.title");
        this.inventory = Bukkit.createInventory(this, 27, title);
        initializeItems();
    }

    private void initializeItems() {
        ItemStack filler = createGuiItem(Material.STAINED_GLASS_PANE, (short) 7, " ");
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }

        String myRegionsName = messageManager.getMessage("main-menu.my-regions");
        String myRegionsLore = messageManager.getMessage("main-menu.my-regions-lore");

        String buyMarketName = messageManager.getMessage("main-menu.buy-market");
        String buyMarketLore = messageManager.getMessage("main-menu.buy-market-lore");

        String rentMarketName = messageManager.getMessage("main-menu.rent-market");
        String rentMarketLore = messageManager.getMessage("main-menu.rent-market-lore");

        String helpName = messageManager.getMessage("settings.help");
        String helpLore = messageManager.getMessage("settings.help-lore");

        String settingsName = messageManager.getMessage("main-menu.settings");
        String settingsLore = messageManager.getMessage("main-menu.settings-lore");

        String closeName = messageManager.getMessage("gui.close-button");

        inventory.setItem(10, createGuiItem(Material.CHEST, myRegionsName, myRegionsLore));
        inventory.setItem(12, createGuiItem(Material.EMERALD, buyMarketName, buyMarketLore));
        inventory.setItem(14, createGuiItem(Material.DIAMOND, rentMarketName, rentMarketLore));
        inventory.setItem(16, createGuiItem(Material.BOOK, helpName, helpLore));
        inventory.setItem(22, createGuiItem(Material.COMPASS, settingsName, settingsLore));
        inventory.setItem(26, createGuiItem(Material.BARRIER, closeName));
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

    @Override
    public Inventory getInventory() { return inventory; }
}
