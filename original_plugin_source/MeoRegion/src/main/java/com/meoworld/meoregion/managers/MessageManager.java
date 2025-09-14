package com.meoworld.meoregion.managers;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class MessageManager {
    private final JavaPlugin plugin;
    private FileConfiguration messages;

    public MessageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    public void loadMessages() {
        FileConfiguration defaultMessages;
        try (InputStream defaultStream = plugin.getResource("messages_en.yml")) {
            if (defaultStream == null) {
                plugin.getLogger().severe("Internal 'messages_en.yml' is missing! This is a critical error.");
                defaultMessages = new YamlConfiguration();
            } else {
                defaultMessages = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Error loading internal 'messages_en.yml'.", e);
            defaultMessages = new YamlConfiguration();
        }

        File enFile = new File(plugin.getDataFolder(), "messages_en.yml");
        if (!enFile.exists()) {
            plugin.saveResource("messages_en.yml", false);
        }
        File ukFile = new File(plugin.getDataFolder(), "messages_uk.yml");
        if (!ukFile.exists()) {
            plugin.saveResource("messages_uk.yml", false);
        }

        String lang = plugin.getConfig().getString("language", "uk").toLowerCase();
        File langFile = new File(plugin.getDataFolder(), "messages_" + lang + ".yml");

        if (!langFile.exists()) {
            plugin.getLogger().warning("Language file '" + langFile.getName() + "' not found. Using default English messages.");
            this.messages = defaultMessages;
        } else {
            this.messages = YamlConfiguration.loadConfiguration(langFile);
            this.messages.setDefaults(defaultMessages);
        }
    }

    public String getMessage(String key, boolean withPrefix) {
        String message = messages.getString("messages." + key, "&cMessage not found: messages." + key);

        String prefix = "";
        if (withPrefix) {
            prefix = messages.getString("messages.prefix", "");
        }

        return ChatColor.translateAlternateColorCodes('&', prefix + message);
    }

    public String getMessage(String key) {
        return getMessage(key, false);
    }
}
