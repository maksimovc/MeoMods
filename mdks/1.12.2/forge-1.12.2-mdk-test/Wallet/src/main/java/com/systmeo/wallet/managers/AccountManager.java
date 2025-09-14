package com.systmeo.wallet.managers;

import com.systmeo.wallet.Wallet;
import com.systmeo.wallet.config.WalletConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Manages all player accounts and balance operations.
 * This class is thread-safe.
 */
public class AccountManager {

    private Map<UUID, Long> accounts = new ConcurrentHashMap<>();

    public void loadAccounts(Map<UUID, Long> loadedAccounts) {
        if (loadedAccounts instanceof ConcurrentHashMap) {
            this.accounts = loadedAccounts;
        } else {
            this.accounts = new ConcurrentHashMap<>(loadedAccounts);
        }
    }

    public long getBalance(UUID playerUuid) {
        return accounts.getOrDefault(playerUuid, 0L);
    }

    public boolean hasAccount(UUID playerUuid) {
        return accounts.containsKey(playerUuid);
    }

    public void setBalance(UUID playerUuid, long amount) {
        if (amount < 0) amount = 0;
        accounts.put(playerUuid, amount);
    }

    public boolean hasEnough(UUID playerUuid, long amount) {
        return getBalance(playerUuid) >= amount;
    }

    public boolean deposit(UUID playerUuid, long amount) {
        if (amount <= 0) return false;
        accounts.compute(playerUuid, (key, currentBalance) -> (currentBalance == null ? 0L : currentBalance) + amount);
        return true;
    }

    public boolean withdraw(UUID playerUuid, long amount) {
        if (amount <= 0) return false;

        final AtomicBoolean success = new AtomicBoolean(false);
        accounts.computeIfPresent(playerUuid, (key, currentBalance) -> {
            if (currentBalance >= amount) {
                success.set(true);
                return currentBalance - amount;
            }
            return currentBalance;
        });

        return success.get();
    }

    public synchronized boolean transfer(UUID fromUuid, UUID toUuid, long amount) {
        if (amount <= 0 || fromUuid.equals(toUuid)) return false;
        long fromBalance = getBalance(fromUuid);
        if (fromBalance < amount) return false;
        accounts.put(fromUuid, fromBalance - amount);
        accounts.compute(toUuid, (key, currentBalance) -> (currentBalance == null ? 0L : currentBalance) + amount);
        return true;
    }

    public Map<UUID, Long> getAllAccounts() { return accounts; }

    public int getAccountsCount() { return accounts.size(); }

    public String getCurrencySymbol() { return WalletConfig.currencySymbol; }

    public UUID getUuidForPlayerName(String playerName) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) {
            Wallet.logger.warn("Attempted to find player UUID while server is not running!");
            return null;
        }

        net.minecraft.entity.player.EntityPlayer player = server.getPlayerList().getPlayerByUsername(playerName);
        if (player != null) return player.getUniqueID();

        com.mojang.authlib.GameProfile profile = server.getPlayerProfileCache().getGameProfileForUsername(playerName);
        if (profile != null) return profile.getId();

        return null;
    }

    public List<Map.Entry<String, Double>> getTopBalances(int limit) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) {
            Wallet.logger.warn("Attempted to get top balances while server is not running!");
            return Collections.emptyList();
        }

        return accounts.entrySet().stream()
                .sorted(Map.Entry.<UUID, Long>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    UUID uuid = entry.getKey();
                    long balanceLong = entry.getValue();
                    com.mojang.authlib.GameProfile profile = server.getPlayerProfileCache().getProfileByUUID(uuid);
                    String name = (profile != null && profile.getName() != null) ? profile.getName() : uuid.toString().substring(0, 8);
                    double balanceDouble = balanceLong / 100.0;
                    return new AbstractMap.SimpleEntry<>(name, balanceDouble);
                })
                .collect(Collectors.toList());
    }
}
