package com.meoworld.meoregion.commands;

import com.meoworld.meoregion.MeoRegion;
import com.meoworld.meoregion.data.ChunkCoordinate;
import com.meoworld.meoregion.data.Region;
import com.meoworld.meoregion.data.Role;
import com.meoworld.meoregion.data.SubChunk;
import com.meoworld.meoregion.gui.MainMenuGUI;
import com.meoworld.meoregion.gui.MenuGUI;
import com.meoworld.meoregion.managers.MessageManager;
import com.meoworld.meoregion.managers.RegionManager;
import com.meoworld.meoregion.managers.TransactionManager;
import com.meoworld.meoregion.tasks.PreviewTask;
import com.meoworld.meoregion.gui.MarketGUI;
import com.meoworld.meoregion.util.Flag;
import com.meoworld.meoregion.util.TimeParser;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RegionCommand implements CommandExecutor, TabCompleter {

    private final MeoRegion plugin;
    private final RegionManager regionManager;
    private final MessageManager messageManager;
    private final TransactionManager transactionManager;
    public static final Map<UUID, String> gridViewPlayers = new HashMap<>();
    private final Map<UUID, PreviewTask> previewTasks = new HashMap<>();

    public RegionCommand(MeoRegion plugin) {
        this.plugin = plugin;
        this.regionManager = plugin.getRegionManager();
        this.messageManager = plugin.getMessageManager();
        this.transactionManager = plugin.getTransactionManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messageManager.getMessage("player-only"));
            return true;
        }
        Player player = (Player) sender;
        if (args.length == 0) {
            player.openInventory(new MenuGUI(plugin).getInventory());
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "claim": handleClaim(player, args); break;
            case "delete": handleDelete(player, args); break;
            case "info": handleInfo(player, args); break;
            case "list": handleList(player, args); break;
            case "gui": handleGui(player, args); break;
            case "menu": player.openInventory(new MenuGUI(plugin).getInventory()); break;
            case "add": handleAdd(player, args); break;
            case "remove": handleRemove(player, args); break;
            case "setrole": handleSetRole(player, args); break;
            case "sell": handleSell(player, args); break;
            case "unsell": handleUnsell(player, args); break;
            case "buy": handleBuy(player, args); break;
            case "market": handleMarket(player, args); break;
            case "listrent": handleListRent(player, args); break;
            case "unlistrent": handleUnlistRent(player, args); break;
            case "rent": handleRent(player, args); break;
            case "subchunk": case "sch": handleSubChunk(player, args); break;
            case "grid": handleGrid(player, args); break;
            case "near": handleFind(player, args); break;
            case "preview": handlePreview(player, args); break;
            case "merge": handleMerge(player, args); break;
            case "adminclaim": handleAdminClaim(player, args); break;
            case "flag": handleFlag(player, args); break;
            case "rename": handleRename(player, args); break;
            case "reload": handleReload(player); break;
            case "log": handleLog(player, args); break;
            default: sendHelp(player); break;
        }
        return true;
    }

    public void sendHelp(Player player) {
        String[] playerCommands = {
                "usage.claim", "usage.delete", "usage.info", "usage.list", "usage.menu", "usage.gui", "usage.add", "usage.remove",
                "usage.setrole", "usage.grid", "usage.near", "usage.merge", "usage.preview",
                "usage.subchunk-flag"
        };
        String[] economyCommands = {"usage.sell", "usage.unsell", "usage.buy", "usage.market", "usage.listrent", "usage.unlistrent", "usage.rent"};
        String[] adminCommands = {"usage.adminclaim", "usage.flag", "usage.rename", "usage.reload", "usage.log"};

        player.sendMessage(messageManager.getMessage("help.header-player"));
        for (String key : playerCommands) {
            player.sendMessage(messageManager.getMessage(key));
        }

        if (plugin.getEconomyManager().isEconomyEnabled()) {
            player.sendMessage(messageManager.getMessage("help.header-economy"));
            for (String key : economyCommands) {
                player.sendMessage(messageManager.getMessage(key));
            }
        }

        if (player.hasPermission("meoregion.admin")) {
            player.sendMessage(messageManager.getMessage("help.header-admin"));
            for (String key : adminCommands) {
                player.sendMessage(messageManager.getMessage(key));
            }
        }
    }

    public void handleClaim(Player player, String[] args) {
        if (!player.hasPermission("meoregion.user")) {
            player.sendMessage(messageManager.getMessage("no-permission", true));
            return;
        }
        int regionSizeLimit = getRegionLimit(player);
        if (regionManager.getPlayerTotalRegionSize(player.getUniqueId()) + 9 > regionSizeLimit) {
            player.sendMessage(messageManager.getMessage("region-limit-reached", true).replace("{limit}", String.valueOf(regionSizeLimit)));
            return;
        }

        String regionName;
        if (args.length > 1 && !args[1].isEmpty()) {
            regionName = args[1];
            if (regionManager.getRegionByName(regionName) != null) {
                player.sendMessage(messageManager.getMessage("region-already-exists", true).replace("{region}", regionName));
                return;
            }
        } else {
            regionName = player.getName().toLowerCase() + "_" + (regionManager.getPlayerRegions(player.getUniqueId()).size() + 1);
        }

        Chunk pChunk = player.getLocation().getChunk();
        int radius = 1;
        int minChunkX = pChunk.getX() - radius;
        int maxChunkX = pChunk.getX() + radius;
        int minChunkZ = pChunk.getZ() - radius;
        int maxChunkZ = pChunk.getZ() + radius;

        int minX = minChunkX << 4;
        int maxX = (maxChunkX << 4) + 15;
        int minZ = minChunkZ << 4;
        int maxZ = (maxChunkZ << 4) + 15;

        if (regionManager.isAreaClaimed(minX, minZ, maxX, maxZ, player.getWorld().getName())) {
            player.sendMessage(messageManager.getMessage("area-already-claimed", true));
            return;
        }

        Region newRegion = new Region(regionName, player.getUniqueId(), player.getWorld().getName(),
                minX, 0, minZ, maxX, 255, maxZ, 9);

        regionManager.addRegion(newRegion);
        player.sendMessage(messageManager.getMessage("region-claimed", true).replace("{region}", newRegion.getName()));
        if (plugin.getChangeLogManager() != null) plugin.getChangeLogManager().logChange(player.getName(), "CLAIM", newRegion.getName());

        if (previewTasks.containsKey(player.getUniqueId())) {
            previewTasks.get(player.getUniqueId()).cancel();
            previewTasks.remove(player.getUniqueId());
        }
    }

    private Region createRegionFromRadius(Player player, String regionName, int radius) {
        Chunk centerChunk = player.getLocation().getChunk();

        int minChunkX = centerChunk.getX() - radius;
        int maxChunkX = centerChunk.getX() + radius;
        int minChunkZ = centerChunk.getZ() - radius;
        int maxChunkZ = centerChunk.getZ() + radius;

        int minX = minChunkX << 4;
        int maxX = (maxChunkX << 4) + 15;
        int minZ = minChunkZ << 4;
        int maxZ = (maxChunkZ << 4) + 15;
        int size = (radius * 2 + 1) * (radius * 2 + 1);

        Region region = new Region(null, null, player.getWorld().getName(), minX, 0, minZ, maxX, 255, maxZ, size);
        region.setName(regionName);
        return region;
    }

    public void handleAdminClaim(Player player, String[] args) {
        if (!player.hasPermission("meoregion.admin.claim")) {
            player.sendMessage(messageManager.getMessage("no-permission", true));
            return;
        }
        if (args.length < 3) {
            player.sendMessage(messageManager.getMessage("usage.prefix") + messageManager.getMessage("usage.adminclaim"));
            return;
        }

        String regionName = args[1];
        if (regionManager.getRegionByName(regionName) != null) {
            player.sendMessage(messageManager.getMessage("region-already-exists", true).replace("{region}", regionName));
            return;
        }

        int radius;
        try {
            radius = Integer.parseInt(args[2]);
            if (radius < 0) {
                player.sendMessage(messageManager.getMessage("input.price-negative", true));
                return;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(messageManager.getMessage("input.invalid-price-format", true));
            return;
        }

        Region region = createRegionFromRadius(player, regionName, radius);

        regionManager.addRegion(region);
        player.sendMessage(messageManager.getMessage("admin-region-claimed", true)
                .replace("{region}", regionName)
                .replace("{radius}", String.valueOf(radius)));
    }

    public void handleDelete(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(messageManager.getMessage("usage.prefix") + messageManager.getMessage("usage.delete"));
            return;
        }
        String regionName = args[1];
        Region region = regionManager.getRegionByName(regionName);
        if (region == null) {
            player.sendMessage(messageManager.getMessage("region-not-found", true).replace("{region}", regionName));
            return;
        }
        if (!isOwnerOrAdmin(player, region)) {
            player.sendMessage(messageManager.getMessage("not-owner", true));
            return;
        }
        regionManager.removeRegion(regionName);
        player.sendMessage(messageManager.getMessage("region-deleted", true).replace("{region}", regionName));
        if (plugin.getChangeLogManager() != null) plugin.getChangeLogManager().logChange(player.getName(), "DELETE", regionName);
    }

    public void handleInfo(Player player, String[] args) {
        Region region;
        if (args.length < 2) {
            region = regionManager.getHighestPriorityRegionAt(player.getLocation());
            if (region == null || (region.getName().startsWith("__global_") && !player.hasPermission("meoregion.admin"))) {
                player.sendMessage(messageManager.getMessage("not-in-region", true));
                return;
            }
        } else {
            region = regionManager.getRegionByName(args[1]);
            if (region == null) {
                player.sendMessage(messageManager.getMessage("region-not-found", true).replace("{region}", args[1]));
                return;
            }
        }

        player.sendMessage(messageManager.getMessage("info.header").replace("{region}", region.getName()));
        String ownerName = region.getOwner().map(uuid -> Bukkit.getOfflinePlayer(uuid).getName()).orElse(messageManager.getMessage("info.server-owner"));
        player.sendMessage(messageManager.getMessage("info.owner").replace("{owner}", ownerName));

        Map<Role, List<String>> participantsByRole = region.getParticipants().entrySet().stream()
                .filter(entry -> entry.getValue() != Role.OWNER && Bukkit.getOfflinePlayer(entry.getKey()).getName() != null)
                .collect(Collectors.groupingBy(Map.Entry::getValue,
                        Collectors.mapping(entry -> Bukkit.getOfflinePlayer(entry.getKey()).getName(), Collectors.toList())));

        participantsByRole.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparingInt(Role::getLevel).reversed()))
                .forEach(entry -> {
                    if (!entry.getValue().isEmpty()) {
                        String roleName = entry.getKey().getName();
                        String playersList = String.join(", ", entry.getValue());
                        player.sendMessage(messageManager.getMessage("info.role-plural").replace("{role}", roleName).replace("{players}", playersList));
                    }
                });

        player.sendMessage(messageManager.getMessage("info.priority").replace("{priority}", String.valueOf(region.getPriority())));
        player.sendMessage(messageManager.getMessage("info.size-chunks").replace("{size}", String.valueOf(region.getSize())));

        Map<String, Object> nonDefaultFlags = regionManager.getNonDefaultFlags(region);
        if (!nonDefaultFlags.isEmpty()) {
            player.sendMessage(messageManager.getMessage("info.set-flags"));
            nonDefaultFlags.forEach((flag, value) -> player.sendMessage(messageManager.getMessage("info.flag-format").replace("{flag}", flag).replace("{value}", value.toString())));
        }

        if (region.isForSale()) {
            player.sendMessage(messageManager.getMessage("info.status") + messageManager.getMessage("info.for-sale").replace("{price}", plugin.getEconomyManager().format(region.getPrice())));
        } else if (region.isForRent()) {
            player.sendMessage(messageManager.getMessage("info.status") + messageManager.getMessage("info.for-rent").replace("{price}", plugin.getEconomyManager().format(region.getRentPrice())));
        }
    }

    public void handleList(Player player, String[] args) {
        if (args.length > 1 && args[1].equalsIgnoreCase("all")) {
            if (!player.hasPermission("meoregion.admin")) {
                player.sendMessage(messageManager.getMessage("no-permission", true));
                return;
            }
            player.sendMessage(messageManager.getMessage("list.all-header"));
            regionManager.getRegions().values()
                    .forEach(r -> {
                        String ownerName = r.getOwner().map(uuid -> Bukkit.getOfflinePlayer(uuid).getName()).orElse(messageManager.getMessage("info.server-owner"));
                        player.sendMessage(r.getName() + messageManager.getMessage("list.owner-suffix").replace("{owner}", ownerName));
                    });
            return;
        }

        UUID targetUUID;
        String targetName;
        if (args.length < 2) {
            targetUUID = player.getUniqueId();
            targetName = player.getName();
        } else {
            OfflinePlayer target = findOfflinePlayerByName(args[1]);
            if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
                player.sendMessage(messageManager.getMessage("player-not-found", true).replace("{player}", args[1]));
                return;
            }
            targetUUID = target.getUniqueId();
            targetName = target.getName();
        }

        List<String> regionNames = regionManager.getPlayerRegions(targetUUID).stream()
                .map(Region::getName)
                .collect(Collectors.toList());

        if (regionNames.isEmpty()) {
            player.sendMessage(messageManager.getMessage("list.no-regions").replace("{player}", targetName));
        } else {
            player.sendMessage(messageManager.getMessage("list.player-regions").replace("{player}", targetName).replace("{regions}", String.join(", ", regionNames)));
        }
    }

    public void handleGui(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(messageManager.getMessage("usage.prefix") + messageManager.getMessage("usage.gui"));
            return;
        }
        Region region = regionManager.getRegionByName(args[1]);
        if (region == null) {
            player.sendMessage(messageManager.getMessage("region-not-found", true).replace("{region}", args[1]));
            return;
        }
        if (!canManageOrAdmin(player, region)) {
            player.sendMessage(messageManager.getMessage("not-owner-or-manager", true));
            return;
        }
        player.openInventory(new MainMenuGUI(plugin, region).getInventory());
    }

    public void handleAdd(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(messageManager.getMessage("usage.prefix") + messageManager.getMessage("usage.add"));
            return;
        }
        handleSetRole(player, new String[]{"setrole", args[1], args[2], "MEMBER"});
    }

    public void handleRemove(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(messageManager.getMessage("usage.prefix") + messageManager.getMessage("usage.remove"));
            return;
        }
        handleSetRole(player, new String[]{"setrole", args[1], args[2], "GUEST"});
    }

    public void handleMarket(Player player, String[] args) {
        if (!plugin.getEconomyManager().isEconomyEnabled()) {
            player.sendMessage(messageManager.getMessage("economy-disabled", true));
            return;
        }

        String mode = "buy";
        if (args.length > 1) {
            mode = args[1].toLowerCase();
        }

        int page = 1;
        if (args.length > 2) {
            try { page = Math.max(1, Integer.parseInt(args[2])); } catch (NumberFormatException ignored) {}
        }

        if (mode.equals("rent")) {
            player.openInventory(new MarketGUI(plugin, page, MarketGUI.Mode.RENT, new MenuGUI(plugin)).getInventory());
        } else {
            player.openInventory(new MarketGUI(plugin, page, MarketGUI.Mode.SALE, new MenuGUI(plugin)).getInventory());
        }
    }

    public void handleSell(Player player, String[] args) {
        if (!plugin.getEconomyManager().isEconomyEnabled()) {
            player.sendMessage(messageManager.getMessage("economy-disabled", true));
            return;
        }
        if (args.length < 3) {
            player.sendMessage(messageManager.getMessage("usage.prefix") + messageManager.getMessage("usage.sell"));
            return;
        }
        Region region = regionManager.getRegionByName(args[1]);
        if (region == null) {
            player.sendMessage(messageManager.getMessage("region-not-found", true).replace("{region}", args[1]));
            return;
        }
        if (!isOwnerOrAdmin(player, region)) {
            player.sendMessage(messageManager.getMessage("not-owner", true));
            return;
        }
        double price;
        try {
            price = Double.parseDouble(args[2]);
            if (price <= 0) {
                player.sendMessage(messageManager.getMessage("input.price-negative", true));
                return;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(messageManager.getMessage("input.invalid-price-format", true));
            return;
        }

        region.setForSale(true);
        region.setPrice(price);
        player.sendMessage(messageManager.getMessage("region-for-sale", true)
                .replace("{region}", region.getName())
                .replace("{price}", plugin.getEconomyManager().format(price)));
        if (plugin.getChangeLogManager() != null) plugin.getChangeLogManager().logChange(player.getName(), "SELL_LISTED", region.getName());
    }

    public void handleUnsell(Player player, String[] args) {
        if (!plugin.getEconomyManager().isEconomyEnabled()) {
            player.sendMessage(messageManager.getMessage("economy-disabled", true));
            return;
        }
        if (args.length < 2) {
            player.sendMessage(messageManager.getMessage("usage.prefix") + messageManager.getMessage("usage.unsell"));
            return;
        }
        Region region = regionManager.getRegionByName(args[1]);
        if (region == null) {
            player.sendMessage(messageManager.getMessage("region-not-found", true).replace("{region}", args[1]));
            return;
        }
        if (!isOwnerOrAdmin(player, region)) {
            player.sendMessage(messageManager.getMessage("not-owner", true));
            return;
        }

        region.setForSale(false);
        region.setPrice(0);
        player.sendMessage(messageManager.getMessage("region-not-for-sale", true).replace("{region}", region.getName()));
        if (plugin.getChangeLogManager() != null) plugin.getChangeLogManager().logChange(player.getName(), "SELL_UNLISTED", region.getName());
    }

    public void handleBuy(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(messageManager.getMessage("usage.prefix") + messageManager.getMessage("usage.buy"));
            return;
        }
        transactionManager.buyRegion(player, args[1]);
    }

    public void handleSetRole(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(messageManager.getMessage("usage.prefix") + messageManager.getMessage("usage.setrole"));
            return;
        }
        Region region = regionManager.getRegionByName(args[1]);
        if (region == null) {
            player.sendMessage(messageManager.getMessage("region-not-found", true).replace("{region}", args[1]));
            return;
        }

        if (!region.hasPermission(player.getUniqueId(), Role.Permission.SET_ROLE) && !player.hasPermission("meoregion.admin")) {
            player.sendMessage(messageManager.getMessage("no-permission", true));
            return;
        }

        OfflinePlayer target = findOfflinePlayerByName(args[2]);
        if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
            player.sendMessage(messageManager.getMessage("player-not-found", true).replace("{player}", args[2]));
            return;
        }

        Role newRole;
        try {
            newRole = Role.valueOf(args[3].toUpperCase());
        } catch (IllegalArgumentException e) {
            String availableRoles = Arrays.stream(Role.values()).map(Role::name).collect(Collectors.joining(", "));
            player.sendMessage(messageManager.getMessage("unknown-role", true).replace("{roles}", availableRoles));
            return;
        }

        UUID targetUuid = target.getUniqueId();
        Optional<UUID> ownerOpt = region.getOwner();
        if (ownerOpt.isPresent() && ownerOpt.get().equals(targetUuid) && newRole != Role.OWNER) {
            player.sendMessage(messageManager.getMessage("cannot-modify-owner-role", true));
            return;
        }

        Role actorRole = region.getRole(player.getUniqueId());
        if (newRole.getLevel() >= actorRole.getLevel() && !player.hasPermission("meoregion.admin")) {
            player.sendMessage(messageManager.getMessage("cannot-set-higher-role", true));
            return;
        }

        if (newRole == Role.OWNER) {
            region.transferOwnership(target.getUniqueId());
            player.sendMessage(messageManager.getMessage("ownership-transferred", true).replace("{region}", region.getName()).replace("{player}", target.getName()));
            if (plugin.getChangeLogManager() != null) plugin.getChangeLogManager().logChange(player.getName(), "TRANSFER_OWNERSHIP", region.getName());
        } else {
            region.setParticipantRole(target.getUniqueId(), newRole);
            player.sendMessage(messageManager.getMessage("role-set-success", true).replace("{player}", target.getName()).replace("{role}", newRole.getName()).replace("{region}", region.getName()));
            if (plugin.getChangeLogManager() != null) plugin.getChangeLogManager().logChange(player.getName(), "SET_ROLE:" + newRole.name(), region.getName());
        }
    }

    public void handleListRent(Player player, String[] args) {
        if (!plugin.getEconomyManager().isEconomyEnabled()) {
            player.sendMessage(messageManager.getMessage("economy-disabled", true));
            return;
        }
        if (args.length < 4) {
            player.sendMessage(messageManager.getMessage("usage.prefix") + messageManager.getMessage("usage.listrent"));
            return;
        }
        Region region = regionManager.getRegionByName(args[1]);
        if (region == null) {
            player.sendMessage(messageManager.getMessage("region-not-found", true).replace("{region}", args[1]));
            return;
        }
        if (!isOwnerOrAdmin(player, region)) {
            player.sendMessage(messageManager.getMessage("not-owner", true));
            return;
        }
        double price;
        try {
            price = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(messageManager.getMessage("input.invalid-price-format", true));
            return;
        }
        long period = TimeParser.parseDuration(args[3]);
        if (period <= 0) {
            player.sendMessage(messageManager.getMessage("input.rent-details-format", true));
            return;
        }

        region.setForSale(false);
        region.setRentPrice(price);
        region.setRentPeriod(period);
        player.sendMessage(messageManager.getMessage("rent-set", true).replace("{region}", region.getName()).replace("{price}", plugin.getEconomyManager().format(price)).replace("{duration}", args[3]));
        if (plugin.getChangeLogManager() != null) {
            plugin.getChangeLogManager().logChange(player.getName(), "RENT_LISTED", region.getName());
        }
    }

    public void handleRent(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(messageManager.getMessage("usage.prefix") + messageManager.getMessage("usage.rent"));
            return;
        }
        transactionManager.rentOrExtendRegion(player, args[1]);
    }

    public void handleUnlistRent(Player player, String[] args) {
        if (!plugin.getEconomyManager().isEconomyEnabled()) {
            player.sendMessage(messageManager.getMessage("economy-disabled", true));
            return;
        }
        if (args.length < 2) {
            player.sendMessage(messageManager.getMessage("usage.prefix") + messageManager.getMessage("usage.unlistrent"));
            return;
        }
        Region region = regionManager.getRegionByName(args[1]);
        if (region == null) {
            player.sendMessage(messageManager.getMessage("region-not-found", true).replace("{region}", args[1]));
            return;
        }
        if (!isOwnerOrAdmin(player, region)) {
            player.sendMessage(messageManager.getMessage("not-owner", true));
            return;
        }
        region.setRentPrice(0);
        region.setRentPeriod(0);
        region.setRenter(null);
        region.setRentDueDate(0);
        player.sendMessage(messageManager.getMessage("rent-stop", true).replace("{region}", region.getName()));
        if (plugin.getChangeLogManager() != null) {
            plugin.getChangeLogManager().logChange(player.getName(), "RENT_STOPPED", region.getName());
        }
    }

    public void handleGrid(Player player, String[] args) {
        if (args.length > 1 && args[1].equalsIgnoreCase("off")) {
            if (gridViewPlayers.remove(player.getUniqueId()) != null) {
                player.sendMessage(messageManager.getMessage("settings.grid-disabled", true));
            }
            return;
        }

        if (args.length < 2 && gridViewPlayers.containsKey(player.getUniqueId())) {
             gridViewPlayers.remove(player.getUniqueId());
             player.sendMessage(messageManager.getMessage("settings.grid-disabled", true));
             return;
        }

        String regionName;
        if (args.length < 2) {
            Region current = regionManager.getHighestPriorityRegionAt(player.getLocation());
            if (current == null || (current.getName().startsWith("__global_") && !player.hasPermission("meoregion.admin"))) {
                player.sendMessage(messageManager.getMessage("not-in-region", true));
                return;
            }
            regionName = current.getName();
        } else {
            regionName = args[1];
        }

        Region region = regionManager.getRegionByName(regionName);
        if (region == null) {
            player.sendMessage(messageManager.getMessage("region-not-found", true).replace("{region}", regionName));
            return;
        }

        gridViewPlayers.put(player.getUniqueId(), region.getName());
        player.sendMessage(messageManager.getMessage("settings.grid-enabled-specific", true).replace("{region}", region.getName()));
    }

    public void handleFind(Player player, String[] args) {
        if (!player.hasPermission("meoregion.user.find")) {
            player.sendMessage(messageManager.getMessage("no-permission", true));
            return;
        }
        int maxRadius = plugin.getConfig().getInt("search.max-radius", 100);
        if (args.length > 1) {
            try { maxRadius = Integer.parseInt(args[1]); } catch (NumberFormatException ignored) {}
        }

        List<Region> found = new ArrayList<>();

        for (Region r : regionManager.getRegions().values()) {
            if (!r.getWorldName().equals(player.getWorld().getName()) || (r.getName().startsWith("__global_") && !player.hasPermission("meoregion.admin"))) continue;

            double distance = Math.sqrt(regionManager.getRegionCenter(r).distanceSquared(player.getLocation().toVector()));
            if (distance <= maxRadius) {
                found.add(r);
            }
        }

        if (found.isEmpty()) {
            player.sendMessage(messageManager.getMessage("find-no-regions", true).replace("{radius}", String.valueOf(maxRadius)));
            return;
        }

        found.sort(Comparator.comparingDouble(r -> regionManager.getRegionCenter(r).distanceSquared(player.getLocation().toVector())));

        player.sendMessage(messageManager.getMessage("find-header", true));
        found.forEach(r -> {
            String ownerName = r.getOwner().map(uuid -> Bukkit.getOfflinePlayer(uuid).getName()).orElse(messageManager.getMessage("info.server-owner"));
            int distance = (int) Math.sqrt(regionManager.getRegionCenter(r).distanceSquared(player.getLocation().toVector()));
            player.sendMessage(messageManager.getMessage("find-format", false)
                    .replace("{region}", r.getName())
                    .replace("{owner}", ownerName)
                    .replace("{distance}", String.valueOf(distance)));
        });
    }

    public void handleMerge(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(messageManager.getMessage("usage.prefix") + messageManager.getMessage("usage.merge"));
            return;
        }
        Region rg1 = regionManager.getRegionByName(args[1]);
        Region rg2 = regionManager.getRegionByName(args[2]);

        if (rg1 == null || rg2 == null) {
            player.sendMessage(messageManager.getMessage("merge-not-found", true));
            return;
        }
        if (!rg1.isOwner(player.getUniqueId()) || !rg2.isOwner(player.getUniqueId())) {
            player.sendMessage(messageManager.getMessage("not-owner", true));
            return;
        }
        if (rg1.getName().equals(rg2.getName())) {
            player.sendMessage(messageManager.getMessage("merge-self", true));
            return;
        }

        int minX = Math.min(rg1.getMinX(), rg2.getMinX());
        int minY = Math.min(rg1.getMinY(), rg2.getMinY());
        int minZ = Math.min(rg1.getMinZ(), rg2.getMinZ());
        int maxX = Math.max(rg1.getMaxX(), rg2.getMaxX());
        int maxY = Math.max(rg1.getMaxY(), rg2.getMaxY());
        int maxZ = Math.max(rg1.getMaxZ(), rg2.getMaxZ());

        String newName = rg1.getName();
        rg1.setMinX(minX);
        rg1.setMinY(minY);
        rg1.setMinZ(minZ);
        rg1.setMaxX(maxX);
        rg1.setMaxY(maxY);
        rg1.setMaxZ(maxZ);
        rg1.setSize(rg1.getSize() + rg2.getSize());

        rg2.getParticipants().forEach((uuid, role) -> {
            Role currentRole = rg1.getRole(uuid);
            if (role.getLevel() > currentRole.getLevel()) {
                rg1.setParticipantRole(uuid, role);
            }
        });

        regionManager.removeRegion(rg2.getName());
        regionManager.updateCache();

        player.sendMessage(messageManager.getMessage("merge-success", true).replace("{region1}", newName).replace("{region2}", args[2]));
    }

    public void handlePreview(Player player, String[] args) {
        if (!player.hasPermission("meoregion.user")) {
            player.sendMessage(messageManager.getMessage("no-permission", true));
            return;
        }
        if (args.length > 1 && args[1].equalsIgnoreCase("off")) {
            if (previewTasks.remove(player.getUniqueId()) != null) {
                previewTasks.get(player.getUniqueId()).cancel();
                player.sendMessage(messageManager.getMessage("settings.preview-disabled", true));
            }
            return;
        }

        if (previewTasks.containsKey(player.getUniqueId())) {
            previewTasks.remove(player.getUniqueId()).cancel();
            player.sendMessage(messageManager.getMessage("settings.preview-disabled", true));
        } else {
            PreviewTask task = new PreviewTask(player);
            task.runTaskTimer(plugin, 0L, 20L);
            previewTasks.put(player.getUniqueId(), task);
            player.sendMessage(messageManager.getMessage("settings.preview-enabled", true));
        }
    }

    public void handleFlag(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(messageManager.getMessage("usage.prefix") + messageManager.getMessage("usage.flag"));
            return;
        }
        Region region = regionManager.getRegionByName(args[1]);
        if (region == null) {
            player.sendMessage(messageManager.getMessage("region-not-found", true).replace("{region}", args[1]));
            return;
        }

        boolean canManageFlags = player.hasPermission("meoregion.admin") ||
                (region.hasPermission(player.getUniqueId(), Role.Permission.SET_FLAGS));

        if (!canManageFlags) {
            player.sendMessage(messageManager.getMessage("no-permission", true));
            return;
        }

        String flagName = args[2].toLowerCase();

        if (flagName.equalsIgnoreCase("list")) {
            player.sendMessage(messageManager.getMessage("flag-list-header").replace("{region}", region.getName()));
            for (Flag flag : Flag.values()) {
                Object value = regionManager.getFlagValue(player, flag, region, null);
                player.sendMessage(messageManager.getMessage("flag-info-format")
                        .replace("{flag}", flag.getName())
                        .replace("{value}", value.toString()));
            }
            return;
        }

        if (args.length < 4) {
            player.sendMessage(messageManager.getMessage("usage.prefix") + messageManager.getMessage("usage.flag"));
            return;
        }

        Flag flag = Flag.fromString(flagName);
        if (flag == null) {
            player.sendMessage(messageManager.getMessage("invalid-flag", true));
            return;
        }

        String durationStr = Arrays.stream(args).filter(s -> s.matches("\\d+[dhms]")).findFirst().orElse(null);
        long duration = TimeParser.parseDuration(durationStr);

        List<String> valueArgs = new ArrayList<>(Arrays.asList(args));
        valueArgs.remove(0);
        valueArgs.remove(0);
        valueArgs.remove(0);
        if (durationStr != null) {
            valueArgs.remove(durationStr);
        }

        String valueStrRaw = String.join(" ", valueArgs);
        Object value;

        if (valueStrRaw.equalsIgnoreCase("reset")) {
            region.getFlags().remove(flag.getName());
            player.sendMessage(messageManager.getMessage("flag-reset", true).replace("{flag}", flag.getName()).replace("{region}", region.getName()));
            return;
        }

        if (flag.getDefaultValue() instanceof Boolean) {
            if (valueStrRaw.equalsIgnoreCase("allow") || valueStrRaw.equalsIgnoreCase("true")) value = true;
            else if (valueStrRaw.equalsIgnoreCase("deny") || valueStrRaw.equalsIgnoreCase("false")) value = false;
            else { player.sendMessage(messageManager.getMessage("invalid-flag-value", true)); return; }
        } else if (flag.isListOfStrings()) {
            @SuppressWarnings("unchecked")
            List<String> list = new ArrayList<>((List<String>) region.getFlags().getOrDefault(flag.getName(), new ArrayList<String>()));
            String[] parts = valueStrRaw.split(" ", 2);
            String action = parts[0];
            String listValue = parts.length > 1 ? parts[1] : "";

            switch (action.toLowerCase()) {
                case "add":
                    list.add(listValue);
                    break;
                case "remove":
                    list.remove(listValue);
                    break;
                default:
                    player.sendMessage(messageManager.getMessage("list-flag-usage", true));
                    return;
            }
            value = list;
        } else {
            value = valueStrRaw;
        }

        if (duration > 0 && player.hasPermission("meoregion.admin.tempflag")) {
            plugin.getTemporaryFlagManager().addTemporaryFlag(region, flag, value, duration);
            player.sendMessage(messageManager.getMessage("flag-set-temp", true)
                    .replace("{flag}", flag.getName())
                    .replace("{region}", region.getName())
                    .replace("{value}", value.toString())
                    .replace("{duration}", durationStr == null ? "" : durationStr));
            if (plugin.getChangeLogManager() != null) plugin.getChangeLogManager().logChange(player.getName(), "TEMP_FLAG_SET:" + flag.getName(), region.getName());
        } else {
            region.getFlags().put(flag.getName(), value);
            player.sendMessage(messageManager.getMessage("flag-set", true)
                    .replace("{flag}", flag.getName())
                    .replace("{region}", region.getName())
                    .replace("{value}", value.toString()));
            if (plugin.getChangeLogManager() != null) plugin.getChangeLogManager().logChange(player.getName(), "FLAG_SET:" + flag.getName(), region.getName());
        }
    }

    public void handleRename(Player player, String[] args) {
        if (!player.hasPermission("meoregion.admin")) {
            player.sendMessage(messageManager.getMessage("no-permission", true));
            return;
        }
        if (args.length < 3) {
            player.sendMessage(messageManager.getMessage("usage.prefix") + messageManager.getMessage("usage.rename"));
            return;
        }
        Region region = regionManager.getRegionByName(args[1]);
        if (region == null) {
            player.sendMessage(messageManager.getMessage("region-not-found", true).replace("{region}", args[1]));
            return;
        }
        String newName = args[2];
        if (regionManager.getRegionByName(newName) != null) {
            player.sendMessage(messageManager.getMessage("region-already-exists", true).replace("{region}", newName));
            return;
        }

        regionManager.removeRegion(region.getName());
        region.setName(newName);
        regionManager.addRegion(region);

        player.sendMessage(messageManager.getMessage("region-renamed", true).replace("{old_name}", args[1]).replace("{new_name}", newName));
        if (plugin.getChangeLogManager() != null) plugin.getChangeLogManager().logChange(player.getName(), "RENAME:" + args[1] + "->" + newName, newName);
    }

    public void handleReload(Player player) {
        if (!player.hasPermission("meoregion.admin")) {
            player.sendMessage(messageManager.getMessage("no-permission", true));
            return;
        }
        plugin.reload();
        player.sendMessage(messageManager.getMessage("reload", true));
    }

    private void handleLog(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(messageManager.getMessage("usage.prefix") + messageManager.getMessage("usage.log"));
            return;
        }
        String regionName = args[1];
        Region region = regionManager.getRegionByName(regionName);
        if (region == null) {
            player.sendMessage(messageManager.getMessage("region-not-found", true).replace("{region}", regionName));
            return;
        }

        if (!isOwnerOrAdmin(player, region)) {
            player.sendMessage(messageManager.getMessage("no-permission", true));
            return;
        }

        int page = 1;
        if (args.length > 2) {
            try {
                page = Integer.parseInt(args[2]);
                if (page < 1) page = 1;
            } catch (NumberFormatException e) {
                player.sendMessage(messageManager.getMessage("input.invalid-page", true));
                return;
            }
        }

        List<String> changes = plugin.getChangeLogManager().getChanges(regionName, page);

        if (changes.isEmpty()) {
            player.sendMessage(messageManager.getMessage("log-no-entries", true).replace("{region}", regionName));
            return;
        }

        player.sendMessage(messageManager.getMessage("log-header", true).replace("{region}", regionName).replace("{page}", String.valueOf(page)));
        changes.forEach(player::sendMessage);
    }

    public void handleSubChunk(Player player, String[] args) {
        if (args.length < 2) {
            sendHelp(player);
            return;
        }

        String subCmd = args[1].toLowerCase();
        Region region = regionManager.getHighestPriorityRegionAt(player.getLocation());
        if (region == null || (region.getName().startsWith("__global_") && !player.hasPermission("meoregion.admin"))) {
            player.sendMessage(messageManager.getMessage("not-in-region", true));
            return;
        }
        if (!region.hasPermission(player.getUniqueId(), Role.Permission.MANAGE_SUBCHUNKS) && !player.hasPermission("meoregion.admin")) {
            player.sendMessage(messageManager.getMessage("no-permission", true));
            return;
        }

        Chunk chunk = player.getLocation().getChunk();
        ChunkCoordinate coord = new ChunkCoordinate(chunk.getX(), chunk.getZ());
        SubChunk subChunk = region.getOrCreateSubChunk(coord);

        switch (subCmd) {
            case "flag":
                if (args.length < 4) {
                    player.sendMessage(messageManager.getMessage("usage.prefix") + messageManager.getMessage("usage.subchunk-flag"));
                    return;
                }
                Flag flag = Flag.fromString(args[2]);
                if (flag == null) {
                    player.sendMessage(messageManager.getMessage("invalid-flag", true));
                    return;
                }
                String valueStr = args[3].toLowerCase();
                Object value;
                if (valueStr.equals("reset")) {
                    subChunk.setFlag(flag.getName(), null);
                    player.sendMessage(messageManager.getMessage("subchunk-flag-reset-specific", true).replace("{flag}", flag.getName()));
                } else {
                    if (flag.getDefaultValue() instanceof Boolean) {
                        value = valueStr.equals("allow") || valueStr.equals("true");
                    } else {
                        value = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                    }
                    subChunk.setFlag(flag.getName(), value);
                    player.sendMessage(messageManager.getMessage("subchunk-flag-set-specific", true).replace("{flag}", flag.getName()).replace("{value}", value.toString()));
                }
                break;
            case "info":
                player.sendMessage(messageManager.getMessage("subchunk-info-header").replace("{x}", String.valueOf(coord.getX())).replace("{z}", String.valueOf(coord.getZ())));
                player.sendMessage(messageManager.getMessage("subchunk-info-parent").replace("{region}", region.getName()));
                if (subChunk.getFlags().isEmpty()) {
                    player.sendMessage(messageManager.getMessage("subchunk-info-no-flags"));
                } else {
                    player.sendMessage(messageManager.getMessage("subchunk-info-flags-header"));
                    subChunk.getFlags().forEach((name, val) -> player.sendMessage(messageManager.getMessage("info.flag-format").replace("{flag}", name).replace("{value}", val.toString())));
                }
                break;
            default:
                sendHelp(player);
                break;
        }
    }

    private boolean canManageOrAdmin(Player player, Region region) {
        if (player == null || region == null) return false;
        if (player.hasPermission("meoregion.admin")) return true;
        return region.getRole(player.getUniqueId()).getLevel() >= Role.MANAGER.getLevel();
    }

    private boolean isOwnerOrAdmin(Player player, Region region) {
        if (player == null || region == null) return false;
        return region.isOwner(player.getUniqueId()) || player.hasPermission("meoregion.admin");
    }

    private int getRegionLimit(Player player) {
        if (player.hasPermission("meoregion.limit.unlimited")) return Integer.MAX_VALUE;
        for (int i = 100; i > 0; i--) {
            if (player.hasPermission("meoregion.limit." + i)) return i;
        }
        return plugin.getConfig().getInt("defaults.default-region-limit", 5);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return Collections.emptyList();
        Player player = (Player) sender;
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subs = new ArrayList<>(Arrays.asList("claim","delete","info","list","gui","menu","help","add","remove","setrole","sell","unsell","buy","market","subchunk","grid","near","preview","merge","sch", "rent", "listrent", "unlistrent"));
            if(player.hasPermission("meoregion.admin")) {
                subs.addAll(Arrays.asList("adminclaim","flag","rename","reload", "log"));
            }
            StringUtil.copyPartialMatches(args[0], subs, completions);
        } else {
            String sub = args[0].toLowerCase();
            if (args.length == 2) {
                Stream<Region> regionStream = regionManager.getRegions().values().stream();
                Stream<String> regionNames;

                if (player.hasPermission("meoregion.admin")) {
                    regionNames = regionStream.map(Region::getName);
                } else {
                    regionNames = regionStream.map(Region::getName).filter(name -> !name.startsWith("__global_"));
                }

                if (Arrays.asList("info", "gui", "buy", "rent", "flag", "log").contains(sub)) {
                    completions.addAll(regionNames.collect(Collectors.toList()));
                } else if (Arrays.asList("delete", "sell", "unsell", "listrent", "unlistrent", "add", "remove", "setrole", "merge").contains(sub)) {
                    completions.addAll(regionManager.getRegions().values().stream()
                        .filter(r -> canManageOrAdmin(player, r))
                        .map(Region::getName)
                        .collect(Collectors.toList()));
                } else if (sub.equals("list")) {
                    completions.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
                    if(player.hasPermission("meoregion.admin")) completions.add("all");
                } else if (sub.equals("market")) {
                    completions.addAll(Arrays.asList("buy", "rent"));
                }
            } else if (args.length == 3) {
                switch (sub) {
                    case "subchunk":
                    case "sch":
                        if (args[1].equalsIgnoreCase("flag")) {
                            completions.addAll(Arrays.stream(Flag.values()).map(Flag::getName).collect(Collectors.toList()));
                        }
                        break;
                    default:
                        if (Arrays.asList("add","remove", "setrole").contains(sub)) {
                            completions.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
                        } else if (sub.equals("flag")) {
                            completions.add("list");
                            completions.addAll(Arrays.stream(Flag.values()).map(Flag::getName).collect(Collectors.toList()));
                        } else if (sub.equals("merge")) {
                             completions.addAll(regionManager.getPlayerRegions(player.getUniqueId()).stream()
                                .map(Region::getName)
                                .filter(name -> !name.equalsIgnoreCase(args[1]))
                                .collect(Collectors.toList()));
                        }
                        break;
                }
            } else if (args.length == 4) {
                switch (sub) {
                    case "subchunk":
                    case "sch":
                        if (args[1].equalsIgnoreCase("flag")) {
                            Flag flag = Flag.fromString(args[2]);
                            if (flag != null && flag.getDefaultValue() instanceof Boolean) {
                                completions.addAll(Arrays.asList("allow", "deny", "reset"));
                            }
                        }
                        break;
                    case "setrole":
                        completions.addAll(Arrays.stream(Role.values()).map(r -> r.name().toLowerCase()).collect(Collectors.toList()));
                        break;
                    case "flag":
                        Flag flag = Flag.fromString(args[2]);
                        if (flag != null && flag.getDefaultValue() instanceof Boolean) {
                            completions.addAll(Arrays.asList("allow", "deny", "reset"));
                        } else if (flag != null && flag.isListOfStrings()) {
                             completions.addAll(Arrays.asList("add", "remove"));
                        }
                        break;
                    default:
                        break;
                }
             }
             StringUtil.copyPartialMatches(args[args.length - 1], completions, new ArrayList<>());
        }
        return completions.stream().sorted().collect(Collectors.toList());
    }

    private OfflinePlayer findOfflinePlayerByName(String name) {
        if (name == null) return null;
        return Arrays.stream(Bukkit.getOfflinePlayers())
                .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }
}
