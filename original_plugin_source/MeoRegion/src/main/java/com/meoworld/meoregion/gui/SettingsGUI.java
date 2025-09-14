package com.meoworld.meoregion.gui;

import com.meoworld.meoregion.MeoRegion;
import com.meoworld.meoregion.commands.RegionCommand;
import com.meoworld.meoregion.managers.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.UUID;

public class SettingsGUI implements InventoryHolder {

    private final Inventory inventory;
    private final MeoRegion plugin;
    private final MessageManager messageManager;
    private final UUID playerUuid;
    private final InventoryHolder parent;

    public SettingsGUI(MeoRegion plugin, UUID playerUuid, InventoryHolder parent) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
        this.playerUuid = playerUuid;
        this.parent = parent;
        
        String title = messageManager.getMessage("settings.title");
        this.inventory = Bukkit.createInventory(this, 27, title);
        initializeItems();
    }

    private void initializeItems() {
        ItemStack filler = createGuiItem(Material.STAINED_GLASS_PANE, (short) 7, " ");
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }

        Player player = Bukkit.getPlayer(playerUuid);
        if (player == null) return;

        String languageName = messageManager.getMessage("settings.language");
        String currentLanguage = plugin.getConfig().getString("language", "uk");
        String languageDisplay = currentLanguage.equals("uk") ? messageManager.getMessage("settings.lang-uk") : messageManager.getMessage("settings.lang-en");
        String languageLore = messageManager.getMessage("settings.language-lore").replace("{language}", languageDisplay);
        inventory.setItem(10, createGuiItem(Material.BOOK, languageName, languageLore));

        String gridName = messageManager.getMessage("settings.grid-toggle");
        String gridLore = messageManager.getMessage("settings.grid-toggle-lore");
        boolean gridEnabled = RegionCommand.gridViewPlayers.containsKey(playerUuid);
        Material gridMaterial = gridEnabled ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK;
        String gridStatus = gridEnabled ? messageManager.getMessage("settings.enabled") : messageManager.getMessage("settings.disabled");
        inventory.setItem(12, createGuiItem(gridMaterial, gridName, gridLore, " ", gridStatus));

        String previewName = messageManager.getMessage("settings.preview-toggle");
        String previewLore = messageManager.getMessage("settings.preview-toggle-lore");
        inventory.setItem(14, createGuiItem(Material.GLASS, previewName, previewLore));

        String helpName = messageManager.getMessage("settings.help");
        String helpLore = messageManager.getMessage("settings.help-lore");
        inventory.setItem(16, createGuiItem(Material.COMPASS, helpName, helpLore));

        inventory.setItem(18, createGuiItem(Material.ARROW, messageManager.getMessage("gui.back-button")));
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

    public InventoryHolder getParent() {
        return parent;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
