package com.machinecoding.performance;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import com.machinecoding.caching.lru.LRUCache;
import com.machinecoding.caching.store.InMemoryKeyValueStore;
import com.machinecoding.messagequeues.queue.CustomMessageQueue;
import com.machinecoding.ratelimiting.TokenBucketRateLimiter;
import com.machinecoding.search.autocomplete.InMemoryAutocompleteService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Performance benchmarking suite for machine coding systems.
 * Measures throughput, latency, and scalability characteristics.
 */
public class PerformanceBenchmark {
    
    private static final int WARMUP_ITERATIONS = 1000;
    private static final int BENCHMARK_ITERATIONS = 10000;
    private static final int CONCURRENT_THREADS = 20;
    
    private Random random;
    
    @BeforeEach
    void setUp() {
        random = new Random(42); // Fixed seed for reproducible results
    }
    
    @Test
    @DisplayName("LRU Cache Performance Benchmark")
    void benchmarkLRUCache() {
        System.out.println("\n=== LRU Cache Performance Benchmark ===");
        
        // Test different cache sizes
        int[] cacheSizes = {100, 1000, 10000};
        
        for (int cacheSize : cacheSizes) {
            LRUCache<String, String> cache = new LRUCache<>(cacheSize);
            
            // Warmup
            warmupCache(cache);
            
            // Benchmark single-threaded performance
            BenchmarkResult singleThreaded = benchmarkCacheSingleThreaded(cache, cacheSize);
            
            // Benchmark multi-threaded performance
            BenchmarkResult multiThreaded = benchmarkCacheMultiThreaded(cache, cacheSize);
            
            System.out.println("Cache Size: " + cacheSize);
            System.out.println("  Single-threaded: " + singleThreaded);
            System.out.println("  Multi-threaded:  " + multiThreaded);
            System.out.println();
        }
    }
    
    @Test
    @DisplayName("Message Queue Throughput Benchmark")
    void benchmarkMessageQueueThroughput() {
        System.out.println("\n=== Message Queue Throughput Benchmark ===");
        
        int[] queueSizes = {1000, 10000, 100000};
        
        for (int queueSize : queueSizes) {
            CustomMessageQueue<String> queue = new CustomMessageQueue<>(queueSize);
            
            BenchmarkResult result = benchmarkMessageQueueThroughput(queue, queueSize);
            
            System.out.println("Queue Size: " + queueSize);
            System.out.println("  " + result);
            System.out.println();
        }
    }
    
    @Test
    @DisplayName("Rate Limiter Performance Benchmark")
    void benchmarkRateLimiter() {
        System.out.println("\n=== Rate Limiter Performance Benchmark ===");
        
        // Test different rate limits
        int[] rateLimits = {100, 1000, 10000};
        
        for (int rateLimit : rateLimits) {
            TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(rateLimit, rateLimit / 10);
            
            BenchmarkResult result = benchmarkRateLimiter(rateLimiter, rateLimit);
            
            System.out.println("Rate Limit: " + rateLimit + " requests");
            System.out.println("  " + result);
            System.out.println();
        }
    }
    
    @Test
    @DisplayName("Search Autocomplete Performance Benchmark")
    void benchmarkSearchAutocomplete() {
        System.out.println("\n=== Search Autocomplete Performance Benchmark ===");
        
        InMemoryAutocompleteService service = new InMemoryAutocompleteService();
        
        // Build index with varying sizes
        int[] indexSizes = {1000, 10000, 100000};
        
        for (int indexSize : indexSizes) {
            // Clear and rebuild index
            service = new InMemoryAutocompleteService();
            buildSearchIndex(service, indexSize);
            
            BenchmarkResult result = benchmarkSearchPerformance(service, indexSize);
            
            System.out.println("Index Size: " + indexSize + " terms");
            System.out.println("  " + result);
            System.out.println();
        }
    }
    
    @Test
    @DisplayName("Memory Usage Analysis")
    void analyzeMemoryUsage() {
        System.out.println("\n=== Memory Usage Analysis ===");
        
        Runtime runtime = Runtime.getRuntime();
        
        // Baseline memory
        System.gc();
        long baselineMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // Test LRU Cache memory usage
        LRUCache<String, String> cache = new LRUCache<>(10000);
        for (int i = 0; i < 10000; i++) {
            cache.put("key" + i, "value" + i + "_" + generateRandomString(50));
        }
        
        System.gc();
        long cacheMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // Test Message Queue memory usage
        CustomMessageQueue<String> queue = new CustomMessageQueue<>(10000);
        for (int i = 0; i < 10000; i++) {
            try {
                queue.enqueue("message" + i + "_" + generateRandomString(50));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        System.gc();
        long queueMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // Test Search Index memory usage
        InMemoryAutocompleteService searchService = new InMemoryAutocompleteService();
        buildSearchIndex(searchService, 10000);
        
        System.gc();
        long searchMemory = runtime.totalMemory() - runtime.freeMemory();
        
        System.out.println("Memory Usage (MB):");
        System.out.println("  Baseline:     " + String.format("%.2f", baselineMemory / 1024.0 / 1024.0));
        System.out.println("  LRU Cache:    " + String.format("%.2f", (cacheMemory - baselineMemory) / 1024.0 / 1024.0));
        System.out.println("  Message Queue:" + String.format("%.2f", (queueMemory - cacheMemory) / 1024.0 / 1024.0));
        System.out.println("  Search Index: " + String.format("%.2f", (searchMemory - queueMemory) / 1024.0 / 1024.0));
        System.out.println("  Total:        " + String.format("%.2f", searchMemory / 1024.0 / 1024.0));
    }
    
    // Helper methods
    
    private void warmupCache(LRUCache<String, String> cache) {
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            cache.put("warmup" + i, "value" + i);
            cache.get("warmup" + (i / 2));
        }
    }
    
    private BenchmarkResult benchmarkCacheSingleThreaded(LRUCache<String, String> cache, int cacheSize) {
        long startTime = System.nanoTime();
        
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            String key = "key" + (i % (cacheSize * 2)); // Create some cache misses
            
            if (i % 3 == 0) {
                cache.put(key, "value" + i);
            } else {
                cache.get(key);
            }
        }
        
        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1_000_000.0; // Convert to milliseconds
        double throughput = BENCHMARK_ITERATIONS / (duration / 1000.0); // Operations per second
        
        return new BenchmarkResult("Cache Operations", BENCHMARK_ITERATIONS, duration, throughput, 1);
    }
    
    private BenchmarkResult benchmarkCacheMultiThreaded(LRUCache<String, String> cache, int cacheSize) {
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREADS);
        CountDownLatch latch = new CountDownLatch(CONCURRENT_THREADS);
        
        long startTime = System.nanoTime();
        
        for (int t = 0; t < CONCURRENT_THREADS; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < BENCHMARK_ITERATIONS / CONCURRENT_THREADS; i++) {
                        String key = "key" + threadId + "_" + (i % (cacheSize / CONCURRENT_THREADS));
                        
                        if (i % 3 == 0) {
                            cache.put(key, "value" + threadId + "_" + i);
                        } else {
                            cache.get(key);
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1_000_000.0;
        double throughput = BENCHMARK_ITERATIONS / (duration / 1000.0);
        
        executor.shutdown();
        return new BenchmarkResult("Cache Operations", BENCHMARK_ITERATIONS, duration, throughput, CONCURRENT_THREADS);
    }
    
    private BenchmarkResult benchmarkMessageQueueThroughput(CustomMessageQueue<String> queue, int queueSize) {
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREADS);
        CountDownLatch producerLatch = new CountDownLatch(CONCURRENT_THREADS / 2);
        CountDownLatch consumerLatch = new CountDownLatch(CONCURRENT_THREADS / 2);
        
        long startTime = System.nanoTime();
        
        // Start producers
        for (int i = 0; i < CONCURRENT_THREADS / 2; i++) {
            final int producerId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < BENCHMARK_ITERATIONS / (CONCURRENT_THREADS / 2); j++) {
                        queue.enqueue("message_" + producerId + "_" + j);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    producerLatch.countDown();
                }
            });
        }
        
        // Start consumers
        for (int i = 0; i < CONCURRENT_THREADS / 2; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < BENCHMARK_ITERATIONS / (CONCURRENT_THREADS / 2); j++) {
                        queue.dequeue();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    consumerLatch.countDown();
                }
            });
        }
        
        try {
            producerLatch.await();
            consumerLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1_000_000.0;
        double throughput = BENCHMARK_ITERATIONS / (duration / 1000.0);
        
        executor.shutdown();
        return new BenchmarkResult("Message Queue Operations", BENCHMARK_ITERATIONS, duration, throughput, CONCURRENT_THREADS);
    }
    
    private BenchmarkResult benchmarkRateLimiter(TokenBucketRateLimiter rateLimiter, int rateLimit) {
        long startTime = System.nanoTime();
        int allowedRequests = 0;
        
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            if (rateLimiter.allowRequest("user" + (i % 100))) {
                allowedRequests++;
            }
        }
        
        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1_000_000.0;
        double throughput = BENCHMARK_ITERATIONS / (duration / 1000.0);
        
        System.out.println("    Allowed: " + allowedRequests + "/" + BENCHMARK_ITERATIONS + 
                         " (" + String.format("%.2f%%", (allowedRequests * 100.0) / BENCHMARK_ITERATIONS) + ")");
        
        return new BenchmarkResult("Rate Limit Checks", BENCHMARK_ITERATIONS, duration, throughput, 1);
    }
    
    private BenchmarkResult benchmarkSearchPerformance(InMemoryAutocompleteService service, int indexSize) {
        String[] prefixes = generateSearchPrefixes(100);
        
        long startTime = System.nanoTime();
        
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            String prefix = prefixes[i % prefixes.length];
            service.getSuggestions(prefix, 10);
        }
        
        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1_000_000.0;
        double throughput = BENCHMARK_ITERATIONS / (duration / 1000.0);
        
        return new BenchmarkResult("Search Queries", BENCHMARK_ITERATIONS, duration, throughput, 1);
    }
    
    private void buildSearchIndex(InMemoryAutocompleteService service, int size) {
        for (int i = 0; i < size; i++) {
            String term = generateRandomString(random.nextInt(10) + 3); // 3-12 character terms
            service.addTerm(term, random.nextDouble());
        }
    }
    
    private String[] generateSearchPrefixes(int count) {
        String[] prefixes = new String[count];
        for (int i = 0; i < count; i++) {
            prefixes[i] = generateRandomString(random.nextInt(3) + 1); // 1-3 character prefixes
        }
        return prefixes;
    }
    
    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append((char) ('a' + random.nextInt(26)));
        }
        return sb.toString();
    }
    
    // Benchmark result class
    private static class BenchmarkResult {
        private final String operation;
        private final int iterations;
        private final double durationMs;
        private final double throughputOps;
        private final int threads;
        
        public BenchmarkResult(String operation, int iterations, double durationMs, double throughputOps, int threads) {
            this.operation = operation;
            this.iterations = iterations;
            this.durationMs = durationMs;
            this.throughputOps = throughputOps;
            this.threads = threads;
        }
        
        @Override
        public String toString() {
            return String.format("%s: %d ops in %.2f ms (%.0f ops/sec, %d threads)", 
                               operation, iterations, durationMs, throughputOps, threads);
        }
    }
}