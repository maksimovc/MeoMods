package com.systmeo.chunkguard.managers;

import com.systmeo.chunkguard.ChunkGuard;
import com.systmeo.chunkguard.data.Region;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataManager {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final File regionsFile;

    public DataManager() {
        File configDir = new File("config", ChunkGuard.MODID);
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        this.regionsFile = new File(configDir, "regions.json");
    }

    public void saveRegions(Map<String, Region> regions) {
        try (FileWriter writer = new FileWriter(regionsFile)) {
            gson.toJson(regions, writer);
        } catch (IOException e) {
            ChunkGuard.logger.error("Could not save regions!", e);
        }
    }

    public Map<String, Region> loadRegions() {
        if (!regionsFile.exists()) {
            return new ConcurrentHashMap<>();
        }
        try (FileReader reader = new FileReader(regionsFile)) {
            Type type = new TypeToken<ConcurrentHashMap<String, Region>>() {}.getType();
            Map<String, Region> loadedRegions = gson.fromJson(reader, type);
            return loadedRegions != null ? loadedRegions : new ConcurrentHashMap<>();
        } catch (IOException e) {
            ChunkGuard.logger.error("Could not load regions!", e);
            return new ConcurrentHashMap<>();
        }
    }
}
