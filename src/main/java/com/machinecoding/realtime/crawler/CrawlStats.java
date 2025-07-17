package com.machinecoding.realtime.crawler;

/**
 * Statistics for web crawler operations.
 */
public class CrawlStats {
    private final int totalRequests;
    private final int completedRequests;
    private final int failedRequests;
    private final int pendingRequests;
    private final int skippedRequests;
    private final long totalCrawlTime;
    private final double averageCrawlTime;
    private final int uniqueDomains;
    private final int totalExtractedUrls;
    private final long totalContentSize;
    private final int activeThreads;
    
    public CrawlStats(int totalRequests, int completedRequests, int failedRequests,
                     int pendingRequests, int skippedRequests, long totalCrawlTime,
                     double averageCrawlTime, int uniqueDomains, int totalExtractedUrls,
                     long totalContentSize, int activeThreads) {
        this.totalRequests = totalRequests;
        this.completedRequests = completedRequests;
        this.failedRequests = failedRequests;
        this.pendingRequests = pendingRequests;
        this.skippedRequests = skippedRequests;
        this.totalCrawlTime = totalCrawlTime;
        this.averageCrawlTime = averageCrawlTime;
        this.uniqueDomains = uniqueDomains;
        this.totalExtractedUrls = totalExtractedUrls;
        this.totalContentSize = totalContentSize;
        this.activeThreads = activeThreads;
    }
    
    // Getters
    public int getTotalRequests() { return totalRequests; }
    public int getCompletedRequests() { return completedRequests; }
    public int getFailedRequests() { return failedRequests; }
    public int getPendingRequests() { return pendingRequests; }
    public int getSkippedRequests() { return skippedRequests; }
    public long getTotalCrawlTime() { return totalCrawlTime; }
    public double getAverageCrawlTime() { return averageCrawlTime; }
    public int getUniqueDomains() { return uniqueDomains; }
    public int getTotalExtractedUrls() { return totalExtractedUrls; }
    public long getTotalContentSize() { return totalContentSize; }
    public int getActiveThreads() { return activeThreads; }
    
    public double getSuccessRate() {
        return totalRequests == 0 ? 0.0 : (double) completedRequests / totalRequests * 100;
    }
    
    public double getFailureRate() {
        return totalRequests == 0 ? 0.0 : (double) failedRequests / totalRequests * 100;
    }
    
    public double getAverageContentSize() {
        return completedRequests == 0 ? 0.0 : (double) totalContentSize / completedRequests;
    }
    
    @Override
    public String toString() {
        return String.format(
            "CrawlStats{total=%d, completed=%d (%.1f%%), failed=%d (%.1f%%), pending=%d, " +
            "domains=%d, extractedUrls=%d, avgTime=%.1fms, contentSize=%d bytes, threads=%d}",
            totalRequests, completedRequests, getSuccessRate(), failedRequests, getFailureRate(),
            pendingRequests, uniqueDomains, totalExtractedUrls, averageCrawlTime, 
            totalContentSize, activeThreads
        );
    }
}