package com.systmeo.wallet.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.systmeo.wallet.Wallet;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages saving and loading account data to/from JSON files.
 */
public class DataManager {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final File accountsFile;
    private final File accountsFileBak;

    public DataManager() {
        File dataDir = new File("config", Wallet.MODID);
        if (!dataDir.exists()) dataDir.mkdirs();
        this.accountsFile = new File(dataDir, "accounts.json");
        this.accountsFileBak = new File(dataDir, "accounts.json.bak");
    }

    public void saveAccounts(Map<UUID, Long> accounts) {
        if (accountsFile.exists()) {
            try {
                Files.copy(accountsFile.toPath(), accountsFileBak.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                Wallet.logger.warn("Could not create backup of accounts file!", e);
            }
        }

        try (FileWriter writer = new FileWriter(accountsFile)) {
            gson.toJson(accounts, writer);
        } catch (IOException e) {
            Wallet.logger.error("CRITICAL: Could not save accounts to " + accountsFile.getName() + "!", e);
        }
    }

    public Map<UUID, Long> loadAccounts() {
        if (!accountsFile.exists() && !accountsFileBak.exists()) {
            return new ConcurrentHashMap<>();
        }

        Map<UUID, Long> loadedMap = loadFromFile(accountsFile);

        if (loadedMap == null) {
            Wallet.logger.warn(accountsFile.getName() + " is corrupted or unreadable. Attempting to load from backup...");
            loadedMap = loadFromFile(accountsFileBak);
            if (loadedMap != null) {
                Wallet.logger.info("Successfully loaded accounts from backup file " + accountsFileBak.getName() + ".");
            } else {
                Wallet.logger.error("CRITICAL: Both primary and backup account files are corrupted. All wallet data is lost.");
                return new ConcurrentHashMap<>();
            }
        }

        return new ConcurrentHashMap<>(loadedMap != null ? loadedMap : new ConcurrentHashMap<>());
    }

    private Map<UUID, Long> loadFromFile(File file) {
        if (!file.exists()) return null;
        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<ConcurrentHashMap<UUID, Long>>() {}.getType();
            return gson.fromJson(reader, type);
        } catch (IOException | JsonSyntaxException e) {
            Wallet.logger.error("Failed to load or parse " + file.getName(), e);
            return null;
        }
    }
}
