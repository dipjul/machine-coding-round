package com.machinecoding.ratelimiting;

import java.util.concurrent.TimeUnit;

/**
 * Interface for distributed locking system.
 * Provides methods for acquiring, renewing, and releasing locks across distributed systems.
 */
public interface DistributedLock {
    
    /**
     * Attempts to acquire a lock for the specified resource.
     * 
     * @param resource the resource to lock
     * @param lockId unique identifier for this lock attempt
     * @param ttl time to live for the lock
     * @param unit time unit for TTL
     * @return true if lock was acquired, false otherwise
     */
    boolean tryLock(String resource, String lockId, long ttl, TimeUnit unit);
    
    /**
     * Attempts to acquire a lock with a timeout.
     * 
     * @param resource the resource to lock
     * @param lockId unique identifier for this lock attempt
     * @param ttl time to live for the lock
     * @param ttlUnit time unit for TTL
     * @param timeout maximum time to wait for lock acquisition
     * @param timeoutUnit time unit for timeout
     * @return true if lock was acquired within timeout, false otherwise
     */
    boolean tryLock(String resource, String lockId, long ttl, TimeUnit ttlUnit, 
                   long timeout, TimeUnit timeoutUnit);
    
    /**
     * Renews an existing lock, extending its TTL.
     * 
     * @param resource the resource that is locked
     * @param lockId the lock identifier
     * @param ttl new time to live for the lock
     * @param unit time unit for TTL
     * @return true if lock was renewed, false if lock doesn't exist or expired
     */
    boolean renewLock(String resource, String lockId, long ttl, TimeUnit unit);
    
    /**
     * Releases a lock.
     * 
     * @param resource the resource to unlock
     * @param lockId the lock identifier
     * @return true if lock was released, false if lock doesn't exist or not owned by lockId
     */
    boolean releaseLock(String resource, String lockId);
    
    /**
     * Checks if a resource is currently locked.
     * 
     * @param resource the resource to check
     * @return true if the resource is locked
     */
    boolean isLocked(String resource);
    
    /**
     * Gets the current lock holder for a resource.
     * 
     * @param resource the resource to check
     * @return the lock ID of the current holder, or null if not locked
     */
    String getLockHolder(String resource);
    
    /**
     * Gets the remaining TTL for a lock.
     * 
     * @param resource the resource to check
     * @return remaining TTL in milliseconds, -1 if not locked
     */
    long getRemainingTTL(String resource);
    
    /**
     * Forces release of a lock (admin operation).
     * 
     * @param resource the resource to force unlock
     * @return true if lock was released
     */
    boolean forceRelease(String resource);
    
    /**
     * Gets statistics about the locking system.
     * 
     * @return lock statistics
     */
    LockStats getStats();
    
    /**
     * Performs cleanup of expired locks.
     * 
     * @return number of expired locks cleaned up
     */
    int cleanupExpiredLocks();
}