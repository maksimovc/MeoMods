package com.systmeo.chunkguard.data;

import java.util.Objects;

public class ChunkCoordinate {
    private final int x;
    private final int z;

    public ChunkCoordinate(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkCoordinate that = (ChunkCoordinate) o;
        return x == that.x && z == that.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }

    @Override
    public String toString() {
        return "ChunkCoordinate{" + "x=" + x + ", z=" + z + '}' + "}";
    }
}
