package com.meoworld.meoregion.managers;

import com.meoworld.meoregion.MeoRegion;
import com.meoworld.meoregion.data.ChunkCoordinate;
import com.meoworld.meoregion.data.Region;
import com.meoworld.meoregion.data.SubChunk;
import com.meoworld.meoregion.util.Flag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RegionManager {

    public static final String GLOBAL_REGION_PREFIX = "__global_";
    private static final int MAX_PARENT_RECURSION_DEPTH = 10;

    private final MeoRegion plugin;
    private Map<String, Region> regions;
    private final Map<String, Set<Region>> chunkCache = new ConcurrentHashMap<>();

    public RegionManager(MeoRegion plugin) {
        this.plugin = plugin;
        this.regions = new ConcurrentHashMap<>();
    }

    public void loadRegions(Map<String, Region> loadedRegions) {
        this.regions = loadedRegions;
        updateCache();
    }

    public void setupGlobalRegions() {
        for (World world : plugin.getServer().getWorlds()) {
            String globalRegionName = GLOBAL_REGION_PREFIX + world.getName().toLowerCase();
            if (regions.get(globalRegionName.toLowerCase()) == null) {
                Region globalRegion = new Region(globalRegionName, null, world.getName(),
                        Integer.MIN_VALUE, 0, Integer.MIN_VALUE,
                        Integer.MAX_VALUE, 255, Integer.MAX_VALUE,
                        Integer.MAX_VALUE);

                for (Flag flag : Flag.values()) {
                    globalRegion.getFlags().put(flag.getName(), flag.getDefaultValue());
                }

                globalRegion.setPriority(-100);
                addRegion(globalRegion);
                plugin.getLogger().info("Created global region for world: " + world.getName());
            }
        }
    }

    public void updateCache() {
        chunkCache.clear();
        if (regions != null) {
            regions.values().forEach(this::addRegionToCache);
            if (plugin != null) {
                plugin.getLogger().info("Region chunk cache has been rebuilt (" + chunkCache.size() + " entries).");
            }
        }
    }

    private void addRegionToCache(Region region) {
        if (region.getName().startsWith(GLOBAL_REGION_PREFIX)) return;

        int minChunkX = region.getMinX() >> 4;
        int maxChunkX = region.getMaxX() >> 4;
        int minChunkZ = region.getMinZ() >> 4;
        int maxChunkZ = region.getMaxZ() >> 4;

        for (int x = minChunkX; x <= maxChunkX; x++) {
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                String key = region.getWorldName() + ":" + x + ":" + z;
                chunkCache.computeIfAbsent(key, k -> new HashSet<>()).add(region);
            }
        }
    }

    private void removeRegionFromCache(Region region) {
        if (region.getName().startsWith(GLOBAL_REGION_PREFIX)) return;
        int minChunkX = region.getMinX() >> 4;
        int maxChunkX = region.getMaxX() >> 4;
        int minChunkZ = region.getMinZ() >> 4;
        int maxChunkZ = region.getMaxZ() >> 4;

        for (int x = minChunkX; x <= maxChunkX; x++) {
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                String key = region.getWorldName() + ":" + x + ":" + z;
                chunkCache.computeIfPresent(key, (k, regionSet) -> {
                    regionSet.remove(region);
                    return regionSet.isEmpty() ? null : regionSet;
                });
            }
        }
    }

    public List<Region> getRegionsAt(Location location) {
        List<Region> foundRegions = new ArrayList<>();
        String cacheKey = location.getWorld().getName() + ":" + location.getChunk().getX() + ":" + location.getChunk().getZ();

        Set<Region> cached = chunkCache.getOrDefault(cacheKey, Collections.emptySet());
        for (Region region : cached) {
            if (location.getBlockX() >= region.getMinX() && location.getBlockX() <= region.getMaxX() &&
                    location.getBlockY() >= region.getMinY() && location.getBlockY() <= region.getMaxY() &&
                    location.getBlockZ() >= region.getMinZ() && location.getBlockZ() <= region.getMaxZ()) {
                foundRegions.add(region);
            }
        }

        Region globalRegion = getRegionByName(GLOBAL_REGION_PREFIX + location.getWorld().getName().toLowerCase());
        if(globalRegion != null) {
            foundRegions.add(globalRegion);
        }

        foundRegions.sort(Comparator.comparingInt(Region::getPriority).reversed());
        return foundRegions;
    }

    public Region getHighestPriorityRegionAt(Location location) {
        return getRegionsAt(location).stream().findFirst().orElse(null);
    }

    public Optional<SubChunk> getSubChunkAt(Location location) {
        Region region = getHighestPriorityRegionAt(location);
        if (region == null || region.getName().startsWith(GLOBAL_REGION_PREFIX)) {
            return Optional.empty();
        }
        ChunkCoordinate coord = new ChunkCoordinate(location.getChunk().getX(), location.getChunk().getZ());
        return region.getSubChunk(coord);
    }

    public Object getFlagValue(@SuppressWarnings("unused") Player player, Flag flag, Region region, ChunkCoordinate subChunkCoord) {
        if (subChunkCoord != null) {
            Object subChunkFlagValue = region.getSubChunk(subChunkCoord)
                    .map(sc -> sc.getFlagValue(flag.getName()))
                    .orElse(null);
            if (subChunkFlagValue != null) {
                return subChunkFlagValue;
            }
        }
        return getFlagValueRecursive(region, flag, MAX_PARENT_RECURSION_DEPTH);
    }

    public Object getFlagValue(@SuppressWarnings("unused") Player player, Flag flag, Location location) {
        Optional<SubChunk> subChunk = getSubChunkAt(location);
        if (subChunk.isPresent()) {
            Object subChunkFlagValue = subChunk.get().getFlagValue(flag.getName());
            if (subChunkFlagValue != null) {
                return subChunkFlagValue;
            }
        }

        for (Region region : getRegionsAt(location)) {
            Object value = getFlagValueRecursive(region, flag, MAX_PARENT_RECURSION_DEPTH);
            if (value != null) {
                return value;
            }
        }

        return flag.getDefaultValue();
    }

    private Object getFlagValueRecursive(Region currentRegion, Flag flag, int maxDepth) {
        if (currentRegion == null || maxDepth <= 0) return null;

        Object flagValue = currentRegion.getFlags().get(flag.getName());
        if (flagValue != null) {
            return flagValue;
        }

        if (currentRegion.getParent() != null) {
            Region parent = getRegionByName(currentRegion.getParent());
            if (parent != null && !parent.getName().equalsIgnoreCase(currentRegion.getName())) {
                return getFlagValueRecursive(parent, flag, maxDepth - 1);
            }
        }
        return null;
    }

    public Map<String, Object> getNonDefaultFlags(Region region) {
        Map<String, Object> nonDefault = new LinkedHashMap<>();
        World world = Bukkit.getWorld(region.getWorldName());
        if (world == null) return nonDefault;
        Location center = getRegionCenter(region).toLocation(world);

        for (Flag flag : Flag.values()) {
            Object value = getFlagValue(null, flag, center);
            if (!value.equals(flag.getDefaultValue())) {
                nonDefault.put(flag.getName(), value);
            }
        }
        return nonDefault;
    }

    public boolean getFlagValueBoolean(@SuppressWarnings("unused") Player player, Flag flag, Location location) {
        Object value = getFlagValue(player, flag, location);
        return value instanceof Boolean && (Boolean) value;
    }

    @SuppressWarnings({"unchecked", "unused"})
    public List<String> getFlagValueStringList(Player player, Flag flag, Location location) {
        Object value = getFlagValue(player, flag, location);
        if (value instanceof List) {
            return (List<String>) value;
        }
        return new ArrayList<>((List<String>) flag.getDefaultValue());
    }

    public String getFlagValueString(@SuppressWarnings("unused") Player player, Flag flag, Location location) {
        Object value = getFlagValue(player, flag, location);
        return (value instanceof String) ? (String) value : "none";
    }

    public boolean isAreaClaimed(int minX, int minZ, int maxX, int maxZ, String worldName) {
        int minChunkX = minX >> 4;
        int maxChunkX = maxX >> 4;
        int minChunkZ = minZ >> 4;
        int maxChunkZ = maxZ >> 4;

        Set<Region> checkedRegions = new HashSet<>();

        for (int x = minChunkX; x <= maxChunkX; x++) {
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                String key = worldName + ":" + x + ":" + z;
                Set<Region> cachedRegions = chunkCache.get(key);
                if (cachedRegions == null) {
                    continue;
                }

                for (Region existingRegion : cachedRegions) {
                    if (checkedRegions.contains(existingRegion)) {
                        continue;
                    }

                    if (minX <= existingRegion.getMaxX() && maxX >= existingRegion.getMinX() &&
                        minZ <= existingRegion.getMaxZ() && maxZ >= existingRegion.getMinZ()) {
                        return true;
                    }
                    checkedRegions.add(existingRegion);
                }
            }
        }
        return false;
    }

    public void addRegion(Region region) {
        if (regions == null) {
            regions = new ConcurrentHashMap<>();
        }
        regions.put(region.getName().toLowerCase(), region);
        addRegionToCache(region);
    }

    public void removeRegion(String regionName) {
        Region region = regions.remove(regionName.toLowerCase());
        if (region != null) {
            removeRegionFromCache(region);
        }
    }

    public int getPlayerTotalRegionSize(UUID playerUuid) {
        if (playerUuid == null || regions == null) return 0;
        return regions.values().stream()
                .filter(r -> r.isOwner(playerUuid))
                .mapToInt(Region::getSize)
                .sum();
    }

    public List<Region> getPlayerRegions(UUID playerUuid) {
        if (playerUuid == null || regions == null) return new ArrayList<>();
        return regions.values().stream()
                .filter(r -> r.isOwner(playerUuid))
                .collect(Collectors.toList());
    }

    public Region getRegionByName(String name) {
        if (name == null || regions == null) return null;
        String normalized = name.trim();
        if (normalized.startsWith("\"") && normalized.endsWith("\"") && normalized.length() >= 2) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        return regions.get(normalized.toLowerCase());
    }

    public Map<String, Region> getRegions() {
        return regions;
    }

    public Vector getRegionCenter(Region region) {
        return new Vector(
                (region.getMinX() + region.getMaxX()) / 2.0,
                (region.getMinY() + region.getMaxY()) / 2.0,
                (region.getMinZ() + region.getMaxZ()) / 2.0
        );
    }
}
