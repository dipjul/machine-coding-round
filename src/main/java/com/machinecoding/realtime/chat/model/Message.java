package com.machinecoding.realtime.chat.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a message in the chat system.
 */
public class Message {
    private final String messageId;
    private final String senderId;
    private final String content;
    private final MessageType type;
    private final LocalDateTime timestamp;
    private final String roomId; // null for direct messages
    private final String recipientId; // null for room messages
    private MessageStatus status;
    
    // Constructor for room messages
    public Message(String senderId, String content, String roomId) {
        this(senderId, content, MessageType.TEXT, roomId, null);
    }
    
    // Constructor for direct messages
    public Message(String senderId, String content, String recipientId, boolean isDirect) {
        this(senderId, content, MessageType.TEXT, null, recipientId);
    }
    
    // Constructor for system messages
    public static Message systemMessage(String content, String roomId) {
        return new Message("SYSTEM", content, MessageType.SYSTEM, roomId, null);
    }
    
    private Message(String senderId, String content, MessageType type, String roomId, String recipientId) {
        if (senderId == null || senderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Sender ID cannot be null or empty");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }
        if (roomId == null && recipientId == null) {
            throw new IllegalArgumentException("Either roomId or recipientId must be specified");
        }
        
        this.messageId = UUID.randomUUID().toString();
        this.senderId = senderId;
        this.content = content;
        this.type = type != null ? type : MessageType.TEXT;
        this.timestamp = LocalDateTime.now();
        this.roomId = roomId;
        this.recipientId = recipientId;
        this.status = MessageStatus.SENT;
    }
    
    // Getters
    public String getMessageId() { return messageId; }
    public String getSenderId() { return senderId; }
    public String getContent() { return content; }
    public MessageType getType() { return type; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getRoomId() { return roomId; }
    public String getRecipientId() { return recipientId; }
    public MessageStatus getStatus() { return status; }
    
    // Status management
    public void markAsDelivered() {
        this.status = MessageStatus.DELIVERED;
    }
    
    public void markAsRead() {
        this.status = MessageStatus.READ;
    }
    
    public void markAsFailed() {
        this.status = MessageStatus.FAILED;
    }
    
    // Utility methods
    public boolean isDirectMessage() {
        return recipientId != null;
    }
    
    public boolean isRoomMessage() {
        return roomId != null;
    }
    
    public boolean isSystemMessage() {
        return type == MessageType.SYSTEM;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(messageId, message.messageId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(messageId);
    }
    
    @Override
    public String toString() {
        return String.format("Message{id='%s', sender='%s', content='%s', type=%s, timestamp=%s, status=%s}", 
                           messageId, senderId, content, type, timestamp, status);
    }
}