package com.machinecoding.caching.store;

/**
 * Statistics for key-value store operations.
 */
public class StoreStats {
    private final long totalGets;
    private final long totalPuts;
    private final long totalRemoves;
    private final long hits;
    private final long misses;
    private final long expiredKeys;
    private final int currentSize;
    private final long memoryUsage;
    
    public StoreStats(long totalGets, long totalPuts, long totalRemoves, 
                     long hits, long misses, long expiredKeys, 
                     int currentSize, long memoryUsage) {
        this.totalGets = totalGets;
        this.totalPuts = totalPuts;
        this.totalRemoves = totalRemoves;
        this.hits = hits;
        this.misses = misses;
        this.expiredKeys = expiredKeys;
        this.currentSize = currentSize;
        this.memoryUsage = memoryUsage;
    }
    
    public long getTotalGets() { return totalGets; }
    public long getTotalPuts() { return totalPuts; }
    public long getTotalRemoves() { return totalRemoves; }
    public long getHits() { return hits; }
    public long getMisses() { return misses; }
    public long getExpiredKeys() { return expiredKeys; }
    public int getCurrentSize() { return currentSize; }
    public long getMemoryUsage() { return memoryUsage; }
    
    public double getHitRate() {
        long total = hits + misses;
        return total == 0 ? 0.0 : (double) hits / total * 100;
    }
    
    @Override
    public String toString() {
        return String.format(
            "StoreStats{gets=%d, puts=%d, removes=%d, hits=%d, misses=%d, " +
            "expired=%d, size=%d, memory=%dB, hitRate=%.1f%%}",
            totalGets, totalPuts, totalRemoves, hits, misses, 
            expiredKeys, currentSize, memoryUsage, getHitRate()
        );
    }
}