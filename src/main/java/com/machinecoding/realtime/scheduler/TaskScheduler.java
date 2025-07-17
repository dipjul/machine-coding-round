package com.machinecoding.realtime.scheduler;

import com.machinecoding.realtime.scheduler.model.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

/**
 * Interface for task scheduling operations.
 */
public interface TaskScheduler {
    
    /**
     * Schedules a task for immediate execution.
     * 
     * @param task the task to execute
     * @return the scheduled task ID
     */
    String scheduleNow(Task task);
    
    /**
     * Schedules a task for execution at a specific time.
     * 
     * @param task the task to execute
     * @param scheduledTime when to execute the task
     * @return the scheduled task ID
     */
    String scheduleAt(Task task, LocalDateTime scheduledTime);
    
    /**
     * Schedules a task for execution after a delay.
     * 
     * @param task the task to execute
     * @param delay delay amount
     * @param unit delay time unit
     * @return the scheduled task ID
     */
    String scheduleAfter(Task task, long delay, ChronoUnit unit);
    
    /**
     * Schedules a recurring task.
     * 
     * @param task the recurring task to schedule
     * @return the scheduled task ID
     */
    String scheduleRecurring(RecurringTask task);
    
    /**
     * Cancels a scheduled task.
     * 
     * @param taskId the task ID to cancel
     * @return true if the task was cancelled
     */
    boolean cancelTask(String taskId);
    
    /**
     * Gets a task by ID.
     * 
     * @param taskId the task ID
     * @return the task, or null if not found
     */
    Task getTask(String taskId);
    
    /**
     * Gets all tasks.
     * 
     * @return list of all tasks
     */
    List<Task> getAllTasks();
    
    /**
     * Gets tasks by status.
     * 
     * @param status the task status to filter by
     * @return list of tasks with the specified status
     */
    List<Task> getTasksByStatus(TaskStatus status);
    
    /**
     * Gets pending tasks (scheduled but not yet executed).
     * 
     * @return list of pending tasks
     */
    List<Task> getPendingTasks();
    
    /**
     * Gets running tasks.
     * 
     * @return list of currently running tasks
     */
    List<Task> getRunningTasks();
    
    /**
     * Starts the scheduler.
     */
    void start();
    
    /**
     * Stops the scheduler.
     */
    void stop();
    
    /**
     * Checks if the scheduler is running.
     * 
     * @return true if the scheduler is running
     */
    boolean isRunning();
    
    /**
     * Gets scheduler statistics.
     * 
     * @return scheduler statistics
     */
    SchedulerStats getStats();
    
    /**
     * Adds a task execution listener.
     * 
     * @param listener the listener to add
     */
    void addTaskListener(TaskExecutionListener listener);
    
    /**
     * Removes a task execution listener.
     * 
     * @param listener the listener to remove
     */
    void removeTaskListener(TaskExecutionListener listener);
    
    /**
     * Interface for task execution event listeners.
     */
    interface TaskExecutionListener {
        void onTaskScheduled(Task task);
        void onTaskStarted(Task task);
        void onTaskCompleted(Task task);
        void onTaskFailed(Task task, String error);
        void onTaskCancelled(Task task);
    }
}