package com.systmeo.permissions.data;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Group {

    private final String name;
    private final Set<PermissionNode> permissions = ConcurrentHashMap.newKeySet();
    private final Set<String> parents = ConcurrentHashMap.newKeySet();
    private String prefix = "";
    private String suffix = "";
    private int weight = 0;
    private boolean isDefault = false;

    public Group(String name) {
        this.name = name.toLowerCase();
    }

    public String getName() { return name; }
    public Set<PermissionNode> getPermissions() { return permissions; }

    public void addPermission(String permission, long expiryTimestamp) {
        permissions.removeIf(p -> p.getPermission().equalsIgnoreCase(permission));
        permissions.add(new PermissionNode(permission, expiryTimestamp));
    }

    public boolean removePermission(String permission) {
        return permissions.removeIf(p -> p.getPermission().equalsIgnoreCase(permission));
    }

    public Set<String> getParents() { return parents; }

    public void addParent(String parent) { if (!this.name.equalsIgnoreCase(parent)) parents.add(parent.toLowerCase()); }
    public void removeParent(String parent) { parents.remove(parent.toLowerCase()); }

    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }

    public String getSuffix() { return suffix; }
    public void setSuffix(String suffix) { this.suffix = suffix; }

    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }

    public void cleanupExpiredNodes() { permissions.removeIf(PermissionNode::isExpired); }
}
