package com.machinecoding.caching.lru;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * Comprehensive demonstration of LRU Cache implementations.
 * Shows basic operations, eviction behavior, access patterns, and performance characteristics.
 */
public class LRUCacheDemo {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== LRU Cache Demo ===\n");
        
        // Demo 1: Basic LRU behavior
        System.out.println("=== Demo 1: Basic LRU Behavior ===");
        demonstrateBasicLRU();
        
        // Demo 2: Eviction patterns
        System.out.println("\n=== Demo 2: Eviction Patterns ===");
        demonstrateEvictionPatterns();
        
        // Demo 3: Access patterns and hit rates
        System.out.println("\n=== Demo 3: Access Patterns & Hit Rates ===");
        demonstrateAccessPatterns();
        
        // Demo 4: Performance testing
        System.out.println("\n=== Demo 4: Performance Testing ===");
        demonstratePerformance();
        
        // Demo 5: Concurrent access (if we had a thread-safe version)
        System.out.println("\n=== Demo 5: Cache Comparison ===");
        compareCacheSizes();
        
        System.out.println("\n=== Demo Complete ===");
    }
    
    private static void demonstrateBasicLRU() {
        System.out.println("1. Creating LRU cache with capacity 3:");
        BasicLRUCache<String, Integer> cache = new BasicLRUCache<>(3);
        
        // Add items
        System.out.println("   Adding items:");
        cache.put("A", 1);
        System.out.println("   Put A=1, cache: " + cache.toOrderedString());
        
        cache.put("B", 2);
        System.out.println("   Put B=2, cache: " + cache.toOrderedString());
        
        cache.put("C", 3);
        System.out.println("   Put C=3, cache: " + cache.toOrderedString());
        
        // Access existing item
        System.out.println("\n2. Accessing existing items:");
        Integer value = cache.get("A");
        System.out.println("   Get A=" + value + ", cache: " + cache.toOrderedString());
        
        // Add item that causes eviction
        System.out.println("\n3. Adding item that causes eviction:");
        cache.put("D", 4);
        System.out.println("   Put D=4, cache: " + cache.toOrderedString());
        System.out.println("   (B was evicted as it was least recently used)");
        
        // Try to access evicted item
        System.out.println("\n4. Accessing evicted item:");
        Integer evicted = cache.get("B");
        System.out.println("   Get B=" + evicted + " (null means evicted)");
        
        // Update existing item
        System.out.println("\n5. Updating existing item:");
        cache.put("A", 10);
        System.out.println("   Put A=10, cache: " + cache.toOrderedString());
        
        System.out.println("\n6. Cache statistics:");
        System.out.println("   " + cache.getStats());
    }
    
    private static void demonstrateEvictionPatterns() {
        System.out.println("1. Testing different eviction scenarios:");
        
        BasicLRUCache<Integer, String> cache = new BasicLRUCache<>(4);
        
        // Fill cache
        for (int i = 1; i <= 4; i++) {
            cache.put(i, "value" + i);
        }
        System.out.println("   Initial cache: " + cache.toOrderedString());
        
        // Access pattern that changes LRU order
        cache.get(1); // 1 becomes most recent
        cache.get(3); // 3 becomes most recent
        System.out.println("   After accessing 1,3: " + cache.toOrderedString());
        
        // Add new item - should evict 2 (least recently used)
        cache.put(5, "value5");
        System.out.println("   After adding 5: " + cache.toOrderedString());
        System.out.println("   (2 was evicted)");
        
        // Sequential access pattern
        System.out.println("\n2. Sequential access pattern:");
        BasicLRUCache<Integer, String> seqCache = new BasicLRUCache<>(3);
        
        for (int i = 1; i <= 6; i++) {
            seqCache.put(i, "seq" + i);
            System.out.println("   Put " + i + ": " + seqCache.toOrderedString());
        }
        
        System.out.println("   Final stats: " + seqCache.getStats());
    }
    
    private static void demonstrateAccessPatterns() {
        System.out.println("1. Random access pattern:");
        
        BasicLRUCache<Integer, String> cache = new BasicLRUCache<>(10);
        Random random = new Random(42); // Fixed seed for reproducible results
        
        // Fill cache
        for (int i = 0; i < 10; i++) {
            cache.put(i, "value" + i);
        }
        
        // Random access pattern
        for (int i = 0; i < 50; i++) {
            int key = random.nextInt(15); // 0-14, some keys not in cache
            String value = cache.get(key);
            if (i < 10) { // Show first 10 accesses
                System.out.println("   Access " + key + ": " + (value != null ? "HIT" : "MISS"));
            }
        }
        
        System.out.println("   Random access stats: " + cache.getStats());
        
        // Locality of reference pattern
        System.out.println("\n2. Locality of reference pattern:");
        BasicLRUCache<Integer, String> localCache = new BasicLRUCache<>(5);
        
        // Fill cache
        for (int i = 0; i < 5; i++) {
            localCache.put(i, "local" + i);
        }
        
        // Access with locality (80% chance to access recently used keys)
        for (int i = 0; i < 30; i++) {
            int key;
            if (random.nextDouble() < 0.8) {
                // Access one of the last 3 keys added
                key = Math.max(0, i - 3 + random.nextInt(4));
            } else {
                // Access random key
                key = random.nextInt(10);
            }
            
            String value = localCache.get(key);
            if (value == null) {
                localCache.put(key, "local" + key);
            }
        }
        
        System.out.println("   Locality pattern stats: " + localCache.getStats());
    }
    
    private static void demonstratePerformance() {
        System.out.println("1. Performance testing with different cache sizes:");
        
        int[] cacheSizes = {100, 1000, 10000};
        int operations = 50000;
        
        for (int cacheSize : cacheSizes) {
            BasicLRUCache<Integer, String> cache = new BasicLRUCache<>(cacheSize);
            Random random = new Random(42);
            
            long startTime = System.currentTimeMillis();
            
            // Mixed workload: 70% gets, 30% puts
            for (int i = 0; i < operations; i++) {
                if (random.nextDouble() < 0.7) {
                    // Get operation
                    int key = random.nextInt(cacheSize * 2);
                    cache.get(key);
                } else {
                    // Put operation
                    int key = random.nextInt(cacheSize * 2);
                    cache.put(key, "value" + key);
                }
            }
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            CacheStats stats = cache.getStats();
            System.out.println(String.format(
                "   Cache size %d: %dms, %d ops/sec, hit rate %.1f%%",
                cacheSize, duration, operations * 1000L / duration, stats.getHitRate()
            ));
        }
        
        // Memory usage estimation
        System.out.println("\n2. Memory usage patterns:");
        for (int size : new int[]{1000, 10000, 100000}) {
            BasicLRUCache<String, String> cache = new BasicLRUCache<>(size);
            
            // Fill cache with data
            for (int i = 0; i < size; i++) {
                cache.put("key" + i, "value" + i + "_with_some_extra_data");
            }
            
            // Rough memory estimation
            long estimatedMemory = size * (50 + 20 + 40); // key + value + overhead
            System.out.println(String.format(
                "   Size %d: ~%d KB estimated memory",
                size, estimatedMemory / 1024
            ));
        }
    }
    
    private static void compareCacheSizes() {
        System.out.println("1. Comparing different cache sizes with same workload:");
        
        int[] sizes = {5, 10, 20, 50};
        int totalOperations = 100;
        Random random = new Random(42);
        
        for (int size : sizes) {
            BasicLRUCache<Integer, String> cache = new BasicLRUCache<>(size);
            
            // Workload: access keys 0-99 randomly
            for (int i = 0; i < totalOperations; i++) {
                int key = random.nextInt(100);
                String value = cache.get(key);
                if (value == null) {
                    cache.put(key, "value" + key);
                }
            }
            
            CacheStats stats = cache.getStats();
            System.out.println(String.format(
                "   Size %2d: hit rate %5.1f%%, evictions %2d, load %5.1f%%",
                size, stats.getHitRate(), stats.getEvictions(), stats.getLoadFactor()
            ));
        }
        
        System.out.println("\n2. Cache behavior analysis:");
        System.out.println("   - Smaller caches have lower hit rates due to frequent evictions");
        System.out.println("   - Larger caches have better hit rates but use more memory");
        System.out.println("   - Optimal cache size depends on access patterns and memory constraints");
    }
}