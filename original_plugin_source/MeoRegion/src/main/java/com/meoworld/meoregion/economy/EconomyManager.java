package com.meoworld.meoregion.economy;

import com.meoworld.meoregion.MeoRegion;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {

    private final MeoRegion plugin;
    private Economy economy = null;

    public EconomyManager(MeoRegion plugin) {
        this.plugin = plugin;
        if (plugin.getConfig().getBoolean("economy.enabled", false)) {
            setupEconomy();
        }
    }

    private void setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Vault not found! Economy features will be disabled.");
            return;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().warning("No economy plugin found! Economy features will be disabled.");
            return;
        }
        economy = rsp.getProvider();
        if (economy != null) {
            plugin.getLogger().info("Successfully hooked into Vault and " + economy.getName() + ".");
        }
    }

    public boolean isEconomyEnabled() {
        return economy != null;
    }

    public boolean hasEnough(OfflinePlayer player, double amount) {
        if (!isEconomyEnabled()) return false;
        return economy.has(player, amount);
    }

    public boolean withdraw(OfflinePlayer player, double amount) {
        if (!isEconomyEnabled() || amount <= 0) return false;
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    public boolean deposit(OfflinePlayer player, double amount) {
        if (!isEconomyEnabled() || amount <= 0) return false;
        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    public String format(double amount) {
        if (!isEconomyEnabled()) return String.valueOf(amount);
        return economy.format(amount);
    }
}