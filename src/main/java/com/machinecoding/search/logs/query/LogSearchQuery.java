package com.machinecoding.search.logs.query;

import com.machinecoding.search.logs.model.LogLevel;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Represents a structured log search query with various filters.
 */
public class LogSearchQuery {
    private final String textQuery;
    private final List<String> keywords;
    private final Set<String> sources;
    private final Set<LogLevel> levels;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final String thread;
    private final String logger;
    private final boolean includeExceptions;
    private final int limit;
    private final SortOrder sortOrder;
    
    public LogSearchQuery(String textQuery, List<String> keywords, Set<String> sources,
                         Set<LogLevel> levels, LocalDateTime startTime, LocalDateTime endTime,
                         String thread, String logger, boolean includeExceptions,
                         int limit, SortOrder sortOrder) {
        this.textQuery = textQuery;
        this.keywords = keywords;
        this.sources = sources;
        this.levels = levels;
        this.startTime = startTime;
        this.endTime = endTime;
        this.thread = thread;
        this.logger = logger;
        this.includeExceptions = includeExceptions;
        this.limit = limit > 0 ? limit : 100;
        this.sortOrder = sortOrder != null ? sortOrder : SortOrder.TIMESTAMP_DESC;
    }
    
    // Getters
    public String getTextQuery() { return textQuery; }
    public List<String> getKeywords() { return keywords; }
    public Set<String> getSources() { return sources; }
    public Set<LogLevel> getLevels() { return levels; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public String getThread() { return thread; }
    public String getLogger() { return logger; }
    public boolean isIncludeExceptions() { return includeExceptions; }
    public int getLimit() { return limit; }
    public SortOrder getSortOrder() { return sortOrder; }
    
    /**
     * Checks if the query has any filters applied.
     */
    public boolean hasFilters() {
        return (textQuery != null && !textQuery.trim().isEmpty()) ||
               (keywords != null && !keywords.isEmpty()) ||
               (sources != null && !sources.isEmpty()) ||
               (levels != null && !levels.isEmpty()) ||
               startTime != null || endTime != null ||
               (thread != null && !thread.trim().isEmpty()) ||
               (logger != null && !logger.trim().isEmpty());
    }
    
    /**
     * Checks if the query is a simple text search.
     */
    public boolean isSimpleTextSearch() {
        return (textQuery != null && !textQuery.trim().isEmpty()) &&
               (keywords == null || keywords.isEmpty()) &&
               (sources == null || sources.isEmpty()) &&
               (levels == null || levels.isEmpty()) &&
               startTime == null && endTime == null &&
               (thread == null || thread.trim().isEmpty()) &&
               (logger == null || logger.trim().isEmpty());
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("LogSearchQuery{");
        
        if (textQuery != null && !textQuery.trim().isEmpty()) {
            sb.append("text='").append(textQuery).append("', ");
        }
        if (keywords != null && !keywords.isEmpty()) {
            sb.append("keywords=").append(keywords).append(", ");
        }
        if (sources != null && !sources.isEmpty()) {
            sb.append("sources=").append(sources).append(", ");
        }
        if (levels != null && !levels.isEmpty()) {
            sb.append("levels=").append(levels).append(", ");
        }
        if (startTime != null) {
            sb.append("startTime=").append(startTime).append(", ");
        }
        if (endTime != null) {
            sb.append("endTime=").append(endTime).append(", ");
        }
        if (thread != null && !thread.trim().isEmpty()) {
            sb.append("thread='").append(thread).append("', ");
        }
        if (logger != null && !logger.trim().isEmpty()) {
            sb.append("logger='").append(logger).append("', ");
        }
        
        sb.append("limit=").append(limit);
        sb.append(", sort=").append(sortOrder);
        sb.append("}");
        
        return sb.toString();
    }
    
    /**
     * Sort order for search results.
     */
    public enum SortOrder {
        TIMESTAMP_ASC("Timestamp Ascending"),
        TIMESTAMP_DESC("Timestamp Descending"),
        LEVEL_ASC("Level Ascending"),
        LEVEL_DESC("Level Descending"),
        SOURCE_ASC("Source Ascending"),
        SOURCE_DESC("Source Descending");
        
        private final String displayName;
        
        SortOrder(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
}