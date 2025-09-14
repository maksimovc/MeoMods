package com.meoworld.meoregion.tasks;

import org.bukkit.Chunk;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PreviewTask extends BukkitRunnable {

    private final Player player;
    private Chunk lastPlayerChunk;

    public PreviewTask(Player player) {
        this.player = player;
        this.lastPlayerChunk = player.getLocation().getChunk();
    }

    @Override
    public void run() {
        if (!player.isOnline()) {
            this.cancel();
            return;
        }

        Chunk currentChunk = player.getLocation().getChunk();
        if (lastPlayerChunk.getX() != currentChunk.getX() || lastPlayerChunk.getZ() != currentChunk.getZ()) {
            lastPlayerChunk = currentChunk;
        }
        displayPreviewBorders(currentChunk);
    }

    private void displayPreviewBorders(Chunk centerChunk) {
        World world = player.getWorld();
        int radius = 1;
        int centerX = centerChunk.getX();
        int centerZ = centerChunk.getZ();

        int minChunkX = centerX - radius;
        int maxChunkX = centerX + radius;
        int minChunkZ = centerZ - radius;
        int maxChunkZ = centerZ + radius;

        int minBlockX = minChunkX << 4;
        int maxBlockX = (maxChunkX << 4) + 15;
        int minBlockZ = minChunkZ << 4;
        int maxBlockZ = (maxChunkZ << 4) + 15;

        double playerY = player.getEyeLocation().getY();
        double step = 2.0;

        for (double x = minBlockX; x <= maxBlockX; x += step) {
            spawnParticleLine(world, x, playerY, minBlockZ);
            spawnParticleLine(world, x, playerY, maxBlockZ + 1);
        }
        for (double z = minBlockZ; z <= maxBlockZ; z += step) {
            spawnParticleLine(world, minBlockX, playerY, z);
            spawnParticleLine(world, maxBlockX + 1, playerY, z);
        }
    }

    private void spawnParticleLine(World world, double x, double y, double z) {
        for (int i = -6; i <= 6; i++) {
            world.spawnParticle(Particle.FLAME, x, y + i, z, 1, 0, 0, 0, 0);
        }
    }
}