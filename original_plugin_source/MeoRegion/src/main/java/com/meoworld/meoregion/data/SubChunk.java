package com.meoworld.meoregion.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class SubChunk {

    private final Map<String, Object> flags;

    public SubChunk() {
        this.flags = new ConcurrentHashMap<>();
    }

    public Map<String, Object> getFlags() {
        return flags;
    }

    public Object getFlagValue(String flagName) {
        return flags.get(flagName);
    }

    public void setFlag(String flagName, Object value) {
        if (value == null) {
            flags.remove(flagName);
        } else {
            flags.put(flagName, value);
        }
    }
}