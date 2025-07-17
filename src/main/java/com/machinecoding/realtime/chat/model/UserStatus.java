package com.machinecoding.realtime.chat.model;

/**
 * Enumeration of possible user statuses in the chat system.
 */
public enum UserStatus {
    ONLINE("Online"),
    OFFLINE("Offline"),
    AWAY("Away"),
    BUSY("Busy");
    
    private final String displayName;
    
    UserStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}