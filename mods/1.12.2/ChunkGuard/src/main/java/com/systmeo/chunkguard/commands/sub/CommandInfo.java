package com.systmeo.chunkguard.commands.sub;

import com.systmeo.chunkguard.ChunkGuard;
import com.systmeo.chunkguard.data.Region;
import com.systmeo.chunkguard.data.Role;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class CommandInfo extends CommandBase {

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/cg info [region_name]";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayer)) {
            sender.sendMessage(new TextComponentString("Цю команду може виконати лише гравець."));
            return;
        }

        EntityPlayer player = (EntityPlayer) sender;
        Region region;

        if (args.length == 0) {
            // Отримати регіон за місцемзнаходженням гравця
            region = ChunkGuard.getRegionManager().getHighestPriorityRegionAt(player.getPosition(), player.world);
            if (region == null || region.getName().startsWith("__global_")) {
                player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Ви не стоїте в жодному приватному регіоні."));
                return;
            }
        } else {
            // Отримати регіон за назвою
            String regionName = args[0];
            region = ChunkGuard.getRegionManager().getRegionByName(regionName);
            if (region == null) {
                player.sendMessage(new TextComponentString(TextFormatting.RED + "Регіон '" + regionName + "' не знайдено."));
                return;
            }
        }

        // Відображення інформації про регіон
        player.sendMessage(new TextComponentString(TextFormatting.GOLD + "--------- Інформація про регіон ---------"));
        player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Назва: " + TextFormatting.WHITE + region.getName()));

        String ownerName = region.getOwner()
                .map(uuid -> server.getPlayerProfileCache().getProfileByUUID(uuid))
                .map(profile -> profile != null ? profile.getName() : "Невідомо")
                .orElse("Ніхто");
        player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Власник: " + TextFormatting.WHITE + ownerName));

        String participants = region.getParticipants().entrySet().stream()
                .filter(entry -> entry.getValue() != Role.OWNER)
                .map(entry -> {
                    String playerName = server.getPlayerProfileCache().getProfileByUUID(entry.getKey()).getName();
                    return playerName + " (" + entry.getValue().getName() + ")";
                })
                .collect(Collectors.joining(", "));
        if (participants.isEmpty()) {
            participants = "Немає";
        }
        player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Учасники: " + TextFormatting.WHITE + participants));

        String flags = region.getFlags().entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(", "));
        if (flags.isEmpty()) {
            flags = "Не встановлено";
        }
        player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Прапори: " + TextFormatting.WHITE + flags));

        player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Пріоритет: " + TextFormatting.WHITE + region.getPriority()));
        player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Розмір: " + TextFormatting.WHITE + region.getSize() + " блоків²"));
        if (region.getParent() != null) {
            player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Батьківський регіон: " + TextFormatting.WHITE + region.getParent()));
        }
        player.sendMessage(new TextComponentString(TextFormatting.GOLD + "------------------------------------"));
    }
}
