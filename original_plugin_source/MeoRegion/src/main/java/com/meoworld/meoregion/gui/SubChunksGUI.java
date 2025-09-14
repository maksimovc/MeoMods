package com.meoworld.meoregion.gui;

import com.meoworld.meoregion.MeoRegion;
import com.meoworld.meoregion.data.ChunkCoordinate;
import com.meoworld.meoregion.data.Region;
import com.meoworld.meoregion.data.SubChunk;
import com.meoworld.meoregion.managers.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SubChunksGUI implements InventoryHolder {

    private final Inventory inventory;
    private final MessageManager messageManager;
    private final Region region;
    private final Player player;
    private final InventoryHolder parent;

    public SubChunksGUI(MeoRegion plugin, Region region, Player player, InventoryHolder parent) {
        this.messageManager = plugin.getMessageManager();
        this.region = region;
        this.player = player;
        this.parent = parent;
        
        String title = messageManager.getMessage("subchunks.title").replace("{region}", region.getName());
        this.inventory = Bukkit.createInventory(this, 27, title);
        initializeItems();
    }

    private void initializeItems() {
        ItemStack filler = createGuiItem(Material.STAINED_GLASS_PANE, (short) 7, " ");
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }

        ChunkCoordinate currentCoord = new ChunkCoordinate(player.getLocation().getChunk().getX(), player.getLocation().getChunk().getZ());
        SubChunk currentSubChunk = region.getOrCreateSubChunk(currentCoord);
        
        String currentName = messageManager.getMessage("subchunks.current-subchunk");
        String currentLore = messageManager.getMessage("subchunks.current-subchunk-lore")
                .replace("{x}", String.valueOf(currentCoord.getX()))
                .replace("{z}", String.valueOf(currentCoord.getZ()));
        
        inventory.setItem(10, createGuiItem(Material.GRASS, currentName, currentLore));

        inventory.setItem(12, createGuiItem(Material.BEACON, messageManager.getMessage("subchunks.manage-flags"), messageManager.getMessage("subchunks.manage-flags-lore")));
        inventory.setItem(14, createGuiItem(Material.REDSTONE_BLOCK, messageManager.getMessage("subchunks.reset-flags"), messageManager.getMessage("subchunks.reset-flags-lore")));
        inventory.setItem(16, createGuiItem(Material.BOOK, messageManager.getMessage("subchunks.info"), messageManager.getMessage("subchunks.info-lore")));
        inventory.setItem(18, createGuiItem(Material.ARROW, messageManager.getMessage("gui.back-button")));

        if (!currentSubChunk.getFlags().isEmpty()) {
            List<String> flagsInfo = new ArrayList<>();
            flagsInfo.add(messageManager.getMessage("subchunks.set-flags-header"));
            currentSubChunk.getFlags().forEach((name, value) -> flagsInfo.add(messageManager.getMessage("info.flag-format").replace("{flag}", name).replace("{value}", value.toString())));
            
            ItemStack infoItem = createGuiItem(Material.PAPER, messageManager.getMessage("subchunks.subchunk-info-item"), flagsInfo.toArray(new String[0]));
            inventory.setItem(22, infoItem);
        }
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

    public Player getPlayer() {
        return player;
    }

    public InventoryHolder getParent() {
        return parent;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}