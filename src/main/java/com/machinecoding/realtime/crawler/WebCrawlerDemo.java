package com.machinecoding.realtime.crawler;

import com.machinecoding.realtime.crawler.model.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Demonstration of the Web Crawler implementation.
 * Shows crawler configuration, URL management, and monitoring features.
 */
public class WebCrawlerDemo {
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== Web Crawler Demo ===\n");
        
        // Demo 1: Basic Crawler Configuration
        System.out.println("=== Demo 1: Basic Crawler Configuration ===");
        demonstrateCrawlerConfiguration();
        
        // Demo 2: URL Management and Validation
        System.out.println("\n=== Demo 2: URL Management and Validation ===");
        demonstrateUrlManagement();
        
        // Demo 3: Crawler Lifecycle Management
        System.out.println("\n=== Demo 3: Crawler Lifecycle Management ===");
        demonstrateCrawlerLifecycle();
        
        // Demo 4: Event Listeners and Monitoring
        System.out.println("\n=== Demo 4: Event Listeners and Monitoring ===");
        demonstrateEventListeners();
        
        // Demo 5: Statistics and Reporting
        System.out.println("\n=== Demo 5: Statistics and Reporting ===");
        demonstrateStatistics();
        
        System.out.println("\n=== Demo Complete ===");
    }
    
    private static void demonstrateCrawlerConfiguration() {
        System.out.println("1. Creating crawler with default configuration:");
        WebCrawler defaultCrawler = new SimpleWebCrawler();
        System.out.println("   Default crawler created successfully");
        
        System.out.println("\n2. Creating crawler with custom configuration:");
        Set<String> allowedDomains = new HashSet<>();
        allowedDomains.add("example.com");
        allowedDomains.add("httpbin.org");
        allowedDomains.add("jsonplaceholder.typicode.com");
        
        WebCrawler customCrawler = new SimpleWebCrawler(3, 2, 500, allowedDomains);
        System.out.println("   Custom crawler created with:");
        System.out.println("   - Max threads: 3");
        System.out.println("   - Max depth: 2");
        System.out.println("   - Request delay: 500ms");
        System.out.println("   - Allowed domains: " + allowedDomains.size());
    }
    
    private static void demonstrateUrlManagement() {
        System.out.println("1. Creating crawler for URL management demo:");
        Set<String> allowedDomains = new HashSet<>();
        allowedDomains.add("httpbin.org");
        allowedDomains.add("jsonplaceholder.typicode.com");
        allowedDomains.add("example.com");
        
        WebCrawler crawler = new SimpleWebCrawler(3, 1, 500, allowedDomains);
        
        System.out.println("\n2. Adding valid URLs:");
        String[] validUrls = {
            "https://httpbin.org/html",
            "https://jsonplaceholder.typicode.com/posts/1",
            "https://example.com",
            "http://httpbin.org/json"
        };
        
        for (String url : validUrls) {
            boolean added = crawler.addUrl(url);
            System.out.println("   " + url + " -> " + (added ? "Added" : "Rejected"));
        }
        
        System.out.println("\n3. Adding URLs with different priorities:");
        boolean highPriority = crawler.addUrl("https://httpbin.org/status/200", CrawlPriority.HIGH, 0);
        boolean lowPriority = crawler.addUrl("https://httpbin.org/delay/1", CrawlPriority.LOW, 0);
        System.out.println("   High priority URL added: " + highPriority);
        System.out.println("   Low priority URL added: " + lowPriority);
        
        System.out.println("\n4. Attempting to add invalid/blocked URLs:");
        String[] invalidUrls = {
            "invalid-url",
            "https://blocked-domain.com/page",
            "ftp://example.com/file.txt"
        };
        
        for (String url : invalidUrls) {
            boolean added = crawler.addUrl(url);
            System.out.println("   " + url + " -> " + (added ? "Added" : "Rejected"));
        }
        
        System.out.println("\n5. Checking pending requests:");
        List<CrawlRequest> pendingRequests = crawler.getPendingRequests();
        System.out.println("   Total pending requests: " + pendingRequests.size());
        
        for (CrawlRequest request : pendingRequests) {
            System.out.println("   - " + request.getUrl() + " (Priority: " + request.getPriority() + 
                             ", Depth: " + request.getDepth() + ")");
        }
    }
    
    private static void demonstrateCrawlerLifecycle() {
        System.out.println("1. Creating crawler for lifecycle demo:");
        Set<String> allowedDomains = new HashSet<>();
        allowedDomains.add("httpbin.org");
        
        WebCrawler crawler = new SimpleWebCrawler(2, 2, 100, allowedDomains);
        
        // Add some URLs
        crawler.addUrl("https://httpbin.org/html");
        crawler.addUrl("https://httpbin.org/json");
        
        System.out.println("\n2. Starting crawler:");
        System.out.println("   Is running before start: " + crawler.isRunning());
        crawler.start();
        System.out.println("   Is running after start: " + crawler.isRunning());
        
        System.out.println("\n3. Letting crawler run for a short time:");
        try {
            Thread.sleep(2000); // Let it run for 2 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("\n4. Pausing crawler:");
        crawler.pause();
        System.out.println("   Crawler paused");
        
        try {
            Thread.sleep(1000); // Pause for 1 second
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("\n5. Resuming crawler:");
        crawler.resume();
        System.out.println("   Crawler resumed");
        
        try {
            Thread.sleep(1000); // Let it run a bit more
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("\n6. Stopping crawler:");
        crawler.stop();
        System.out.println("   Is running after stop: " + crawler.isRunning());
        
        System.out.println("\n7. Final statistics:");
        CrawlStats stats = crawler.getStats();
        System.out.println("   " + stats);
    }
    
    private static void demonstrateEventListeners() {
        System.out.println("1. Creating crawler with event listeners:");
        Set<String> allowedDomains = new HashSet<>();
        allowedDomains.add("httpbin.org");
        
        WebCrawler crawler = new SimpleWebCrawler(1, 2, 200, allowedDomains);
        
        // Add event listeners
        WebCrawler.CrawlListener listener = new WebCrawler.CrawlListener() {
            @Override
            public void onCrawlStarted(CrawlRequest request) {
                System.out.println("   [STARTED] " + request.getUrl());
            }
            
            @Override
            public void onCrawlCompleted(CrawlRequest request, CrawlResult result) {
                System.out.println("   [COMPLETED] " + request.getUrl() + 
                                 " (Status: " + result.getStatusCode() + 
                                 ", Size: " + result.getContentLength() + " bytes)");
            }
            
            @Override
            public void onCrawlFailed(CrawlRequest request, String error) {
                System.out.println("   [FAILED] " + request.getUrl() + " - " + error);
            }
            
            @Override
            public void onUrlsExtracted(String parentUrl, Set<String> extractedUrls) {
                System.out.println("   [EXTRACTED] " + extractedUrls.size() + 
                                 " URLs from " + parentUrl);
            }
        };
        
        crawler.addCrawlListener(listener);
        
        System.out.println("\n2. Adding URLs and starting crawler:");
        crawler.addUrl("https://httpbin.org/html");
        crawler.addUrl("https://httpbin.org/json");
        crawler.addUrl("https://httpbin.org/status/404"); // This should fail
        
        crawler.start();
        
        // Let it run and observe events
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        crawler.stop();
        
        System.out.println("\n3. Event listener demonstration complete");
    }
    
    private static void demonstrateStatistics() {
        System.out.println("1. Creating crawler for statistics demo:");
        Set<String> allowedDomains = new HashSet<>();
        allowedDomains.add("httpbin.org");
        allowedDomains.add("jsonplaceholder.typicode.com");
        
        WebCrawler crawler = new SimpleWebCrawler(2, 1, 100, allowedDomains);
        
        // Add multiple URLs
        String[] urls = {
            "https://httpbin.org/html",
            "https://httpbin.org/json",
            "https://httpbin.org/xml",
            "https://httpbin.org/status/200",
            "https://httpbin.org/status/404",
            "https://jsonplaceholder.typicode.com/posts/1",
            "https://jsonplaceholder.typicode.com/users/1"
        };
        
        System.out.println("\n2. Adding " + urls.length + " URLs:");
        for (String url : urls) {
            crawler.addUrl(url);
            System.out.println("   Added: " + url);
        }
        
        System.out.println("\n3. Initial statistics:");
        CrawlStats initialStats = crawler.getStats();
        System.out.println("   " + initialStats);
        
        System.out.println("\n4. Starting crawler and monitoring progress:");
        crawler.start();
        
        // Monitor progress
        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            
            CrawlStats currentStats = crawler.getStats();
            System.out.println("   [" + (i + 1) + "s] " + currentStats);
            
            // Stop if all requests are processed
            if (currentStats.getPendingRequests() == 0 && currentStats.getActiveThreads() == 0) {
                break;
            }
        }
        
        crawler.stop();
        
        System.out.println("\n5. Final results analysis:");
        CrawlStats finalStats = crawler.getStats();
        System.out.println("   " + finalStats);
        
        List<CrawlResult> results = crawler.getResults();
        System.out.println("\n6. Detailed results:");
        System.out.println("   Total results: " + results.size());
        
        for (CrawlResult result : results) {
            if (result.isSuccessful()) {
                System.out.println("   ✓ " + result.getUrl() + 
                                 " (Status: " + result.getStatusCode() + 
                                 ", Size: " + result.getContentLength() + " bytes" +
                                 (result.getTitle() != null ? ", Title: \"" + result.getTitle() + "\"" : "") + ")");
            } else {
                System.out.println("   ✗ " + result.getUrl() + " - " + result.getErrorMessage());
            }
        }
        
        System.out.println("\n7. Results by domain:");
        results.stream()
                .map(CrawlResult::getDomain)
                .distinct()
                .forEach(domain -> {
                    List<CrawlResult> domainResults = crawler.getResultsByDomain(domain);
                    long successful = domainResults.stream().filter(CrawlResult::isSuccessful).count();
                    System.out.println("   " + domain + ": " + successful + "/" + domainResults.size() + " successful");
                });
    }
}