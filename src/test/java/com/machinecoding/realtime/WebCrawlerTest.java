package com.machinecoding.realtime;

import com.machinecoding.realtime.crawler.*;
import com.machinecoding.realtime.crawler.model.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Comprehensive unit tests for the Web Crawler System.
 * Tests multi-threading, URL management, event handling, and statistics.
 */
public class WebCrawlerTest {
    
    public static void main(String[] args) {
        System.out.println("=== Web Crawler Unit Tests ===\n");
        
        runAllTests();
        
        System.out.println("\n=== All Tests Complete ===");
    }
    
    private static void runAllTests() {
        testCrawlerCreation();
        testUrlManagement();
        testUrlValidation();
        testCrawlerLifecycle();
        testEventListeners();
        testStatistics();
        testConcurrentCrawling();
        testDomainFiltering();
        testPriorityHandling();
        testErrorHandling();
    }
    
    private static void testCrawlerCreation() {
        System.out.println("Test 1: Crawler Creation");
        
        try {
            // Test default constructor
            WebCrawler defaultCrawler = new SimpleWebCrawler();
            assert defaultCrawler != null : "Default crawler should not be null";
            assert !defaultCrawler.isRunning() : "New crawler should not be running";
            System.out.println("   ✓ Default crawler creation");
            
            // Test custom constructor
            Set<String> domains = new HashSet<>();
            domains.add("example.com");
            WebCrawler customCrawler = new SimpleWebCrawler(2, 3, 100, domains);
            assert customCrawler != null : "Custom crawler should not be null";
            assert !customCrawler.isRunning() : "New custom crawler should not be running";
            System.out.println("   ✓ Custom crawler creation");
            
            System.out.println("   ✓ Crawler creation tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Crawler creation test failed: " + e.getMessage());
        }
    }
    
    private static void testUrlManagement() {
        System.out.println("\nTest 2: URL Management");
        
        try {
            Set<String> domains = new HashSet<>();
            domains.add("httpbin.org");
            WebCrawler crawler = new SimpleWebCrawler(1, 1, 100, domains);
            
            // Test adding valid URLs
            boolean added1 = crawler.addUrl("https://httpbin.org/html");
            assert added1 : "Valid URL should be added";
            
            boolean added2 = crawler.addUrl("https://httpbin.org/json");
            assert added2 : "Another valid URL should be added";
            
            // Test adding duplicate URL
            boolean duplicate = crawler.addUrl("https://httpbin.org/html");
            assert !duplicate : "Duplicate URL should not be added";
            
            // Test pending requests
            List<CrawlRequest> pending = crawler.getPendingRequests();
            assert pending.size() == 2 : "Should have 2 pending requests, got " + pending.size();
            
            System.out.println("   ✓ URL management tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ URL management test failed: " + e.getMessage());
        }
    }
    
    private static void testUrlValidation() {
        System.out.println("\nTest 3: URL Validation");
        
        try {
            Set<String> domains = new HashSet<>();
            domains.add("allowed.com");
            WebCrawler crawler = new SimpleWebCrawler(1, 1, 100, domains);
            
            // Test invalid URLs
            boolean invalid1 = crawler.addUrl("invalid-url");
            assert !invalid1 : "Invalid URL should be rejected";
            
            boolean invalid2 = crawler.addUrl("");
            assert !invalid2 : "Empty URL should be rejected";
            
            boolean invalid3 = crawler.addUrl(null);
            assert !invalid3 : "Null URL should be rejected";
            
            // Test blocked domain
            boolean blocked = crawler.addUrl("https://blocked.com/page");
            assert !blocked : "Blocked domain should be rejected";
            
            // Test allowed domain
            boolean allowed = crawler.addUrl("https://allowed.com/page");
            assert allowed : "Allowed domain should be accepted";
            
            System.out.println("   ✓ URL validation tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ URL validation test failed: " + e.getMessage());
        }
    }
    
    private static void testCrawlerLifecycle() {
        System.out.println("\nTest 4: Crawler Lifecycle");
        
        try {
            WebCrawler crawler = new SimpleWebCrawler();
            
            // Test initial state
            assert !crawler.isRunning() : "New crawler should not be running";
            
            // Test start
            crawler.start();
            assert crawler.isRunning() : "Started crawler should be running";
            
            // Test stop
            crawler.stop();
            assert !crawler.isRunning() : "Stopped crawler should not be running";
            
            System.out.println("   ✓ Crawler lifecycle tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Crawler lifecycle test failed: " + e.getMessage());
        }
    }
    
    private static void testEventListeners() {
        System.out.println("\nTest 5: Event Listeners");
        
        try {
            Set<String> domains = new HashSet<>();
            domains.add("httpbin.org");
            WebCrawler crawler = new SimpleWebCrawler(1, 1, 200, domains);
            
            // Event tracking
            final int[] startedCount = {0};
            final int[] completedCount = {0};
            final int[] failedCount = {0};
            
            WebCrawler.CrawlListener listener = new WebCrawler.CrawlListener() {
                @Override
                public void onCrawlStarted(CrawlRequest request) {
                    startedCount[0]++;
                }
                
                @Override
                public void onCrawlCompleted(CrawlRequest request, CrawlResult result) {
                    completedCount[0]++;
                }
                
                @Override
                public void onCrawlFailed(CrawlRequest request, String error) {
                    failedCount[0]++;
                }
                
                @Override
                public void onUrlsExtracted(String parentUrl, Set<String> extractedUrls) {
                    // URL extraction event
                }
            };
            
            crawler.addCrawlListener(listener);
            
            // Add URLs and crawl
            crawler.addUrl("https://httpbin.org/html");
            crawler.addUrl("https://httpbin.org/status/404"); // This should fail
            
            crawler.start();
            
            // Wait for crawling to complete
            Thread.sleep(3000);
            
            crawler.stop();
            
            // Verify events were fired
            assert startedCount[0] > 0 : "Should have started events";
            assert completedCount[0] > 0 : "Should have completed events";
            assert failedCount[0] > 0 : "Should have failed events";
            
            System.out.println("   ✓ Event listener tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Event listener test failed: " + e.getMessage());
        }
    }
    
    private static void testStatistics() {
        System.out.println("\nTest 6: Statistics");
        
        try {
            Set<String> domains = new HashSet<>();
            domains.add("httpbin.org");
            WebCrawler crawler = new SimpleWebCrawler(1, 1, 100, domains);
            
            // Initial stats
            CrawlStats initialStats = crawler.getStats();
            assert initialStats.getTotalRequests() == 0 : "Initial total requests should be 0";
            assert initialStats.getCompletedRequests() == 0 : "Initial completed requests should be 0";
            
            // Add URLs
            crawler.addUrl("https://httpbin.org/html");
            crawler.addUrl("https://httpbin.org/json");
            
            CrawlStats afterAdd = crawler.getStats();
            assert afterAdd.getTotalRequests() == 2 : "Should have 2 total requests";
            assert afterAdd.getPendingRequests() == 2 : "Should have 2 pending requests";
            
            // Run crawler
            crawler.start();
            Thread.sleep(2000);
            crawler.stop();
            
            CrawlStats finalStats = crawler.getStats();
            assert finalStats.getCompletedRequests() > 0 : "Should have completed requests";
            assert finalStats.getSuccessRate() > 0 : "Should have positive success rate";
            
            System.out.println("   ✓ Statistics tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Statistics test failed: " + e.getMessage());
        }
    }
    
    private static void testConcurrentCrawling() {
        System.out.println("\nTest 7: Concurrent Crawling");
        
        try {
            Set<String> domains = new HashSet<>();
            domains.add("httpbin.org");
            WebCrawler crawler = new SimpleWebCrawler(3, 1, 100, domains);
            
            // Add multiple URLs
            String[] urls = {
                "https://httpbin.org/html",
                "https://httpbin.org/json",
                "https://httpbin.org/xml",
                "https://httpbin.org/status/200"
            };
            
            for (String url : urls) {
                crawler.addUrl(url);
            }
            
            long startTime = System.currentTimeMillis();
            crawler.start();
            
            // Wait for completion
            while (crawler.getStats().getPendingRequests() > 0 && 
                   System.currentTimeMillis() - startTime < 10000) {
                Thread.sleep(100);
            }
            
            crawler.stop();
            
            CrawlStats stats = crawler.getStats();
            assert stats.getCompletedRequests() > 0 : "Should have completed some requests";
            
            List<CrawlResult> results = crawler.getResults();
            assert results.size() > 0 : "Should have some results";
            
            System.out.println("   ✓ Concurrent crawling tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Concurrent crawling test failed: " + e.getMessage());
        }
    }
    
    private static void testDomainFiltering() {
        System.out.println("\nTest 8: Domain Filtering");
        
        try {
            Set<String> allowedDomains = new HashSet<>();
            allowedDomains.add("httpbin.org");
            allowedDomains.add("example.com");
            
            WebCrawler crawler = new SimpleWebCrawler(1, 1, 100, allowedDomains);
            
            // Test allowed domains
            boolean allowed1 = crawler.addUrl("https://httpbin.org/html");
            assert allowed1 : "Allowed domain should be accepted";
            
            boolean allowed2 = crawler.addUrl("https://example.com/page");
            assert allowed2 : "Another allowed domain should be accepted";
            
            // Test blocked domain
            boolean blocked = crawler.addUrl("https://google.com/search");
            assert !blocked : "Non-allowed domain should be rejected";
            
            System.out.println("   ✓ Domain filtering tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Domain filtering test failed: " + e.getMessage());
        }
    }
    
    private static void testPriorityHandling() {
        System.out.println("\nTest 9: Priority Handling");
        
        try {
            Set<String> domains = new HashSet<>();
            domains.add("httpbin.org");
            WebCrawler crawler = new SimpleWebCrawler(1, 1, 100, domains);
            
            // Add URLs with different priorities
            boolean high = crawler.addUrl("https://httpbin.org/html", CrawlPriority.HIGH, 0);
            boolean normal = crawler.addUrl("https://httpbin.org/json", CrawlPriority.NORMAL, 0);
            boolean low = crawler.addUrl("https://httpbin.org/xml", CrawlPriority.LOW, 0);
            
            assert high : "High priority URL should be added";
            assert normal : "Normal priority URL should be added";
            assert low : "Low priority URL should be added";
            
            List<CrawlRequest> pending = crawler.getPendingRequests();
            assert pending.size() == 3 : "Should have 3 pending requests";
            
            // Verify priority ordering (high priority should be first)
            CrawlRequest firstRequest = pending.get(0);
            assert firstRequest.getPriority() == CrawlPriority.HIGH : "First request should be high priority";
            
            System.out.println("   ✓ Priority handling tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Priority handling test failed: " + e.getMessage());
        }
    }
    
    private static void testErrorHandling() {
        System.out.println("\nTest 10: Error Handling");
        
        try {
            Set<String> domains = new HashSet<>();
            domains.add("httpbin.org");
            WebCrawler crawler = new SimpleWebCrawler(1, 1, 100, domains);
            
            // Add URLs that will cause different types of errors
            crawler.addUrl("https://httpbin.org/status/404"); // HTTP 404
            crawler.addUrl("https://httpbin.org/status/500"); // HTTP 500
            
            crawler.start();
            Thread.sleep(2000);
            crawler.stop();
            
            CrawlStats stats = crawler.getStats();
            assert stats.getFailedRequests() > 0 : "Should have failed requests";
            assert stats.getFailureRate() > 0 : "Should have positive failure rate";
            
            List<CrawlResult> results = crawler.getResults();
            boolean hasFailedResult = results.stream().anyMatch(r -> !r.isSuccessful());
            assert hasFailedResult : "Should have at least one failed result";
            
            System.out.println("   ✓ Error handling tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Error handling test failed: " + e.getMessage());
        }
    }
}