package com.systmeo.permissions.data;

import java.util.Objects;

public class PermissionNode {

    private final String permission;
    private final long expiryTimestamp;

    public PermissionNode(String permission, long expiryTimestamp) {
        this.permission = permission.toLowerCase();
        this.expiryTimestamp = expiryTimestamp;
    }

    public String getPermission() { return permission; }
    public long getExpiryTimestamp() { return expiryTimestamp; }
    public boolean isPermanent() { return expiryTimestamp == 0; }
    public boolean isExpired() { return !isPermanent() && System.currentTimeMillis() > expiryTimestamp; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PermissionNode that = (PermissionNode) o;
        return permission.equals(that.permission);
    }

    @Override
    public int hashCode() { return Objects.hash(permission); }
}
