package com.machinecoding.realtime.chat.model;

/**
 * Enumeration of message types in the chat system.
 */
public enum MessageType {
    TEXT("Text"),
    IMAGE("Image"),
    FILE("File"),
    SYSTEM("System"),
    NOTIFICATION("Notification");
    
    private final String displayName;
    
    MessageType(String displayName) {
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