package com.meoworld.meoregion.util;

import java.util.ArrayList;
import java.util.List;

public enum Flag {
    PVP("pvp", false),
    ANIMAL_DAMAGE("animal-damage", true),
    MONSTER_DAMAGE("monster-damage", true),
    MOB_DAMAGE("mob-damage", true),

    USE("use", false),
    CHEST_ACCESS("chest-access", false),
    VEHICLE_USE("vehicle-use", true),
    VEHICLE_DESTROY("vehicle-destroy", false),
    ITEM_FRAME_DESTROY("item-frame-destroy", false),

    TNT("tnt", false),
    CREEPER_EXPLOSION("creeper-explosion", false),
    EXPLOSION("explosion", false),
    MOB_SPAWNING("mob-spawning", true),

    ITEM_DROP("item-drop", true),
    ITEM_PICKUP("item-pickup", true),
    BANNED_USE_ITEMS("banned-use-items", new ArrayList<String>()),

    REDSTONE("redstone", true),
    PISTON("piston", true),
    FIRE_SPREAD("fire-spread", false),
    LAVA_FLOW("lava-flow", false),
    WATER_FLOW("water-flow", false),

    LEAF_DECAY("leaf-decay", true),
    GRASS_GROWTH("grass-growth", true),
    VINE_GROWTH("vine-growth", true),
    SOIL_DRY("soil-dry", true),
    ICE_FORM("ice-form", true),
    ICE_MELT("ice-melt", true),
    SNOW_FORM("snow-form", true),
    SNOW_MELT("snow-melt", true),

    DENY_ENTER("deny-enter", false),
    GREETING("greeting", "none"),
    FAREWELL("farewell", "none"),
    DENY_MESSAGE("deny-message", "none"),
    BLOCKED_CMDS("blocked-cmds", new ArrayList<String>()),
    ALLOWED_CMDS("allowed-cmds", new ArrayList<String>()),

    KEEP_INVENTORY("keep-inventory", false),
    KEEP_EXPERIENCE("keep-experience", false),
    GOD_MODE("god-mode", false),
    FLIGHT("flight", false),
    ENDERPEARL("enderpearl", true);


    private final String name;
    private final Object defaultValue;

    Flag(String name, Object defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public static Flag fromString(String text) {
        for (Flag f : Flag.values()) {
            if (f.name.equalsIgnoreCase(text)) {
                return f;
            }
        }
        return null;
    }

    public boolean isListOfStrings() {
        return this.defaultValue instanceof List;
    }
}