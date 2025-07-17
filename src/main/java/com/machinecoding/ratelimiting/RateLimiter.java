package com.machinecoding.ratelimiting;

/**
 * Core interface for rate limiting implementations.
 * Provides methods to check if requests are allowed based on configured limits.
 */
public interface RateLimiter {
    
    /**
     * Attempts to acquire a permit for a single request.
     * 
     * @return true if the request is allowed, false if rate limit is exceeded
     */
    boolean tryAcquire();
    
    /**
     * Attempts to acquire permits for multiple requests.
     * 
     * @param permits number of permits to acquire
     * @return true if all permits are acquired, false otherwise
     */
    boolean tryAcquire(int permits);
    
    /**
     * Attempts to acquire a permit for a specific identifier (e.g., user ID, IP address).
     * 
     * @param identifier the identifier to rate limit
     * @return true if the request is allowed, false if rate limit is exceeded
     */
    boolean tryAcquire(String identifier);
    
    /**
     * Attempts to acquire permits for a specific identifier.
     * 
     * @param identifier the identifier to rate limit
     * @param permits number of permits to acquire
     * @return true if all permits are acquired, false otherwise
     */
    boolean tryAcquire(String identifier, int permits);
    
    /**
     * Gets the current number of available permits.
     * 
     * @return number of available permits
     */
    long getAvailablePermits();
    
    /**
     * Gets the current number of available permits for a specific identifier.
     * 
     * @param identifier the identifier to check
     * @return number of available permits for the identifier
     */
    long getAvailablePermits(String identifier);
    
    /**
     * Gets the rate limit configuration.
     * 
     * @return rate limit configuration
     */
    RateLimitConfig getConfig();
    
    /**
     * Gets statistics about rate limiting.
     * 
     * @return rate limiting statistics
     */
    RateLimitStats getStats();
    
    /**
     * Resets the rate limiter state.
     */
    void reset();
    
    /**
     * Resets the rate limiter state for a specific identifier.
     * 
     * @param identifier the identifier to reset
     */
    void reset(String identifier);
}