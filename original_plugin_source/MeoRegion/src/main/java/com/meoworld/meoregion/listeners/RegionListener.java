package com.meoworld.meoregion.listeners;

import com.meoworld.meoregion.MeoRegion;
import com.meoworld.meoregion.data.Region;
import com.meoworld.meoregion.data.Role;
import com.meoworld.meoregion.managers.MessageManager;
import com.meoworld.meoregion.managers.RegionManager;
import com.meoworld.meoregion.util.Flag;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RegionListener implements Listener {

    private final RegionManager regionManager;
    private final MessageManager messageManager;
    private final Map<UUID, String> playerRegionCache = new ConcurrentHashMap<>();

    private final Set<Material> interactContainers = EnumSet.of(
            Material.CHEST, Material.TRAPPED_CHEST, Material.FURNACE, Material.BURNING_FURNACE,
            Material.DISPENSER, Material.DROPPER, Material.HOPPER, Material.BREWING_STAND,
            Material.BEACON, Material.ANVIL, Material.ENCHANTMENT_TABLE, Material.ENDER_CHEST, Material.WORKBENCH,
            Material.BLACK_SHULKER_BOX, Material.BLUE_SHULKER_BOX, Material.BROWN_SHULKER_BOX, Material.CYAN_SHULKER_BOX,
            Material.GRAY_SHULKER_BOX, Material.GREEN_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX, Material.LIME_SHULKER_BOX,
            Material.MAGENTA_SHULKER_BOX, Material.ORANGE_SHULKER_BOX, Material.PINK_SHULKER_BOX, Material.PURPLE_SHULKER_BOX,
            Material.RED_SHULKER_BOX, Material.SILVER_SHULKER_BOX, Material.WHITE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX
    );
    private final Set<Material> interactDoorsAndSwitches = EnumSet.of(
            Material.WOODEN_DOOR, Material.SPRUCE_DOOR, Material.BIRCH_DOOR, Material.JUNGLE_DOOR,
            Material.ACACIA_DOOR, Material.DARK_OAK_DOOR, Material.IRON_DOOR_BLOCK, Material.FENCE_GATE,
            Material.SPRUCE_FENCE_GATE, Material.BIRCH_FENCE_GATE, Material.JUNGLE_FENCE_GATE,
            Material.ACACIA_FENCE_GATE, Material.DARK_OAK_FENCE_GATE, Material.TRAP_DOOR, Material.IRON_TRAPDOOR,
            Material.LEVER, Material.STONE_BUTTON, Material.WOOD_BUTTON, Material.NOTE_BLOCK, Material.JUKEBOX,
            Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON, Material.REDSTONE_COMPARATOR_OFF, Material.REDSTONE_COMPARATOR_ON,
            Material.CAULDRON
    );

    public RegionListener(MeoRegion plugin) {
        this.regionManager = plugin.getRegionManager();
        this.messageManager = plugin.getMessageManager();
    }

    //<editor-fold desc="Helper Methods">
    private void sendDenyMessage(Player player, Location location, String messageKey) {
        String customMessage = regionManager.getFlagValueString(player, Flag.DENY_MESSAGE, location);
        if (customMessage != null && !customMessage.equalsIgnoreCase("none") && !customMessage.isEmpty()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', customMessage));
        } else {
            player.sendMessage(messageManager.getMessage(messageKey, true));
        }
    }

    private boolean hasPermission(Player player, Location location, Role.Permission permission) {
        if (player.hasPermission("meoregion.admin")) return true;
        Region region = regionManager.getHighestPriorityRegionAt(location);
        return region == null || region.hasPermission(player.getUniqueId(), permission);
    }

    private Player getPlayerAttacker(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent edbe = (EntityDamageByEntityEvent) event;
            Entity damager = edbe.getDamager();

            if (damager instanceof Player) {
                return (Player) damager;
            }
            if (damager instanceof Projectile) {
                ProjectileSource shooter = ((Projectile) damager).getShooter();
                if (shooter instanceof Player) {
                    return (Player) shooter;
                }
            }
            if (damager instanceof TNTPrimed) {
                Entity source = ((TNTPrimed) damager).getSource();
                if (source instanceof Player) {
                    return (Player) source;
                }
            }
            if (damager instanceof EnderCrystal) {
                EntityDamageEvent crystalDamage = damager.getLastDamageCause();
                if (crystalDamage instanceof EntityDamageByEntityEvent) {
                    Entity crystalDamager = ((EntityDamageByEntityEvent) crystalDamage).getDamager();
                    if (crystalDamager instanceof Player) {
                        return (Player) crystalDamager;
                    }
                }
            }
        }
        return null;
    }
    //</editor-fold>

    //<editor-fold desc="Flag Handling Methods">
    private void handlePvpCheck(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player victim = (Player) event.getEntity();
        Player attacker = getPlayerAttacker(event);

        if (attacker != null && !attacker.equals(victim)) {
            if (attacker.hasPermission("meoregion.admin.pvp")) return;
            if (!regionManager.getFlagValueBoolean(attacker, Flag.PVP, victim.getLocation())) {
                event.setCancelled(true);
                sendDenyMessage(attacker, victim.getLocation(), "deny-pvp");
            }
        }
    }

    private void handleEntityDamageByPlayer(EntityDamageByEntityEvent event) {
        Player damager = getPlayerAttacker(event);
        if (damager == null || damager.hasPermission("meoregion.admin")) return;

        Entity victim = event.getEntity();
        Location loc = victim.getLocation();

        if (hasPermission(damager, loc, Role.Permission.BUILD)) return;

        if (victim instanceof Animals || victim instanceof WaterMob || victim instanceof Villager || victim instanceof Golem) {
            if (!regionManager.getFlagValueBoolean(damager, Flag.ANIMAL_DAMAGE, loc)) {
                event.setCancelled(true);
                sendDenyMessage(damager, loc, "deny-damage-animal");
            }
        } else if (victim instanceof Monster) {
            if (!regionManager.getFlagValueBoolean(damager, Flag.MONSTER_DAMAGE, loc)) {
                event.setCancelled(true);
                sendDenyMessage(damager, loc, "deny-damage-monster");
            }
        } else if (!(victim instanceof Player)) {
            event.setCancelled(true);
            sendDenyMessage(damager, loc, "deny-break");
        }
    }

    private void handleContainerAccess(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || !interactContainers.contains(clickedBlock.getType())) return;

        Player player = event.getPlayer();
        if (hasPermission(player, clickedBlock.getLocation(), Role.Permission.INTERACT)) return;

        if (!regionManager.getFlagValueBoolean(player, Flag.CHEST_ACCESS, clickedBlock.getLocation())) {
            event.setCancelled(true);
            sendDenyMessage(player, clickedBlock.getLocation(), "deny-interact");
        }
    }

    private void handleUse(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || !interactDoorsAndSwitches.contains(clickedBlock.getType())) return;

        Player player = event.getPlayer();
        if (hasPermission(player, clickedBlock.getLocation(), Role.Permission.INTERACT)) return;

        if (!regionManager.getFlagValueBoolean(player, Flag.USE, clickedBlock.getLocation())) {
            event.setCancelled(true);
            sendDenyMessage(player, clickedBlock.getLocation(), "deny-interact");
        }
    }

    private void handleExplosion(EntityExplodeEvent event) {
        Location loc = event.getLocation();
        boolean cancelled = false;
        EntityType type = event.getEntityType();

        if (type == EntityType.PRIMED_TNT || type == EntityType.MINECART_TNT) {
            if (!regionManager.getFlagValueBoolean(null, Flag.TNT, loc)) cancelled = true;
        } else if (type == EntityType.CREEPER) {
            if (!regionManager.getFlagValueBoolean(null, Flag.CREEPER_EXPLOSION, loc)) cancelled = true;
        } else {
            if (!regionManager.getFlagValueBoolean(null, Flag.EXPLOSION, loc)) cancelled = true;
        }

        if (cancelled) {
            event.setYield(0f);
            event.setCancelled(true);
        }
    }

    private void handleIgnite(BlockIgniteEvent event) {
        BlockIgniteEvent.IgniteCause cause = event.getCause();
        if (cause == BlockIgniteEvent.IgniteCause.SPREAD || cause == BlockIgniteEvent.IgniteCause.LAVA) {
            if (!regionManager.getFlagValueBoolean(null, Flag.FIRE_SPREAD, event.getBlock().getLocation())) event.setCancelled(true);
        } else if (cause == BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL || cause == BlockIgniteEvent.IgniteCause.FIREBALL) {
            Player player = event.getPlayer();
            if (player != null && !hasPermission(player, event.getBlock().getLocation(), Role.Permission.BUILD)) {
                event.setCancelled(true);
                sendDenyMessage(player, event.getBlock().getLocation(), "deny-ignite");
            }
        }
    }

    private void handleFlightCheck(Player player, Location to) {
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        boolean canFly = regionManager.getFlagValueBoolean(player, Flag.FLIGHT, to);
        if (player.getAllowFlight() && !canFly) {
            player.setAllowFlight(false);
            player.setFlying(false);
            sendDenyMessage(player, to, "deny-flight");
        } else if (!player.getAllowFlight() && canFly) {
            player.setAllowFlight(true);
        }
    }

    private void handleRegionTransition(Player player, Location from, Location to) {
        Region toRegion = regionManager.getHighestPriorityRegionAt(to);
        String fromRegionName = playerRegionCache.get(player.getUniqueId());
        String toRegionName = (toRegion != null && !toRegion.getName().startsWith(RegionManager.GLOBAL_REGION_PREFIX)) ? toRegion.getName() : null;

        if (!Objects.equals(fromRegionName, toRegionName)) {
            if (fromRegionName != null) {
                Region fromRegion = regionManager.getRegionByName(fromRegionName);
                if (fromRegion != null) {
                    String farewell = regionManager.getFlagValueString(player, Flag.FAREWELL, from);
                    if (farewell != null && !farewell.equalsIgnoreCase("none") && !farewell.isEmpty()) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', farewell.replace("{region}", fromRegionName)));
                    }
                }
            }
            if (toRegionName != null) {
                String greeting = regionManager.getFlagValueString(player, Flag.GREETING, to);
                if (greeting != null && !greeting.equalsIgnoreCase("none") && !greeting.isEmpty()) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', greeting.replace("{region}", toRegionName)));
                }
            }

            if (toRegionName != null) {
                playerRegionCache.put(player.getUniqueId(), toRegionName);
            } else {
                playerRegionCache.remove(player.getUniqueId());
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="Event Handlers">

    //<editor-fold desc="Block Events">
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!hasPermission(event.getPlayer(), event.getBlock().getLocation(), Role.Permission.BUILD)) {
            event.setCancelled(true);
            sendDenyMessage(event.getPlayer(), event.getBlock().getLocation(), "deny-break");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!hasPermission(event.getPlayer(), event.getBlock().getLocation(), Role.Permission.BUILD)) {
            event.setCancelled(true);
            sendDenyMessage(event.getPlayer(), event.getBlock().getLocation(), "deny-place");
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        handleIgnite(event);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        if (!regionManager.getFlagValueBoolean(null, Flag.FIRE_SPREAD, event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockRedstone(BlockRedstoneEvent event) {
        if (!regionManager.getFlagValueBoolean(null, Flag.REDSTONE, event.getBlock().getLocation())) {
            event.setNewCurrent(event.getOldCurrent());
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (!regionManager.getFlagValueBoolean(null, Flag.PISTON, event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (!regionManager.getFlagValueBoolean(null, Flag.PISTON, event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event) {
        Flag flag = null;
        if (event.getBlock().getType() == Material.ICE) flag = Flag.ICE_MELT;
        else if (event.getBlock().getType() == Material.SNOW) flag = Flag.SNOW_MELT;
        else if (event.getBlock().getType() == Material.SOIL) flag = Flag.SOIL_DRY;

        if (flag != null && !regionManager.getFlagValueBoolean(null, flag, event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event) {
        if (!regionManager.getFlagValueBoolean(null, Flag.LEAF_DECAY, event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }
    //</editor-fold>

    //<editor-fold desc="Entity Events">
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        handlePvpCheck(event);

        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (getPlayerAttacker(event) == null) {
                if (!regionManager.getFlagValueBoolean(player, Flag.MOB_DAMAGE, player.getLocation())) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (regionManager.getFlagValueBoolean(player, Flag.GOD_MODE, player.getLocation())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && getPlayerAttacker(event) != null) {
            return;
        }
        handleEntityDamageByPlayer(event);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event) {
        ProjectileSource shooter = event.getPotion().getShooter();
        if (!(shooter instanceof Player)) return;

        for (LivingEntity entity : event.getAffectedEntities()) {
            if (entity instanceof Player) {
                Player victim = (Player) entity;
                Player attacker = (Player) shooter;
                if (attacker.equals(victim)) continue;

                if (!regionManager.getFlagValueBoolean(attacker, Flag.PVP, victim.getLocation())) {
                    event.setIntensity(victim, 0);
                    sendDenyMessage(attacker, victim.getLocation(), "deny-pvp");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        handleExplosion(event);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (event.getRemover() instanceof Player) {
            if (!hasPermission((Player) event.getRemover(), event.getEntity().getLocation(), Role.Permission.BUILD)) {
                event.setCancelled(true);
                sendDenyMessage((Player) event.getRemover(), event.getEntity().getLocation(), "deny-break");
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="Player Events">
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("meoregion.admin")) return;

        Block clickedBlock = event.getClickedBlock();
        Location loc = (clickedBlock != null) ? clickedBlock.getLocation() : player.getLocation();

        if (event.hasItem()) {
            List<String> bannedItems = regionManager.getFlagValueStringList(player, Flag.BANNED_USE_ITEMS, loc);
            if (bannedItems != null && bannedItems.contains(event.getItem().getType().toString().toUpperCase())) {
                event.setCancelled(true);
                sendDenyMessage(player, loc, "deny-interact");
                return;
            }
        }

        if (clickedBlock == null) return;

        handleContainerAccess(event);

        handleUse(event);

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (!hasPermission(player, loc, Role.Permission.BUILD)) {
                event.setCancelled(true);
                sendDenyMessage(player, loc, "deny-interact");
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockY() == event.getTo().getBlockY() && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Player player = event.getPlayer();
        Location to = event.getTo();
        Region toRegion = regionManager.getHighestPriorityRegionAt(to);

        if (toRegion != null && !toRegion.hasPermission(player.getUniqueId(), Role.Permission.ENTER) && !player.hasPermission("meoregion.admin")) {
            if (regionManager.getFlagValueBoolean(player, Flag.DENY_ENTER, to)) {
                sendDenyMessage(player, to, "deny-enter");
                event.setTo(event.getFrom());
                return;
            }
        }

        handleRegionTransition(player, event.getFrom(), to);
        handleFlightCheck(player, to);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Location deathLocation = player.getLocation();

        if (regionManager.getFlagValueBoolean(player, Flag.KEEP_INVENTORY, deathLocation)) {
            event.setKeepInventory(true);
            event.getDrops().clear();
        }
        if (regionManager.getFlagValueBoolean(player, Flag.KEEP_EXPERIENCE, deathLocation)) {
            event.setKeepLevel(true);
            event.setDroppedExp(0);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!hasPermission(event.getPlayer(), event.getPlayer().getLocation(), Role.Permission.BUILD)) {
            if (!regionManager.getFlagValueBoolean(event.getPlayer(), Flag.ITEM_DROP, event.getPlayer().getLocation())) {
                event.setCancelled(true);
                sendDenyMessage(event.getPlayer(), event.getPlayer().getLocation(), "deny-item-drop");
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (!hasPermission(player, player.getLocation(), Role.Permission.BUILD)) {
            if (!regionManager.getFlagValueBoolean(player, Flag.ITEM_PICKUP, player.getLocation())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Region region = regionManager.getHighestPriorityRegionAt(player.getLocation());
        String regionName = (region != null && !region.getName().startsWith(RegionManager.GLOBAL_REGION_PREFIX)) ? region.getName() : null;
        if (regionName != null) {
            playerRegionCache.put(player.getUniqueId(), regionName);
        } else {
            playerRegionCache.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerRegionCache.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        playerRegionCache.remove(event.getPlayer().getUniqueId());
    }
    //</editor-fold>

    //<editor-fold desc="Vehicle Events">
    @EventHandler(ignoreCancelled = true)
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (!(event.getEntered() instanceof Player)) return;
        Player player = (Player) event.getEntered();
        if (!hasPermission(player, event.getVehicle().getLocation(), Role.Permission.BUILD)) {
            if (!regionManager.getFlagValueBoolean(player, Flag.VEHICLE_USE, event.getVehicle().getLocation())) {
                event.setCancelled(true);
                sendDenyMessage(player, event.getVehicle().getLocation(), "deny-vehicle-use");
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        if (!(event.getAttacker() instanceof Player)) return;
        Player player = (Player) event.getAttacker();
        if (!hasPermission(player, event.getVehicle().getLocation(), Role.Permission.BUILD)) {
            if (!regionManager.getFlagValueBoolean(player, Flag.VEHICLE_DESTROY, event.getVehicle().getLocation())) {
                event.setCancelled(true);
                sendDenyMessage(player, event.getVehicle().getLocation(), "deny-vehicle-destroy");
            }
        }
    }
    //</editor-fold>
}
