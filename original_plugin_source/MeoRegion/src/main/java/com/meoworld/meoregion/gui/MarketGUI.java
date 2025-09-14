package com.meoworld.meoregion.gui;

import com.meoworld.meoregion.MeoRegion;
import com.meoworld.meoregion.data.Region;
import com.meoworld.meoregion.managers.MessageManager;
import com.meoworld.meoregion.util.PlayerHeadUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MarketGUI implements InventoryHolder {

    public enum Mode { SALE, RENT }

    private final Inventory inventory;
    private final MeoRegion plugin;
    private final MessageManager messageManager;
    private final int page;
    private final InventoryHolder parent;
    private final Mode mode;

    public MarketGUI(MeoRegion plugin, int page, InventoryHolder parent) {
        this(plugin, page, Mode.SALE, parent);
    }

    public MarketGUI(MeoRegion plugin, int page, Mode mode, InventoryHolder parent) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
        this.page = page;
        this.parent = parent;
        this.mode = mode == null ? Mode.SALE : mode;
        String title = messageManager.getMessage("market.title").replace("{page}", String.valueOf(page));
        this.inventory = Bukkit.createInventory(this, 54, title);
        initializeItems();
    }

    private void initializeItems() {
        List<Region> forSaleRegions;
        if (mode == Mode.RENT) {
            forSaleRegions = plugin.getRegionManager().getRegions().values().stream()
                    .filter(Region::isForRent)
                    .collect(Collectors.toList());
        } else {
            forSaleRegions = plugin.getRegionManager().getRegions().values().stream()
                    .filter(Region::isForSale)
                    .collect(Collectors.toList());
        }

        int startIndex = (page - 1) * 45;

        for (int i = 0; i < 45; i++) {
            int regionIndex = startIndex + i;
            if (regionIndex < forSaleRegions.size()) {
                Region region = forSaleRegions.get(regionIndex);
                inventory.setItem(i, createRegionItem(region));
            }
        }

        inventory.setItem(49, createGuiItem(Material.BARRIER, messageManager.getMessage("gui.back-button")));

        if (page > 1) {
            inventory.setItem(48, createGuiItem(Material.ARROW, messageManager.getMessage("gui.previous-page")));
        }
        if (forSaleRegions.size() > page * 45) {
            inventory.setItem(50, createGuiItem(Material.ARROW, messageManager.getMessage("gui.next-page")));
        }
    }

    private ItemStack createRegionItem(Region region) {
        ItemStack item;
        OfflinePlayer owner = null;
        if (region.getOwner().isPresent()) {
            owner = Bukkit.getOfflinePlayer(region.getOwner().get());
        }

        String price = plugin.getEconomyManager().format(region.getPrice());
        String size = String.valueOf(region.getSize());

        if (owner != null && owner.getName() != null) {
            List<String> lore = new ArrayList<>();
            lore.add(messageManager.getMessage("gui.price").replace("{price}", price));
            lore.add(messageManager.getMessage("gui.size").replace("{size}", size) + messageManager.getMessage("gui.chunks-suffix"));
            lore.add(" ");
            lore.add(mode == Mode.RENT ? messageManager.getMessage("market.rent-prompt") : messageManager.getMessage("market.buy-prompt"));

            item = PlayerHeadUtil.createPlayerHead(owner, ChatColor.AQUA + region.getName(), lore);
        } else {
            item = new ItemStack(Material.CHEST, 1);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.AQUA + region.getName());
                List<String> lore = new ArrayList<>();
                lore.add(messageManager.getMessage("gui.price").replace("{price}", price));
                lore.add(messageManager.getMessage("gui.size").replace("{size}", size) + messageManager.getMessage("gui.chunks-suffix"));
                lore.add(messageManager.getMessage("gui.owner-server"));
                lore.add(" ");
                lore.add(mode == Mode.RENT ? messageManager.getMessage("market.rent-prompt") : messageManager.getMessage("market.buy-prompt"));
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
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

    public int getPage() {
        return page;
    }

    public InventoryHolder getParent() {
        return parent;
    }

    public Mode getMode() { 
        return mode; 
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}