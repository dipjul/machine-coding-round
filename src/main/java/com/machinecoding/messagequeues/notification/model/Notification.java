package com.machinecoding.messagequeues.notification.model;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * Represents a notification message with metadata.
 */
public class Notification {
    private final String id;
    private final String recipient;
    private final String subject;
    private final String content;
    private final NotificationType type;
    private final Priority priority;
    private final LocalDateTime createdAt;
    private final Map<String, String> metadata;
    
    private Notification(Builder builder) {
        this.id = builder.id;
        this.recipient = builder.recipient;
        this.subject = builder.subject;
        this.content = builder.content;
        this.type = builder.type;
        this.priority = builder.priority;
        this.createdAt = LocalDateTime.now();
        this.metadata = new HashMap<>(builder.metadata);
    }
    
    // Getters
    public String getId() { return id; }
    public String getRecipient() { return recipient; }
    public String getSubject() { return subject; }
    public String getContent() { return content; }
    public NotificationType getType() { return type; }
    public Priority getPriority() { return priority; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Map<String, String> getMetadata() { return new HashMap<>(metadata); }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String id;
        private String recipient;
        private String subject;
        private String content;
        private NotificationType type = NotificationType.EMAIL;
        private Priority priority = Priority.NORMAL;
        private Map<String, String> metadata = new HashMap<>();
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder recipient(String recipient) {
            this.recipient = recipient;
            return this;
        }
        
        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }
        
        public Builder content(String content) {
            this.content = content;
            return this;
        }
        
        public Builder type(NotificationType type) {
            this.type = type;
            return this;
        }
        
        public Builder priority(Priority priority) {
            this.priority = priority;
            return this;
        }
        
        public Builder metadata(String key, String value) {
            this.metadata.put(key, value);
            return this;
        }
        
        public Notification build() {
            if (id == null || recipient == null || content == null) {
                throw new IllegalArgumentException("ID, recipient, and content are required");
            }
            return new Notification(this);
        }
    }
    
    @Override
    public String toString() {
        return String.format("Notification{id='%s', recipient='%s', subject='%s', type=%s, priority=%s}", 
                           id, recipient, subject, type, priority);
    }
}