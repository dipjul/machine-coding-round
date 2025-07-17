package com.machinecoding.splitwise.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a group of users who share expenses.
 */
public class Group {
    private final String groupId;
    private final String name;
    private final String description;
    private final String createdBy;
    private final List<String> memberIds;
    private final LocalDateTime createdAt;
    private boolean isActive;
    
    public Group(String groupId, String name, String description, String createdBy) {
        this.groupId = groupId != null ? groupId.trim() : "";
        this.name = name != null ? name.trim() : "";
        this.description = description != null ? description.trim() : "";
        this.createdBy = createdBy != null ? createdBy.trim() : "";
        this.memberIds = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
        
        // Add creator as first member
        if (!this.createdBy.isEmpty()) {
            this.memberIds.add(this.createdBy);
        }
    }
    
    // Getters
    public String getGroupId() { return groupId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCreatedBy() { return createdBy; }
    public List<String> getMemberIds() { return new ArrayList<>(memberIds); }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isActive() { return isActive; }
    
    public void setActive(boolean active) {
        this.isActive = active;
    }
    
    public boolean addMember(String userId) {
        if (userId != null && !userId.trim().isEmpty() && !memberIds.contains(userId.trim())) {
            memberIds.add(userId.trim());
            return true;
        }
        return false;
    }
    
    public boolean removeMember(String userId) {
        if (userId != null && !userId.trim().equals(createdBy)) {
            return memberIds.remove(userId.trim());
        }
        return false; // Cannot remove group creator
    }
    
    public boolean isMember(String userId) {
        return userId != null && memberIds.contains(userId.trim());
    }
    
    public int getMemberCount() {
        return memberIds.size();
    }
    
    @Override
    public String toString() {
        return String.format("Group{id='%s', name='%s', members=%d, createdBy='%s', active=%s}", 
                           groupId, name, memberIds.size(), createdBy, isActive);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Group group = (Group) obj;
        return groupId.equals(group.groupId);
    }
    
    @Override
    public int hashCode() {
        return groupId.hashCode();
    }
}