package com.systmeo.wallet.config;

import com.systmeo.wallet.Wallet;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class WalletConfig {

    public static double startingBalance = 100.0;
    public static String currencySymbol = "$";
    public static int autosaveIntervalMinutes = 10;

    public static void init(File configFile) {
        Configuration config = new Configuration(configFile);

        try {
            config.load();

            startingBalance = config.get("General", "startingBalance", 100.0, "The balance new players start with.").getDouble();
            currencySymbol = config.get("General", "currencySymbol", "$", "The symbol used for the currency (e.g., $, €, etc.).").getString();
            autosaveIntervalMinutes = config.get("General", "autosaveIntervalMinutes", 10, "The interval in minutes for automatically saving wallet data. Set to 0 or a negative number to disable.").getInt();

        } catch (Exception e) {
            Wallet.logger.error("Failed to load wallet configuration!", e);
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }
}
