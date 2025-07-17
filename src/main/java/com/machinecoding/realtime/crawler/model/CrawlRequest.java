package com.machinecoding.realtime.crawler.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a web crawl request with URL and metadata.
 */
public class CrawlRequest {
    private final String url;
    private final int depth;
    private final CrawlPriority priority;
    private final LocalDateTime createdAt;
    private final String parentUrl;
    private CrawlStatus status;
    private int retryCount;
    private LocalDateTime lastAttempt;
    private String errorMessage;
    
    public CrawlRequest(String url) {
        this(url, 0, CrawlPriority.NORMAL, null);
    }
    
    public CrawlRequest(String url, int depth, CrawlPriority priority, String parentUrl) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }
        if (depth < 0) {
            throw new IllegalArgumentException("Depth cannot be negative");
        }
        
        this.url = url.trim();
        this.depth = depth;
        this.priority = priority != null ? priority : CrawlPriority.NORMAL;
        this.parentUrl = parentUrl;
        this.createdAt = LocalDateTime.now();
        this.status = CrawlStatus.PENDING;
        this.retryCount = 0;
    }
    
    // Getters
    public String getUrl() { return url; }
    public int getDepth() { return depth; }
    public CrawlPriority getPriority() { return priority; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getParentUrl() { return parentUrl; }
    public CrawlStatus getStatus() { return status; }
    public int getRetryCount() { return retryCount; }
    public LocalDateTime getLastAttempt() { return lastAttempt; }
    public String getErrorMessage() { return errorMessage; }
    
    // Status management
    public void markAsProcessing() {
        this.status = CrawlStatus.PROCESSING;
        this.lastAttempt = LocalDateTime.now();
    }
    
    public void markAsCompleted() {
        this.status = CrawlStatus.COMPLETED;
    }
    
    public void markAsFailed(String errorMessage) {
        this.status = CrawlStatus.FAILED;
        this.errorMessage = errorMessage;
        this.retryCount++;
    }
    
    public void markAsSkipped(String reason) {
        this.status = CrawlStatus.SKIPPED;
        this.errorMessage = reason;
    }
    
    public boolean canRetry(int maxRetries) {
        return retryCount < maxRetries && status == CrawlStatus.FAILED;
    }
    
    public String getDomain() {
        try {
            java.net.URL urlObj = new java.net.URL(url);
            return urlObj.getHost().toLowerCase();
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CrawlRequest that = (CrawlRequest) o;
        return Objects.equals(url, that.url);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(url);
    }
    
    @Override
    public String toString() {
        return String.format("CrawlRequest{url='%s', depth=%d, priority=%s, status=%s, retries=%d}", 
                           url, depth, priority, status, retryCount);
    }
}