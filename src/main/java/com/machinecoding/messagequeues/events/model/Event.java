package com.machinecoding.messagequeues.events.model;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

/**
 * Base class for all events in the system.
 * Provides common metadata and structure for event processing.
 */
public abstract class Event {
    private final String eventId;
    private final String eventType;
    private final LocalDateTime timestamp;
    private final String source;
    private final Map<String, Object> metadata;
    
    protected Event(String eventType, String source) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
        this.source = source;
        this.metadata = new HashMap<>();
    }
    
    protected Event(String eventId, String eventType, String source, LocalDateTime timestamp) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.source = source;
        this.timestamp = timestamp;
        this.metadata = new HashMap<>();
    }
    
    // Getters
    public String getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getSource() { return source; }
    public Map<String, Object> getMetadata() { return new HashMap<>(metadata); }
    
    // Metadata management
    public void addMetadata(String key, Object value) {
        metadata.put(key, value);
    }
    
    public Object getMetadata(String key) {
        return metadata.get(key);
    }
    
    public boolean hasMetadata(String key) {
        return metadata.containsKey(key);
    }
    
    /**
     * Abstract method to get the event payload.
     * Each concrete event type should implement this.
     */
    public abstract Object getPayload();
    
    /**
     * Get event priority for processing order.
     * Default is NORMAL, can be overridden by specific events.
     */
    public EventPriority getPriority() {
        return EventPriority.NORMAL;
    }
    
    @Override
    public String toString() {
        return String.format("Event{id='%s', type='%s', source='%s', timestamp=%s}", 
                           eventId, eventType, source, timestamp);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Event event = (Event) obj;
        return eventId.equals(event.eventId);
    }
    
    @Override
    public int hashCode() {
        return eventId.hashCode();
    }
}