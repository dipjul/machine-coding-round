package com.machinecoding.ratelimiting;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * In-memory implementation of distributed locking system.
 * 
 * Features:
 * - Lock acquisition with TTL
 * - Lock renewal and release
 * - Deadlock detection and prevention
 * - Automatic cleanup of expired locks
 * - Comprehensive statistics and monitoring
 * 
 * Note: This is a single-node implementation for demonstration.
 * In production, you would use Redis, Zookeeper, or etcd for true distributed locking.
 */
public class InMemoryDistributedLock implements DistributedLock {
    
    private final ConcurrentHashMap<String, LockEntry> locks;
    private final ConcurrentHashMap<String, Set<String>> lockHolders; // lockId -> resources held
    private final ReentrantReadWriteLock globalLock;
    private final ScheduledExecutorService cleanupExecutor;
    
    // Statistics
    private final AtomicLong totalLockAttempts;
    private final AtomicLong successfulLocks;
    private final AtomicLong failedLocks;
    private final AtomicLong lockRenewals;
    private final AtomicLong lockReleases;
    private final AtomicLong expiredLocks;
    private final AtomicLong deadlockDetections;
    
    public InMemoryDistributedLock() {
        this(true, 30); // Auto cleanup every 30 seconds
    }
    
    public InMemoryDistributedLock(boolean enableAutoCleanup, long cleanupIntervalSeconds) {
        this.locks = new ConcurrentHashMap<>();
        this.lockHolders = new ConcurrentHashMap<>();
        this.globalLock = new ReentrantReadWriteLock();
        
        this.totalLockAttempts = new AtomicLong(0);
        this.successfulLocks = new AtomicLong(0);
        this.failedLocks = new AtomicLong(0);
        this.lockRenewals = new AtomicLong(0);
        this.lockReleases = new AtomicLong(0);
        this.expiredLocks = new AtomicLong(0);
        this.deadlockDetections = new AtomicLong(0);
        
        if (enableAutoCleanup) {
            this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "DistributedLock-Cleanup");
                t.setDaemon(true);
                return t;
            });
            
            cleanupExecutor.scheduleAtFixedRate(
                this::cleanupExpiredLocks,
                cleanupIntervalSeconds,
                cleanupIntervalSeconds,
                TimeUnit.SECONDS
            );
        } else {
            this.cleanupExecutor = null;
        }
    }
    
    @Override
    public boolean tryLock(String resource, String lockId, long ttl, TimeUnit unit) {
        totalLockAttempts.incrementAndGet();
        
        if (resource == null || lockId == null) {
            failedLocks.incrementAndGet();
            return false;
        }
        
        long expirationTime = System.currentTimeMillis() + unit.toMillis(ttl);
        
        globalLock.writeLock().lock();
        try {
            // Check for deadlock potential
            if (wouldCauseDeadlock(resource, lockId)) {
                deadlockDetections.incrementAndGet();
                failedLocks.incrementAndGet();
                return false;
            }
            
            LockEntry existingLock = locks.get(resource);
            
            // Check if resource is already locked by someone else
            if (existingLock != null && !existingLock.isExpired() && !existingLock.getLockId().equals(lockId)) {
                failedLocks.incrementAndGet();
                return false;
            }
            
            // Acquire or renew the lock
            LockEntry newLock = new LockEntry(lockId, expirationTime, System.currentTimeMillis());
            locks.put(resource, newLock);
            
            // Track lock holder
            lockHolders.computeIfAbsent(lockId, k -> ConcurrentHashMap.newKeySet()).add(resource);
            
            successfulLocks.incrementAndGet();
            return true;
            
        } finally {
            globalLock.writeLock().unlock();
        }
    }
    
    @Override
    public boolean tryLock(String resource, String lockId, long ttl, TimeUnit ttlUnit,
                          long timeout, TimeUnit timeoutUnit) {
        long timeoutMillis = timeoutUnit.toMillis(timeout);
        long startTime = System.currentTimeMillis();
        
        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            if (tryLock(resource, lockId, ttl, ttlUnit)) {
                return true;
            }
            
            // Wait a bit before retrying
            try {
                Thread.sleep(Math.min(100, timeoutMillis / 10));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean renewLock(String resource, String lockId, long ttl, TimeUnit unit) {
        if (resource == null || lockId == null) {
            return false;
        }
        
        globalLock.writeLock().lock();
        try {
            LockEntry existingLock = locks.get(resource);
            
            if (existingLock == null || existingLock.isExpired() || !existingLock.getLockId().equals(lockId)) {
                return false;
            }
            
            long newExpirationTime = System.currentTimeMillis() + unit.toMillis(ttl);
            LockEntry renewedLock = new LockEntry(lockId, newExpirationTime, existingLock.getAcquiredTime());
            locks.put(resource, renewedLock);
            
            lockRenewals.incrementAndGet();
            return true;
            
        } finally {
            globalLock.writeLock().unlock();
        }
    }
    
    @Override
    public boolean releaseLock(String resource, String lockId) {
        if (resource == null || lockId == null) {
            return false;
        }
        
        globalLock.writeLock().lock();
        try {
            LockEntry existingLock = locks.get(resource);
            
            if (existingLock == null || !existingLock.getLockId().equals(lockId)) {
                return false;
            }
            
            locks.remove(resource);
            
            // Remove from lock holders tracking
            Set<String> heldResources = lockHolders.get(lockId);
            if (heldResources != null) {
                heldResources.remove(resource);
                if (heldResources.isEmpty()) {
                    lockHolders.remove(lockId);
                }
            }
            
            lockReleases.incrementAndGet();
            return true;
            
        } finally {
            globalLock.writeLock().unlock();
        }
    }
    
    @Override
    public boolean isLocked(String resource) {
        if (resource == null) {
            return false;
        }
        
        globalLock.readLock().lock();
        try {
            LockEntry lock = locks.get(resource);
            return lock != null && !lock.isExpired();
        } finally {
            globalLock.readLock().unlock();
        }
    }
    
    @Override
    public String getLockHolder(String resource) {
        if (resource == null) {
            return null;
        }
        
        globalLock.readLock().lock();
        try {
            LockEntry lock = locks.get(resource);
            return (lock != null && !lock.isExpired()) ? lock.getLockId() : null;
        } finally {
            globalLock.readLock().unlock();
        }
    }
    
    @Override
    public long getRemainingTTL(String resource) {
        if (resource == null) {
            return -1;
        }
        
        globalLock.readLock().lock();
        try {
            LockEntry lock = locks.get(resource);
            if (lock == null || lock.isExpired()) {
                return -1;
            }
            
            return Math.max(0, lock.getExpirationTime() - System.currentTimeMillis());
        } finally {
            globalLock.readLock().unlock();
        }
    }
    
    @Override
    public boolean forceRelease(String resource) {
        if (resource == null) {
            return false;
        }
        
        globalLock.writeLock().lock();
        try {
            LockEntry removedLock = locks.remove(resource);
            
            if (removedLock != null) {
                // Remove from lock holders tracking
                String lockId = removedLock.getLockId();
                Set<String> heldResources = lockHolders.get(lockId);
                if (heldResources != null) {
                    heldResources.remove(resource);
                    if (heldResources.isEmpty()) {
                        lockHolders.remove(lockId);
                    }
                }
                return true;
            }
            
            return false;
        } finally {
            globalLock.writeLock().unlock();
        }
    }
    
    @Override
    public LockStats getStats() {
        globalLock.readLock().lock();
        try {
            return new LockStats(
                totalLockAttempts.get(),
                successfulLocks.get(),
                failedLocks.get(),
                lockRenewals.get(),
                lockReleases.get(),
                expiredLocks.get(),
                locks.size(),
                deadlockDetections.get()
            );
        } finally {
            globalLock.readLock().unlock();
        }
    }
    
    @Override
    public int cleanupExpiredLocks() {
        globalLock.writeLock().lock();
        try {
            List<String> expiredResources = new ArrayList<>();
            
            for (Map.Entry<String, LockEntry> entry : locks.entrySet()) {
                if (entry.getValue().isExpired()) {
                    expiredResources.add(entry.getKey());
                }
            }
            
            for (String resource : expiredResources) {
                LockEntry expiredLock = locks.remove(resource);
                if (expiredLock != null) {
                    // Remove from lock holders tracking
                    String lockId = expiredLock.getLockId();
                    Set<String> heldResources = lockHolders.get(lockId);
                    if (heldResources != null) {
                        heldResources.remove(resource);
                        if (heldResources.isEmpty()) {
                            lockHolders.remove(lockId);
                        }
                    }
                }
            }
            
            expiredLocks.addAndGet(expiredResources.size());
            return expiredResources.size();
            
        } finally {
            globalLock.writeLock().unlock();
        }
    }
    
    /**
     * Simple deadlock detection: check if acquiring this lock would create a cycle.
     * This is a simplified version - in production, you'd use more sophisticated algorithms.
     */
    private boolean wouldCauseDeadlock(String resource, String lockId) {
        // Get resources currently held by this lockId
        Set<String> currentlyHeld = lockHolders.get(lockId);
        if (currentlyHeld == null || currentlyHeld.isEmpty()) {
            return false; // No resources held, no deadlock possible
        }
        
        // Check if any holder of the requested resource also wants resources we hold
        LockEntry existingLock = locks.get(resource);
        if (existingLock == null || existingLock.isExpired()) {
            return false; // Resource not locked, no deadlock
        }
        
        String currentHolder = existingLock.getLockId();
        Set<String> holderResources = lockHolders.get(currentHolder);
        
        if (holderResources != null) {
            // Simple cycle detection: if current holder holds any resource we want
            for (String heldResource : currentlyHeld) {
                if (holderResources.contains(heldResource)) {
                    return true; // Potential deadlock detected
                }
            }
        }
        
        return false;
    }
    
    /**
     * Shuts down the distributed lock system.
     */
    public void shutdown() {
        if (cleanupExecutor != null && !cleanupExecutor.isShutdown()) {
            cleanupExecutor.shutdown();
            try {
                if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    cleanupExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                cleanupExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Internal class representing a lock entry.
     */
    private static class LockEntry {
        private final String lockId;
        private final long expirationTime;
        private final long acquiredTime;
        
        public LockEntry(String lockId, long expirationTime, long acquiredTime) {
            this.lockId = lockId;
            this.expirationTime = expirationTime;
            this.acquiredTime = acquiredTime;
        }
        
        public String getLockId() { return lockId; }
        public long getExpirationTime() { return expirationTime; }
        public long getAcquiredTime() { return acquiredTime; }
        
        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
        
        public long getAge() {
            return System.currentTimeMillis() - acquiredTime;
        }
    }
}