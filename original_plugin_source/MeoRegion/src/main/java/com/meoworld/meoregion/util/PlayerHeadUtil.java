package com.meoworld.meoregion.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.logging.Level;

public class PlayerHeadUtil {

    public static ItemStack createPlayerHead(OfflinePlayer player, String displayName, List<String> lore) {
        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        if (meta != null) {
            try {
                meta.setOwningPlayer(player);
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.WARNING, "[MeoRegion] Could not fetch player skin for " + player.getName() + ". This might be due to offline mode or Mojang server issues. A default head will be used.");
            }

            meta.setDisplayName(displayName);
            if (lore != null) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }

        return item;
    }

    public static ItemStack createPlayerHead(OfflinePlayer player, List<String> lore) {
        String displayName = "§b" + (player.getName() != null ? player.getName() : "Unknown");
        return createPlayerHead(player, displayName, lore);
    }
}
