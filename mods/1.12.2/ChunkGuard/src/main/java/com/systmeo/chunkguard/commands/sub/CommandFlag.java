package com.systmeo.chunkguard.commands.sub;

import com.systmeo.chunkguard.ChunkGuard;
import com.systmeo.chunkguard.data.Region;
import com.systmeo.chunkguard.data.Role;
import com.systmeo.permissions.PermissionsAPI;
import com.systmeo.chunkguard.util.Flag;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;

public class CommandFlag extends CommandBase {

    @Override
    public String getName() {
        return "flag";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/cg flag <region_name> <flag_name> [value]";
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
        String flagName = args[1];

        Region region = ChunkGuard.getRegionManager().getRegionByName(regionName);
        if (region == null) {
            player.sendMessage(new TextComponentString(TextFormatting.RED + "Регіон '" + regionName + "' не знайдено."));
            return;
        }

        if (!region.hasPermission(player.getUniqueID(), Role.Permission.SET_FLAGS) &&
            !PermissionsAPI.hasPermission(player, "chunkguard.admin.flags")) {
            player.sendMessage(new TextComponentString(TextFormatting.RED + "Ви не маєте права керувати прапорами в цьому регіоні."));
            return;
        }

        Flag flag = Flag.fromString(flagName);
        if (flag == null) {
            player.sendMessage(new TextComponentString(TextFormatting.RED + "Прапор '" + flagName + "' не існує."));
            return;
        }

        // Якщо значення не вказано, показуємо поточне
        if (args.length == 2) {
            Object currentValue = region.getFlags().getOrDefault(flag.getName(), "(не встановлено, за замовчуванням: " + flag.getDefaultValue() + ")");
            player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Поточне значення прапора '" + flag.getName() + "' для регіону '" + region.getName() + "': " + TextFormatting.WHITE + currentValue));
            return;
        }

        // Встановлюємо нове значення
        String valueStr = args[2];
        Object value;

        try {
            if (flag.getDefaultValue() instanceof Boolean) {
                if (valueStr.equalsIgnoreCase("true") || valueStr.equalsIgnoreCase("allow")) {
                    value = true;
                } else if (valueStr.equalsIgnoreCase("false") || valueStr.equalsIgnoreCase("deny")) {
                    value = false;
                } else {
                    throw new Exception();
                }
            } else if (flag.getDefaultValue() instanceof String) {
                value = valueStr;
            } else if (flag.isListOfStrings()) {
                // Для списків ми будемо використовувати додаткові команди (add/remove), тут просто показуємо
                player.sendMessage(new TextComponentString(TextFormatting.RED + "Для управління списками використовуйте /cg flag " + regionName + " " + flagName + " <add|remove|list> <value>"));
                return;
            } else {
                value = valueStr;
            }
        } catch (Exception e) {
            player.sendMessage(new TextComponentString(TextFormatting.RED + "Неправильне значення '" + valueStr + "' для прапора '" + flag.getName() + "'."));
            return;
        }

        region.getFlags().put(flag.getName(), value);
        ChunkGuard.getDataManager().saveRegions(ChunkGuard.getRegionManager().getRegions());

        player.sendMessage(new TextComponentString(TextFormatting.GREEN + "Прапор '" + flag.getName() + "' для регіону '" + region.getName() + "' встановлено на '" + value + "'."));
    }
}
