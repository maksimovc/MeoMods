package com.meoworld.meoregion.managers;

import com.meoworld.meoregion.MeoRegion;
import com.meoworld.meoregion.data.Region;
import com.meoworld.meoregion.economy.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public class TransactionManager {

    private final MeoRegion plugin;
    private final RegionManager regionManager;
    private final EconomyManager economyManager;
    private final MessageManager messageManager;
    private final ChangeLogManager changeLogManager;

    public TransactionManager(MeoRegion plugin) {
        this.plugin = plugin;
        this.regionManager = plugin.getRegionManager();
        this.economyManager = plugin.getEconomyManager();
        this.messageManager = plugin.getMessageManager();
        this.changeLogManager = plugin.getChangeLogManager();
    }

    public boolean buyRegion(Player player, String regionName) {
        if (economyManager == null || !economyManager.isEconomyEnabled()) {
            player.sendMessage(messageManager.getMessage("economy-disabled", true));
            return false;
        }
        Region region = regionManager.getRegionByName(regionName);
        if (region == null) {
            player.sendMessage(messageManager.getMessage("region-not-found", true).replace("{region}", regionName));
            return false;
        }
        if (!region.isForSale()) {
            player.sendMessage(messageManager.getMessage("region-not-for-sale-info", true));
            return false;
        }
        if (region.isOwner(player.getUniqueId())) {
            player.sendMessage(messageManager.getMessage("buy-own-region-error", true));
            return false;
        }
        if (!economyManager.hasEnough(player, region.getPrice())) {
            player.sendMessage(messageManager.getMessage("not-enough-money", true));
            return false;
        }

        if (!economyManager.withdraw(player, region.getPrice())) {
            player.sendMessage(messageManager.getMessage("transaction-failed-withdraw", true));
            return false;
        }

        try {
            Optional<UUID> ownerUUIDOpt = region.getOwner();
            if (ownerUUIDOpt.isPresent()) {
                OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerUUIDOpt.get());
                if (!economyManager.deposit(owner, region.getPrice())) {
                    plugin.getLogger().log(Level.SEVERE, "CRITICAL: Failed to deposit funds to seller " + (owner != null ? owner.getName() : "<unknown>") + ". Aborting transaction and refunding buyer " + player.getName());
                    throw new Exception("Failed to deposit to seller");
                }
            }

            region.transferOwnership(player.getUniqueId());
            region.setForSale(false);
            region.setPrice(0);

            changeLogManager.logChange(player.getName(), "BUY_REGION", region.getName());

            player.sendMessage(messageManager.getMessage("region-bought", true).replace("{region}", region.getName()));
            ownerUUIDOpt.map(Bukkit::getOfflinePlayer).ifPresent(owner -> {
                if (owner.isOnline() && owner.getPlayer() != null) {
                    owner.getPlayer().sendMessage(messageManager.getMessage("owner-region-bought", true).replace("{region}", region.getName()));
                }
            });
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "CRITICAL: An error occurred during region purchase. Attempting to refund player.", e);
            if (!economyManager.deposit(player, region.getPrice())) {
                plugin.getLogger().log(Level.SEVERE, "CRITICAL FAILURE: Could not refund " + player.getName() + " " + region.getPrice() + " after a failed region purchase. MANUAL INTERVENTION REQUIRED.");
            }
            player.sendMessage(messageManager.getMessage("transaction-failed-internal", true));
            return false;
        }
    }

    public void rentOrExtendRegion(Player player, String regionName) {
        if (economyManager == null || !economyManager.isEconomyEnabled()) {
            player.sendMessage(messageManager.getMessage("economy-disabled", true));
            return;
        }
        Region region = regionManager.getRegionByName(regionName);
        if (region == null) {
            player.sendMessage(messageManager.getMessage("region-not-found", true).replace("{region}", regionName));
            return;
        }
        if (!region.isForRent()) {
            player.sendMessage(messageManager.getMessage("not-for-rent", true));
            return;
        }
        UUID currentRenter = region.getRenter();
        if (currentRenter != null && !currentRenter.equals(player.getUniqueId())) {
            OfflinePlayer renterPlayer = Bukkit.getOfflinePlayer(currentRenter);
            player.sendMessage(messageManager.getMessage("already-rented", true).replace("{renter}", renterPlayer != null ? renterPlayer.getName() : "???"));
            return;
        }
        double price = region.getRentPrice();
        long period = region.getRentPeriod();
        if (price <= 0 || period <= 0) {
            player.sendMessage(messageManager.getMessage("not-for-rent", true));
            return;
        }
        if (!economyManager.hasEnough(player, price)) {
            player.sendMessage(messageManager.getMessage("not-enough-money", true));
            return;
        }

        if (!economyManager.withdraw(player, price)) {
            player.sendMessage(messageManager.getMessage("transaction-failed-withdraw", true));
            return;
        }

        try {
            Optional<UUID> ownerUUIDOpt = region.getOwner();
            if (ownerUUIDOpt.isPresent()) {
                OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerUUIDOpt.get());
                if (!economyManager.deposit(owner, price)) {
                    plugin.getLogger().log(Level.SEVERE, "CRITICAL: Failed to deposit rent funds to owner " + owner.getName() + ". Aborting transaction and refunding renter " + player.getName());
                    throw new Exception("Failed to deposit to owner");
                }
            }

            long now = System.currentTimeMillis();
            boolean isExtending = currentRenter != null && currentRenter.equals(player.getUniqueId()) && region.getRentDueDate() > now;

            if (isExtending) {
                region.setRentDueDate(region.getRentDueDate() + period);
                player.sendMessage(messageManager.getMessage("rent-extended", true).replace("{region}", region.getName()));
                changeLogManager.logChange(player.getName(), "RENT_EXTEND", region.getName());
            } else {
                region.setRenter(player.getUniqueId());
                region.setRentDueDate(now + period);
                player.sendMessage(messageManager.getMessage("rent-success", true).replace("{region}", region.getName()));
                changeLogManager.logChange(player.getName(), "RENT_START", region.getName());
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "CRITICAL: An error occurred during region rental. Attempting to refund player.", e);
            if (!economyManager.deposit(player, price)) {
                plugin.getLogger().log(Level.SEVERE, "CRITICAL FAILURE: Could not refund " + player.getName() + " " + price + " after a failed region rental. MANUAL INTERVENTION REQUIRED.");
            }
            player.sendMessage(messageManager.getMessage("transaction-failed-internal", true));
        }
    }
}
