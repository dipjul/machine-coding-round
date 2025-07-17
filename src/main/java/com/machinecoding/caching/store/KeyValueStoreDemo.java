package com.machinecoding.caching.store;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Comprehensive demonstration of the KeyValueStore implementation.
 * Shows basic operations, TTL functionality, concurrent access, and performance characteristics.
 */
public class KeyValueStoreDemo {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Key-Value Store Demo ===\n");
        
        InMemoryKeyValueStore<String, String> store = new InMemoryKeyValueStore<>();
        
        // Demo 1: Basic operations
        System.out.println("=== Demo 1: Basic Operations ===");
        demonstrateBasicOperations(store);
        
        // Demo 2: TTL functionality
        System.out.println("\n=== Demo 2: TTL (Time To Live) Functionality ===");
        demonstrateTTL(store);
        
        // Demo 3: Concurrent access
        System.out.println("\n=== Demo 3: Concurrent Access ===");
        demonstrateConcurrentAccess(store);
        
        // Demo 4: Memory management
        System.out.println("\n=== Demo 4: Memory Management & Statistics ===");
        demonstrateMemoryManagement(store);
        
        // Demo 5: Performance testing
        System.out.println("\n=== Demo 5: Performance Testing ===");
        demonstratePerformance(store);
        
        // Cleanup
        store.shutdown();
        System.out.println("\n=== Demo Complete ===");
    }
    
    private static void demonstrateBasicOperations(KeyValueStore<String, String> store) {
        System.out.println("1. Basic put/get operations:");
        
        // Put some values
        store.put("user:1", "John Doe");
        store.put("user:2", "Jane Smith");
        store.put("config:timeout", "30");
        
        // Get values
        System.out.println("   user:1 = " + store.get("user:1").orElse("NOT_FOUND"));
        System.out.println("   user:2 = " + store.get("user:2").orElse("NOT_FOUND"));
        System.out.println("   config:timeout = " + store.get("config:timeout").orElse("NOT_FOUND"));
        System.out.println("   non-existent = " + store.get("non-existent").orElse("NOT_FOUND"));
        
        System.out.println("\n2. Store operations:");
        System.out.println("   Size: " + store.size());
        System.out.println("   Contains 'user:1': " + store.containsKey("user:1"));
        System.out.println("   Keys: " + store.keySet());
        
        // Remove a key
        System.out.println("\n3. Remove operation:");
        boolean removed = store.remove("user:2");
        System.out.println("   Removed 'user:2': " + removed);
        System.out.println("   Size after removal: " + store.size());
        System.out.println("   Keys after removal: " + store.keySet());
    }
    
    private static void demonstrateTTL(KeyValueStore<String, String> store) throws InterruptedException {
        System.out.println("1. Setting values with TTL:");
        
        // Set values with different TTL
        store.put("session:abc123", "user_data", 2, TimeUnit.SECONDS);
        store.put("cache:temp", "temporary_data", 1, TimeUnit.SECONDS);
        store.put("permanent", "permanent_data"); // No TTL
        
        System.out.println("   Set session:abc123 with 2s TTL");
        System.out.println("   Set cache:temp with 1s TTL");
        System.out.println("   Set permanent with no TTL");
        
        // Check TTL values
        System.out.println("\n2. Checking TTL values:");
        System.out.println("   session:abc123 TTL: " + store.getTTL("session:abc123") + "ms");
        System.out.println("   cache:temp TTL: " + store.getTTL("cache:temp") + "ms");
        System.out.println("   permanent TTL: " + store.getTTL("permanent") + " (no expiration)");
        
        // Wait and check expiration
        System.out.println("\n3. Testing expiration:");
        System.out.println("   Waiting 1.5 seconds...");
        Thread.sleep(1500);
        
        System.out.println("   session:abc123 = " + store.get("session:abc123").orElse("EXPIRED"));
        System.out.println("   cache:temp = " + store.get("cache:temp").orElse("EXPIRED"));
        System.out.println("   permanent = " + store.get("permanent").orElse("EXPIRED"));
        
        System.out.println("\n   Waiting another 1 second...");
        Thread.sleep(1000);
        
        System.out.println("   session:abc123 = " + store.get("session:abc123").orElse("EXPIRED"));
        System.out.println("   permanent = " + store.get("permanent").orElse("EXPIRED"));
        
        // Test expire method
        System.out.println("\n4. Setting expiration on existing key:");
        boolean expired = store.expire("permanent", 1, TimeUnit.SECONDS);
        System.out.println("   Set expiration on 'permanent': " + expired);
        System.out.println("   permanent TTL: " + store.getTTL("permanent") + "ms");
        
        Thread.sleep(1200);
        System.out.println("   permanent after expiration = " + store.get("permanent").orElse("EXPIRED"));
    }
    
    private static void demonstrateConcurrentAccess(KeyValueStore<String, String> store) throws InterruptedException {
        System.out.println("1. Concurrent read/write operations:");
        
        final int numThreads = 10;
        final int operationsPerThread = 100;
        final CountDownLatch latch = new CountDownLatch(numThreads);
        final AtomicInteger successfulOps = new AtomicInteger(0);
        
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        
        // Launch concurrent threads
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        String key = "thread:" + threadId + ":key:" + j;
                        String value = "value_" + threadId + "_" + j;
                        
                        // Put operation
                        store.put(key, value);
                        
                        // Get operation
                        Optional<String> retrieved = store.get(key);
                        if (retrieved.isPresent() && retrieved.get().equals(value)) {
                            successfulOps.incrementAndGet();
                        }
                        
                        // Occasionally remove
                        if (j % 10 == 0) {
                            store.remove(key);
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();
        
        System.out.println("   Threads: " + numThreads);
        System.out.println("   Operations per thread: " + operationsPerThread);
        System.out.println("   Successful operations: " + successfulOps.get());
        System.out.println("   Final store size: " + store.size());
    }
    
    private static void demonstrateMemoryManagement(KeyValueStore<String, String> store) throws InterruptedException {
        System.out.println("1. Adding data with expiration:");
        
        // Add many entries with short TTL
        for (int i = 0; i < 1000; i++) {
            store.put("temp:" + i, "data_" + i, 100, TimeUnit.MILLISECONDS);
        }
        
        System.out.println("   Added 1000 entries with 100ms TTL");
        System.out.println("   Store size: " + store.size());
        
        // Wait for expiration
        Thread.sleep(200);
        
        System.out.println("\n2. After expiration:");
        System.out.println("   Store size (with expired): " + store.size());
        
        // Manual cleanup
        int cleaned = store.cleanupExpired();
        System.out.println("   Cleaned up expired keys: " + cleaned);
        System.out.println("   Store size after cleanup: " + store.size());
        
        // Show statistics
        System.out.println("\n3. Store statistics:");
        StoreStats stats = store.getStats();
        System.out.println("   " + stats);
    }
    
    private static void demonstratePerformance(KeyValueStore<String, String> store) {
        System.out.println("1. Performance testing:");
        
        final int numOperations = 10000;
        
        // Test put performance
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < numOperations; i++) {
            store.put("perf:" + i, "value_" + i);
        }
        long putTime = System.currentTimeMillis() - startTime;
        
        // Test get performance
        startTime = System.currentTimeMillis();
        int hits = 0;
        for (int i = 0; i < numOperations; i++) {
            if (store.get("perf:" + i).isPresent()) {
                hits++;
            }
        }
        long getTime = System.currentTimeMillis() - startTime;
        
        // Test mixed operations
        startTime = System.currentTimeMillis();
        for (int i = 0; i < numOperations / 2; i++) {
            store.put("mixed:" + i, "value_" + i);
            store.get("mixed:" + (i / 2));
        }
        long mixedTime = System.currentTimeMillis() - startTime;
        
        System.out.println("   Operations: " + numOperations);
        System.out.println("   Put operations: " + putTime + "ms (" + 
                          (numOperations * 1000L / putTime) + " ops/sec)");
        System.out.println("   Get operations: " + getTime + "ms (" + 
                          (numOperations * 1000L / getTime) + " ops/sec)");
        System.out.println("   Mixed operations: " + mixedTime + "ms");
        System.out.println("   Hit rate: " + (hits * 100.0 / numOperations) + "%");
        
        // Final statistics
        System.out.println("\n2. Final statistics:");
        StoreStats finalStats = store.getStats();
        System.out.println("   " + finalStats);
    }
}