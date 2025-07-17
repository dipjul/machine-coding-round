package com.machinecoding.realtime.crawler;

import com.machinecoding.realtime.crawler.model.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple multi-threaded web crawler implementation.
 * Demonstrates core crawling concepts with thread safety and politeness policies.
 */
public class SimpleWebCrawler implements WebCrawler {
    
    private final int maxThreads;
    private final int maxDepth;
    private final long requestDelayMs;
    private final Set<String> allowedDomains;
    private final RobotsParser robotsParser;
    
    // Thread management
    private ExecutorService executorService;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicInteger activeThreads = new AtomicInteger(0);
    
    // URL management
    private final PriorityBlockingQueue<CrawlRequest> urlQueue;
    private final Set<String> visitedUrls;
    private final List<CrawlResult> results;
    private final List<CrawlListener> listeners;
    
    // Statistics
    private final AtomicInteger totalRequests = new AtomicInteger(0);
    private final AtomicInteger completedRequests = new AtomicInteger(0);
    private final AtomicInteger failedRequests = new AtomicInteger(0);
    
    // URL extraction pattern
    private static final Pattern URL_PATTERN = Pattern.compile(
        "href\\s*=\\s*[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
    private static final Pattern TITLE_PATTERN = Pattern.compile(
        "<title[^>]*>([^<]+)</title>", Pattern.CASE_INSENSITIVE);
    
    public SimpleWebCrawler() {
        this(3, 2, 1000, new HashSet<>());
    }
    
    public SimpleWebCrawler(int maxThreads, int maxDepth, long requestDelayMs, Set<String> allowedDomains) {
        this.maxThreads = maxThreads;
        this.maxDepth = maxDepth;
        this.requestDelayMs = requestDelayMs;
        this.allowedDomains = new HashSet<>(allowedDomains);
        this.robotsParser = new RobotsParser("WebCrawler/1.0", 3600000); // 1 hour cache
        
        this.urlQueue = new PriorityBlockingQueue<>(1000, 
            (a, b) -> Integer.compare(b.getPriority().getValue(), a.getPriority().getValue()));
        this.visitedUrls = ConcurrentHashMap.newKeySet();
        this.results = new CopyOnWriteArrayList<>();
        this.listeners = new CopyOnWriteArrayList<>();
    }
    
    @Override
    public boolean addUrl(String url) {
        return addUrl(url, CrawlPriority.NORMAL, 0);
    }
    
    @Override
    public boolean addUrl(String url, CrawlPriority priority, int depth) {
        if (url == null || url.trim().isEmpty() || depth > maxDepth) {
            return false;
        }
        
        url = normalizeUrl(url);
        
        if (visitedUrls.contains(url)) {
            return false;
        }
        
        String domain = extractDomain(url);
        if (!allowedDomains.isEmpty() && !allowedDomains.contains(domain)) {
            return false;
        }
        
        // Check robots.txt politeness policy
        if (!robotsParser.isAllowed(url)) {
            return false;
        }
        
        CrawlRequest request = new CrawlRequest(url, depth, priority, null);
        if (urlQueue.offer(request)) {
            totalRequests.incrementAndGet();
            return true;
        }
        
        return false;
    }
    
    @Override
    public void start() {
        if (running.get()) {
            return;
        }
        
        running.set(true);
        executorService = Executors.newFixedThreadPool(maxThreads);
        
        for (int i = 0; i < maxThreads; i++) {
            executorService.submit(this::crawlerWorker);
        }
        
        System.out.println("Web crawler started with " + maxThreads + " threads");
    }
    
    @Override
    public void stop() {
        if (!running.get()) {
            return;
        }
        
        running.set(false);
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        System.out.println("Web crawler stopped");
    }
    
    @Override
    public void pause() {
        System.out.println("Pause not implemented in simple crawler");
    }
    
    @Override
    public void resume() {
        System.out.println("Resume not implemented in simple crawler");
    }
    
    @Override
    public boolean isRunning() {
        return running.get();
    }
    
    private void crawlerWorker() {
        while (running.get()) {
            try {
                CrawlRequest request = urlQueue.poll(1, TimeUnit.SECONDS);
                if (request == null) {
                    continue;
                }
                
                activeThreads.incrementAndGet();
                try {
                    crawlUrl(request);
                } finally {
                    activeThreads.decrementAndGet();
                }
                
                // Apply request delay and robots.txt crawl delay
                long delay = Math.max(requestDelayMs, robotsParser.getCrawlDelay(request.getUrl()));
                if (delay > 0) {
                    Thread.sleep(delay);
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Error in crawler worker: " + e.getMessage());
            }
        }
    }
    
    private void crawlUrl(CrawlRequest request) {
        String url = request.getUrl();
        
        if (visitedUrls.contains(url)) {
            return;
        }
        
        visitedUrls.add(url);
        request.markAsProcessing();
        
        notifyListeners(listener -> listener.onCrawlStarted(request));
        
        long startTime = System.currentTimeMillis();
        
        try {
            CrawlResult result = performHttpRequest(url);
            long duration = System.currentTimeMillis() - startTime;
            
            if (result.isSuccessful()) {
                request.markAsCompleted();
                completedRequests.incrementAndGet();
                
                // Extract URLs from content if within depth limit
                if (result.hasContent() && request.getDepth() < maxDepth) {
                    Set<String> extractedUrls = extractUrls(result.getContent(), url);
                    if (!extractedUrls.isEmpty()) {
                        notifyListeners(listener -> listener.onUrlsExtracted(url, extractedUrls));
                        
                        for (String extractedUrl : extractedUrls) {
                            addUrl(extractedUrl, CrawlPriority.NORMAL, request.getDepth() + 1);
                        }
                    }
                }
                
                notifyListeners(listener -> listener.onCrawlCompleted(request, result));
            } else {
                request.markAsFailed(result.getErrorMessage());
                failedRequests.incrementAndGet();
                notifyListeners(listener -> listener.onCrawlFailed(request, result.getErrorMessage()));
            }
            
            results.add(result);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            request.markAsFailed(e.getMessage());
            failedRequests.incrementAndGet();
            
            CrawlResult errorResult = new CrawlResult(url, e.getMessage(), duration);
            results.add(errorResult);
            
            notifyListeners(listener -> listener.onCrawlFailed(request, e.getMessage()));
        }
    }
    
    private CrawlResult performHttpRequest(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(15000);
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; WebCrawler/1.0)");
        
        int statusCode = connection.getResponseCode();
        Map<String, String> headers = new HashMap<>();
        
        if (statusCode >= 200 && statusCode < 300) {
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\\n");
                }
            }
            
            String contentStr = content.toString();
            Set<String> extractedUrls = extractUrls(contentStr, urlString);
            String title = extractTitle(contentStr);
            
            return new CrawlResult(urlString, statusCode, contentStr, headers,
                                 extractedUrls, title, null, new ArrayList<>(), 0);
        } else {
            return new CrawlResult(urlString, "HTTP " + statusCode, 0);
        }
    }
    
    private Set<String> extractUrls(String content, String baseUrl) {
        Set<String> urls = new HashSet<>();
        Matcher matcher = URL_PATTERN.matcher(content);
        
        while (matcher.find()) {
            String url = matcher.group(1);
            String absoluteUrl = resolveUrl(url, baseUrl);
            if (absoluteUrl != null && isValidUrl(absoluteUrl)) {
                urls.add(absoluteUrl);
            }
        }
        
        return urls;
    }
    
    private String extractTitle(String content) {
        Matcher matcher = TITLE_PATTERN.matcher(content);
        return matcher.find() ? matcher.group(1).trim() : null;
    }
    
    private String normalizeUrl(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        return url.toLowerCase();
    }
    
    private String extractDomain(String url) {
        try {
            URL urlObj = new URL(url);
            return urlObj.getHost().toLowerCase();
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    private boolean isValidUrl(String url) {
        try {
            new URL(url);
            return url.startsWith("http://") || url.startsWith("https://");
        } catch (Exception e) {
            return false;
        }
    }
    
    private String resolveUrl(String url, String baseUrl) {
        try {
            if (url.startsWith("http://") || url.startsWith("https://")) {
                return url;
            }
            
            URL base = new URL(baseUrl);
            if (url.startsWith("/")) {
                return base.getProtocol() + "://" + base.getHost() + url;
            } else {
                String basePath = base.getPath();
                if (basePath.endsWith("/")) {
                    return baseUrl + url;
                } else {
                    int lastSlash = basePath.lastIndexOf('/');
                    String parentPath = lastSlash >= 0 ? basePath.substring(0, lastSlash + 1) : "/";
                    return base.getProtocol() + "://" + base.getHost() + parentPath + url;
                }
            }
        } catch (Exception e) {
            return null;
        }
    }
    
    private void notifyListeners(java.util.function.Consumer<CrawlListener> action) {
        for (CrawlListener listener : listeners) {
            try {
                action.accept(listener);
            } catch (Exception e) {
                System.err.println("Error notifying crawler listener: " + e.getMessage());
            }
        }
    }
    
    @Override
    public CrawlStats getStats() {
        long totalCrawlTime = results.stream().mapToLong(CrawlResult::getCrawlDurationMs).sum();
        double avgCrawlTime = results.isEmpty() ? 0.0 : (double) totalCrawlTime / results.size();
        int uniqueDomains = (int) results.stream().map(CrawlResult::getDomain).distinct().count();
        int totalExtractedUrls = results.stream()
                .filter(CrawlResult::hasExtractedUrls)
                .mapToInt(r -> r.getExtractedUrls().size())
                .sum();
        long totalContentSize = results.stream().mapToLong(CrawlResult::getContentLength).sum();
        
        return new CrawlStats(
            totalRequests.get(),
            completedRequests.get(),
            failedRequests.get(),
            urlQueue.size(),
            0, // skipped requests
            totalCrawlTime,
            avgCrawlTime,
            uniqueDomains,
            totalExtractedUrls,
            totalContentSize,
            activeThreads.get()
        );
    }
    
    @Override
    public List<CrawlResult> getResults() {
        return new ArrayList<>(results);
    }
    
    @Override
    public List<CrawlResult> getResultsByDomain(String domain) {
        return results.stream()
                .filter(result -> result.getDomain().equals(domain))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    @Override
    public List<CrawlRequest> getPendingRequests() {
        return new ArrayList<>(urlQueue);
    }
    
    @Override
    public void addCrawlListener(CrawlListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }
    
    @Override
    public void removeCrawlListener(CrawlListener listener) {
        listeners.remove(listener);
    }
}