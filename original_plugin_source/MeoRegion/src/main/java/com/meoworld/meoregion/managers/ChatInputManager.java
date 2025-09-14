package com.meoworld.meoregion.managers;

import com.meoworld.meoregion.MeoRegion;
import com.meoworld.meoregion.data.ChunkCoordinate;
import com.meoworld.meoregion.data.Region;
import com.meoworld.meoregion.gui.EconomyGUI;
import com.meoworld.meoregion.gui.FlagsGUI;
import com.meoworld.meoregion.gui.MembersGUI;
import com.meoworld.meoregion.util.TimeParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class ChatInputManager {
    public enum Type {
        SET_SALE_PRICE,
        SET_RENT_DETAILS,
        SET_FLAG,
        SET_SUBCHUNK_FLAG,
        ADD_MEMBER
    }

    public static class Pending {
        public final Type type;
        public final String regionName;
        public final String flagName;
        public final InventoryHolder parentGui;
        public final ChunkCoordinate subChunkCoordinate;

        public Pending(Type type, String regionName, String flagName, InventoryHolder parentGui, ChunkCoordinate subChunkCoordinate) {
            this.type = type;
            this.regionName = regionName;
            this.flagName = flagName;
            this.parentGui = parentGui;
            this.subChunkCoordinate = subChunkCoordinate;
        }
    }

    private final Map<UUID, Pending> pending = new ConcurrentHashMap<>();
    private final MeoRegion plugin;
    private final MessageManager messageManager;

    public ChatInputManager(MeoRegion plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
    }

    public void setPending(UUID player, Pending p) {
        pending.put(player, p);
    }

    public Pending consume(UUID player) {
        return pending.remove(player);
    }

    public boolean hasPending(UUID player) {
        return pending.containsKey(player);
    }

    public void requestInput(Player player, Type type, String regionName, String flagName, InventoryHolder parentGui, ChunkCoordinate subChunkCoordinate) {
        Pending pendingInput = new Pending(type, regionName, flagName, parentGui, subChunkCoordinate);
        setPending(player.getUniqueId(), pendingInput);

        String message = getInputPrompt(type, regionName, flagName);
        player.closeInventory();
        player.sendMessage(message);
        player.sendMessage(messageManager.getMessage("input.cancel-prompt", true));
    }

    private String getInputPrompt(Type type, String regionName, String flagName) {
        String key;
        switch (type) {
            case SET_SALE_PRICE: key = "input.set-sale-price"; break;
            case SET_RENT_DETAILS: key = "input.set-rent-details"; break;
            case SET_FLAG: case SET_SUBCHUNK_FLAG: key = "input.set-flag-value"; break;
            case ADD_MEMBER: key = "input.add-member"; break;
            default: key = "input.generic"; break;
        }
        return messageManager.getMessage(key, true)
                .replace("{region}", regionName != null ? regionName : "")
                .replace("{flag}", flagName != null ? flagName : "");
    }

    public void processInput(Player player, String input) {
        Pending pendingInput = consume(player.getUniqueId());
        if (pendingInput == null) {
            return;
        }

        if (input.equalsIgnoreCase("cancel")) {
            player.sendMessage(messageManager.getMessage("input.cancelled", true));
            reopenParentGui(player, pendingInput);
            return;
        }

        try {
            boolean success;
            switch (pendingInput.type) {
                case SET_SALE_PRICE: success = processSalePrice(player, pendingInput, input); break;
                case SET_RENT_DETAILS: success = processRentDetails(player, pendingInput, input); break;
                case SET_FLAG: success = processFlag(player, pendingInput, input); break;
                case SET_SUBCHUNK_FLAG: success = processSubChunkFlag(player, pendingInput, input); break;
                case ADD_MEMBER: success = processAddMember(player, pendingInput, input); break;
                default:
                    player.sendMessage(messageManager.getMessage("input.unknown-type", true));
                    success = false;
                    break;
            }

            if (success) {
                Region region = plugin.getRegionManager().getRegionByName(pendingInput.regionName);
                if (region != null) {
                    reopenParentGui(player, pendingInput);
                }
            } else {
                player.sendMessage(messageManager.getMessage("input.try-again", true));
                setPending(player.getUniqueId(), pendingInput);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error processing chat input for " + player.getName() + " (type: " + pendingInput.type + ")", e);
            player.sendMessage(messageManager.getMessage("input.error", true).replace("{error}", e.getMessage()));
            reopenParentGui(player, pendingInput);
        }
    }

    private void reopenParentGui(Player player, Pending pendingInput) {
        if (pendingInput.parentGui == null) return;

        Region region = plugin.getRegionManager().getRegionByName(pendingInput.regionName);
        if (region == null) return;

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (pendingInput.parentGui instanceof EconomyGUI) {
                player.openInventory(new EconomyGUI(plugin, region, ((EconomyGUI) pendingInput.parentGui).getParent()).getInventory());
            } else if (pendingInput.parentGui instanceof FlagsGUI) {
                FlagsGUI oldGui = (FlagsGUI) pendingInput.parentGui;
                player.openInventory(new FlagsGUI(plugin, region, oldGui.getPage(), oldGui.getSubChunkCoordinate(), oldGui.getParent()).getInventory());
            } else if (pendingInput.parentGui instanceof MembersGUI) {
                MembersGUI oldGui = (MembersGUI) pendingInput.parentGui;
                player.openInventory(new MembersGUI(plugin, region, oldGui.getPage(), oldGui.getParent()).getInventory());
            } else {
                player.openInventory(pendingInput.parentGui.getInventory());
            }
        });
    }

    private boolean processSalePrice(Player player, Pending pending, String input) {
        Region region = plugin.getRegionManager().getRegionByName(pending.regionName);
        if (region == null) {
            player.sendMessage(messageManager.getMessage("region-not-found", true).replace("{region}", pending.regionName));
            return true;
        }
        try {
            double price = Double.parseDouble(input);
            if (price < 0) {
                player.sendMessage(messageManager.getMessage("input.price-negative", true));
                return false;
            }
            region.setPrice(price);
            plugin.getChangeLogManager().logChange(player.getName(), "set-price", region.getName());
            player.sendMessage(messageManager.getMessage("input.sale-price-set", true).replace("{region}", region.getName()).replace("{price}", plugin.getEconomyManager().format(price)));
            return true;
        } catch (NumberFormatException e) {
            player.sendMessage(messageManager.getMessage("input.invalid-price-format", true));
            return false;
        }
    }

    private boolean processRentDetails(Player player, Pending pending, String input) {
        Region region = plugin.getRegionManager().getRegionByName(pending.regionName);
        if (region == null) {
            player.sendMessage(messageManager.getMessage("region-not-found", true).replace("{region}", pending.regionName));
            return true;
        }
        try {
            String[] parts = input.split("/");
            if (parts.length != 2) {
                player.sendMessage(messageManager.getMessage("input.rent-details-format", true));
                return false;
            }
            double price = Double.parseDouble(parts[0].trim());
            String timeStr = parts[1].trim();
            long period = TimeParser.parseDuration(timeStr);

            if (price < 0) {
                player.sendMessage(messageManager.getMessage("input.price-negative", true));
                return false;
            }
            if (period <= 0) {
                player.sendMessage(messageManager.getMessage("input.rent-details-format", true));
                return false;
            }
            region.setRentPrice(price);
            region.setRentPeriod(period);
            plugin.getChangeLogManager().logChange(player.getName(), "set-rent", region.getName());
            player.sendMessage(messageManager.getMessage("input.rent-details-set", true).replace("{region}", region.getName()).replace("{price}", plugin.getEconomyManager().format(price)).replace("{duration}", timeStr));
            return true;
        } catch (NumberFormatException e) {
            player.sendMessage(messageManager.getMessage("input.invalid-price-format", true));
            return false;
        }
    }

    private boolean processFlag(Player player, Pending pending, String input) {
        plugin.getRegionCommand().handleFlag(player, new String[]{"flag", pending.regionName, pending.flagName, input});
        return true;
    }

    private boolean processSubChunkFlag(Player player, Pending pending, String input) {
        plugin.getRegionCommand().handleSubChunk(player, new String[]{"sch", "flag", pending.regionName, pending.flagName, input});
        return true;
    }

    private boolean processAddMember(Player player, Pending pending, String input) {
        plugin.getRegionCommand().handleAdd(player, new String[]{"add", pending.regionName, input});
        return true;
    }
}
