package com.machinecoding.messagequeues.events.model;

/**
 * Priority levels for event processing.
 */
public enum EventPriority {
    LOW(1),
    NORMAL(2),
    HIGH(3),
    CRITICAL(4);
    
    private final int level;
    
    EventPriority(int level) {
        this.level = level;
    }
    
    public int getLevel() {
        return level;
    }
    
    public boolean isHigherThan(EventPriority other) {
        return this.level > other.level;
    }
}