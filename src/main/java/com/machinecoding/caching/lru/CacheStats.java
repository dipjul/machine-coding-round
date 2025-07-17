package com.machinecoding.caching.lru;

/**
 * Statistics for LRU cache operations.
 */
public class CacheStats {
    private final long totalGets;
    private final long totalPuts;
    private final long totalRemoves;
    private final long hits;
    private final long misses;
    private final long evictions;
    private final int currentSize;
    private final int capacity;
    
    public CacheStats(long totalGets, long totalPuts, long totalRemoves,
                     long hits, long misses, long evictions,
                     int currentSize, int capacity) {
        this.totalGets = totalGets;
        this.totalPuts = totalPuts;
        this.totalRemoves = totalRemoves;
        this.hits = hits;
        this.misses = misses;
        this.evictions = evictions;
        this.currentSize = currentSize;
        this.capacity = capacity;
    }
    
    public long getTotalGets() { return totalGets; }
    public long getTotalPuts() { return totalPuts; }
    public long getTotalRemoves() { return totalRemoves; }
    public long getHits() { return hits; }
    public long getMisses() { return misses; }
    public long getEvictions() { return evictions; }
    public int getCurrentSize() { return currentSize; }
    public int getCapacity() { return capacity; }
    
    public double getHitRate() {
        long total = hits + misses;
        return total == 0 ? 0.0 : (double) hits / total * 100;
    }
    
    public double getLoadFactor() {
        return capacity == 0 ? 0.0 : (double) currentSize / capacity * 100;
    }
    
    @Override
    public String toString() {
        return String.format(
            "CacheStats{gets=%d, puts=%d, removes=%d, hits=%d, misses=%d, " +
            "evictions=%d, size=%d/%d, hitRate=%.1f%%, load=%.1f%%}",
            totalGets, totalPuts, totalRemoves, hits, misses, evictions,
            currentSize, capacity, getHitRate(), getLoadFactor()
        );
    }
}