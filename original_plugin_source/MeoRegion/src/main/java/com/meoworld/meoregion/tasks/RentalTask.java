package com.meoworld.meoregion.tasks;

import com.meoworld.meoregion.MeoRegion;
import com.meoworld.meoregion.data.Region;
import com.meoworld.meoregion.managers.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import java.util.UUID;

public class RentalTask implements Runnable {

    private final MeoRegion plugin;
    private final MessageManager messageManager;

    public RentalTask(MeoRegion plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
    }

    @Override
    public void run() {
        if (!plugin.getEconomyManager().isEconomyEnabled()) {
            return;
        }

        long currentTime = System.currentTimeMillis();

        for (Region region : plugin.getRegionManager().getRegions().values()) {
            if (region.getRenter() != null && region.getRentDueDate() > 0 && region.getRentPeriod() > 0 && currentTime > region.getRentDueDate()) {

                UUID renterUUID = region.getRenter();
                OfflinePlayer renter = Bukkit.getOfflinePlayer(renterUUID);
                OfflinePlayer owner = region.getOwner().map(Bukkit::getOfflinePlayer).orElse(null);

                if (plugin.getEconomyManager().withdraw(renter, region.getRentPrice())) {
                    if (owner != null) {
                        plugin.getEconomyManager().deposit(owner, region.getRentPrice());
                    }

                    region.setRentDueDate(currentTime + region.getRentPeriod());

                    if (renter.isOnline()) {
                        String message = messageManager.getMessage("rent-extended", true);
                        ((Player) renter).sendMessage(message.replace("{region}", region.getName()));
                    }
                } else {
                    region.setRenter(null);
                    region.setRentDueDate(0);

                    if (owner != null && owner.isOnline()) {
                        String message = messageManager.getMessage("owner-renter-evicted", true);
                        ((Player) owner).sendMessage(message.replace("{renter}", renter.getName()).replace("{region}", region.getName()));
                    }
                    if (renter.isOnline()) {
                        String message = messageManager.getMessage("renter-evicted", true);
                        ((Player) renter).sendMessage(message.replace("{region}", region.getName()));
                    }
                }
            }
        }
    }
}