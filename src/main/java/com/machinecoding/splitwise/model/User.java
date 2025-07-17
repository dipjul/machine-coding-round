package com.machinecoding.splitwise.model;

import java.time.LocalDateTime;

/**
 * Represents a user in the Splitwise application.
 */
public class User {
    private final String userId;
    private final String name;
    private final String email;
    private final String phoneNumber;
    private final LocalDateTime registeredAt;
    private boolean isActive;
    
    public User(String userId, String name, String email, String phoneNumber) {
        this.userId = userId != null ? userId.trim() : "";
        this.name = name != null ? name.trim() : "";
        this.email = email != null ? email.trim() : "";
        this.phoneNumber = phoneNumber != null ? phoneNumber.trim() : "";
        this.registeredAt = LocalDateTime.now();
        this.isActive = true;
    }
    
    // Getters
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public boolean isActive() { return isActive; }
    
    public void setActive(boolean active) {
        this.isActive = active;
    }
    
    @Override
    public String toString() {
        return String.format("User{id='%s', name='%s', email='%s', active=%s}", 
                           userId, name, email, isActive);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return userId.equals(user.userId);
    }
    
    @Override
    public int hashCode() {
        return userId.hashCode();
    }
}