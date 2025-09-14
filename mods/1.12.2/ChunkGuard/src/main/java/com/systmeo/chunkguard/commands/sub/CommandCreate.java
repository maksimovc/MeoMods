package com.systmeo.chunkguard.commands.sub;

import com.systmeo.chunkguard.ChunkGuard;
import com.systmeo.chunkguard.data.Region;
import com.systmeo.permissions.PermissionsAPI;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class CommandCreate extends CommandBase {

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/cg create <name>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayer)) {
            sender.sendMessage(new TextComponentString("Цю команду може виконати лише гравець."));
            return;
        }

        if (args.length < 1) {
            throw new WrongUsageException(getUsage(sender));
        }

        EntityPlayer player = (EntityPlayer) sender;
        String regionName = args[0];

        // Check if player has permission to create regions
        if (!PermissionsAPI.hasPermission(player, "chunkguard.create")) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "У вас немає дозволу створювати регіони."));
            return;
        }

        // Перевірка на унікальність імені регіону
        if (ChunkGuard.getRegionManager().getRegionByName(regionName) != null) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Регіон з такою назвою вже існує."));
            return;
        }

        // Визначаємо чанк, в якому знаходиться гравець
        ChunkPos chunkPos = player.world.getChunkFromBlockCoords(player.getPosition()).getPos();
        int minX = chunkPos.x * 16;
        int minZ = chunkPos.z * 16;
        int maxX = minX + 15;
        int maxZ = minZ + 15;
        String worldName = player.world.provider.getDimensionType().getName();

        // Перевірка, чи територія вже не зайнята
        if (ChunkGuard.getRegionManager().isAreaClaimed(minX, minZ, maxX, maxZ, worldName)) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Ця територія вже зайнята."));
            return;
        }

        // Створюємо регіон, вказуючи гравця як власника
        int size = (maxX - minX + 1) * (maxZ - minZ + 1);
        Region region = new Region(regionName, player.getUniqueID(), worldName, minX, 0, minZ, maxX, 255, maxZ, size);
        
        // Додаємо регіон в менеджер
        ChunkGuard.getRegionManager().addRegion(region);

        // Зберігаємо зміни
        ChunkGuard.getDataManager().saveRegions(ChunkGuard.getRegionManager().getRegions());

        sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Регіон '" + regionName + "' успішно створено! Ви автоматично стали його власником."));
    }
}
