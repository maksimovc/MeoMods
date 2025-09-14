package com.meoworld.meoregion.gui;

import com.meoworld.meoregion.MeoRegion;
import com.meoworld.meoregion.data.ChunkCoordinate;
import com.meoworld.meoregion.data.Region;
import com.meoworld.meoregion.managers.MessageManager;
import com.meoworld.meoregion.util.Flag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FlagsGUI implements InventoryHolder {

    private final Inventory inventory;
    private final Region region;
    private final int page;
    private final MeoRegion plugin;
    private final MessageManager messageManager;
    private final boolean isSubChunkMode;
    private final ChunkCoordinate subChunkCoord;
    private final InventoryHolder parent;

    public FlagsGUI(MeoRegion plugin, Region region, int page, ChunkCoordinate subChunkCoord, InventoryHolder parent) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
        this.region = region;
        this.page = page;
        this.subChunkCoord = subChunkCoord;
        this.isSubChunkMode = subChunkCoord != null;
        this.parent = parent;

        String title;
        if (isSubChunkMode) {
            title = messageManager.getMessage("flags-gui.subchunk-title").replace("{region}", region.getName());
        } else {
            title = messageManager.getMessage("flags-gui.region-title").replace("{region}", region.getName());
        }

        this.inventory = Bukkit.createInventory(this, 54, title + messageManager.getMessage("gui.page-suffix").replace("{page}", String.valueOf(page)));
        initializeItems();
    }

    private void initializeItems() {
        List<Flag> flags = Arrays.stream(Flag.values()).collect(Collectors.toList());
        int startIndex = (page - 1) * 45;

        for (int i = 0; i < 45; i++) {
            int flagIndex = startIndex + i;
            if (flagIndex < flags.size()) {
                Flag flag = flags.get(flagIndex);

                Object valueOnObject = isSubChunkMode
                        ? region.getSubChunk(subChunkCoord).map(sc -> sc.getFlagValue(flag.getName())).orElse(null)
                        : region.getRawFlagValue(flag.getName());

                boolean isInherited = valueOnObject == null;

                Object displayValue = plugin.getRegionManager().getFlagValue(null, flag, region, subChunkCoord);

                Material material;
                String status;
                List<String> lore = new ArrayList<>();

                if (displayValue instanceof Boolean) {
                    material = (Boolean) displayValue ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK;
                    status = (Boolean) displayValue ? messageManager.getMessage("flags-gui.allowed") : messageManager.getMessage("flags-gui.denied");
                    lore.add(messageManager.getMessage("flags-gui.click-to-toggle"));
                } else {
                    material = Material.BOOK_AND_QUILL;
                    if (displayValue != null) {
                        status = "§e" + displayValue;
                    } else {
                        status = "§7" + messageManager.getMessage("gui.value-not-set");
                    }
                    lore.add(messageManager.getMessage("flags-gui.command-only"));
                }

                if (isInherited) {
                    lore.add(messageManager.getMessage("flags-gui.inherited"));
                }

                if (isSubChunkMode) {
                    lore.add(messageManager.getMessage("flags-gui.reset-to-region"));
                } else {
                    lore.add(messageManager.getMessage("flags-gui.reset-to-global"));
                }

                ItemStack item = createGuiItem(material, "§b" + flag.getName(), messageManager.getMessage("flags-gui.value").replace("{value}", status));
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
                inventory.setItem(i, item);
            }
        }

        if (isSubChunkMode) {
            inventory.setItem(47, createGuiItem(Material.COMPASS, messageManager.getMessage("flags-gui.goto-region-flags")));
        } else {
            inventory.setItem(47, createGuiItem(Material.MAP, messageManager.getMessage("flags-gui.goto-subchunk-flags"), messageManager.getMessage("flags-gui.current-location-subtext")));
        }

        inventory.setItem(49, createGuiItem(Material.BARRIER, messageManager.getMessage("gui.back-button")));

        if (page > 1) {
            inventory.setItem(48, createGuiItem(Material.ARROW, messageManager.getMessage("gui.previous-page")));
        }
        if (flags.size() > page * 45) {
            inventory.setItem(50, createGuiItem(Material.ARROW, messageManager.getMessage("gui.next-page")));
        }
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

    public Region getRegion() {
        return region;
    }

    public int getPage() {
        return page;
    }

    public boolean isSubChunkMode() {
        return isSubChunkMode;
    }

    public ChunkCoordinate getSubChunkCoordinate() {
        return subChunkCoord;
    }

    public InventoryHolder getParent() {
        return parent;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}