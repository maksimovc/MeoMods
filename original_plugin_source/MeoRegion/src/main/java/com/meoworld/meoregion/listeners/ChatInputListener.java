package com.meoworld.meoregion.listeners;

import com.meoworld.meoregion.MeoRegion;
import com.meoworld.meoregion.managers.ChatInputManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ChatInputListener implements Listener {
    private final MeoRegion plugin;

    public ChatInputListener(MeoRegion plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        ChatInputManager chatManager = plugin.getChatInputManager();

        if (!chatManager.hasPending(player.getUniqueId())) {
            return;
        }

        event.setCancelled(true);

        String message = event.getMessage().trim();

        plugin.getServer().getScheduler().runTask(plugin, () -> chatManager.processInput(player, message));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ChatInputManager chatManager = plugin.getChatInputManager();

        if (chatManager.hasPending(player.getUniqueId())) {
            chatManager.consume(player.getUniqueId());
        }
    }
}
