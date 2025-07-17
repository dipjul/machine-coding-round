package com.machinecoding.realtime.scheduler.model;

/**
 * Priority levels for task scheduling.
 */
public enum TaskPriority {
    LOW(1, "Low"),
    NORMAL(2, "Normal"),
    HIGH(3, "High"),
    CRITICAL(4, "Critical");
    
    private final int value;
    private final String displayName;
    
    TaskPriority(int value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }
    
    public int getValue() {
        return value;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}