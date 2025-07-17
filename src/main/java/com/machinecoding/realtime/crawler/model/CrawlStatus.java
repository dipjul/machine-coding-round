package com.machinecoding.realtime.crawler.model;

/**
 * Status of a crawl request.
 */
public enum CrawlStatus {
    PENDING("Pending"),
    PROCESSING("Processing"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    SKIPPED("Skipped");
    
    private final String displayName;
    
    CrawlStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}