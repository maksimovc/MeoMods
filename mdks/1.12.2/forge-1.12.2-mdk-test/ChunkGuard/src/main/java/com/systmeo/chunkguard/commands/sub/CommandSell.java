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

public class CommandSell extends CommandBase {

    @Override
    public String getName() {
        return "sell";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/cg sell <region_name> <price>";
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
        double price;

        try {
            price = Double.parseDouble(args[1]);
            if (price <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Неправильна ціна."));
            return;
        }

        Region region = ChunkGuard.getRegionManager().getRegionByName(regionName);
        if (region == null) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Регіон '" + regionName + "' не знайдено."));
            return;
        }

        if (!region.isOwner(player.getUniqueID()) && !PermissionsAPI.hasPermission(player, "chunkguard.admin.sell")) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Ви не є власником цього регіону."));
            return;
        }

        region.setForSale(true);
        region.setPrice(price);
        ChunkGuard.getDataManager().saveRegions(ChunkGuard.getRegionManager().getRegions());

        sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Регіон '" + regionName + "' виставлено на продаж за " + price + " монет."));
    }
}