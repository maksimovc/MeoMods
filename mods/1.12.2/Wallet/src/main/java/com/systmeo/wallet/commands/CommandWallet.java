package com.systmeo.wallet.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.entity.player.EntityPlayer;
import com.systmeo.wallet.WalletManager;
import com.systmeo.wallet.GuiHandler;
import javax.annotation.Nonnull;

public class CommandWallet extends CommandBase {
    @Override
    public String getName() {
        return "wallet";
    }

    @Override
    public @Nonnull String getUsage(@Nonnull ICommandSender sender) {
        return "/wallet";
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) {
        if (!(sender instanceof EntityPlayer)) {
            sender.sendMessage(new TextComponentString("Only players can use this command."));
            return;
        }
        EntityPlayer player = (EntityPlayer) sender;
        if (args.length == 0) {
            int bal = WalletManager.getBalance(player);
            player.sendMessage(new TextComponentString("Your balance: " + bal));
            return;
        }
        if (args[0].equalsIgnoreCase("add") && args.length == 2) {
            try {
                int amount = Integer.parseInt(args[1]);
                WalletManager.addBalance(player, amount);
                player.sendMessage(new TextComponentString("Added " + amount + ". New balance: " + WalletManager.getBalance(player)));
            } catch (NumberFormatException e) {
                player.sendMessage(new TextComponentString("Invalid amount."));
            }
            return;
        }
        if (args[0].equalsIgnoreCase("remove") && args.length == 2) {
            try {
                int amount = Integer.parseInt(args[1]);
                if (WalletManager.removeBalance(player, amount)) {
                    player.sendMessage(new TextComponentString("Removed " + amount + ". New balance: " + WalletManager.getBalance(player)));
                } else {
                    player.sendMessage(new TextComponentString("Not enough funds."));
                }
            } catch (NumberFormatException e) {
                player.sendMessage(new TextComponentString("Invalid amount."));
            }
            return;
        }
        if (args[0].equalsIgnoreCase("pay") && args.length == 3) {
            EntityPlayer target = server.getPlayerList().getPlayerByUsername(args[1]);
            if (target == null) {
                player.sendMessage(new TextComponentString("Player not found."));
                return;
            }
            try {
                int amount = Integer.parseInt(args[2]);
                if (WalletManager.removeBalance(player, amount)) {
                    WalletManager.addBalance(target, amount);
                    player.sendMessage(new TextComponentString("Sent " + amount + " to " + target.getName() + ". New balance: " + WalletManager.getBalance(player)));
                    target.sendMessage(new TextComponentString("You received " + amount + " from " + player.getName()));
                } else {
                    player.sendMessage(new TextComponentString("Not enough funds."));
                }
            } catch (NumberFormatException e) {
                player.sendMessage(new TextComponentString("Invalid amount."));
            }
            return;
        }
        if (args[0].equalsIgnoreCase("gui")) {
            if (player.world.isRemote) {
                // Only run on client
                return;
            }
            GuiHandler.openWalletGui(player);
            return;
        }
        player.sendMessage(new TextComponentString("Usage: /wallet [add|remove|pay] ..."));
    }
}
