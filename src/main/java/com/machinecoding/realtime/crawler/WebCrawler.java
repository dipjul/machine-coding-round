package com.machinecoding.realtime.crawler;

import com.machinecoding.realtime.crawler.model.*;
import java.util.List;
import java.util.Set;

/**
 * Interface for web crawler operations.
 */
public interface WebCrawler {
    
    /**
     * Adds a URL to the crawl queue.
     * 
     * @param url the URL to crawl
     * @return true if added successfully
     */
    boolean addUrl(String url);
    
    /**
     * Adds a URL with specific priority and depth.
     * 
     * @param url the URL to crawl
     * @param priority crawl priority
     * @param depth crawl depth
     * @return true if added successfully
     */
    boolean addUrl(String url, CrawlPriority priority, int depth);
    
    /**
     * Starts the crawler.
     */
    void start();
    
    /**
     * Stops the crawler.
     */
    void stop();
    
    /**
     * Pauses the crawler.
     */
    void pause();
    
    /**
     * Resumes the crawler.
     */
    void resume();
    
    /**
     * Checks if the crawler is running.
     * 
     * @return true if running
     */
    boolean isRunning();
    
    /**
     * Gets the current crawl statistics.
     * 
     * @return crawl statistics
     */
    CrawlStats getStats();
    
    /**
     * Gets all crawl results.
     * 
     * @return list of crawl results
     */
    List<CrawlResult> getResults();
    
    /**
     * Gets crawl results for a specific domain.
     * 
     * @param domain the domain to filter by
     * @return list of crawl results for the domain
     */
    List<CrawlResult> getResultsByDomain(String domain);
    
    /**
     * Gets pending crawl requests.
     * 
     * @return list of pending requests
     */
    List<CrawlRequest> getPendingRequests();
    
    /**
     * Adds a crawl listener for events.
     * 
     * @param listener the listener to add
     */
    void addCrawlListener(CrawlListener listener);
    
    /**
     * Removes a crawl listener.
     * 
     * @param listener the listener to remove
     */
    void removeCrawlListener(CrawlListener listener);
    
    /**
     * Interface for crawl event listeners.
     */
    interface CrawlListener {
        void onCrawlStarted(CrawlRequest request);
        void onCrawlCompleted(CrawlRequest request, CrawlResult result);
        void onCrawlFailed(CrawlRequest request, String error);
        void onUrlsExtracted(String parentUrl, Set<String> extractedUrls);
    }
}