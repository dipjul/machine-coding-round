package com.machinecoding.ratelimiting;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.ArrayList;

/**
 * Comprehensive demonstration of Rate Limiter implementations.
 * Shows different algorithms, burst handling, concurrent access, and performance characteristics.
 */
public class RateLimiterDemo {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Rate Limiter Demo ===\n");
        
        // Demo 1: Token Bucket Algorithm
        System.out.println("=== Demo 1: Token Bucket Algorithm ===");
        demonstrateTokenBucket();
        
        // Demo 2: Algorithm Comparison
        System.out.println("\n=== Demo 2: Burst Traffic Handling ===");
        demonstrateBurstTraffic();
        
        // Demo 3: Multi-user Rate Limiting
        System.out.println("\n=== Demo 3: Multi-user Rate Limiting ===");
        demonstrateMultiUserLimiting();
        
        // Demo 4: Concurrent Access
        System.out.println("\n=== Demo 4: Concurrent Access ===");
        demonstrateConcurrentAccess();
        
        // Demo 5: Performance Testing
        System.out.println("\n=== Demo 5: Performance Testing ===");
        demonstratePerformance();
        
        System.out.println("\n=== Demo Complete ===");
    }
    
    private static void demonstrateTokenBucket() throws InterruptedException {
        System.out.println("1. Creating Token Bucket Rate Limiter (5 requests per second):");
        
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(5, 1, TimeUnit.SECONDS);
        System.out.println("   Config: " + limiter.getConfig());
        System.out.println("   Available permits: " + limiter.getAvailablePermits());
        
        System.out.println("\n2. Testing normal request rate:");
        for (int i = 1; i <= 5; i++) {
            boolean allowed = limiter.tryAcquire();
            System.out.println("   Request " + i + ": " + (allowed ? "ALLOWED" : "REJECTED") + 
                             " (available: " + limiter.getAvailablePermits() + ")");
        }
        
        System.out.println("\n3. Testing rate limit exceeded:");
        for (int i = 6; i <= 8; i++) {
            boolean allowed = limiter.tryAcquire();
            System.out.println("   Request " + i + ": " + (allowed ? "ALLOWED" : "REJECTED") + 
                             " (available: " + limiter.getAvailablePermits() + ")");
        }
        
        System.out.println("\n4. Waiting for token refill (1 second)...");
        Thread.sleep(1000);
        
        System.out.println("   Available permits after refill: " + limiter.getAvailablePermits());
        boolean allowed = limiter.tryAcquire();
        System.out.println("   New request: " + (allowed ? "ALLOWED" : "REJECTED"));
        
        System.out.println("\n5. Statistics:");
        System.out.println("   " + limiter.getStats());
    }
    
    private static void demonstrateBurstTraffic() throws InterruptedException {
        System.out.println("1. Testing burst traffic handling:");
        
        // Token bucket allows bursts up to capacity
        TokenBucketRateLimiter tokenBucket = new TokenBucketRateLimiter(10, 1, TimeUnit.SECONDS);
        
        System.out.println("   Token Bucket (10 requests/sec capacity):");
        
        // Simulate burst of 15 requests
        int allowedBurst = 0;
        for (int i = 1; i <= 15; i++) {
            if (tokenBucket.tryAcquire()) {
                allowedBurst++;
            }
        }
        System.out.println("   Burst test: " + allowedBurst + "/15 requests allowed");
        System.out.println("   " + tokenBucket.getStats());
        
        // Wait and test sustained rate
        System.out.println("\n2. Testing sustained rate after burst:");
        Thread.sleep(500); // Wait for partial refill
        
        int sustainedAllowed = 0;
        for (int i = 1; i <= 10; i++) {
            if (tokenBucket.tryAcquire()) {
                sustainedAllowed++;
            }
            Thread.sleep(100); // 10 requests per second
        }
        System.out.println("   Sustained rate: " + sustainedAllowed + "/10 requests allowed");
        System.out.println("   " + tokenBucket.getStats());
    }
    
    private static void demonstrateMultiUserLimiting() throws InterruptedException {
        System.out.println("1. Multi-user rate limiting (3 requests per user per second):");
        
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(3, 1, TimeUnit.SECONDS);
        
        String[] users = {"user1", "user2", "user3"};
        
        // Each user makes requests
        for (String user : users) {
            System.out.println("   Testing user: " + user);
            for (int i = 1; i <= 4; i++) {
                boolean allowed = limiter.tryAcquire(user);
                System.out.println("     Request " + i + ": " + (allowed ? "ALLOWED" : "REJECTED") +
                                 " (available: " + limiter.getAvailablePermits(user) + ")");
            }
        }
        
        System.out.println("\n2. Cross-user isolation test:");
        System.out.println("   user1 available: " + limiter.getAvailablePermits("user1"));
        System.out.println("   user2 available: " + limiter.getAvailablePermits("user2"));
        System.out.println("   user3 available: " + limiter.getAvailablePermits("user3"));
        
        System.out.println("\n3. Multi-user statistics:");
        System.out.println("   " + limiter.getStats());
    }
    
    private static void demonstrateConcurrentAccess() throws InterruptedException {
        System.out.println("1. Concurrent access test (10 threads, 100 requests each):");
        
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(50, 1, TimeUnit.SECONDS);
        
        final int numThreads = 10;
        final int requestsPerThread = 100;
        final CountDownLatch latch = new CountDownLatch(numThreads);
        final AtomicInteger totalAllowed = new AtomicInteger(0);
        final AtomicInteger totalRejected = new AtomicInteger(0);
        
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    int allowed = 0;
                    int rejected = 0;
                    
                    for (int j = 0; j < requestsPerThread; j++) {
                        if (limiter.tryAcquire("thread-" + threadId)) {
                            allowed++;
                        } else {
                            rejected++;
                        }
                        
                        // Small delay to simulate realistic request pattern
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                    
                    totalAllowed.addAndGet(allowed);
                    totalRejected.addAndGet(rejected);
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        
        System.out.println("   Total threads: " + numThreads);
        System.out.println("   Requests per thread: " + requestsPerThread);
        System.out.println("   Total allowed: " + totalAllowed.get());
        System.out.println("   Total rejected: " + totalRejected.get());
        System.out.println("   Success rate: " + (totalAllowed.get() * 100.0 / (numThreads * requestsPerThread)) + "%");
        System.out.println("   " + limiter.getStats());
    }
    
    private static void demonstratePerformance() {
        System.out.println("1. Performance testing different rate limits:");
        
        int[] rateLimits = {100, 1000, 10000};
        int testRequests = 50000;
        
        for (int rateLimit : rateLimits) {
            TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(rateLimit, 1, TimeUnit.SECONDS);
            
            long startTime = System.currentTimeMillis();
            int allowed = 0;
            
            for (int i = 0; i < testRequests; i++) {
                if (limiter.tryAcquire()) {
                    allowed++;
                }
            }
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            System.out.println(String.format(
                "   Rate limit %d: %dms, %d ops/sec, %d/%d allowed (%.1f%%)",
                rateLimit, duration, testRequests * 1000L / duration,
                allowed, testRequests, allowed * 100.0 / testRequests
            ));
        }
        
        System.out.println("\n2. Memory usage with many identifiers:");
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(10, 1, TimeUnit.SECONDS);
        
        // Create rate limiters for many users
        for (int i = 0; i < 10000; i++) {
            limiter.tryAcquire("user-" + i);
        }
        
        System.out.println("   Created rate limiters for 10,000 users");
        System.out.println("   " + limiter.getStats());
        
        // Test access patterns
        long startTime = System.currentTimeMillis();
        int requests = 0;
        for (int i = 0; i < 1000; i++) {
            limiter.tryAcquire("user-" + (i % 100)); // Access pattern with locality
            requests++;
        }
        long endTime = System.currentTimeMillis();
        
        System.out.println("   1000 requests with locality: " + (endTime - startTime) + "ms");
        System.out.println("   Final stats: " + limiter.getStats());
    }
}