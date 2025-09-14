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
import java.util.UUID;

public class MyRegionsGUI implements InventoryHolder {

    private final Inventory inventory;
    private final MeoRegion plugin;
    private final MessageManager messageManager;
    private final UUID playerUuid;
    private final int page;
    private final InventoryHolder parent;

    public MyRegionsGUI(MeoRegion plugin, UUID playerUuid, int page, InventoryHolder parent) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
        this.playerUuid = playerUuid;
        this.page = page;
        this.parent = parent;
        
        String title = messageManager.getMessage("my-regions.title").replace("{page}", String.valueOf(page));
        this.inventory = Bukkit.createInventory(this, 54, title);
        initializeItems();
    }

    private void initializeItems() {
        List<Region> playerRegions = plugin.getRegionManager().getPlayerRegions(playerUuid);
        
        if (playerRegions.isEmpty()) {
            inventory.setItem(22, createGuiItem(Material.BARRIER, messageManager.getMessage("my-regions.no-regions")));
            inventory.setItem(13, createGuiItem(Material.EMERALD, messageManager.getMessage("my-regions.create-region"), messageManager.getMessage("my-regions.create-region-lore")));
        } else {
            int startIndex = (page - 1) * 45;
            for (int i = 0; i < 45; i++) {
                int regionIndex = startIndex + i;
                if (regionIndex < playerRegions.size()) {
                    Region region = playerRegions.get(regionIndex);
                    inventory.setItem(i, createRegionItem(region));
                }
            }
            inventory.setItem(45, createGuiItem(Material.EMERALD, messageManager.getMessage("my-regions.create-region"), messageManager.getMessage("my-regions.create-region-lore")));
        }
        
        inventory.setItem(49, createGuiItem(Material.ARROW, messageManager.getMessage("gui.back-button")));
        
        if (page > 1) {
            inventory.setItem(48, createGuiItem(Material.ARROW, messageManager.getMessage("gui.previous-page")));
        }
        if (playerRegions.size() > page * 45) {
            inventory.setItem(50, createGuiItem(Material.ARROW, messageManager.getMessage("gui.next-page")));
        }
    }

    private ItemStack createRegionItem(Region region) {
        ItemStack item = new ItemStack(Material.CHEST, 1);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(region.getName());
            
            List<String> lore = new ArrayList<>();
            lore.add(messageManager.getMessage("my-regions.region-info").replace("{size}", String.valueOf(region.getSize())));
            
            if (region.isForSale()) {
                String forSaleMsg = messageManager.getMessage("my-regions.for-sale").replace("{price}", plugin.getEconomyManager().format(region.getPrice()));
                lore.add(messageManager.getMessage("my-regions.status") + forSaleMsg);
            } else if (region.isForRent()) {
                String forRentMsg = messageManager.getMessage("my-regions.for-rent").replace("{price}", plugin.getEconomyManager().format(region.getRentPrice()));
                lore.add(messageManager.getMessage("my-regions.status") + forRentMsg);
            }
            
            lore.add(" ");
            lore.add(messageManager.getMessage("my-regions.region-actions"));
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public int getPage() {
        return page;
    }

    public InventoryHolder getParent() {
        return parent;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}