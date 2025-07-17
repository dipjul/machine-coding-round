package com.machinecoding.realtime.crawler.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Result of a web crawl operation.
 */
public class CrawlResult {
    private final String url;
    private final int statusCode;
    private final String content;
    private final Map<String, String> headers;
    private final Set<String> extractedUrls;
    private final String title;
    private final String description;
    private final List<String> keywords;
    private final long contentLength;
    private final String contentType;
    private final LocalDateTime crawledAt;
    private final long crawlDurationMs;
    private final boolean successful;
    private final String errorMessage;
    
    // Constructor for successful crawl
    public CrawlResult(String url, int statusCode, String content, Map<String, String> headers,
                      Set<String> extractedUrls, String title, String description, 
                      List<String> keywords, long crawlDurationMs) {
        this.url = url;
        this.statusCode = statusCode;
        this.content = content;
        this.headers = headers;
        this.extractedUrls = extractedUrls;
        this.title = title;
        this.description = description;
        this.keywords = keywords;
        this.contentLength = content != null ? content.length() : 0;
        this.contentType = headers != null ? headers.get("Content-Type") : null;
        this.crawledAt = LocalDateTime.now();
        this.crawlDurationMs = crawlDurationMs;
        this.successful = statusCode >= 200 && statusCode < 300;
        this.errorMessage = null;
    }
    
    // Constructor for failed crawl
    public CrawlResult(String url, String errorMessage, long crawlDurationMs) {
        this.url = url;
        this.statusCode = -1;
        this.content = null;
        this.headers = null;
        this.extractedUrls = null;
        this.title = null;
        this.description = null;
        this.keywords = null;
        this.contentLength = 0;
        this.contentType = null;
        this.crawledAt = LocalDateTime.now();
        this.crawlDurationMs = crawlDurationMs;
        this.successful = false;
        this.errorMessage = errorMessage;
    }
    
    // Getters
    public String getUrl() { return url; }
    public int getStatusCode() { return statusCode; }
    public String getContent() { return content; }
    public Map<String, String> getHeaders() { return headers; }
    public Set<String> getExtractedUrls() { return extractedUrls; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<String> getKeywords() { return keywords; }
    public long getContentLength() { return contentLength; }
    public String getContentType() { return contentType; }
    public LocalDateTime getCrawledAt() { return crawledAt; }
    public long getCrawlDurationMs() { return crawlDurationMs; }
    public boolean isSuccessful() { return successful; }
    public String getErrorMessage() { return errorMessage; }
    
    public boolean hasContent() {
        return content != null && !content.trim().isEmpty();
    }
    
    public boolean hasExtractedUrls() {
        return extractedUrls != null && !extractedUrls.isEmpty();
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
    public String toString() {
        if (successful) {
            return String.format("CrawlResult{url='%s', status=%d, contentLength=%d, extractedUrls=%d, duration=%dms}", 
                               url, statusCode, contentLength, 
                               extractedUrls != null ? extractedUrls.size() : 0, crawlDurationMs);
        } else {
            return String.format("CrawlResult{url='%s', failed='%s', duration=%dms}", 
                               url, errorMessage, crawlDurationMs);
        }
    }
}