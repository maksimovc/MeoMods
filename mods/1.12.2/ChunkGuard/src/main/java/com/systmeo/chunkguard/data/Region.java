package com.systmeo.chunkguard.data;

import com.systmeo.wallet.WalletManager;
import net.minecraft.entity.player.EntityPlayer;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Region {
    private String name;
    private UUID owner;
    private final Map<UUID, Role> participants;
    private final Map<ChunkCoordinate, SubChunk> subChunks;
    private final String worldName;
    private int minX, minY, minZ;
    private int maxX, maxY, maxZ;
    private int size;

    private int priority;
    private final Map<String, Object> flags;

    private boolean forSale;
    private double price;
    private UUID renter;
    private long rentDueDate;
    private long rentPeriod;
    private double rentPrice;
    private String parent;

    public Region(String name, UUID owner, String worldName,
                  int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int size) {
        this.name = name;
        this.worldName = worldName;
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        this.size = size;

        this.owner = owner;
        this.participants = new ConcurrentHashMap<>();
        if (owner != null) {
            this.participants.put(owner, Role.OWNER);
        }

        this.subChunks = new ConcurrentHashMap<>();
        this.flags = new ConcurrentHashMap<>();
        this.priority = 0;
    }

    //<editor-fold desc="Getters">
    public String getName() { return name; }
    public String getWorldName() { return worldName; }
    public Map<UUID, Role> getParticipants() { return participants; }
    public Optional<UUID> getOwner() { return Optional.ofNullable(owner); }
    public int getMinX() { return minX; }
    public int getMinY() { return minY; }
    public int getMinZ() { return minZ; }
    public int getMaxX() { return maxX; }
    public int getMaxY() { return maxY; }
    public int getMaxZ() { return maxZ; }
    public int getSize() { return size; }
    public int getPriority() { return priority; }
    public Map<String, Object> getFlags() { return flags; }
    public boolean isForSale() { return forSale; }
    public double getPrice() { return price; }
    public UUID getRenter() { return renter; }
    public long getRentDueDate() { return rentDueDate; }
    public long getRentPeriod() { return rentPeriod; }
    public double getRentPrice() { return rentPrice; }
    public String getParent() { return parent; }
    //</editor-fold>

    //<editor-fold desc="Setters">
    public void setName(String name) { this.name = name; }
    public void setPriority(int priority) { this.priority = priority; }
    public void setForSale(boolean forSale) { this.forSale = forSale; }
    public void setPrice(double price) { this.price = price; }
    public void setRenter(UUID renter) { this.renter = renter; }
    public void setRentDueDate(long rentDueDate) { this.rentDueDate = rentDueDate; }
    public void setRentPeriod(long rentPeriod) { this.rentPeriod = rentPeriod; }
    public void setRentPrice(double rentPrice) { this.rentPrice = rentPrice; }
    public void setMinX(int minX) { this.minX = minX; }
    public void setMinY(int minY) { this.minY = minY; }
    public void setMinZ(int minZ) { this.minZ = minZ; }
    public void setMaxX(int maxX) { this.maxX = maxX; }
    public void setMaxY(int maxY) { this.maxY = maxY; }
    public void setMaxZ(int maxZ) { this.maxZ = maxZ; }
    public void setSize(int size) { this.size = size; }
    public void setParent(String parent) { this.parent = parent; }
    //</editor-fold>

    public boolean isOwner(UUID uuid) {
        return this.owner != null && this.owner.equals(uuid);
    }

    public boolean isForRent() {
        return getRentPeriod() > 0 && getRentPrice() > 0;
    }

    public Object getRawFlagValue(String flagName) {
        return flags.get(flagName);
    }

    public Role getRole(UUID playerUuid) {
        return participants.getOrDefault(playerUuid, Role.GUEST);
    }

    public boolean hasPermission(UUID playerUuid, Role.Permission permission) {
        if (playerUuid == null) return false;
        return getRole(playerUuid).hasPermission(permission);
    }

    public void setParticipantRole(UUID playerUuid, Role role) {
        if (role == Role.OWNER) {
            throw new IllegalArgumentException("Cannot set OWNER role directly. Use transferOwnership().");
        }
        if (role == Role.GUEST) {
            participants.remove(playerUuid);
        } else {
            participants.put(playerUuid, role);
        }
    }

    public void transferOwnership(UUID newOwnerUuid) {
        if (this.owner != null) {
            setParticipantRole(this.owner, Role.CO_OWNER);
        }
        this.owner = newOwnerUuid;
        this.participants.put(newOwnerUuid, Role.OWNER);
    }

    public boolean sellRegion(EntityPlayer buyer, double price) {
        if (!this.forSale || this.price != price) {
            return false;
        }

        if (WalletManager.removeBalance(buyer, (int)price)) {
            // Add money to seller
            Optional<UUID> sellerUuid = this.getOwner();
            if (sellerUuid.isPresent()) {
                // Note: We would need to get EntityPlayer from UUID, but for now we'll assume the seller gets the money
                // This would require additional logic to find the seller player
            }

            // Transfer ownership
            this.transferOwnership(buyer.getUniqueID());
            this.forSale = false;
            this.price = 0;
            return true;
        }
        return false;
    }

    public boolean rentRegion(EntityPlayer renter, int days) {
        if (!this.isForRent()) {
            return false;
        }

        double totalCost = this.rentPrice * days;
        if (WalletManager.removeBalance(renter, (int)totalCost)) {
            this.renter = renter.getUniqueID();
            this.rentDueDate = System.currentTimeMillis() + (days * 24 * 60 * 60 * 1000L); // days in milliseconds
            return true;
        }
        return false;
    }

    public boolean isRentExpired() {
        return this.rentDueDate > 0 && System.currentTimeMillis() > this.rentDueDate;
    }

    public Optional<SubChunk> getSubChunk(ChunkCoordinate coord) {
        if (coord == null) return Optional.empty();
        return Optional.ofNullable(this.subChunks.get(coord));
    }

}
