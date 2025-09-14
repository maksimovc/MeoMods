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
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import com.systmeo.permissions.PermissionsAPI;

public class CommandDelete extends CommandBase {

    @Override
    public String getName() {
        return "delete";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/cg delete <name>";
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
        Region region = ChunkGuard.getRegionManager().getRegionByName(regionName);

        if (region == null) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Регіон '" + regionName + "' не знайдено."));
            return;
        }

        // Check if player has admin delete permission or is the owner
        boolean hasAdminPermission = PermissionsAPI.hasPermission(player, "chunkguard.admin.delete");
        boolean isOwner = region.isOwner(player.getUniqueID());

        if (!hasAdminPermission && !isOwner) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Ви не маєте права видаляти цей регіон."));
            return;
        }

        // Видаляємо регіон
        ChunkGuard.getRegionManager().removeRegion(regionName);

        // Зберігаємо зміни
        ChunkGuard.getDataManager().saveRegions(ChunkGuard.getRegionManager().getRegions());

        sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Регіон '" + regionName + "' успішно видалено."));
    }
}
