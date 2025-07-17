package com.machinecoding.search;

import com.machinecoding.search.logsearch.*;
import com.machinecoding.search.logsearch.model.*;
import com.machinecoding.search.logsearch.query.QueryParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for the Log Search System.
 */
public class LogSearchTest {
    
    private LogSearchService searchService;
    private LocalDateTime baseTime;
    
    @BeforeEach
    void setUp() {
        searchService = new InMemoryLogSearchService();
        baseTime = LocalDateTime.now().minusHours(1);
        addTestLogs();
    }
    
    @Test
    void testBasicTextSearch() {
        SearchResult result = searchService.searchByText("error", 10);
        
        assertTrue(result.getTotalMatches() > 0);
        assertFalse(result.getEntries().isEmpty());
        
        // Verify all results contain the search term
        for (LogEntry entry : result.getEntries()) {
            assertTrue(entry.getMessage().toLowerCase().contains("error") ||
                      entry.getSource().toLowerCase().contains("error"));
        }
    }
    
    @Test
    void testCaseInsensitiveSearch() {
        SearchResult lowerResult = searchService.searchByText("error", 10);
        SearchResult upperResult = searchService.searchByText("ERROR", 10);
        SearchResult mixedResult = searchService.searchByText("ErRoR", 10);
        
        assertEquals(lowerResult.getTotalMatches(), upperResult.getTotalMatches());
        assertEquals(lowerResult.getTotalMatches(), mixedResult.getTotalMatches());
    }
    
    @Test
    void testEmptySearch() {
        SearchResult result = searchService.searchByText("", 10);
        assertEquals(searchService.getTotalLogEntries(), result.getTotalMatches());
        
        SearchResult nullResult = searchService.searchByText(null, 10);
        assertEquals(searchService.getTotalLogEntries(), nullResult.getTotalMatches());
    }
    
    @Test
    void testNonExistentTermSearch() {
        SearchResult result = searchService.searchByText("nonexistentterm12345", 10);
        assertEquals(0, result.getTotalMatches());
        assertTrue(result.getEntries().isEmpty());
    }
    
    @Test
    void testLevelFiltering() {
        SearchResult errorResult = searchService.searchByLevel(LogLevel.ERROR, 10);
        SearchResult warnResult = searchService.searchByLevel(LogLevel.WARN, 10);
        SearchResult infoResult = searchService.searchByLevel(LogLevel.INFO, 10);
        
        // Verify all results have correct level
        for (LogEntry entry : errorResult.getEntries()) {
            assertEquals(LogLevel.ERROR, entry.getLevel());
        }
        
        for (LogEntry entry : warnResult.getEntries()) {
            assertEquals(LogLevel.WARN, entry.getLevel());
        }
        
        for (LogEntry entry : infoResult.getEntries()) {
            assertEquals(LogLevel.INFO, entry.getLevel());
        }
    }
    
    @Test
    void testSourceFiltering() {
        SearchResult appResult = searchService.searchBySource("app-server", 10);
        SearchResult dbResult = searchService.searchBySource("database", 10);
        
        // Verify all results have correct source
        for (LogEntry entry : appResult.getEntries()) {
            assertEquals("app-server", entry.getSource());
        }
        
        for (LogEntry entry : dbResult.getEntries()) {
            assertEquals("database", entry.getSource());
        }
    }
    
    @Test
    void testTimeRangeFiltering() {
        LocalDateTime start = baseTime.plusMinutes(10);
        LocalDateTime end = baseTime.plusMinutes(30);
        
        SearchResult result = searchService.searchByTimeRange(start, end, 10);
        
        // Verify all results are within time range
        for (LogEntry entry : result.getEntries()) {
            assertTrue(!entry.getTimestamp().isBefore(start));
            assertTrue(!entry.getTimestamp().isAfter(end));
        }
    }
    
    @Test
    void testComplexQuery() {
        SearchQuery query = new SearchQuery.Builder()
                .withText("connection")
                .withLevel(LogLevel.ERROR)
                .withSource("database")
                .withLimit(5)
                .build();
        
        SearchResult result = searchService.search(query);
        
        // Verify all filters are applied
        for (LogEntry entry : result.getEntries()) {
            assertTrue(entry.getMessage().toLowerCase().contains("connection"));
            assertEquals(LogLevel.ERROR, entry.getLevel());
            assertEquals("database", entry.getSource());
        }
        
        assertTrue(result.getEntries().size() <= 5);
    }
    
    @Test
    void testQueryBuilder() {
        SearchQuery query = new SearchQuery.Builder()
                .withText("test message")
                .withLevel(LogLevel.ERROR)
                .withLevel(LogLevel.WARN)
                .withSource("app-server")
                .withStartTime(baseTime)
                .withEndTime(baseTime.plusHours(1))
                .withLimit(20)
                .withCaseSensitive(true)
                .build();
        
        assertEquals("test message", query.getText());
        assertTrue(query.getLevels().contains(LogLevel.ERROR));
        assertTrue(query.getLevels().contains(LogLevel.WARN));
        assertTrue(query.getSources().contains("app-server"));
        assertEquals(baseTime, query.getStartTime());
        assertEquals(baseTime.plusHours(1), query.getEndTime());
        assertEquals(20, query.getLimit());
        assertTrue(query.isCaseSensitive());
    }
    
    @Test
    void testQueryParser() {
        // Simple text query
        SearchQuery query1 = QueryParser.parse("database error");
        assertEquals("database error", query1.getText());
        
        // Query with level filter
        SearchQuery query2 = QueryParser.parse("connection level:ERROR");
        assertEquals("connection", query2.getText());
        assertTrue(query2.getLevels().contains(LogLevel.ERROR));
        
        // Query with source filter
        SearchQuery query3 = QueryParser.parse("timeout source:app-server");
        assertEquals("timeout", query3.getText());
        assertTrue(query3.getSources().contains("app-server"));
        
        // Query with limit
        SearchQuery query4 = QueryParser.parse("error limit:5");
        assertEquals("error", query4.getText());
        assertEquals(5, query4.getLimit());
        
        // Complex query
        SearchQuery query5 = QueryParser.parse("database level:ERROR source:db-server limit:10");
        assertEquals("database", query5.getText());
        assertTrue(query5.getLevels().contains(LogLevel.ERROR));
        assertTrue(query5.getSources().contains("db-server"));
        assertEquals(10, query5.getLimit());
    }
    
    @Test
    void testResultSorting() {
        SearchResult result = searchService.searchByText("log", 10);
        
        // Results should be sorted by timestamp (most recent first)
        List<LogEntry> entries = result.getEntries();
        for (int i = 1; i < entries.size(); i++) {
            assertTrue(!entries.get(i-1).getTimestamp().isBefore(entries.get(i).getTimestamp()));
        }
    }
    
    @Test
    void testLimitAndPagination() {
        SearchResult result = searchService.searchByText("", 5); // Get all logs with limit 5
        
        assertEquals(5, result.getEntries().size());
        assertTrue(result.hasMoreResults());
        assertTrue(result.getTotalMatches() > 5);
    }
    
    @Test
    void testServiceStatistics() {
        LogSearchStats stats = searchService.getStats();
        
        assertTrue(stats.getTotalEntries() > 0);
        assertTrue(stats.getUniqueSources() > 0);
        assertNotNull(stats.getEntriesByLevel());
        assertNotNull(stats.getEntriesBySource());
        assertTrue(stats.getTotalSearches() > 0); // setUp() calls search methods
    }
    
    @Test
    void testGetAllSources() {
        Set<String> sources = searchService.getAllSources();
        
        assertFalse(sources.isEmpty());
        assertTrue(sources.contains("app-server"));
        assertTrue(sources.contains("database"));
    }
    
    @Test
    void testGetTimeRange() {
        LocalDateTime[] timeRange = searchService.getTimeRange();
        
        assertNotNull(timeRange);
        assertEquals(2, timeRange.length);
        assertNotNull(timeRange[0]); // oldest
        assertNotNull(timeRange[1]); // newest
        assertTrue(!timeRange[0].isAfter(timeRange[1]));
    }
    
    @Test
    void testClearService() {
        int initialCount = searchService.getTotalLogEntries();
        assertTrue(initialCount > 0);
        
        searchService.clear();
        
        assertEquals(0, searchService.getTotalLogEntries());
        assertTrue(searchService.getAllSources().isEmpty());
        assertNull(searchService.getTimeRange());
    }
    
    @Test
    void testLogEntryValidation() {
        // Test valid log entry
        LogEntry validEntry = new LogEntry("test_id", LocalDateTime.now(), LogLevel.INFO, "test-source", "test message");
        assertNotNull(validEntry);
        assertEquals("test_id", validEntry.getId());
        
        // Test invalid entries
        assertThrows(IllegalArgumentException.class, () -> 
            new LogEntry(null, LocalDateTime.now(), LogLevel.INFO, "source", "message"));
        
        assertThrows(IllegalArgumentException.class, () -> 
            new LogEntry("", LocalDateTime.now(), LogLevel.INFO, "source", "message"));
        
        assertThrows(IllegalArgumentException.class, () -> 
            new LogEntry("id", null, LogLevel.INFO, "source", "message"));
        
        assertThrows(IllegalArgumentException.class, () -> 
            new LogEntry("id", LocalDateTime.now(), LogLevel.INFO, "source", null));
    }
    
    @Test
    void testLogEntryAttributes() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("userId", "user123");
        attributes.put("requestId", "req456");
        
        LogEntry entry = new LogEntry("test_id", LocalDateTime.now(), LogLevel.INFO, "test-source", "test message", attributes);
        
        assertEquals("user123", entry.getAttribute("userId"));
        assertEquals("req456", entry.getAttribute("requestId"));
        assertNull(entry.getAttribute("nonexistent"));
        
        // Test attribute search
        assertTrue(entry.containsText("user123"));
        assertTrue(entry.containsText("req456"));
        assertFalse(entry.containsText("nonexistent"));
    }
    
    @Test
    void testMinimumLevelFilter() {
        SearchQuery query = new SearchQuery.Builder()
                .withMinimumLevel(LogLevel.WARN)
                .withLimit(20)
                .build();
        
        SearchResult result = searchService.search(query);
        
        // All results should be WARN or higher (ERROR, FATAL)
        for (LogEntry entry : result.getEntries()) {
            assertTrue(entry.getLevel().isAtLeast(LogLevel.WARN));
        }
    }
    
    private void addTestLogs() {
        List<LogEntry> testLogs = Arrays.asList(
            new LogEntry("log_001", baseTime.plusMinutes(5), LogLevel.INFO, "app-server", "Application started successfully"),
            new LogEntry("log_002", baseTime.plusMinutes(10), LogLevel.ERROR, "database", "Connection error occurred"),
            new LogEntry("log_003", baseTime.plusMinutes(15), LogLevel.WARN, "app-server", "High memory usage detected"),
            new LogEntry("log_004", baseTime.plusMinutes(20), LogLevel.INFO, "web-server", "HTTP request processed"),
            new LogEntry("log_005", baseTime.plusMinutes(25), LogLevel.ERROR, "database", "Database connection timeout"),
            new LogEntry("log_006", baseTime.plusMinutes(30), LogLevel.DEBUG, "app-server", "Debug information logged"),
            new LogEntry("log_007", baseTime.plusMinutes(35), LogLevel.FATAL, "system", "System crash detected"),
            new LogEntry("log_008", baseTime.plusMinutes(40), LogLevel.WARN, "auth-service", "Authentication warning"),
            new LogEntry("log_009", baseTime.plusMinutes(45), LogLevel.INFO, "payment-service", "Payment processed"),
            new LogEntry("log_010", baseTime.plusMinutes(50), LogLevel.ERROR, "app-server", "Unexpected error in application")
        );
        
        searchService.addLogEntries(testLogs);
    }
}