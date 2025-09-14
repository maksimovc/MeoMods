package com.systmeo.permissions.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.systmeo.permissions.Permissions;
import com.systmeo.permissions.data.Group;
import com.systmeo.permissions.data.Track;
import com.systmeo.permissions.data.User;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DataManager {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().enableComplexMapKeySerialization().create();
    private final File groupsFile;
    private final File usersFile;
    private final File tracksFile;

    public DataManager() {
        File configDir = new File("config", Permissions.MODID);
        if (!configDir.exists()) configDir.mkdirs();
        this.groupsFile = new File(configDir, "groups.json");
        this.usersFile = new File(configDir, "users.json");
        this.tracksFile = new File(configDir, "tracks.json");
    }

    public void saveGroups(Map<String, Group> groups) {
        try (FileWriter writer = new FileWriter(groupsFile)) {
            gson.toJson(groups, writer);
        } catch (IOException e) {
            Permissions.logger.error("Could not save groups!", e);
        }
    }

    public Map<String, Group> loadGroups() {
        if (!groupsFile.exists()) return new ConcurrentHashMap<>();
        try (FileReader reader = new FileReader(groupsFile)) {
            Type type = new TypeToken<ConcurrentHashMap<String, Group>>() {}.getType();
            Map<String, Group> loaded = gson.fromJson(reader, type);
            return loaded != null ? loaded : new ConcurrentHashMap<>();
        } catch (IOException e) {
            Permissions.logger.error("Could not load groups!", e);
            return new ConcurrentHashMap<>();
        }
    }

    public void saveUsers(Map<UUID, User> users) {
        try (FileWriter writer = new FileWriter(usersFile)) {
            gson.toJson(users, writer);
        } catch (IOException e) {
            Permissions.logger.error("Could not save users!", e);
        }
    }

    public Map<UUID, User> loadUsers() {
        if (!usersFile.exists()) return new ConcurrentHashMap<>();
        try (FileReader reader = new FileReader(usersFile)) {
            Type type = new TypeToken<ConcurrentHashMap<UUID, User>>() {}.getType();
            Map<UUID, User> loaded = gson.fromJson(reader, type);
            return loaded != null ? loaded : new ConcurrentHashMap<>();
        } catch (IOException e) {
            Permissions.logger.error("Could not load users!", e);
            return new ConcurrentHashMap<>();
        }
    }

    public void saveTracks(Map<String, Track> tracks) {
        try (FileWriter writer = new FileWriter(tracksFile)) {
            gson.toJson(tracks, writer);
        } catch (IOException e) {
            Permissions.logger.error("Could not save tracks!", e);
        }
    }

    public Map<String, Track> loadTracks() {
        if (!tracksFile.exists()) return new ConcurrentHashMap<>();
        try (FileReader reader = new FileReader(tracksFile)) {
            Type type = new TypeToken<ConcurrentHashMap<String, Track>>() {}.getType();
            Map<String, Track> loaded = gson.fromJson(reader, type);
            return loaded != null ? loaded : new ConcurrentHashMap<>();
        } catch (IOException e) {
            Permissions.logger.error("Could not load tracks!", e);
            return new ConcurrentHashMap<>();
        }
    }
}
