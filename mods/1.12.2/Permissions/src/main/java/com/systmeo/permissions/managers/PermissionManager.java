package com.systmeo.permissions.managers;

import com.systmeo.permissions.Permissions;
import com.systmeo.permissions.data.Group;
import com.systmeo.permissions.data.GroupNode;
import com.systmeo.permissions.data.PermissionNode;
import com.systmeo.permissions.data.Track;
import com.systmeo.permissions.data.User;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PermissionManager {

    private final Map<String, Group> groups = new ConcurrentHashMap<>();
    private final Map<UUID, User> users = new ConcurrentHashMap<>();
    private final Map<String, Track> tracks = new ConcurrentHashMap<>();

    private final Map<UUID, Set<String>> permissionCache = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cacheTimestamp = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION_MS = 2500;

    public Map<String, Group> getGroups() { return groups; }
    public Map<UUID, User> getUsers() { return users; }
    public Map<String, Track> getTracks() { return tracks; }

    public Group createGroup(String name) { return groups.computeIfAbsent(name.toLowerCase(), Group::new); }
    public Group getGroup(String name) { return groups.get(name.toLowerCase()); }
    public User getOrCreateUser(UUID uuid) { return users.computeIfAbsent(uuid, User::new); }
    public User getUser(UUID uuid) { return users.get(uuid); }
    public Track createTrack(String name) { return tracks.computeIfAbsent(name.toLowerCase(), Track::new); }
    public Track getTrack(String name) { return tracks.get(name.toLowerCase()); }

    public void deleteGroup(String name) {
        groups.remove(name.toLowerCase());
        users.values().forEach(user -> user.removeGroup(name));
        groups.values().forEach(group -> group.removeParent(name));
        invalidateAllCaches();
    }

    public void deleteTrack(String name) { tracks.remove(name.toLowerCase()); }

    public boolean hasPermission(UUID uuid, String permission) {
        Set<String> userPermissions = getResolvedPermissions(uuid);
        return checkWildcardPermission(userPermissions, permission.toLowerCase());
    }

    private Set<String> getResolvedPermissions(UUID uuid) {
        Long timestamp = cacheTimestamp.get(uuid);
        if (timestamp != null && System.currentTimeMillis() < timestamp) {
            Set<String> cachedPerms = permissionCache.get(uuid);
            if (cachedPerms != null) return cachedPerms;
        }
        Set<String> resolvedPermissions = resolveAllPermissions(uuid);
        permissionCache.put(uuid, resolvedPermissions);
        cacheTimestamp.put(uuid, System.currentTimeMillis() + CACHE_DURATION_MS);
        return resolvedPermissions;
    }

    private Set<String> resolveAllPermissions(UUID uuid) {
        User user = getOrCreateUser(uuid);
        Set<String> permissions = new HashSet<>();
        Set<String> checkedGroups = new HashSet<>();

        user.getPermissions().stream()
                .filter(node -> !node.isExpired())
                .map(PermissionNode::getPermission)
                .forEach(permissions::add);

        for (Group group : getActiveUserGroups(uuid)) {
            resolveGroupPermissionsRecursive(group, permissions, checkedGroups);
        }
        return permissions;
    }

    private void resolveGroupPermissionsRecursive(Group group, Set<String> permissions, Set<String> checkedGroups) {
        if (group == null || checkedGroups.contains(group.getName())) return;
        checkedGroups.add(group.getName());

        group.getPermissions().stream()
                .filter(node -> !node.isExpired())
                .map(PermissionNode::getPermission)
                .forEach(permissions::add);

        for (String parentName : group.getParents()) {
            resolveGroupPermissionsRecursive(getGroup(parentName), permissions, checkedGroups);
        }
    }

    private boolean checkWildcardPermission(Set<String> permissions, String neededPermission) {
        if (permissions.contains("-" + neededPermission)) return false;
        if (permissions.contains(neededPermission)) return true;

        String[] parts = neededPermission.split("\\.");
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i < parts.length - 1) {
                current.append(parts[i]).append(".");
            }
            if (permissions.contains("-" + current.toString() + "*")) return false;
            if (permissions.contains(current.toString() + "*")) return true;
        }
        return permissions.contains("*");
    }

    public void invalidateCache(UUID userUuid) {
        permissionCache.remove(userUuid);
        cacheTimestamp.remove(userUuid);
    }

    public void invalidateAllCaches() {
        permissionCache.clear();
        cacheTimestamp.clear();
        if (Permissions.logger != null) {
            Permissions.logger.info("All permission caches have been invalidated due to a group change.");
        }
    }

    public List<Group> getActiveUserGroups(UUID userUuid) {
        User user = getUser(userUuid);
        if (user == null) return Collections.emptyList();
        return user.getGroups().stream()
                .filter(node -> !node.isExpired())
                .map(GroupNode::getGroupName)
                .map(this::getGroup)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public Optional<Group> getPrimaryGroup(UUID userUuid) {
        return getActiveUserGroups(userUuid).stream()
                .max(Comparator.comparingInt(Group::getWeight));
    }

    public String getPrefix(UUID userUuid) {
        return getPrimaryGroup(userUuid).map(Group::getPrefix).orElse("");
    }

    public String getSuffix(UUID userUuid) {
        return getPrimaryGroup(userUuid).map(Group::getSuffix).orElse("");
    }

    public String promoteUser(UUID userUuid, String trackName) {
        Track track = getTrack(trackName);
        if (track == null || track.getGroups().isEmpty()) return null;

        User user = getOrCreateUser(userUuid);
        List<String> trackGroups = track.getGroups();
        String currentHighestGroup = null;
        int currentGroupIndex = -1;

        for (GroupNode userGroupNode : user.getGroups()) {
            int index = trackGroups.indexOf(userGroupNode.getGroupName());
            if (index > currentGroupIndex) {
                currentGroupIndex = index;
                currentHighestGroup = userGroupNode.getGroupName();
            }
        }

        if (currentGroupIndex < trackGroups.size() - 1) {
            String nextGroup = trackGroups.get(currentGroupIndex + 1);
            if (currentHighestGroup != null) {
                user.removeGroup(currentHighestGroup);
            }
            user.addGroup(nextGroup, 0);
            invalidateCache(userUuid);
            return nextGroup;
        }
        return null;
    }

    public void cleanupExpiredNodes() {
        users.values().forEach(User::cleanupExpiredNodes);
        groups.values().forEach(Group::cleanupExpiredNodes);
        invalidateAllCaches();
        if (Permissions.logger != null) {
            Permissions.logger.info("Expired permission nodes and group memberships have been cleaned up.");
        }
    }
}
