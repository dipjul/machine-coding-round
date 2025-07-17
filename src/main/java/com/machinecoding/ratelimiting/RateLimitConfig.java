package com.machinecoding.ratelimiting;

import java.util.concurrent.TimeUnit;

/**
 * Configuration for rate limiting.
 */
public class RateLimitConfig {
    private final long maxRequests;
    private final long timeWindow;
    private final TimeUnit timeUnit;
    private final String algorithm;
    
    public RateLimitConfig(long maxRequests, long timeWindow, TimeUnit timeUnit, String algorithm) {
        if (maxRequests <= 0) {
            throw new IllegalArgumentException("Max requests must be positive");
        }
        if (timeWindow <= 0) {
            throw new IllegalArgumentException("Time window must be positive");
        }
        
        this.maxRequests = maxRequests;
        this.timeWindow = timeWindow;
        this.timeUnit = timeUnit;
        this.algorithm = algorithm;
    }
    
    public long getMaxRequests() { return maxRequests; }
    public long getTimeWindow() { return timeWindow; }
    public TimeUnit getTimeUnit() { return timeUnit; }
    public String getAlgorithm() { return algorithm; }
    
    public long getTimeWindowMillis() {
        return timeUnit.toMillis(timeWindow);
    }
    
    public double getRequestsPerSecond() {
        return (double) maxRequests / timeUnit.toSeconds(timeWindow);
    }
    
    @Override
    public String toString() {
        return String.format("%s: %d requests per %d %s (%.2f req/sec)", 
                           algorithm, maxRequests, timeWindow, timeUnit.toString().toLowerCase(),
                           getRequestsPerSecond());
    }
}