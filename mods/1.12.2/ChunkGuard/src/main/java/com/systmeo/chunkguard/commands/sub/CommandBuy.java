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

public class CommandBuy extends CommandBase {

    @Override
    public String getName() {
        return "buy";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/cg buy <region_name>";
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

        if (!region.isForSale()) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Цей регіон не продається."));
            return;
        }

        if (region.isOwner(player.getUniqueID())) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Ви вже є власником цього регіону."));
            return;
        }

        int playerBalance = WalletManager.getBalance(player);
        if (playerBalance < region.getPrice()) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "У вас недостатньо коштів. Потрібно: " + region.getPrice() + ", у вас: " + playerBalance));
            return;
        }

        if (region.sellRegion(player, region.getPrice())) {
            sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Ви успішно купили регіон '" + regionName + "' за " + region.getPrice() + " монет."));
        } else {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Не вдалося купити регіон."));
        }
    }
}