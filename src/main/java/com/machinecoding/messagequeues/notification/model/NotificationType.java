package com.machinecoding.messagequeues.notification.model;

/**
 * Enumeration of supported notification types.
 */
public enum NotificationType {
    EMAIL("Email"),
    SMS("SMS"),
    PUSH("Push Notification"),
    SLACK("Slack Message"),
    WEBHOOK("Webhook");
    
    private final String displayName;
    
    NotificationType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}