package com.machinecoding.search.logs;

import com.machinecoding.search.logs.model.*;
import com.machinecoding.search.logs.query.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Comprehensive demonstration of the Log Search System.
 * Shows inverted index search, multi-dimensional filtering, and query processing.
 */
public class LogSearchDemo {
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public static void main(String[] args) {
        System.out.println("=== Log Search System Demo ===\n");
        
        // Demo 1: Basic Log Search Functionality
        System.out.println("=== Demo 1: Basic Log Search Functionality ===");
        demonstrateBasicSearch();
        
        // Demo 2: Multi-Dimensional Filtering
        System.out.println("\n=== Demo 2: Multi-Dimensional Filtering ===");
        demonstrateFiltering();
        
        // Demo 3: Performance and Statistics
        System.out.println("\n=== Demo 3: Performance and Statistics ===");
        demonstratePerformance();
        
        // Demo 4: Real-World Use Cases
        System.out.println("\n=== Demo 4: Real-World Use Cases ===");
        demonstrateUseCases();
        
        System.out.println("\n=== Demo Complete ===");
    }
    
    private static void demonstrateBasicSearch() {
        System.out.println("1. Creating log search service:");
        LogSearchService searchService = new InMemoryLogSearchService();
        
        System.out.println("\n2. Adding sample log entries:");
        List<LogEntry> sampleLogs = generateSampleLogs(50);
        searchService.indexLogs(sampleLogs);
        System.out.println("   Added " + sampleLogs.size() + " log entries");
        
        System.out.println("\n3. Basic text search:");
        List<LogEntry> errorResults = searchService.searchText("error", 5);
        System.out.println("   Search for 'error': " + errorResults.size() + " matches");
        printResults(errorResults, 3);
        
        List<LogEntry> databaseResults = searchService.searchText("database", 5);
        System.out.println("\n   Search for 'database': " + databaseResults.size() + " matches");
        printResults(databaseResults, 3);
        
        System.out.println("\n4. Empty and edge case searches:");
        List<LogEntry> emptyResults = searchService.searchText("", 5);
        System.out.println("   Empty search: " + emptyResults.size() + " matches");
        
        List<LogEntry> nonExistentResults = searchService.searchText("xyznonexistent", 5);
        System.out.println("   Non-existent term: " + nonExistentResults.size() + " matches");
    }
    
    private static void demonstrateFiltering() {
        LogSearchService searchService = new InMemoryLogSearchService();
        List<LogEntry> sampleLogs = generateSampleLogs(100);
        searchService.indexLogs(sampleLogs);
        
        System.out.println("1. Filter by source:");
        List<LogEntry> appLogs = searchService.searchBySource("app-server", 10);
        System.out.println("   app-server logs: " + appLogs.size() + " matches");
        printResults(appLogs, 2);
        
        System.out.println("\n2. Filter by time range:");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = now.minusHours(1);
        List<LogEntry> recentLogs = searchService.searchByLevelAndTime(LogLevel.INFO, oneHourAgo, now, 10);
        System.out.println("   Recent INFO+ logs (last hour): " + recentLogs.size() + " matches");
        printResults(recentLogs, 2);
        
        System.out.println("\n3. Get logs with exceptions:");
        List<LogEntry> exceptionLogs = searchService.getLogsWithExceptions(5);
        System.out.println("   Exception logs: " + exceptionLogs.size() + " matches");
        printResults(exceptionLogs, 3);
        
        System.out.println("\n4. Get most recent logs:");
        List<LogEntry> recentAll = searchService.getRecentLogs(5);
        System.out.println("   Most recent logs: " + recentAll.size() + " matches");
        printResults(recentAll, 3);
    }
    
    private static void demonstratePerformance() {
        System.out.println("1. Creating large dataset:");
        LogSearchService searchService = new InMemoryLogSearchService();
        List<LogEntry> largeLogs = generateSampleLogs(10000);
        
        long startTime = System.currentTimeMillis();
        searchService.indexLogs(largeLogs);
        long indexTime = System.currentTimeMillis() - startTime;
        System.out.println("   Indexed " + largeLogs.size() + " logs in " + indexTime + "ms");
        
        System.out.println("\n2. Performance benchmarks:");
        
        // Text search performance
        startTime = System.currentTimeMillis();
        List<LogEntry> textResults = searchService.searchText("error", 100);
        long textSearchTime = System.currentTimeMillis() - startTime;
        System.out.println("   Text search: " + textResults.size() + " matches in " + textSearchTime + "ms");
        
        // Source filter performance
        startTime = System.currentTimeMillis();
        List<LogEntry> sourceResults = searchService.searchBySource("database", 100);
        long sourceSearchTime = System.currentTimeMillis() - startTime;
        System.out.println("   Source filter: " + sourceResults.size() + " matches in " + sourceSearchTime + "ms");
        
        System.out.println("\n3. Service statistics:");
        LogSearchStats stats = searchService.getStats();
        System.out.println("   Total logs: " + searchService.getTotalLogs());
        System.out.println("   Unique sources: " + searchService.getAllSources().size());
        System.out.println("   Available sources: " + searchService.getAllSources());
    }
    
    private static void demonstrateUseCases() {
        LogSearchService searchService = new InMemoryLogSearchService();
        
        // Add realistic log entries
        addRealisticLogs(searchService);
        
        System.out.println("1. Troubleshooting scenario - Find all error logs:");
        List<LogEntry> errorLogs = searchService.searchByLevelAndTime(LogLevel.ERROR, 
                LocalDateTime.now().minusHours(1), LocalDateTime.now(), 10);
        System.out.println("   Found " + errorLogs.size() + " error logs:");
        printResults(errorLogs, 3);
        
        System.out.println("\n2. Security monitoring - Find authentication failures:");
        List<LogEntry> authResults = searchService.searchText("authentication failed", 5);
        System.out.println("   Found " + authResults.size() + " authentication failures:");
        printResults(authResults, 2);
        
        System.out.println("\n3. Performance monitoring - Find slow database queries:");
        List<LogEntry> slowQueryResults = searchService.searchText("slow query", 5);
        System.out.println("   Found " + slowQueryResults.size() + " slow queries:");
        printResults(slowQueryResults, 2);
        
        System.out.println("\n4. System health check - Get overview by source:");
        Set<String> sources = searchService.getAllSources();
        System.out.println("   Available log sources: " + sources);
        
        for (String source : sources) {
            List<LogEntry> sourceResults = searchService.searchBySource(source, 1);
            System.out.println("   " + source + ": " + sourceResults.size() + " recent logs");
        }
    }
    
    private static void printResults(List<LogEntry> results, int maxResults) {
        int count = Math.min(maxResults, results.size());
        
        for (int i = 0; i < count; i++) {
            System.out.println("     " + formatLogEntry(results.get(i)));
        }
        
        if (results.size() > count) {
            System.out.println("     ... and " + (results.size() - count) + " more");
        }
    }
    
    private static String formatLogEntry(LogEntry entry) {
        return String.format("[%s] %s %s: %s", 
                           entry.getTimestamp().format(TIME_FORMATTER),
                           entry.getLevel(),
                           entry.getSource(),
                           entry.getMessage().length() > 60 ? 
                               entry.getMessage().substring(0, 60) + "..." : 
                               entry.getMessage());
    }
    
    private static List<LogEntry> generateSampleLogs(int count) {
        List<LogEntry> logs = new ArrayList<>();
        Random random = new Random();
        
        String[] sources = {"app-server", "database", "web-server", "auth-service", "payment-service"};
        String[] messages = {
            "User authentication successful",
            "Database connection established",
            "HTTP request processed successfully",
            "Cache miss for key: user_123",
            "Payment transaction completed",
            "Error connecting to database",
            "Authentication failed for user",
            "Slow query detected: SELECT * FROM users",
            "Memory usage warning: 85% utilized",
            "Connection timeout occurred",
            "Invalid request format received",
            "System startup completed",
            "Configuration loaded successfully",
            "Backup process started",
            "File upload completed"
        };
        
        LogLevel[] levels = LogLevel.values();
        LocalDateTime baseTime = LocalDateTime.now().minusHours(24);
        
        for (int i = 0; i < count; i++) {
            LocalDateTime timestamp = baseTime.plusMinutes(random.nextInt(1440)); // Random time in last 24 hours
            LogLevel level = levels[random.nextInt(levels.length)];
            String source = sources[random.nextInt(sources.length)];
            String message = messages[random.nextInt(messages.length)];
            
            Map<String, String> metadata = new HashMap<>();
            if (random.nextBoolean()) {
                metadata.put("userId", "user_" + random.nextInt(1000));
            }
            if (random.nextBoolean()) {
                metadata.put("requestId", "req_" + random.nextInt(10000));
            }
            
            Throwable exception = null;
            if (random.nextInt(10) == 0) { // 10% chance of exception
                exception = new RuntimeException("Sample exception for testing");
            }
            
            logs.add(new LogEntry(timestamp, level, source, message, 
                    "thread-" + random.nextInt(10), 
                    "logger-" + random.nextInt(5), 
                    metadata.isEmpty() ? null : metadata, 
                    exception));
        }
        
        return logs;
    }
    
    private static void addRealisticLogs(LogSearchService searchService) {
        LocalDateTime now = LocalDateTime.now();
        
        // Add some realistic log entries
        List<LogEntry> realisticLogs = Arrays.asList(
            new LogEntry(now.minusMinutes(30), LogLevel.ERROR, "auth-service", 
                        "Authentication failed for user john.doe@example.com"),
            new LogEntry(now.minusMinutes(25), LogLevel.WARN, "database", 
                        "Slow query detected: execution time 2.5s"),
            new LogEntry(now.minusMinutes(20), LogLevel.INFO, "app-server", 
                        "User session created successfully"),
            new LogEntry(now.minusMinutes(15), LogLevel.ERROR, "payment-service", 
                        "Payment processing failed: insufficient funds"),
            new LogEntry(now.minusMinutes(10), LogLevel.WARN, "web-server", 
                        "High memory usage detected: 90% utilized"),
            new LogEntry(now.minusMinutes(5), LogLevel.ERROR, "auth-service", 
                        "Multiple authentication failed attempts detected"),
            new LogEntry(now.minusMinutes(2), LogLevel.INFO, "database", 
                        "Database backup completed successfully")
        );
        
        searchService.indexLogs(realisticLogs);
    }
}