package com.machinecoding.messagequeues.notification.model;

/**
 * Priority levels for notifications.
 */
public enum Priority {
    LOW(1),
    NORMAL(2),
    HIGH(3),
    URGENT(4);
    
    private final int level;
    
    Priority(int level) {
        this.level = level;
    }
    
    public int getLevel() {
        return level;
    }
    
    public boolean isHigherThan(Priority other) {
        return this.level > other.level;
    }
}