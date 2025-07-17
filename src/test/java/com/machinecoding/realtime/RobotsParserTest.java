package com.machinecoding.realtime;

import com.machinecoding.realtime.crawler.RobotsParser;

/**
 * Unit tests for the RobotsParser implementation.
 * Tests robots.txt parsing, caching, and politeness policies.
 */
public class RobotsParserTest {
    
    public static void main(String[] args) {
        System.out.println("=== Robots.txt Parser Tests ===\n");
        
        runAllTests();
        
        System.out.println("\n=== All Robots.txt Tests Complete ===");
    }
    
    private static void runAllTests() {
        testParserCreation();
        testUrlAllowance();
        testCrawlDelay();
        testCaching();
        testInvalidUrls();
    }
    
    private static void testParserCreation() {
        System.out.println("Test 1: Parser Creation");
        
        try {
            // Test default constructor
            RobotsParser defaultParser = new RobotsParser();
            assert defaultParser != null : "Default parser should not be null";
            System.out.println("   ✓ Default parser creation");
            
            // Test custom constructor
            RobotsParser customParser = new RobotsParser("TestBot/1.0", 1800000);
            assert customParser != null : "Custom parser should not be null";
            System.out.println("   ✓ Custom parser creation");
            
            System.out.println("   ✓ Parser creation tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Parser creation test failed: " + e.getMessage());
        }
    }
    
    private static void testUrlAllowance() {
        System.out.println("\nTest 2: URL Allowance");
        
        try {
            RobotsParser parser = new RobotsParser();
            
            // Test with URLs that typically don't have restrictive robots.txt
            boolean allowed1 = parser.isAllowed("https://httpbin.org/html");
            System.out.println("   httpbin.org/html allowed: " + allowed1);
            
            boolean allowed2 = parser.isAllowed("https://jsonplaceholder.typicode.com/posts/1");
            System.out.println("   jsonplaceholder.typicode.com allowed: " + allowed2);
            
            // Test with invalid URL
            boolean invalidUrl = parser.isAllowed("invalid-url");
            assert invalidUrl : "Invalid URLs should be allowed by default";
            System.out.println("   ✓ Invalid URL handling");
            
            System.out.println("   ✓ URL allowance tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ URL allowance test failed: " + e.getMessage());
        }
    }
    
    private static void testCrawlDelay() {
        System.out.println("\nTest 3: Crawl Delay");
        
        try {
            RobotsParser parser = new RobotsParser();
            
            // Test crawl delay for various URLs
            long delay1 = parser.getCrawlDelay("https://httpbin.org/html");
            System.out.println("   httpbin.org crawl delay: " + delay1 + "ms");
            
            long delay2 = parser.getCrawlDelay("https://example.com/page");
            System.out.println("   example.com crawl delay: " + delay2 + "ms");
            
            // Test with invalid URL
            long invalidDelay = parser.getCrawlDelay("invalid-url");
            assert invalidDelay == 0 : "Invalid URLs should have 0 delay";
            System.out.println("   ✓ Invalid URL delay handling");
            
            System.out.println("   ✓ Crawl delay tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Crawl delay test failed: " + e.getMessage());
        }
    }
    
    private static void testCaching() {
        System.out.println("\nTest 4: Caching");
        
        try {
            RobotsParser parser = new RobotsParser();
            
            // Initial cache size
            int initialSize = parser.getCacheSize();
            System.out.println("   Initial cache size: " + initialSize);
            
            // Make some requests to populate cache
            parser.isAllowed("https://httpbin.org/html");
            parser.isAllowed("https://example.com/page");
            
            int afterRequests = parser.getCacheSize();
            System.out.println("   Cache size after requests: " + afterRequests);
            
            // Clear cache
            parser.clearCache();
            int afterClear = parser.getCacheSize();
            assert afterClear == 0 : "Cache should be empty after clear";
            System.out.println("   ✓ Cache clearing");
            
            System.out.println("   ✓ Caching tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Caching test failed: " + e.getMessage());
        }
    }
    
    private static void testInvalidUrls() {
        System.out.println("\nTest 5: Invalid URLs");
        
        try {
            RobotsParser parser = new RobotsParser();
            
            // Test null URL
            boolean nullUrl = parser.isAllowed(null);
            System.out.println("   Null URL allowed: " + nullUrl);
            
            // Test empty URL
            boolean emptyUrl = parser.isAllowed("");
            System.out.println("   Empty URL allowed: " + emptyUrl);
            
            // Test malformed URL
            boolean malformedUrl = parser.isAllowed("not-a-url");
            System.out.println("   Malformed URL allowed: " + malformedUrl);
            
            // Test crawl delay for invalid URLs
            long nullDelay = parser.getCrawlDelay(null);
            assert nullDelay == 0 : "Null URL should have 0 delay";
            
            long emptyDelay = parser.getCrawlDelay("");
            assert emptyDelay == 0 : "Empty URL should have 0 delay";
            
            System.out.println("   ✓ Invalid URL tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Invalid URL test failed: " + e.getMessage());
        }
    }
}