package com.systmeo.chunkguard.commands.sub;

import com.systmeo.chunkguard.ChunkGuard;
import com.systmeo.chunkguard.data.Region;
import com.systmeo.permissions.PermissionsAPI;
import com.systmeo.wallet.WalletManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import com.systmeo.permissions.PermissionsAPI;

public class CommandRent extends CommandBase {

    @Override
    public String getName() {
        return "rent";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/cg rent <region_name> <days>";
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
        int days;

        try {
            days = Integer.parseInt(args[1]);
            if (days <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Неправильна кількість днів."));
            return;
        }

        Region region = ChunkGuard.getRegionManager().getRegionByName(regionName);
        if (region == null) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Регіон '" + regionName + "' не знайдено."));
            return;
        }

        if (!region.isForRent()) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Цей регіон не здається в оренду."));
            return;
        }

        double totalCost = region.getRentPrice() * days;
        int playerBalance = WalletManager.getBalance(player);

        if (playerBalance < totalCost) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "У вас недостатньо коштів. Потрібно: " + totalCost + ", у вас: " + playerBalance));
            return;
        }

        if (region.rentRegion(player, days)) {
            ChunkGuard.getDataManager().saveRegions(ChunkGuard.getRegionManager().getRegions());
            sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Ви успішно орендували регіон '" + regionName + "' на " + days + " днів за " + totalCost + " монет."));
        } else {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Не вдалося орендувати регіон."));
        }
    }
}