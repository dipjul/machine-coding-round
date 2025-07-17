package com.machinecoding.ratelimiting;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * Comprehensive demonstration of Distributed Lock system.
 * Shows lock acquisition, renewal, release, deadlock prevention, and concurrent access patterns.
 */
public class DistributedLockDemo {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Distributed Lock Demo ===\n");
        
        InMemoryDistributedLock lockSystem = new InMemoryDistributedLock();
        
        // Demo 1: Basic lock operations
        System.out.println("=== Demo 1: Basic Lock Operations ===");
        demonstrateBasicOperations(lockSystem);
        
        // Demo 2: Lock renewal and expiration
        System.out.println("\n=== Demo 2: Lock Renewal & Expiration ===");
        demonstrateLockRenewal(lockSystem);
        
        // Demo 3: Deadlock detection
        System.out.println("\n=== Demo 3: Deadlock Detection ===");
        demonstrateDeadlockDetection(lockSystem);
        
        // Demo 4: Concurrent access patterns
        System.out.println("\n=== Demo 4: Concurrent Access Patterns ===");
        demonstrateConcurrentAccess(lockSystem);
        
        // Demo 5: Performance and scalability
        System.out.println("\n=== Demo 5: Performance & Scalability ===");
        demonstratePerformance(lockSystem);
        
        // Cleanup
        lockSystem.shutdown();
        System.out.println("\n=== Demo Complete ===");
    }
    
    private static void demonstrateBasicOperations(DistributedLock lockSystem) throws InterruptedException {
        System.out.println("1. Basic lock acquisition and release:");
        
        String resource = "database-connection-1";
        String lockId1 = "client-1";
        String lockId2 = "client-2";
        
        // Client 1 acquires lock
        boolean acquired1 = lockSystem.tryLock(resource, lockId1, 5, TimeUnit.SECONDS);
        System.out.println("   Client 1 acquire lock: " + acquired1);
        System.out.println("   Resource locked: " + lockSystem.isLocked(resource));
        System.out.println("   Lock holder: " + lockSystem.getLockHolder(resource));
        System.out.println("   Remaining TTL: " + lockSystem.getRemainingTTL(resource) + "ms");
        
        // Client 2 tries to acquire same lock
        boolean acquired2 = lockSystem.tryLock(resource, lockId2, 5, TimeUnit.SECONDS);
        System.out.println("   Client 2 acquire lock: " + acquired2 + " (should be false)");
        
        // Client 1 releases lock
        boolean released = lockSystem.releaseLock(resource, lockId1);
        System.out.println("   Client 1 release lock: " + released);
        System.out.println("   Resource locked after release: " + lockSystem.isLocked(resource));
        
        // Client 2 can now acquire lock
        acquired2 = lockSystem.tryLock(resource, lockId2, 5, TimeUnit.SECONDS);
        System.out.println("   Client 2 acquire lock after release: " + acquired2);
        
        // Cleanup
        lockSystem.releaseLock(resource, lockId2);
        
        System.out.println("\n2. Lock with timeout:");
        
        // Client 1 holds lock
        lockSystem.tryLock(resource, lockId1, 2, TimeUnit.SECONDS);
        System.out.println("   Client 1 acquired lock with 2s TTL");
        
        // Client 2 tries with timeout
        long startTime = System.currentTimeMillis();
        boolean acquiredWithTimeout = lockSystem.tryLock(resource, lockId2, 5, TimeUnit.SECONDS, 
                                                        3, TimeUnit.SECONDS);
        long waitTime = System.currentTimeMillis() - startTime;
        System.out.println("   Client 2 acquire with 3s timeout: " + acquiredWithTimeout + 
                          " (waited " + waitTime + "ms)");
        
        // Cleanup
        lockSystem.releaseLock(resource, lockId1);
        lockSystem.releaseLock(resource, lockId2);
        
        System.out.println("\n3. Statistics after basic operations:");
        System.out.println("   " + lockSystem.getStats());
    }
    
    private static void demonstrateLockRenewal(DistributedLock lockSystem) throws InterruptedException {
        System.out.println("1. Lock renewal:");
        
        String resource = "critical-section";
        String lockId = "worker-1";
        
        // Acquire lock with short TTL
        boolean acquired = lockSystem.tryLock(resource, lockId, 1, TimeUnit.SECONDS);
        System.out.println("   Acquired lock with 1s TTL: " + acquired);
        System.out.println("   Initial TTL: " + lockSystem.getRemainingTTL(resource) + "ms");
        
        // Wait a bit
        Thread.sleep(500);
        System.out.println("   TTL after 500ms: " + lockSystem.getRemainingTTL(resource) + "ms");
        
        // Renew lock
        boolean renewed = lockSystem.renewLock(resource, lockId, 3, TimeUnit.SECONDS);
        System.out.println("   Lock renewed: " + renewed);
        System.out.println("   TTL after renewal: " + lockSystem.getRemainingTTL(resource) + "ms");
        
        // Test expiration
        System.out.println("\n2. Lock expiration:");
        lockSystem.releaseLock(resource, lockId);
        
        // Acquire with very short TTL
        lockSystem.tryLock(resource, lockId, 100, TimeUnit.MILLISECONDS);
        System.out.println("   Acquired lock with 100ms TTL");
        System.out.println("   Initial TTL: " + lockSystem.getRemainingTTL(resource) + "ms");
        
        // Wait for expiration
        Thread.sleep(200);
        System.out.println("   After 200ms - Resource locked: " + lockSystem.isLocked(resource));
        System.out.println("   TTL: " + lockSystem.getRemainingTTL(resource) + "ms");
        
        // Another client can now acquire
        boolean acquiredAfterExpiry = lockSystem.tryLock(resource, "worker-2", 1, TimeUnit.SECONDS);
        System.out.println("   Another client acquire after expiry: " + acquiredAfterExpiry);
        
        // Cleanup
        lockSystem.releaseLock(resource, "worker-2");
        
        System.out.println("\n3. Cleanup expired locks:");
        int cleanedUp = lockSystem.cleanupExpiredLocks();
        System.out.println("   Cleaned up " + cleanedUp + " expired locks");
    }
    
    private static void demonstrateDeadlockDetection(DistributedLock lockSystem) {
        System.out.println("1. Deadlock detection scenario:");
        
        String resource1 = "database-1";
        String resource2 = "database-2";
        String client1 = "client-1";
        String client2 = "client-2";
        
        // Client 1 acquires resource 1
        boolean lock1 = lockSystem.tryLock(resource1, client1, 10, TimeUnit.SECONDS);
        System.out.println("   Client 1 acquires resource 1: " + lock1);
        
        // Client 2 acquires resource 2
        boolean lock2 = lockSystem.tryLock(resource2, client2, 10, TimeUnit.SECONDS);
        System.out.println("   Client 2 acquires resource 2: " + lock2);
        
        // Now client 1 tries to acquire resource 2 (held by client 2)
        boolean lock3 = lockSystem.tryLock(resource2, client1, 10, TimeUnit.SECONDS);
        System.out.println("   Client 1 tries to acquire resource 2: " + lock3 + " (should fail - already held)");
        
        // Client 2 tries to acquire resource 1 (held by client 1) - potential deadlock
        boolean lock4 = lockSystem.tryLock(resource1, client2, 10, TimeUnit.SECONDS);
        System.out.println("   Client 2 tries to acquire resource 1: " + lock4 + " (deadlock detection)");
        
        // Cleanup
        lockSystem.releaseLock(resource1, client1);
        lockSystem.releaseLock(resource2, client2);
        
        System.out.println("\n2. Force release (admin operation):");
        
        // Create a lock and force release it
        lockSystem.tryLock(resource1, client1, 30, TimeUnit.SECONDS);
        System.out.println("   Client 1 acquired resource 1 with 30s TTL");
        
        boolean forceReleased = lockSystem.forceRelease(resource1);
        System.out.println("   Force released resource 1: " + forceReleased);
        System.out.println("   Resource 1 locked after force release: " + lockSystem.isLocked(resource1));
        
        System.out.println("\n3. Deadlock statistics:");
        System.out.println("   " + lockSystem.getStats());
    }
    
    private static void demonstrateConcurrentAccess(DistributedLock lockSystem) throws InterruptedException {
        System.out.println("1. Concurrent lock contention (10 clients, 1 resource):");
        
        String resource = "shared-resource";
        final int numClients = 10;
        final CountDownLatch latch = new CountDownLatch(numClients);
        final AtomicInteger successfulAcquisitions = new AtomicInteger(0);
        final AtomicInteger failedAcquisitions = new AtomicInteger(0);
        
        ExecutorService executor = Executors.newFixedThreadPool(numClients);
        
        for (int i = 0; i < numClients; i++) {
            final int clientId = i;
            executor.submit(() -> {
                try {
                    String lockId = "client-" + clientId;
                    
                    // Try to acquire lock
                    boolean acquired = lockSystem.tryLock(resource, lockId, 1, TimeUnit.SECONDS);
                    
                    if (acquired) {
                        successfulAcquisitions.incrementAndGet();
                        System.out.println("   Client " + clientId + " acquired lock");
                        
                        // Hold lock for a bit
                        Thread.sleep(100);
                        
                        // Release lock
                        lockSystem.releaseLock(resource, lockId);
                        System.out.println("   Client " + clientId + " released lock");
                    } else {
                        failedAcquisitions.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();
        
        System.out.println("   Successful acquisitions: " + successfulAcquisitions.get());
        System.out.println("   Failed acquisitions: " + failedAcquisitions.get());
        
        System.out.println("\n2. Multiple resources, multiple clients:");
        
        final int numResources = 5;
        final int numClientsPerResource = 3;
        final CountDownLatch multiLatch = new CountDownLatch(numResources * numClientsPerResource);
        final AtomicInteger totalSuccess = new AtomicInteger(0);
        
        ExecutorService multiExecutor = Executors.newFixedThreadPool(numResources * numClientsPerResource);
        
        for (int r = 0; r < numResources; r++) {
            final String resourceName = "resource-" + r;
            
            for (int c = 0; c < numClientsPerResource; c++) {
                final int clientId = r * numClientsPerResource + c;
                
                multiExecutor.submit(() -> {
                    try {
                        String lockId = "multi-client-" + clientId;
                        
                        if (lockSystem.tryLock(resourceName, lockId, 500, TimeUnit.MILLISECONDS)) {
                            totalSuccess.incrementAndGet();
                            Thread.sleep(50); // Simulate work
                            lockSystem.releaseLock(resourceName, lockId);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        multiLatch.countDown();
                    }
                });
            }
        }
        
        multiLatch.await(10, TimeUnit.SECONDS);
        multiExecutor.shutdown();
        
        System.out.println("   Total successful acquisitions: " + totalSuccess.get() + 
                          "/" + (numResources * numClientsPerResource));
        
        System.out.println("\n3. Concurrent access statistics:");
        System.out.println("   " + lockSystem.getStats());
    }
    
    private static void demonstratePerformance(DistributedLock lockSystem) {
        System.out.println("1. Performance testing:");
        
        final int numOperations = 10000;
        String[] resources = new String[100];
        for (int i = 0; i < resources.length; i++) {
            resources[i] = "perf-resource-" + i;
        }
        
        Random random = new Random(42);
        
        // Test lock acquisition performance
        long startTime = System.currentTimeMillis();
        int successful = 0;
        
        for (int i = 0; i < numOperations; i++) {
            String resource = resources[random.nextInt(resources.length)];
            String lockId = "perf-client-" + (i % 50); // 50 different clients
            
            if (lockSystem.tryLock(resource, lockId, 100, TimeUnit.MILLISECONDS)) {
                successful++;
                lockSystem.releaseLock(resource, lockId);
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("   Operations: " + numOperations);
        System.out.println("   Duration: " + duration + "ms");
        System.out.println("   Throughput: " + (numOperations * 1000L / duration) + " ops/sec");
        System.out.println("   Success rate: " + (successful * 100.0 / numOperations) + "%");
        
        System.out.println("\n2. Memory usage with many locks:");
        
        // Create many locks
        for (int i = 0; i < 1000; i++) {
            lockSystem.tryLock("memory-test-" + i, "client-" + (i % 10), 1, TimeUnit.MINUTES);
        }
        
        System.out.println("   Created 1000 locks across 10 clients");
        System.out.println("   " + lockSystem.getStats());
        
        // Cleanup
        for (int i = 0; i < 1000; i++) {
            lockSystem.forceRelease("memory-test-" + i);
        }
        
        System.out.println("\n3. Final performance statistics:");
        System.out.println("   " + lockSystem.getStats());
    }
}