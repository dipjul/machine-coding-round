package com.machinecoding.realtime.scheduler.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a schedulable task with execution metadata.
 */
public class Task {
    private final String taskId;
    private final String name;
    private final String description;
    private final Runnable action;
    private final TaskPriority priority;
    private final LocalDateTime createdAt;
    private TaskStatus status;
    private LocalDateTime scheduledTime;
    private LocalDateTime lastExecutionTime;
    private LocalDateTime nextExecutionTime;
    private int executionCount;
    private int maxRetries;
    private int retryCount;
    private String lastError;
    private long executionDurationMs;
    
    public Task(String name, String description, Runnable action) {
        this(name, description, action, TaskPriority.NORMAL);
    }
    
    public Task(String name, String description, Runnable action, TaskPriority priority) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Task name cannot be null or empty");
        }
        if (action == null) {
            throw new IllegalArgumentException("Task action cannot be null");
        }
        
        this.taskId = UUID.randomUUID().toString();
        this.name = name;
        this.description = description != null ? description : "";
        this.action = action;
        this.priority = priority != null ? priority : TaskPriority.NORMAL;
        this.createdAt = LocalDateTime.now();
        this.status = TaskStatus.PENDING;
        this.executionCount = 0;
        this.maxRetries = 3;
        this.retryCount = 0;
    }
    
    // Getters
    public String getTaskId() { return taskId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Runnable getAction() { return action; }
    public TaskPriority getPriority() { return priority; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public TaskStatus getStatus() { return status; }
    public LocalDateTime getScheduledTime() { return scheduledTime; }
    public LocalDateTime getLastExecutionTime() { return lastExecutionTime; }
    public LocalDateTime getNextExecutionTime() { return nextExecutionTime; }
    public int getExecutionCount() { return executionCount; }
    public int getMaxRetries() { return maxRetries; }
    public int getRetryCount() { return retryCount; }
    public String getLastError() { return lastError; }
    public long getExecutionDurationMs() { return executionDurationMs; }
    
    // Setters for scheduling
    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
        this.nextExecutionTime = scheduledTime;
        this.status = TaskStatus.SCHEDULED;
    }
    
    public void setNextExecutionTime(LocalDateTime nextExecutionTime) {
        this.nextExecutionTime = nextExecutionTime;
    }
    
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = Math.max(0, maxRetries);
    }
    
    // Status management
    public void markAsRunning() {
        this.status = TaskStatus.RUNNING;
    }
    
    public void markAsCompleted(long executionDurationMs) {
        this.status = TaskStatus.COMPLETED;
        this.lastExecutionTime = LocalDateTime.now();
        this.executionCount++;
        this.executionDurationMs = executionDurationMs;
        this.lastError = null;
    }
    
    public void markAsFailed(String error, long executionDurationMs) {
        this.status = TaskStatus.FAILED;
        this.lastExecutionTime = LocalDateTime.now();
        this.executionCount++;
        this.executionDurationMs = executionDurationMs;
        this.lastError = error;
        this.retryCount++;
    }
    
    public void markAsCancelled() {
        this.status = TaskStatus.CANCELLED;
    }
    
    public void resetForRetry() {
        this.status = TaskStatus.PENDING;
    }
    
    // Utility methods
    public boolean isReadyToExecute() {
        return status == TaskStatus.SCHEDULED && 
               nextExecutionTime != null && 
               LocalDateTime.now().isAfter(nextExecutionTime);
    }
    
    public boolean canRetry() {
        return status == TaskStatus.FAILED && retryCount < maxRetries;
    }
    
    public boolean isRecurring() {
        return false; // Override in RecurringTask
    }
    
    public long getDelayUntilExecution() {
        if (nextExecutionTime == null) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), nextExecutionTime).toMillis();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(taskId, task.taskId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(taskId);
    }
    
    @Override
    public String toString() {
        return String.format("Task{id='%s', name='%s', priority=%s, status=%s, executions=%d}", 
                           taskId, name, priority, status, executionCount);
    }
}