package com.machinecoding.realtime.crawler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Parser for robots.txt files to implement politeness policies.
 * Caches robots.txt content and provides URL filtering based on robot rules.
 */
public class RobotsParser {
    
    private final Map<String, RobotsRules> robotsCache;
    private final String userAgent;
    private final long cacheExpirationMs;
    
    public RobotsParser() {
        this("*", 3600000); // Default: any user agent, 1 hour cache
    }
    
    public RobotsParser(String userAgent, long cacheExpirationMs) {
        this.userAgent = userAgent;
        this.cacheExpirationMs = cacheExpirationMs;
        this.robotsCache = new ConcurrentHashMap<>();
    }
    
    /**
     * Checks if a URL is allowed to be crawled according to robots.txt.
     * 
     * @param url the URL to check
     * @return true if crawling is allowed, false otherwise
     */
    public boolean isAllowed(String url) {
        try {
            URL urlObj = new URL(url);
            String domain = urlObj.getHost().toLowerCase();
            String path = urlObj.getPath();
            
            RobotsRules rules = getRobotsRules(domain);
            return rules.isAllowed(path, userAgent);
            
        } catch (Exception e) {
            // If we can't parse the URL or fetch robots.txt, allow by default
            return true;
        }
    }
    
    /**
     * Gets the crawl delay for a domain according to robots.txt.
     * 
     * @param url the URL to check
     * @return crawl delay in milliseconds, or 0 if no delay specified
     */
    public long getCrawlDelay(String url) {
        try {
            URL urlObj = new URL(url);
            String domain = urlObj.getHost().toLowerCase();
            
            RobotsRules rules = getRobotsRules(domain);
            return rules.getCrawlDelay(userAgent);
            
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Gets robots.txt rules for a domain, using cache if available.
     */
    private RobotsRules getRobotsRules(String domain) {
        RobotsRules cached = robotsCache.get(domain);
        
        if (cached != null && !cached.isExpired()) {
            return cached;
        }
        
        // Fetch and parse robots.txt
        RobotsRules rules = fetchAndParseRobots(domain);
        robotsCache.put(domain, rules);
        
        return rules;
    }
    
    /**
     * Fetches and parses robots.txt for a domain.
     */
    private RobotsRules fetchAndParseRobots(String domain) {
        try {
            String robotsUrl = "http://" + domain + "/robots.txt";
            URL url = new URL(robotsUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("User-Agent", userAgent);
            
            int statusCode = connection.getResponseCode();
            
            if (statusCode == 200) {
                StringBuilder content = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\\n");
                    }
                }
                
                return parseRobotsContent(content.toString());
            } else {
                // If robots.txt is not found or inaccessible, allow all
                return new RobotsRules(cacheExpirationMs);
            }
            
        } catch (Exception e) {
            // If we can't fetch robots.txt, allow all by default
            return new RobotsRules(cacheExpirationMs);
        }
    }
    
    /**
     * Parses robots.txt content into rules.
     */
    private RobotsRules parseRobotsContent(String content) {
        RobotsRules rules = new RobotsRules(cacheExpirationMs);
        
        String[] lines = content.split("\\n");
        String currentUserAgent = null;
        
        for (String line : lines) {
            line = line.trim();
            
            // Skip empty lines and comments
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            
            String[] parts = line.split(":", 2);
            if (parts.length != 2) {
                continue;
            }
            
            String directive = parts[0].trim().toLowerCase();
            String value = parts[1].trim();
            
            switch (directive) {
                case "user-agent":
                    currentUserAgent = value.toLowerCase();
                    break;
                    
                case "disallow":
                    if (currentUserAgent != null && (currentUserAgent.equals("*") || 
                        currentUserAgent.equals(userAgent.toLowerCase()))) {
                        rules.addDisallowRule(value);
                    }
                    break;
                    
                case "allow":
                    if (currentUserAgent != null && (currentUserAgent.equals("*") || 
                        currentUserAgent.equals(userAgent.toLowerCase()))) {
                        rules.addAllowRule(value);
                    }
                    break;
                    
                case "crawl-delay":
                    if (currentUserAgent != null && (currentUserAgent.equals("*") || 
                        currentUserAgent.equals(userAgent.toLowerCase()))) {
                        try {
                            long delay = Long.parseLong(value) * 1000; // Convert to milliseconds
                            rules.setCrawlDelay(currentUserAgent, delay);
                        } catch (NumberFormatException e) {
                            // Ignore invalid crawl-delay values
                        }
                    }
                    break;
                    
                case "sitemap":
                    rules.addSitemap(value);
                    break;
            }
        }
        
        return rules;
    }
    
    /**
     * Clears the robots.txt cache.
     */
    public void clearCache() {
        robotsCache.clear();
    }
    
    /**
     * Gets the current cache size.
     */
    public int getCacheSize() {
        return robotsCache.size();
    }
    
    /**
     * Represents parsed robots.txt rules for a domain.
     */
    private static class RobotsRules {
        private final List<String> disallowRules;
        private final List<String> allowRules;
        private final Map<String, Long> crawlDelays;
        private final List<String> sitemaps;
        private final long createdAt;
        private final long expirationMs;
        
        public RobotsRules(long expirationMs) {
            this.disallowRules = new ArrayList<>();
            this.allowRules = new ArrayList<>();
            this.crawlDelays = new HashMap<>();
            this.sitemaps = new ArrayList<>();
            this.createdAt = System.currentTimeMillis();
            this.expirationMs = expirationMs;
        }
        
        public void addDisallowRule(String path) {
            if (path != null && !path.isEmpty()) {
                disallowRules.add(path);
            }
        }
        
        public void addAllowRule(String path) {
            if (path != null && !path.isEmpty()) {
                allowRules.add(path);
            }
        }
        
        public void setCrawlDelay(String userAgent, long delayMs) {
            crawlDelays.put(userAgent.toLowerCase(), delayMs);
        }
        
        public void addSitemap(String sitemap) {
            if (sitemap != null && !sitemap.isEmpty()) {
                sitemaps.add(sitemap);
            }
        }
        
        public boolean isAllowed(String path, String userAgent) {
            // Check allow rules first (they take precedence)
            for (String allowRule : allowRules) {
                if (pathMatches(path, allowRule)) {
                    return true;
                }
            }
            
            // Check disallow rules
            for (String disallowRule : disallowRules) {
                if (pathMatches(path, disallowRule)) {
                    return false;
                }
            }
            
            // If no rules match, allow by default
            return true;
        }
        
        public long getCrawlDelay(String userAgent) {
            Long delay = crawlDelays.get(userAgent.toLowerCase());
            if (delay != null) {
                return delay;
            }
            
            // Check for wildcard user agent
            delay = crawlDelays.get("*");
            return delay != null ? delay : 0;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() - createdAt > expirationMs;
        }
        
        public List<String> getSitemaps() {
            return new ArrayList<>(sitemaps);
        }
        
        /**
         * Checks if a path matches a robots.txt rule pattern.
         */
        private boolean pathMatches(String path, String pattern) {
            if (pattern.equals("/")) {
                return true; // Disallow all
            }
            
            if (pattern.isEmpty()) {
                return false; // Allow all
            }
            
            // Simple prefix matching (robots.txt uses prefix matching)
            return path.startsWith(pattern);
        }
    }
}