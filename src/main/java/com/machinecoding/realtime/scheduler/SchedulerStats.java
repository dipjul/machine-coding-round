package com.machinecoding.realtime.scheduler;

/**
 * Statistics for the task scheduler.
 */
public class SchedulerStats {
    private final int totalTasks;
    private final int pendingTasks;
    private final int runningTasks;
    private final int completedTasks;
    private final int failedTasks;
    private final int cancelledTasks;
    private final int recurringTasks;
    private final long totalExecutions;
    private final double averageExecutionTime;
    private final int activeThreads;
    private final long uptimeMs;
    
    public SchedulerStats(int totalTasks, int pendingTasks, int runningTasks, int completedTasks,
                         int failedTasks, int cancelledTasks, int recurringTasks, long totalExecutions,
                         double averageExecutionTime, int activeThreads, long uptimeMs) {
        this.totalTasks = totalTasks;
        this.pendingTasks = pendingTasks;
        this.runningTasks = runningTasks;
        this.completedTasks = completedTasks;
        this.failedTasks = failedTasks;
        this.cancelledTasks = cancelledTasks;
        this.recurringTasks = recurringTasks;
        this.totalExecutions = totalExecutions;
        this.averageExecutionTime = averageExecutionTime;
        this.activeThreads = activeThreads;
        this.uptimeMs = uptimeMs;
    }
    
    // Getters
    public int getTotalTasks() { return totalTasks; }
    public int getPendingTasks() { return pendingTasks; }
    public int getRunningTasks() { return runningTasks; }
    public int getCompletedTasks() { return completedTasks; }
    public int getFailedTasks() { return failedTasks; }
    public int getCancelledTasks() { return cancelledTasks; }
    public int getRecurringTasks() { return recurringTasks; }
    public long getTotalExecutions() { return totalExecutions; }
    public double getAverageExecutionTime() { return averageExecutionTime; }
    public int getActiveThreads() { return activeThreads; }
    public long getUptimeMs() { return uptimeMs; }
    
    public double getSuccessRate() {
        long totalFinished = completedTasks + failedTasks;
        return totalFinished == 0 ? 0.0 : (double) completedTasks / totalFinished * 100;
    }
    
    public double getFailureRate() {
        long totalFinished = completedTasks + failedTasks;
        return totalFinished == 0 ? 0.0 : (double) failedTasks / totalFinished * 100;
    }
    
    @Override
    public String toString() {
        return String.format(
            "SchedulerStats{total=%d, pending=%d, running=%d, completed=%d (%.1f%%), failed=%d (%.1f%%), " +
            "cancelled=%d, recurring=%d, executions=%d, avgTime=%.1fms, threads=%d, uptime=%dms}",
            totalTasks, pendingTasks, runningTasks, completedTasks, getSuccessRate(), 
            failedTasks, getFailureRate(), cancelledTasks, recurringTasks, totalExecutions, 
            averageExecutionTime, activeThreads, uptimeMs
        );
    }
}