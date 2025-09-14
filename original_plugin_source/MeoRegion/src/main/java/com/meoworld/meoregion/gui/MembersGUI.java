package com.meoworld.meoregion.gui;

import com.meoworld.meoregion.MeoRegion;
import com.meoworld.meoregion.data.Region;
import com.meoworld.meoregion.data.Role;
import com.meoworld.meoregion.managers.MessageManager;
import com.meoworld.meoregion.util.PlayerHeadUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class MembersGUI implements InventoryHolder {

    private final Inventory inventory;
    private final Region region;
    private final int page;
    private final MessageManager messageManager;
    private final InventoryHolder parent;

    public MembersGUI(MeoRegion plugin, Region region, int page, InventoryHolder parent) {
        this.messageManager = plugin.getMessageManager();
        this.region = region;
        this.page = page;
        this.parent = parent;
        String title = messageManager.getMessage("members-gui.title").replace("{region}", region.getName()) + messageManager.getMessage("gui.page-suffix").replace("{page}", String.valueOf(page));
        this.inventory = Bukkit.createInventory(this, 54, title);
        initializeItems();
    }

    private void initializeItems() {
        region.getOwner().ifPresent(ownerUuid -> {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerUuid);
            if (owner != null) {
                List<String> ownerLore = new ArrayList<>();
                ownerLore.add(messageManager.getMessage("gui.role").replace("{role}", Role.OWNER.getName()));
                ownerLore.add(" ");
                ownerLore.add(messageManager.getMessage("gui.region-owner"));
                inventory.setItem(4, PlayerHeadUtil.createPlayerHead(owner, ownerLore));
            }
        });

        List<Map.Entry<UUID, Role>> participants = region.getParticipants().entrySet().stream()
                .filter(entry -> entry.getValue() != Role.OWNER)
                .sorted(Map.Entry.comparingByValue(Comparator.comparingInt(Role::getLevel).reversed()))
                .collect(Collectors.toList());

        int startIndex = (page - 1) * 45;

        for (int i = 0; i < 45; i++) {
            int userIndex = startIndex + i;
            if (userIndex < participants.size()) {
                Map.Entry<UUID, Role> entry = participants.get(userIndex);
                OfflinePlayer player = Bukkit.getOfflinePlayer(entry.getKey());
                Role role = entry.getValue();

                List<String> lore = new ArrayList<>();
                lore.add(messageManager.getMessage("gui.role").replace("{role}", role.getName()));
                lore.add(" ");
                lore.add(messageManager.getMessage("gui.lmb-info"));
                lore.add(messageManager.getMessage("gui.rmb-change-role"));

                inventory.setItem(i, PlayerHeadUtil.createPlayerHead(player, lore));
            }
        }

        inventory.setItem(45, createGuiItem(Material.EMERALD, messageManager.getMessage("members-gui.add-member-button"), messageManager.getMessage("members-gui.add-member-lore")));
        inventory.setItem(49, createGuiItem(Material.ARROW, messageManager.getMessage("gui.back-button")));

        if (page > 1) {
            inventory.setItem(48, createGuiItem(Material.ARROW, messageManager.getMessage("gui.previous-page")));
        }
        if (participants.size() > page * 45) {
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

    public InventoryHolder getParent() {
        return parent;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}