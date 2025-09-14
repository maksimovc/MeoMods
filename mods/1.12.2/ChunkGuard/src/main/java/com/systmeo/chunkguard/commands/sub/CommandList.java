package com.systmeo.chunkguard.commands.sub;

import com.systmeo.chunkguard.ChunkGuard;
import com.systmeo.chunkguard.data.Region;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.List;
import java.util.stream.Collectors;

public class CommandList extends CommandBase {

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/cg list";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayer)) {
            sender.sendMessage(new TextComponentString("Цю команду може виконати лише гравець."));
            return;
        }

        EntityPlayer player = (EntityPlayer) sender;

        List<Region> playerRegions = ChunkGuard.getRegionManager().getPlayerRegions(player.getUniqueID());

        if (playerRegions.isEmpty()) {
            player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "У вас немає жодного регіону."));
            return;
        }

        String regionNames = playerRegions.stream()
                .map(Region::getName)
                .collect(Collectors.joining(", "));

        player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Ваші регіони (" + playerRegions.size() + "): " + TextFormatting.WHITE + regionNames));
    }
}
