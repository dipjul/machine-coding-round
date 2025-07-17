package com.machinecoding.realtime.chat.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a user in the chat system.
 */
public class User {
    private final String userId;
    private final String username;
    private final String email;
    private UserStatus status;
    private LocalDateTime lastSeen;
    private LocalDateTime joinedAt;
    
    public User(String userId, String username, String email) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.status = UserStatus.OFFLINE;
        this.joinedAt = LocalDateTime.now();
        this.lastSeen = LocalDateTime.now();
    }
    
    // Getters
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public UserStatus getStatus() { return status; }
    public LocalDateTime getLastSeen() { return lastSeen; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
    
    // Status management
    public void setOnline() {
        this.status = UserStatus.ONLINE;
        this.lastSeen = LocalDateTime.now();
    }
    
    public void setOffline() {
        this.status = UserStatus.OFFLINE;
        this.lastSeen = LocalDateTime.now();
    }
    
    public void setAway() {
        this.status = UserStatus.AWAY;
        this.lastSeen = LocalDateTime.now();
    }
    
    public void updateLastSeen() {
        this.lastSeen = LocalDateTime.now();
    }
    
    public boolean isOnline() {
        return status == UserStatus.ONLINE;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
    
    @Override
    public String toString() {
        return String.format("User{id='%s', username='%s', status=%s, lastSeen=%s}", 
                           userId, username, status, lastSeen);
    }
}