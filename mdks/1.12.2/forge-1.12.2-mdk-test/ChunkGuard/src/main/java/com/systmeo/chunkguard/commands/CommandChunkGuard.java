package com.systmeo.chunkguard.commands;

import com.systmeo.chunkguard.commands.sub.*;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandChunkGuard extends CommandBase {

    private final Map<String, CommandBase> subCommands = new HashMap<>();

    public CommandChunkGuard() {
        subCommands.put("create", new CommandCreate());
        subCommands.put("delete", new CommandDelete());
        subCommands.put("addmember", new CommandAddMember());
        subCommands.put("removemember", new CommandRemoveMember());
        subCommands.put("info", new CommandInfo());
        subCommands.put("list", new CommandList());
        subCommands.put("flag", new CommandFlag());
        subCommands.put("sell", new CommandSell());
        subCommands.put("buy", new CommandBuy());
        subCommands.put("rent", new CommandRent());
    }

    @Override
    public String getName() {
        return "chunkguard";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("cg");
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/cg <subcommand>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Використовуйте /cg help для списку команд."));
            return;
        }

        String subCommandName = args[0].toLowerCase();
        CommandBase subCommand = subCommands.get(subCommandName);

        if (subCommand == null) {
            throw new WrongUsageException("Невідома підкоманда. Використовуйте /cg help.");
        }

        String[] subCommandArgs = new String[args.length - 1];
        System.arraycopy(args, 1, subCommandArgs, 0, args.length - 1);
        subCommand.execute(server, sender, subCommandArgs);
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0; // Дозволи перевіряються в підкомандах
    }
}
