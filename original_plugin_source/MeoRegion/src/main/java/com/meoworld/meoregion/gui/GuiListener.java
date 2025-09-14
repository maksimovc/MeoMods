package com.meoworld.meoregion.gui;

import com.meoworld.meoregion.MeoRegion;
import com.meoworld.meoregion.commands.RegionCommand;
import com.meoworld.meoregion.data.ChunkCoordinate;
import com.meoworld.meoregion.data.Region;
import com.meoworld.meoregion.data.Role;
import com.meoworld.meoregion.managers.ChatInputManager;
import com.meoworld.meoregion.managers.MessageManager;
import com.meoworld.meoregion.util.Flag;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.UUID;

public class GuiListener implements Listener {

    private final MeoRegion plugin;
    private final MessageManager messageManager;
    private final RegionCommand regionCommand;

    public GuiListener(MeoRegion plugin, RegionCommand regionCommand) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
        this.regionCommand = regionCommand;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.getInventory() == null || event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        InventoryHolder holder = event.getInventory().getHolder();
        Player player = (Player) event.getWhoClicked();

        if (holder instanceof MenuGUI) {
            handleMenuGUIClick(event, player, (MenuGUI) holder);
        } else if (holder instanceof MainMenuGUI) {
            handleMainMenuGUIClick(event, player, (MainMenuGUI) holder);
        } else if (holder instanceof MarketGUI) {
            handleMarketGUIClick(event, player, (MarketGUI) holder);
        } else if (holder instanceof ConfirmGUI) {
            handleConfirmClick(event, player, (ConfirmGUI) holder);
        } else if (holder instanceof FlagsGUI) {
            handleFlagsGUIClick(event, player, (FlagsGUI) holder);
        } else if (holder instanceof MembersGUI) {
            handleMembersGUIClick(event, player, (MembersGUI) holder);
        } else if (holder instanceof RoleSelectionGUI) {
            handleRoleSelectionClick(event, player, (RoleSelectionGUI) holder);
        } else if (holder instanceof EconomyGUI) {
            handleEconomyGUIClick(event, player, (EconomyGUI) holder);
        } else if (holder instanceof MyRegionsGUI) {
            handleMyRegionsGUIClick(event, player, (MyRegionsGUI) holder);
        } else if (holder instanceof SettingsGUI) {
            handleSettingsGUIClick(event, player, (SettingsGUI) holder);
        } else if (holder instanceof SubChunksGUI) {
            handleSubChunksGUIClick(event, player, (SubChunksGUI) holder);
        }
    }

    private void handleMenuGUIClick(InventoryClickEvent event, Player player, MenuGUI gui) {
        event.setCancelled(true);
        switch (event.getSlot()) {
            case 10: // My Regions
                player.openInventory(new MyRegionsGUI(plugin, player.getUniqueId(), 1, gui).getInventory());
                break;
            case 12: // Buy Market
                player.openInventory(new MarketGUI(plugin, 1, MarketGUI.Mode.SALE, gui).getInventory());
                break;
            case 14: // Rent Market
                player.openInventory(new MarketGUI(plugin, 1, MarketGUI.Mode.RENT, gui).getInventory());
                break;
            case 16: // Help
                regionCommand.sendHelp(player);
                player.closeInventory();
                break;
            case 22: // Settings
                player.openInventory(new SettingsGUI(plugin, player.getUniqueId(), gui).getInventory());
                break;
            case 26: // Close
                player.closeInventory();
                break;
        }
    }

    private void handleMainMenuGUIClick(InventoryClickEvent event, Player player, MainMenuGUI gui) {
        event.setCancelled(true);
        Region region = gui.getRegion();

        switch (event.getSlot()) {
            case 11: // Flags
                player.openInventory(new FlagsGUI(plugin, region, 1, null, gui).getInventory());
                break;
            case 13: // Members
                player.openInventory(new MembersGUI(plugin, region, 1, gui).getInventory());
                break;
            case 15: // Economy
                if (plugin.getEconomyManager().isEconomyEnabled()) {
                    player.openInventory(new EconomyGUI(plugin, region, gui).getInventory());
                } else {
                    player.sendMessage(messageManager.getMessage("economy-disabled", true));
                    player.closeInventory();
                }
                break;
            case 17: // Subchunks
                if (region.hasPermission(player.getUniqueId(), Role.Permission.MANAGE_SUBCHUNKS) || player.hasPermission("meoregion.admin")) {
                    player.openInventory(new SubChunksGUI(plugin, region, player, gui).getInventory());
                } else {
                    player.sendMessage(messageManager.getMessage("no-permission", true));
                    player.closeInventory();
                }
                break;
        }
    }

    private void handleMarketGUIClick(InventoryClickEvent event, Player player, MarketGUI gui) {
        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();

        if (event.getSlot() >= 45) { // Navigation buttons
            if (clickedItem != null && clickedItem.getType() == Material.ARROW) {
                if (event.getSlot() == 48 && gui.getPage() > 1) { // Previous Page
                    player.openInventory(new MarketGUI(plugin, gui.getPage() - 1, gui.getMode(), gui.getParent()).getInventory());
                } else if (event.getSlot() == 50) { // Next Page
                    player.openInventory(new MarketGUI(plugin, gui.getPage() + 1, gui.getMode(), gui.getParent()).getInventory());
                }
            } else if (clickedItem != null && clickedItem.getType() == Material.BARRIER) { // Back button
                if (gui.getParent() != null) {
                    player.openInventory(gui.getParent().getInventory());
                } else {
                    player.closeInventory();
                }
            }
            return;
        }

        if (clickedItem != null && (clickedItem.getType() == Material.SKULL_ITEM || clickedItem.getType() == Material.CHEST)) {
            String regionName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
            MarketGUI.Mode mode = gui.getMode();
            String action = mode == MarketGUI.Mode.RENT ? "RENT" : "BUY";
            player.openInventory(new ConfirmGUI(plugin, action, regionName, gui).getInventory());
        }
    }

    private void handleConfirmClick(InventoryClickEvent event, Player player, ConfirmGUI gui) {
        event.setCancelled(true);
        if (event.getSlot() == 11) { // Confirm
            String action = gui.getAction();
            String regionName = gui.getRegionName();
            player.closeInventory();
            if (action.equals("BUY")) {
                plugin.getTransactionManager().buyRegion(player, regionName);
            } else if (action.equals("RENT")) {
                plugin.getTransactionManager().rentOrExtendRegion(player, regionName);
            }
        } else if (event.getSlot() == 15) { // Cancel
            if (gui.getParent() != null) {
                player.openInventory(gui.getParent().getInventory());
            } else {
                player.closeInventory();
            }
        }
    }

    private void handleFlagsGUIClick(InventoryClickEvent event, Player player, FlagsGUI gui) {
        event.setCancelled(true);
        Region region = gui.getRegion();
        boolean canManageFlags = player.hasPermission("meoregion.admin") || region.hasPermission(player.getUniqueId(), Role.Permission.SET_FLAGS);
        if (!canManageFlags) {
            player.sendMessage(messageManager.getMessage("no-permission", true));
            player.closeInventory();
            return;
        }
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;

        if (clickedItem.getType() == Material.ARROW || clickedItem.getType() == Material.COMPASS || clickedItem.getType() == Material.BARRIER || clickedItem.getType() == Material.MAP) {
            switch (event.getSlot()) {
                case 49: // Back
                    if (gui.getParent() != null) {
                        player.openInventory(gui.getParent().getInventory());
                    } else {
                        player.closeInventory();
                    }
                    return;
                case 48: // Previous Page
                    if (gui.getPage() > 1) player.openInventory(new FlagsGUI(plugin, region, gui.getPage() - 1, gui.getSubChunkCoordinate(), gui.getParent()).getInventory());
                    return;
                case 50: // Next Page
                    player.openInventory(new FlagsGUI(plugin, region, gui.getPage() + 1, gui.getSubChunkCoordinate(), gui.getParent()).getInventory());
                    return;
                case 47: // Switch Mode (Region/Subchunk)
                    if (gui.isSubChunkMode()) {
                        player.openInventory(new FlagsGUI(plugin, region, 1, null, gui.getParent()).getInventory());
                    } else {
                        ChunkCoordinate playerCoord = new ChunkCoordinate(player.getLocation().getChunk().getX(), player.getLocation().getChunk().getZ());
                        player.openInventory(new FlagsGUI(plugin, region, 1, playerCoord, gui.getParent()).getInventory());
                    }
                    return;
            }
        }

        String flagName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
        Flag flag = Flag.fromString(flagName);
        if (flag == null) return;

        if (flag.getDefaultValue() instanceof Boolean) {
            String value;
            if (event.getClick() == ClickType.SHIFT_LEFT) {
                value = "reset";
            } else {
                Object currentValueObj = plugin.getRegionManager().getFlagValue(player, flag, region, gui.getSubChunkCoordinate());
                boolean currentValue = (currentValueObj instanceof Boolean && (Boolean) currentValueObj);
                value = !currentValue ? "allow" : "deny";
            }
            
            if(gui.isSubChunkMode()) {
                regionCommand.handleSubChunk(player, new String[]{"sch", "flag", flag.getName(), value});
            } else {
                regionCommand.handleFlag(player, new String[]{"flag", region.getName(), flag.getName(), value});
            }
            player.openInventory(new FlagsGUI(plugin, region, gui.getPage(), gui.getSubChunkCoordinate(), gui.getParent()).getInventory());
        } else {
            player.closeInventory();
            ChatInputManager.Type inputType = gui.isSubChunkMode() ? ChatInputManager.Type.SET_SUBCHUNK_FLAG : ChatInputManager.Type.SET_FLAG;
            plugin.getChatInputManager().requestInput(player, inputType, region.getName(), flag.getName(), gui, gui.getSubChunkCoordinate());
        }
    }

    private void handleMembersGUIClick(InventoryClickEvent event, Player player, MembersGUI gui) {
        event.setCancelled(true);
        Region region = gui.getRegion();

        if (event.getSlot() >= 45) { // Navigation
            switch (event.getSlot()) {
                case 45: // Add Member
                    if (region.hasPermission(player.getUniqueId(), Role.Permission.MANAGE_MEMBERS) || player.hasPermission("meoregion.admin")) {
                        player.closeInventory();
                        plugin.getChatInputManager().requestInput(player, ChatInputManager.Type.ADD_MEMBER, region.getName(), null, gui, null);
                    } else {
                        player.sendMessage(messageManager.getMessage("no-permission", true));
                    }
                    break;
                case 48: // Previous Page
                    if(gui.getPage() > 1) player.openInventory(new MembersGUI(plugin, region, gui.getPage() - 1, gui.getParent()).getInventory());
                    break;
                case 49: // Back
                    if (gui.getParent() != null) {
                        player.openInventory(gui.getParent().getInventory());
                    } else {
                        player.closeInventory();
                    }
                    break;
                case 50: // Next Page
                    player.openInventory(new MembersGUI(plugin, region, gui.getPage() + 1, gui.getParent()).getInventory());
                    break;
            }
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem != null && clickedItem.getType() == Material.SKULL_ITEM) {
            String targetName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
            OfflinePlayer target = findOfflinePlayerByName(targetName);
            if (target == null) return;

            if (event.getClick() == ClickType.RIGHT) { // Change Role
                if (!region.hasPermission(player.getUniqueId(), Role.Permission.SET_ROLE) && !player.hasPermission("meoregion.admin")) {
                    player.sendMessage(messageManager.getMessage("no-permission", true));
                    return;
                }
                if (region.isOwner(target.getUniqueId())) {
                    player.sendMessage(messageManager.getMessage("cannot-modify-owner-role", true));
                    return;
                }
                player.openInventory(new RoleSelectionGUI(plugin, region, target.getUniqueId(), gui).getInventory());
            } else { // Get Info
                player.sendMessage(messageManager.getMessage("members-gui.player-info").replace("{player}", target.getName()));
                player.sendMessage(messageManager.getMessage("members-gui.player-info-role").replace("{role}", region.getRole(target.getUniqueId()).getName()));
            }
        }
    }

    private void handleRoleSelectionClick(InventoryClickEvent event, Player player, RoleSelectionGUI gui) {
        event.setCancelled(true);
        Region region = gui.getRegion();
        UUID targetUuid = gui.getTargetPlayer();
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetUuid);
        if (target == null || target.getName() == null) return;

        Role roleToSet = null;
        switch (event.getSlot()) {
            case 0: roleToSet = Role.CO_OWNER; break;
            case 1: roleToSet = Role.MANAGER; break;
            case 2: roleToSet = Role.BUILDER; break;
            case 3: roleToSet = Role.MEMBER; break;
            case 8: roleToSet = Role.GUEST; break;
        }

        if (roleToSet != null) {
            regionCommand.handleSetRole(player, new String[]{"setrole", region.getName(), target.getName(), roleToSet.name()});
            InventoryHolder parentGUI = gui.getParent();
            if (parentGUI instanceof MembersGUI) {
                MembersGUI membersGUI = (MembersGUI) parentGUI;
                Bukkit.getScheduler().runTaskLater(plugin, () -> player.openInventory(new MembersGUI(plugin, region, membersGUI.getPage(), membersGUI.getParent()).getInventory()), 1L);
            } else {
                player.closeInventory();
            }
        }
    }

    private void handleEconomyGUIClick(InventoryClickEvent event, Player player, EconomyGUI gui) {
        event.setCancelled(true);
        Region region = gui.getRegion();

        switch (event.getSlot()) {
            case 10: // Set Sale Price
                player.closeInventory();
                plugin.getChatInputManager().requestInput(player, ChatInputManager.Type.SET_SALE_PRICE, region.getName(), null, gui, null);
                break;
            case 11: // Toggle For Sale
                if (region.getPrice() > 0) {
                    if(region.isForSale()) {
                        regionCommand.handleUnsell(player, new String[]{"unsell", region.getName()});
                    } else {
                        regionCommand.handleSell(player, new String[]{"sell", region.getName(), String.valueOf(region.getPrice())});
                    }
                    Bukkit.getScheduler().runTaskLater(plugin, () -> player.openInventory(new EconomyGUI(plugin, region, gui.getParent()).getInventory()), 1L);
                } else {
                    player.sendMessage(messageManager.getMessage("economy-gui.set-sale-price-first", true));
                    player.closeInventory();
                }
                break;
            case 15: // Set Rent Details
                player.closeInventory();
                plugin.getChatInputManager().requestInput(player, ChatInputManager.Type.SET_RENT_DETAILS, region.getName(), null, gui, null);
                break;
            case 16: // Toggle For Rent
                if (region.getRentPrice() > 0 && region.getRentPeriod() > 0) {
                     if(region.isForRent()) {
                        regionCommand.handleUnlistRent(player, new String[]{"unlistrent", region.getName()});
                    } else {
                        regionCommand.handleRent(player, new String[]{"rent", region.getName(), String.valueOf(region.getRentPrice()), formatDurationArg(region.getRentPeriod())});
                    }
                    Bukkit.getScheduler().runTaskLater(plugin, () -> player.openInventory(new EconomyGUI(plugin, region, gui.getParent()).getInventory()), 1L);
                } else {
                    player.sendMessage(messageManager.getMessage("economy-gui.set-rent-details-first", true));
                    player.closeInventory();
                }
                break;
            case 18: // Back
                if (gui.getParent() != null) {
                    player.openInventory(gui.getParent().getInventory());
                } else {
                    player.closeInventory();
                }
                break;
        }
    }

    private void handleMyRegionsGUIClick(InventoryClickEvent event, Player player, MyRegionsGUI gui) {
        event.setCancelled(true);
        
        if (event.getSlot() >= 45) { // Navigation
            switch (event.getSlot()) {
                case 45: // Claim new region
                    player.closeInventory();
                    regionCommand.handleClaim(player, new String[]{"claim"});
                    break;
                case 48: // Previous Page
                    if (gui.getPage() > 1) {
                        player.openInventory(new MyRegionsGUI(plugin, gui.getPlayerUuid(), gui.getPage() - 1, gui.getParent()).getInventory());
                    }
                    break;
                case 49: // Back
                    if (gui.getParent() != null) {
                        player.openInventory(gui.getParent().getInventory());
                    } else {
                        player.closeInventory();
                    }
                    break;
                case 50: // Next Page
                    player.openInventory(new MyRegionsGUI(plugin, gui.getPlayerUuid(), gui.getPage() + 1, gui.getParent()).getInventory());
                    break;
            }
            return;
        }
        
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem != null && clickedItem.getType() == Material.CHEST) {
            String regionName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
            Region region = plugin.getRegionManager().getRegionByName(regionName);
            if (region == null) return;
            
            if (event.getClick() == ClickType.LEFT) { // Open Main Menu for region
                if (region.hasPermission(player.getUniqueId(), Role.Permission.BUILD) || player.hasPermission("meoregion.admin")) {
                    player.openInventory(new MainMenuGUI(plugin, region).getInventory());
                } else {
                    player.sendMessage(messageManager.getMessage("no-permission", true));
                }
            } else if (event.getClick() == ClickType.RIGHT) { // Get info
                regionCommand.handleInfo(player, new String[]{"info", regionName});
                player.closeInventory();
            }
        }
    }

    private void handleSettingsGUIClick(InventoryClickEvent event, Player player, SettingsGUI gui) {
        event.setCancelled(true);
        
        switch (event.getSlot()) {
            case 10: // Language
                String currentLanguage = plugin.getConfig().getString("language", "uk");
                String newLanguage = currentLanguage.equals("uk") ? "en" : "uk";
                plugin.getConfig().set("language", newLanguage);
                plugin.saveConfig();
                plugin.reload();
                player.sendMessage(messageManager.getMessage("reload", true));
                player.closeInventory();
                break;
            case 12: // Grid
                regionCommand.handleGrid(player, new String[]{"grid"});
                player.closeInventory();
                break;
            case 14: // Preview
                regionCommand.handlePreview(player, new String[]{"preview"});
                player.closeInventory();
                break;
            case 16: // Help
                regionCommand.sendHelp(player);
                player.closeInventory();
                break;
            case 18: // Back
                if (gui.getParent() != null) {
                    player.openInventory(gui.getParent().getInventory());
                } else {
                    player.closeInventory();
                }
                break;
        }
    }

    private void handleSubChunksGUIClick(InventoryClickEvent event, Player player, SubChunksGUI gui) {
        event.setCancelled(true);
        ChunkCoordinate coord = new ChunkCoordinate(player.getLocation().getChunk().getX(), player.getLocation().getChunk().getZ());

        switch (event.getSlot()) {
            case 10:
            case 16: // Info
                regionCommand.handleSubChunk(player, new String[]{"sch", "info"});
                player.closeInventory();
                break;
            case 12: // Manage Flags
                player.openInventory(new FlagsGUI(plugin, gui.getRegion(), 1, coord, gui).getInventory());
                break;
            case 14: // Reset Flags
                gui.getRegion().getOrCreateSubChunk(coord).getFlags().clear();
                player.sendMessage(messageManager.getMessage("subchunk-flag-reset", true));
                player.closeInventory();
                break;
            case 18: // Back
                if (gui.getParent() != null) {
                    player.openInventory(gui.getParent().getInventory());
                } else {
                    player.closeInventory();
                }
                break;
        }
    }

    private String formatDurationArg(long millis) {
        if (millis <= 0) return "0s";
        long days = millis / 86400000L;
        if (days > 0) return days + "d";
        long hours = (millis % 86400000L) / 3600000L;
        if (hours > 0) return hours + "h";
        long minutes = (millis % 3600000L) / 60000L;
        if (minutes > 0) return minutes + "m";
        return (millis / 1000L) + "s";
    }

    private OfflinePlayer findOfflinePlayerByName(String name) {
        if (name == null) return null;
        return Arrays.stream(Bukkit.getOfflinePlayers())
                .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }
}
