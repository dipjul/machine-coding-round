package com.machinecoding.realtime.crawler.model;

/**
 * Priority levels for crawl requests.
 */
public enum CrawlPriority {
    LOW(1, "Low"),
    NORMAL(2, "Normal"),
    HIGH(3, "High"),
    URGENT(4, "Urgent");
    
    private final int value;
    private final String displayName;
    
    CrawlPriority(int value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }
    
    public int getValue() {
        return value;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}