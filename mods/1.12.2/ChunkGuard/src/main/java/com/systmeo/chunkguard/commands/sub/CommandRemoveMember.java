package com.systmeo.chunkguard.commands.sub;

import com.mojang.authlib.GameProfile;
import com.systmeo.chunkguard.ChunkGuard;
import com.systmeo.chunkguard.data.Region;
import com.systmeo.chunkguard.data.Role;
import com.systmeo.permissions.PermissionsAPI;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.UUID;

public class CommandRemoveMember extends CommandBase {

    @Override
    public String getName() {
        return "removemember";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/cg removemember <region_name> <player_name>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayer)) {
            sender.sendMessage(new TextComponentString("Цю команду може виконати лише гравець."));
            return;
        }

        if (args.length < 2) {
            throw new WrongUsageException(getUsage(sender));
        }

        EntityPlayer player = (EntityPlayer) sender;
        String regionName = args[0];
        String targetPlayerName = args[1];

        Region region = ChunkGuard.getRegionManager().getRegionByName(regionName);

        if (region == null) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Регіон '" + regionName + "' не знайдено."));
            return;
        }

        if (!region.hasPermission(player.getUniqueID(), Role.Permission.MANAGE_MEMBERS) &&
            !PermissionsAPI.hasPermission(player, "chunkguard.admin.members")) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Ви не маєте права керувати учасниками в цьому регіоні."));
            return;
        }

        GameProfile targetProfile = server.getPlayerProfileCache().getGameProfileForUsername(targetPlayerName);
        if (targetProfile == null) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Гравець '" + targetPlayerName + "' не знайдений."));
            return;
        }
        UUID targetUUID = targetProfile.getId();

        if (region.isOwner(targetUUID)) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Ви не можете видалити власника з регіону."));
            return;
        }

        if (region.getRole(targetUUID) == Role.GUEST) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Гравець '" + targetPlayerName + "' не є учасником цього регіону."));
            return;
        }

        // Встановлюємо роль GUEST, що фактично видаляє гравця зі списку учасників
        region.setParticipantRole(targetUUID, Role.GUEST);

        ChunkGuard.getDataManager().saveRegions(ChunkGuard.getRegionManager().getRegions());

        sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Гравець '" + targetPlayerName + "' успішно видалений з регіону '" + regionName + "."));
    }
}
