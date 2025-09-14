package com.meoworld.meoregion.listeners;

import com.meoworld.meoregion.MeoRegion;
import com.meoworld.meoregion.data.Region;
import com.meoworld.meoregion.managers.MessageManager;
import com.meoworld.meoregion.managers.RegionManager;
import com.meoworld.meoregion.managers.TransactionManager;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignListener implements Listener {

    private final MeoRegion plugin;
    private final MessageManager messageManager;
    private final TransactionManager transactionManager;
    private final RegionManager regionManager;

    private static final String SIGN_TAG = "[MeoRegion]";
    private static final String SELL_ACTION = "sell";
    private static final String RENT_ACTION = "rent";

    public SignListener(MeoRegion plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
        this.transactionManager = plugin.getTransactionManager();
        this.regionManager = plugin.getRegionManager();
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (!ChatColor.stripColor(event.getLine(0)).equalsIgnoreCase(SIGN_TAG)) {
            return;
        }

        Player player = event.getPlayer();
        String action = event.getLine(1).toLowerCase();
        String regionName = event.getLine(2);
        String priceLine = event.getLine(3);

        if (regionName == null || regionName.trim().isEmpty()) {
            player.sendMessage(messageManager.getMessage("sign-error-no-region", true));
            event.getBlock().breakNaturally();
            return;
        }

        Region region = regionManager.getRegionByName(regionName);
        if (region == null) {
            player.sendMessage(messageManager.getMessage("region-not-found", true).replace("{region}", regionName));
            event.getBlock().breakNaturally();
            return;
        }

        if (!region.isOwner(player.getUniqueId()) && !player.hasPermission("meoregion.admin.sign")) {
            player.sendMessage(messageManager.getMessage("not-owner", true));
            event.getBlock().breakNaturally();
            return;
        }

        switch (action) {
            case SELL_ACTION:
                setupSellSign(event, player, region, priceLine);
                break;
            case RENT_ACTION:
                setupRentSign(event, player, region, priceLine);
                break;
            default:
                player.sendMessage(messageManager.getMessage("sign-error-invalid-action", true));
                event.getBlock().breakNaturally();
                break;
        }
    }

    private void setupSellSign(SignChangeEvent event, Player player, Region region, String priceLine) {
        double price;
        try {
            price = Double.parseDouble(priceLine);
            if (price <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            player.sendMessage(messageManager.getMessage("input.invalid-price-format", true));
            event.getBlock().breakNaturally();
            return;
        }

        region.setForSale(true);
        region.setPrice(price);

        event.setLine(0, messageManager.getMessage("commercial-signs.for-sale-line-1"));
        event.setLine(1, ChatColor.AQUA + region.getName());
        event.setLine(2, messageManager.getMessage("commercial-signs.price").replace("{price}", plugin.getEconomyManager().format(price)));
        event.setLine(3, messageManager.getMessage("commercial-signs.click-to-buy"));
        player.sendMessage(messageManager.getMessage("sign-created", true));
    }

    private void setupRentSign(SignChangeEvent event, Player player, Region region, String priceLine) {
        if (region.getRentPeriod() <= 0) {
            player.sendMessage(messageManager.getMessage("sign-error-rent-period-not-set", true));
            event.getBlock().breakNaturally();
            return;
        }

        double rentPrice;
        try {
            rentPrice = Double.parseDouble(priceLine);
            if (rentPrice <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            player.sendMessage(messageManager.getMessage("input.invalid-price-format", true));
            event.getBlock().breakNaturally();
            return;
        }

        // region.setForRent(true);
        region.setRentPrice(rentPrice);

        event.setLine(0, messageManager.getMessage("commercial-signs.for-rent-line-1"));
        event.setLine(1, ChatColor.AQUA + region.getName());
        event.setLine(2, messageManager.getMessage("commercial-signs.price").replace("{price}", plugin.getEconomyManager().format(rentPrice)));
        event.setLine(3, messageManager.getMessage("commercial-signs.click-to-rent"));
        player.sendMessage(messageManager.getMessage("sign-created", true));
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || !(clickedBlock.getState() instanceof Sign)) return;

        Player player = event.getPlayer();
        Sign sign = (Sign) clickedBlock.getState();
        String firstLine = ChatColor.stripColor(sign.getLine(0)).trim();
        String regionName = ChatColor.stripColor(sign.getLine(1)).trim();

        String forSaleTag = ChatColor.stripColor(messageManager.getMessage("commercial-signs.for-sale-line-1")).trim();
        String forRentTag = ChatColor.stripColor(messageManager.getMessage("commercial-signs.for-rent-line-1")).trim();

        boolean isSaleSign = firstLine.equalsIgnoreCase(forSaleTag);
        boolean isRentSign = firstLine.equalsIgnoreCase(forRentTag);

        if (!isSaleSign && !isRentSign) {
            return;
        }

        Region region = regionManager.getRegionByName(regionName);
        if (region == null) {
            player.sendMessage(messageManager.getMessage("sign-region-not-found", true));
            clickedBlock.breakNaturally();
            return;
        }

        if (isSaleSign) {
            boolean success = transactionManager.buyRegion(player, regionName);
            if (success) {
                updateSignToSold(sign, player);
            }
        } else {
            transactionManager.rentOrExtendRegion(player, regionName);
        }
    }

    private void updateSignToSold(Sign sign, Player newOwner) {
        sign.setLine(0, messageManager.getMessage("commercial-signs.sold-line-1"));
        sign.setLine(2, messageManager.getMessage("commercial-signs.owner").replace("{owner}", newOwner.getName()));
        sign.setLine(3, "");
        sign.update();
    }
}
