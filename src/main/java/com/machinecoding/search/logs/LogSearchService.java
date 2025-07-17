package com.machinecoding.search.logs;

import com.machinecoding.search.logs.model.LogEntry;
import com.machinecoding.search.logs.model.LogLevel;
import com.machinecoding.search.logs.query.LogSearchQuery;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Interface for log search and indexing operations.
 */
public interface LogSearchService {
    
    /**
     * Indexes a single log entry.
     * 
     * @param logEntry the log entry to index
     */
    void indexLog(LogEntry logEntry);
    
    /**
     * Indexes multiple log entries in batch.
     * 
     * @param logEntries the log entries to index
     */
    void indexLogs(List<LogEntry> logEntries);
    
    /**
     * Searches logs using a structured query.
     * 
     * @param query the search query
     * @return list of matching log entries
     */
    List<LogEntry> search(LogSearchQuery query);
    
    /**
     * Performs a simple text search across all log messages.
     * 
     * @param text the text to search for
     * @param limit maximum number of results
     * @return list of matching log entries
     */
    List<LogEntry> searchText(String text, int limit);
    
    /**
     * Searches logs by level and time range.
     * 
     * @param level minimum log level
     * @param startTime start of time range (inclusive)
     * @param endTime end of time range (inclusive)
     * @param limit maximum number of results
     * @return list of matching log entries
     */
    List<LogEntry> searchByLevelAndTime(LogLevel level, LocalDateTime startTime, 
                                       LocalDateTime endTime, int limit);
    
    /**
     * Searches logs by source.
     * 
     * @param source the log source to filter by
     * @param limit maximum number of results
     * @return list of matching log entries
     */
    List<LogEntry> searchBySource(String source, int limit);
    
    /**
     * Gets logs containing exceptions.
     * 
     * @param limit maximum number of results
     * @return list of log entries with exceptions
     */
    List<LogEntry> getLogsWithExceptions(int limit);
    
    /**
     * Gets the most recent logs.
     * 
     * @param limit maximum number of results
     * @return list of most recent log entries
     */
    List<LogEntry> getRecentLogs(int limit);
    
    /**
     * Gets all unique sources in the index.
     * 
     * @return set of unique log sources
     */
    Set<String> getAllSources();
    
    /**
     * Gets all unique loggers in the index.
     * 
     * @return set of unique logger names
     */
    Set<String> getAllLoggers();
    
    /**
     * Gets log statistics.
     * 
     * @return log search statistics
     */
    LogSearchStats getStats();
    
    /**
     * Clears all indexed logs.
     */
    void clear();
    
    /**
     * Gets the total number of indexed logs.
     * 
     * @return total number of logs
     */
    int getTotalLogs();
    
    /**
     * Rebuilds the search index.
     */
    void rebuildIndex();
}