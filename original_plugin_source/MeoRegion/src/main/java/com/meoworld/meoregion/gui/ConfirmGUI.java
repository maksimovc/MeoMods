package com.meoworld.meoregion.gui;

import com.meoworld.meoregion.MeoRegion;
import com.meoworld.meoregion.managers.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class ConfirmGUI implements InventoryHolder {
    private final Inventory inventory;
    private final String action;
    private final String regionName;
    private final InventoryHolder parent;

    public ConfirmGUI(MeoRegion plugin, String action, String regionName, InventoryHolder parent) {
        this.action = action;
        this.regionName = regionName;
        this.parent = parent;
        MessageManager messageManager = plugin.getMessageManager();
        
        String title = messageManager.getMessage("confirm-gui.title").replace("{action}", action);
        this.inventory = Bukkit.createInventory(this, 27, title);
        initialize(messageManager);
    }

    private void initialize(MessageManager messageManager) {
        ItemStack yes = new ItemStack(Material.WOOL, 1, (short) 5);
        ItemMeta ym = yes.getItemMeta();
        if (ym != null) {
            ym.setDisplayName(messageManager.getMessage("confirm-gui.confirm"));
            ym.setLore(Collections.singletonList(messageManager.getMessage("confirm-gui.confirm-action").replace("{action}", this.action)));
            yes.setItemMeta(ym);
        }

        ItemStack no = new ItemStack(Material.WOOL, 1, (short) 14);
        ItemMeta nm = no.getItemMeta();
        if (nm != null) {
            nm.setDisplayName(messageManager.getMessage("confirm-gui.cancel"));
            nm.setLore(Collections.singletonList(messageManager.getMessage("confirm-gui.cancel-operation")));
            no.setItemMeta(nm);
        }

        inventory.setItem(11, yes);
        inventory.setItem(15, no);
    }

    public String getAction() { return action; }
    public String getRegionName() { return regionName; }

    public InventoryHolder getParent() {
        return parent;
    }

    @Override
    public Inventory getInventory() { return inventory; }
}
