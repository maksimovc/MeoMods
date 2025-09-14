package com.meoworld.meoregion.tasks;

import com.meoworld.meoregion.MeoRegion;
import com.meoworld.meoregion.commands.RegionCommand;
import com.meoworld.meoregion.data.Region;
import com.meoworld.meoregion.managers.RegionManager;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class GridTask extends BukkitRunnable {

    private final RegionManager regionManager;

    public GridTask(MeoRegion plugin) {
        this.regionManager = plugin.getRegionManager();
    }

    @Override
    public void run() {
        Iterator<Map.Entry<UUID, String>> iterator = RegionCommand.gridViewPlayers.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, String> entry = iterator.next();
            Player player = Bukkit.getPlayer(entry.getKey());

            if (player == null || !player.isOnline()) {
                iterator.remove();
                continue;
            }

            Region region = regionManager.getRegionByName(entry.getValue());
            if (region == null) {
                iterator.remove();
                continue;
            }

            if (player.getWorld().getName().equals(region.getWorldName())) {
                displayBorders(player, region);
            }
        }
    }

    private void displayBorders(Player player, Region region) {
        World world = player.getWorld();

        int regionMinX = region.getMinX();
        int regionMaxX = region.getMaxX();
        int regionMinZ = region.getMinZ();
        int regionMaxZ = region.getMaxZ();

        double playerX = player.getLocation().getX();
        double playerZ = player.getLocation().getZ();
        double playerY = player.getEyeLocation().getY();
        int radius = 48;

        double step = 2.0;

        for (double z = regionMinZ; z <= regionMaxZ; z += step) {
            if (Math.abs(z - playerZ) <= radius) {
                if (Math.abs(regionMinX - playerX) <= radius) {
                    spawnParticleLine(world, regionMinX, playerY, z);
                }
                if (Math.abs(regionMaxX + 1 - playerX) <= radius) {
                    spawnParticleLine(world, regionMaxX + 1, playerY, z);
                }
            }
        }

        for (double x = regionMinX; x <= regionMaxX + 1; x += step) {
            if (Math.abs(x - playerX) <= radius) {
                if (Math.abs(regionMinZ - playerZ) <= radius) {
                    spawnParticleLine(world, x, playerY, regionMinZ);
                }
                if (Math.abs(regionMaxZ + 1 - playerZ) <= radius) {
                    spawnParticleLine(world, x, playerY, regionMaxZ + 1);
                }
            }
        }
    }

    private void spawnParticleLine(World world, double x, double y, double z) {
        for (int i = -4; i <= 4; i++) {
            world.spawnParticle(Particle.VILLAGER_HAPPY, x, y + i, z, 1, 0, 0, 0, 0);
        }
    }
}