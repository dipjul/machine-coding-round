package com.machinecoding.realtime.scheduler.model;

/**
 * Status of a scheduled task.
 */
public enum TaskStatus {
    PENDING("Pending"),
    SCHEDULED("Scheduled"),
    RUNNING("Running"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    CANCELLED("Cancelled");
    
    private final String displayName;
    
    TaskStatus(String displayName) {
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