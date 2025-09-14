package com.systmeo.chunkguard.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public enum Role {
    OWNER("Власник", 5, new HashSet<>(Arrays.asList(Permission.values()))),
    CO_OWNER("Співвласник", 4, new HashSet<>(Arrays.asList(Permission.SET_ROLE, Permission.MANAGE_SUBCHUNKS, Permission.SET_FLAGS, Permission.MANAGE_MEMBERS, Permission.BUILD, Permission.INTERACT, Permission.ECONOMY, Permission.ENTER))),
    MANAGER("Менеджер", 3, new HashSet<>(Arrays.asList(Permission.MANAGE_SUBCHUNKS, Permission.SET_FLAGS, Permission.MANAGE_MEMBERS, Permission.BUILD, Permission.INTERACT, Permission.ECONOMY, Permission.ENTER))),
    BUILDER("Будівельник", 2, new HashSet<>(Arrays.asList(Permission.BUILD, Permission.INTERACT, Permission.ENTER))),
    MEMBER("Учасник", 1, new HashSet<>(Arrays.asList(Permission.INTERACT, Permission.ENTER))),
    GUEST("Гість", 0, new HashSet<>(Collections.singletonList(Permission.ENTER)));

    private final String name;
    private final int level;
    private final Set<Permission> permissions;

    Role(String name, int level, Set<Permission> permissions) {
        this.name = name;
        this.level = level;
        this.permissions = permissions;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public boolean hasPermission(Permission permission) {
        return this.permissions.contains(permission);
    }

    public enum Permission {
        BUILD,
        INTERACT,
        MANAGE_MEMBERS,
        SET_FLAGS,
        MANAGE_SUBCHUNKS,
        SET_ROLE,
        ECONOMY,
        ENTER
    }
}
