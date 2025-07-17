package com.machinecoding.realtime.scheduler.model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Represents a recurring task with scheduling patterns.
 */
public class RecurringTask extends Task {
    private final RecurrencePattern pattern;
    private final long intervalValue;
    private final ChronoUnit intervalUnit;
    private final LocalDateTime endTime;
    private int maxExecutions;
    private boolean enabled;
    
    // Constructor for interval-based recurrence
    public RecurringTask(String name, String description, Runnable action, 
                        long intervalValue, ChronoUnit intervalUnit) {
        this(name, description, action, TaskPriority.NORMAL, intervalValue, intervalUnit, null, -1);
    }
    
    // Constructor for pattern-based recurrence
    public RecurringTask(String name, String description, Runnable action, 
                        RecurrencePattern pattern) {
        super(name, description, action, TaskPriority.NORMAL);
        this.pattern = pattern;
        this.intervalValue = 0;
        this.intervalUnit = null;
        this.endTime = null;
        this.maxExecutions = -1;
        this.enabled = true;
    }
    
    // Full constructor
    public RecurringTask(String name, String description, Runnable action, TaskPriority priority,
                        long intervalValue, ChronoUnit intervalUnit, LocalDateTime endTime, int maxExecutions) {
        super(name, description, action, priority);
        this.pattern = RecurrencePattern.INTERVAL;
        this.intervalValue = intervalValue;
        this.intervalUnit = intervalUnit;
        this.endTime = endTime;
        this.maxExecutions = maxExecutions;
        this.enabled = true;
    }
    
    // Getters
    public RecurrencePattern getPattern() { return pattern; }
    public long getIntervalValue() { return intervalValue; }
    public ChronoUnit getIntervalUnit() { return intervalUnit; }
    public LocalDateTime getEndTime() { return endTime; }
    public int getMaxExecutions() { return maxExecutions; }
    public boolean isEnabled() { return enabled; }
    
    // Control methods
    public void enable() { this.enabled = true; }
    public void disable() { this.enabled = false; }
    public void setMaxExecutions(int maxExecutions) { this.maxExecutions = maxExecutions; }
    
    @Override
    public boolean isRecurring() {
        return true;
    }
    
    /**
     * Calculates the next execution time based on the recurrence pattern.
     */
    public LocalDateTime calculateNextExecution(LocalDateTime currentTime) {
        if (!enabled) {
            return null;
        }
        
        // Check if we've reached max executions
        if (maxExecutions > 0 && getExecutionCount() >= maxExecutions) {
            return null;
        }
        
        // Check if we've passed the end time
        if (endTime != null && currentTime.isAfter(endTime)) {
            return null;
        }
        
        LocalDateTime nextTime = null;
        
        switch (pattern) {
            case INTERVAL:
                nextTime = currentTime.plus(intervalValue, intervalUnit);
                break;
            case DAILY:
                nextTime = currentTime.plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
                break;
            case WEEKLY:
                nextTime = currentTime.plusWeeks(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
                break;
            case MONTHLY:
                nextTime = currentTime.plusMonths(1).withDayOfMonth(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
                break;
            case HOURLY:
                nextTime = currentTime.plusHours(1).withMinute(0).withSecond(0).withNano(0);
                break;
        }
        
        // Ensure next time is not past end time
        if (endTime != null && nextTime != null && nextTime.isAfter(endTime)) {
            return null;
        }
        
        return nextTime;
    }
    
    /**
     * Updates the task for the next execution.
     */
    public void scheduleNext() {
        LocalDateTime nextTime = calculateNextExecution(LocalDateTime.now());
        if (nextTime != null) {
            setNextExecutionTime(nextTime);
            resetForRetry(); // Reset status to PENDING for next execution
        } else {
            markAsCancelled(); // No more executions
        }
    }
    
    @Override
    public String toString() {
        return String.format("RecurringTask{id='%s', name='%s', pattern=%s, priority=%s, status=%s, executions=%d, enabled=%s}", 
                           getTaskId(), getName(), pattern, getPriority(), getStatus(), getExecutionCount(), enabled);
    }
    
    /**
     * Enumeration of recurrence patterns.
     */
    public enum RecurrencePattern {
        INTERVAL("Interval-based"),
        HOURLY("Every Hour"),
        DAILY("Daily"),
        WEEKLY("Weekly"),
        MONTHLY("Monthly");
        
        private final String displayName;
        
        RecurrencePattern(String displayName) {
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
}