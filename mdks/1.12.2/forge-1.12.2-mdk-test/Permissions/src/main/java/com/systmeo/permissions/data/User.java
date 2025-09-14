package com.systmeo.permissions.data;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class User {

    private final UUID uuid;
    private final Set<PermissionNode> permissions = ConcurrentHashMap.newKeySet();
    private final Set<GroupNode> groups = ConcurrentHashMap.newKeySet();

    public User(UUID uuid) { this.uuid = uuid; }
    public UUID getUuid() { return uuid; }
    public Set<PermissionNode> getPermissions() { return permissions; }
    public Set<GroupNode> getGroups() { return groups; }

    public void addPermission(String permission, long expiryTimestamp) {
        permissions.removeIf(p -> p.getPermission().equalsIgnoreCase(permission));
        permissions.add(new PermissionNode(permission, expiryTimestamp));
    }

    public boolean removePermission(String permission) { return permissions.removeIf(p -> p.getPermission().equalsIgnoreCase(permission)); }

    public void addGroup(String groupName, long expiryTimestamp) {
        groups.removeIf(g -> g.getGroupName().equalsIgnoreCase(groupName));
        groups.add(new GroupNode(groupName, expiryTimestamp));
    }

    public boolean removeGroup(String groupName) { return groups.removeIf(g -> g.getGroupName().equalsIgnoreCase(groupName)); }

    public void cleanupExpiredNodes() {
        permissions.removeIf(PermissionNode::isExpired);
        groups.removeIf(GroupNode::isExpired);
    }
}
