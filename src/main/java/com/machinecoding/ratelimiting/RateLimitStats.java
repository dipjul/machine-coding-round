package com.machinecoding.ratelimiting;

/**
 * Statistics for rate limiting operations.
 */
public class RateLimitStats {
    private final long totalRequests;
    private final long allowedRequests;
    private final long rejectedRequests;
    private final long activeIdentifiers;
    private final String algorithm;
    
    public RateLimitStats(long totalRequests, long allowedRequests, long rejectedRequests,
                         long activeIdentifiers, String algorithm) {
        this.totalRequests = totalRequests;
        this.allowedRequests = allowedRequests;
        this.rejectedRequests = rejectedRequests;
        this.activeIdentifiers = activeIdentifiers;
        this.algorithm = algorithm;
    }
    
    public long getTotalRequests() { return totalRequests; }
    public long getAllowedRequests() { return allowedRequests; }
    public long getRejectedRequests() { return rejectedRequests; }
    public long getActiveIdentifiers() { return activeIdentifiers; }
    public String getAlgorithm() { return algorithm; }
    
    public double getAllowRate() {
        return totalRequests == 0 ? 0.0 : (double) allowedRequests / totalRequests * 100;
    }
    
    public double getRejectRate() {
        return totalRequests == 0 ? 0.0 : (double) rejectedRequests / totalRequests * 100;
    }
    
    @Override
    public String toString() {
        return String.format(
            "%s Stats{total=%d, allowed=%d, rejected=%d, identifiers=%d, allowRate=%.1f%%, rejectRate=%.1f%%}",
            algorithm, totalRequests, allowedRequests, rejectedRequests, activeIdentifiers,
            getAllowRate(), getRejectRate()
        );
    }
}