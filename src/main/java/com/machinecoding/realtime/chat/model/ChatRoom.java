package com.machinecoding.realtime.chat.model;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a chat room that can contain multiple users and messages.
 */
public class ChatRoom {
    private final String roomId;
    private final String roomName;
    private final String description;
    private final String createdBy;
    private final LocalDateTime createdAt;
    private final Set<String> members;
    private final Set<String> admins;
    private final List<Message> messages;
    private final Map<String, LocalDateTime> lastReadTimestamps;
    private boolean isPrivate;
    private int maxMembers;
    
    public ChatRoom(String roomId, String roomName, String createdBy) {
        this(roomId, roomName, "", createdBy, false, 100);
    }
    
    public ChatRoom(String roomId, String roomName, String description, String createdBy, 
                   boolean isPrivate, int maxMembers) {
        if (roomId == null || roomId.trim().isEmpty()) {
            throw new IllegalArgumentException("Room ID cannot be null or empty");
        }
        if (roomName == null || roomName.trim().isEmpty()) {
            throw new IllegalArgumentException("Room name cannot be null or empty");
        }
        if (createdBy == null || createdBy.trim().isEmpty()) {
            throw new IllegalArgumentException("Creator ID cannot be null or empty");
        }
        if (maxMembers <= 0) {
            throw new IllegalArgumentException("Max members must be positive");
        }
        
        this.roomId = roomId;
        this.roomName = roomName;
        this.description = description != null ? description : "";
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
        this.isPrivate = isPrivate;
        this.maxMembers = maxMembers;
        
        // Thread-safe collections for concurrent access
        this.members = ConcurrentHashMap.newKeySet();
        this.admins = ConcurrentHashMap.newKeySet();
        this.messages = new CopyOnWriteArrayList<>();
        this.lastReadTimestamps = new ConcurrentHashMap<>();
        
        // Add creator as first member and admin
        this.members.add(createdBy);
        this.admins.add(createdBy);
    }
    
    // Getters
    public String getRoomId() { return roomId; }
    public String getRoomName() { return roomName; }
    public String getDescription() { return description; }
    public String getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isPrivate() { return isPrivate; }
    public int getMaxMembers() { return maxMembers; }
    public Set<String> getMembers() { return new HashSet<>(members); }
    public Set<String> getAdmins() { return new HashSet<>(admins); }
    public List<Message> getMessages() { return new ArrayList<>(messages); }
    
    // Member management
    public boolean addMember(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return false;
        }
        if (members.size() >= maxMembers) {
            return false;
        }
        
        boolean added = members.add(userId);
        if (added) {
            lastReadTimestamps.put(userId, LocalDateTime.now());
            // Add system message about user joining
            Message joinMessage = Message.systemMessage(
                "User " + userId + " joined the room", roomId);
            messages.add(joinMessage);
        }
        return added;
    }
    
    public boolean removeMember(String userId) {
        if (userId == null || userId.equals(createdBy)) {
            return false; // Cannot remove creator
        }
        
        boolean removed = members.remove(userId);
        if (removed) {
            admins.remove(userId); // Remove admin privileges if any
            lastReadTimestamps.remove(userId);
            // Add system message about user leaving
            Message leaveMessage = Message.systemMessage(
                "User " + userId + " left the room", roomId);
            messages.add(leaveMessage);
        }
        return removed;
    }
    
    public boolean isMember(String userId) {
        return members.contains(userId);
    }
    
    public int getMemberCount() {
        return members.size();
    }
    
    // Admin management
    public boolean addAdmin(String userId, String requesterId) {
        if (!isAdmin(requesterId) || !isMember(userId)) {
            return false;
        }
        return admins.add(userId);
    }
    
    public boolean removeAdmin(String userId, String requesterId) {
        if (!isAdmin(requesterId) || userId.equals(createdBy)) {
            return false; // Cannot remove creator's admin privileges
        }
        return admins.remove(userId);
    }
    
    public boolean isAdmin(String userId) {
        return admins.contains(userId);
    }
    
    // Message management
    public boolean addMessage(Message message) {
        if (message == null || !message.isRoomMessage() || 
            !message.getRoomId().equals(roomId)) {
            return false;
        }
        
        // Check if sender is a member (except for system messages)
        if (!message.isSystemMessage() && !isMember(message.getSenderId())) {
            return false;
        }
        
        return messages.add(message);
    }
    
    public List<Message> getRecentMessages(int count) {
        if (count <= 0) {
            return new ArrayList<>();
        }
        
        int size = messages.size();
        int fromIndex = Math.max(0, size - count);
        return new ArrayList<>(messages.subList(fromIndex, size));
    }
    
    public List<Message> getMessagesSince(LocalDateTime since) {
        return messages.stream()
                .filter(msg -> msg.getTimestamp().isAfter(since))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    // Read status management
    public void updateLastRead(String userId) {
        if (isMember(userId)) {
            lastReadTimestamps.put(userId, LocalDateTime.now());
        }
    }
    
    public LocalDateTime getLastRead(String userId) {
        return lastReadTimestamps.get(userId);
    }
    
    public int getUnreadCount(String userId) {
        LocalDateTime lastRead = lastReadTimestamps.get(userId);
        if (lastRead == null) {
            return messages.size();
        }
        
        return (int) messages.stream()
                .filter(msg -> msg.getTimestamp().isAfter(lastRead))
                .count();
    }
    
    // Room settings
    public void setPrivate(boolean isPrivate, String requesterId) {
        if (isAdmin(requesterId)) {
            this.isPrivate = isPrivate;
        }
    }
    
    public void setMaxMembers(int maxMembers, String requesterId) {
        if (isAdmin(requesterId) && maxMembers > 0 && maxMembers >= members.size()) {
            this.maxMembers = maxMembers;
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatRoom chatRoom = (ChatRoom) o;
        return Objects.equals(roomId, chatRoom.roomId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(roomId);
    }
    
    @Override
    public String toString() {
        return String.format("ChatRoom{id='%s', name='%s', members=%d, messages=%d, private=%s}", 
                           roomId, roomName, members.size(), messages.size(), isPrivate);
    }
}