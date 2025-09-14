package com.systmeo.permissions.data;

public class GroupNode {

    private final String groupName;
    private final long expiryTimestamp;

    public GroupNode(String groupName, long expiryTimestamp) {
        this.groupName = groupName.toLowerCase();
        this.expiryTimestamp = expiryTimestamp;
    }

    public GroupNode(String groupName) { this(groupName, 0); }

    public String getGroupName() { return groupName; }
    public long getExpiryTimestamp() { return expiryTimestamp; }
    public boolean isExpired() { return expiryTimestamp != 0 && System.currentTimeMillis() > expiryTimestamp; }
}
