package com.machinecoding.ratelimiting;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Token Bucket Rate Limiter implementation.
 * 
 * The token bucket algorithm maintains a bucket of tokens that are refilled at a constant rate.
 * Each request consumes one or more tokens. If no tokens are available, the request is rejected.
 * 
 * Characteristics:
 * - Allows burst traffic up to bucket capacity
 * - Smooth rate limiting over time
 * - Memory efficient
 * - Good for APIs that need to handle occasional spikes
 */
public class TokenBucketRateLimiter implements RateLimiter {
    
    private final RateLimitConfig config;
    private final ConcurrentHashMap<String, TokenBucket> buckets;
    private final TokenBucket globalBucket;
    private final AtomicLong totalRequests;
    private final AtomicLong allowedRequests;
    private final AtomicLong rejectedRequests;
    
    public TokenBucketRateLimiter(long maxRequests, long timeWindow, TimeUnit timeUnit) {
        this.config = new RateLimitConfig(maxRequests, timeWindow, timeUnit, "TokenBucket");
        this.buckets = new ConcurrentHashMap<>();
        this.globalBucket = new TokenBucket(maxRequests, calculateRefillRate());
        this.totalRequests = new AtomicLong(0);
        this.allowedRequests = new AtomicLong(0);
        this.rejectedRequests = new AtomicLong(0);
    }
    
    @Override
    public boolean tryAcquire() {
        return tryAcquire(1);
    }
    
    @Override
    public boolean tryAcquire(int permits) {
        totalRequests.incrementAndGet();
        
        if (globalBucket.tryConsume(permits)) {
            allowedRequests.incrementAndGet();
            return true;
        } else {
            rejectedRequests.incrementAndGet();
            return false;
        }
    }
    
    @Override
    public boolean tryAcquire(String identifier) {
        return tryAcquire(identifier, 1);
    }
    
    @Override
    public boolean tryAcquire(String identifier, int permits) {
        totalRequests.incrementAndGet();
        
        TokenBucket bucket = buckets.computeIfAbsent(identifier, 
            k -> new TokenBucket(config.getMaxRequests(), calculateRefillRate()));
        
        if (bucket.tryConsume(permits)) {
            allowedRequests.incrementAndGet();
            return true;
        } else {
            rejectedRequests.incrementAndGet();
            return false;
        }
    }
    
    @Override
    public long getAvailablePermits() {
        return globalBucket.getAvailableTokens();
    }
    
    @Override
    public long getAvailablePermits(String identifier) {
        TokenBucket bucket = buckets.get(identifier);
        return bucket != null ? bucket.getAvailableTokens() : config.getMaxRequests();
    }
    
    @Override
    public RateLimitConfig getConfig() {
        return config;
    }
    
    @Override
    public RateLimitStats getStats() {
        return new RateLimitStats(
            totalRequests.get(),
            allowedRequests.get(),
            rejectedRequests.get(),
            buckets.size(),
            "TokenBucket"
        );
    }
    
    @Override
    public void reset() {
        globalBucket.reset();
        totalRequests.set(0);
        allowedRequests.set(0);
        rejectedRequests.set(0);
    }
    
    @Override
    public void reset(String identifier) {
        TokenBucket bucket = buckets.get(identifier);
        if (bucket != null) {
            bucket.reset();
        }
    }
    
    private double calculateRefillRate() {
        // Tokens per millisecond
        return (double) config.getMaxRequests() / config.getTimeWindowMillis();
    }
    
    /**
     * Internal token bucket implementation.
     */
    private static class TokenBucket {
        private final long capacity;
        private final double refillRate;
        private final ReentrantLock lock;
        private double tokens;
        private long lastRefillTime;
        
        public TokenBucket(long capacity, double refillRate) {
            this.capacity = capacity;
            this.refillRate = refillRate;
            this.lock = new ReentrantLock();
            this.tokens = capacity;
            this.lastRefillTime = System.currentTimeMillis();
        }
        
        public boolean tryConsume(int tokensToConsume) {
            lock.lock();
            try {
                refill();
                
                if (tokens >= tokensToConsume) {
                    tokens -= tokensToConsume;
                    return true;
                }
                return false;
            } finally {
                lock.unlock();
            }
        }
        
        public long getAvailableTokens() {
            lock.lock();
            try {
                refill();
                return (long) tokens;
            } finally {
                lock.unlock();
            }
        }
        
        public void reset() {
            lock.lock();
            try {
                tokens = capacity;
                lastRefillTime = System.currentTimeMillis();
            } finally {
                lock.unlock();
            }
        }
        
        private void refill() {
            long now = System.currentTimeMillis();
            long timePassed = now - lastRefillTime;
            
            if (timePassed > 0) {
                double tokensToAdd = timePassed * refillRate;
                tokens = Math.min(capacity, tokens + tokensToAdd);
                lastRefillTime = now;
            }
        }
    }
}