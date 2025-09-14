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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EconomyGUI implements InventoryHolder {

    private final Inventory inventory;
    private final Region region;
    private final MeoRegion plugin;
    private final MessageManager messageManager;
    private final InventoryHolder parent;

    public EconomyGUI(MeoRegion plugin, Region region, InventoryHolder parent) {
        this.plugin = plugin;
        this.region = region;
        this.messageManager = plugin.getMessageManager();
        this.parent = parent;
        String title = messageManager.getMessage("economy-gui.title").replace("{region}", region.getName());
        this.inventory = Bukkit.createInventory(this, 27, title);
        initializeItems();
    }

    private void initializeItems() {
        ItemStack filler = createFillerItem();
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }

        String saleName = messageManager.getMessage("economy-gui.set-sale-price");
        String saleLoreTemplate = messageManager.getMessage("economy-gui.set-sale-price-lore");
        List<String> saleLore = Arrays.asList(saleLoreTemplate.replace("{price}", plugin.getEconomyManager().format(region.getPrice())).split("\\n"));
        inventory.setItem(10, createGuiItem(Material.GOLD_NUGGET, saleName, saleLore));

        Material saleToggleMat = region.isForSale() ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK;
        String saleToggleName = region.isForSale() ? messageManager.getMessage("economy-gui.unsell") : messageManager.getMessage("economy-gui.sell");
        List<String> saleToggleLore = new ArrayList<>();
        saleToggleLore.add(region.isForSale() ? messageManager.getMessage("economy-gui.is-for-sale") : messageManager.getMessage("economy-gui.is-not-for-sale"));
        saleToggleLore.add(messageManager.getMessage("economy-gui.toggle-lore"));
        inventory.setItem(11, createGuiItem(saleToggleMat, saleToggleName, saleToggleLore));

        String rentName = messageManager.getMessage("economy-gui.set-rent-details");
        String rentLoreTemplate = messageManager.getMessage("economy-gui.set-rent-details-lore");
        List<String> rentLore = Arrays.asList(rentLoreTemplate.replace("{price}", plugin.getEconomyManager().format(region.getRentPrice())).replace("{duration}", formatDuration(region.getRentPeriod())).split("\\n"));
        inventory.setItem(15, createGuiItem(Material.IRON_INGOT, rentName, rentLore));

        boolean isForRent = region.getRentPeriod() > 0 && region.getRentPrice() > 0;
        Material rentToggleMat = isForRent ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK;
        String rentToggleName = isForRent ? messageManager.getMessage("economy-gui.unrent") : messageManager.getMessage("economy-gui.rent");
        inventory.setItem(16, createGuiItem(rentToggleMat, rentToggleName, messageManager.getMessage("economy-gui.toggle-lore")));

        inventory.setItem(18, createGuiItem(Material.ARROW, messageManager.getMessage("gui.back-button")));
    }

    private String formatDuration(long millis) {
        if (millis <= 0) return messageManager.getMessage("gui.duration-not-set");
        long days = millis / 86400000;
        long hours = (millis % 86400000) / 3600000;
        long minutes = (millis % 3600000) / 60000;
        return String.format("%dd %dh %dm", days, hours, minutes);
    }

    private ItemStack createFillerItem() {
        ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createGuiItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
        return createGuiItem(material, name, Arrays.asList(lore));
    }

    public Region getRegion() {
        return region;
    }

    public InventoryHolder getParent() {
        return parent;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}