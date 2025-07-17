package com.machinecoding.ratelimiting;

/**
 * Statistics for distributed locking operations.
 */
public class LockStats {
    private final long totalLockAttempts;
    private final long successfulLocks;
    private final long failedLocks;
    private final long lockRenewals;
    private final long lockReleases;
    private final long expiredLocks;
    private final long currentActiveLocks;
    private final long deadlockDetections;
    
    public LockStats(long totalLockAttempts, long successfulLocks, long failedLocks,
                    long lockRenewals, long lockReleases, long expiredLocks,
                    long currentActiveLocks, long deadlockDetections) {
        this.totalLockAttempts = totalLockAttempts;
        this.successfulLocks = successfulLocks;
        this.failedLocks = failedLocks;
        this.lockRenewals = lockRenewals;
        this.lockReleases = lockReleases;
        this.expiredLocks = expiredLocks;
        this.currentActiveLocks = currentActiveLocks;
        this.deadlockDetections = deadlockDetections;
    }
    
    public long getTotalLockAttempts() { return totalLockAttempts; }
    public long getSuccessfulLocks() { return successfulLocks; }
    public long getFailedLocks() { return failedLocks; }
    public long getLockRenewals() { return lockRenewals; }
    public long getLockReleases() { return lockReleases; }
    public long getExpiredLocks() { return expiredLocks; }
    public long getCurrentActiveLocks() { return currentActiveLocks; }
    public long getDeadlockDetections() { return deadlockDetections; }
    
    public double getSuccessRate() {
        return totalLockAttempts == 0 ? 0.0 : (double) successfulLocks / totalLockAttempts * 100;
    }
    
    @Override
    public String toString() {
        return String.format(
            "LockStats{attempts=%d, successful=%d, failed=%d, renewals=%d, releases=%d, " +
            "expired=%d, active=%d, deadlocks=%d, successRate=%.1f%%}",
            totalLockAttempts, successfulLocks, failedLocks, lockRenewals, lockReleases,
            expiredLocks, currentActiveLocks, deadlockDetections, getSuccessRate()
        );
    }
}