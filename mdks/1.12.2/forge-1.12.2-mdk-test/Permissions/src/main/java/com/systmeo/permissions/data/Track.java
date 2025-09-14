package com.systmeo.permissions.data;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Track {

    private final String name;
    private final List<String> groups = new CopyOnWriteArrayList<>();

    public Track(String name) { this.name = name.toLowerCase(); }
    public String getName() { return name; }
    public List<String> getGroups() { return groups; }

    public void appendGroup(String groupName) { groups.add(groupName.toLowerCase()); }
    public void insertGroup(String groupName, int index) throws IndexOutOfBoundsException { groups.add(index, groupName.toLowerCase()); }
    public void removeGroup(String groupName) { groups.remove(groupName.toLowerCase()); }
}
