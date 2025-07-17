package com.machinecoding.search.logs;

import java.util.Map;

/**
 * Statistics for the log search service.
 */
public class LogSearchStats {
    private final int totalLogs;
    private final int uniqueSources;
    private final int uniqueLoggers;
    private final Map<String, Integer> logsByLevel;
    private final Map<String, Integer> logsBySource;
    private final long indexSizeBytes;
    private final int totalKeywords;
    private final double averageLogLength;
    private final String oldestLogTime;
    private final String newestLogTime;
    
    public LogSearchStats(int totalLogs, int uniqueSources, int uniqueLoggers,
                         Map<String, Integer> logsByLevel, Map<String, Integer> logsBySource,
                         long indexSizeBytes, int totalKeywords, double averageLogLength,
                         String oldestLogTime, String newestLogTime) {
        this.totalLogs = totalLogs;
        this.uniqueSources = uniqueSources;
        this.uniqueLoggers = uniqueLoggers;
        this.logsByLevel = logsByLevel;
        this.logsBySource = logsBySource;
        this.indexSizeBytes = indexSizeBytes;
        this.totalKeywords = totalKeywords;
        this.averageLogLength = averageLogLength;
        this.oldestLogTime = oldestLogTime;
        this.newestLogTime = newestLogTime;
    }
    
    // Getters
    public int getTotalLogs() { return totalLogs; }
    public int getUniqueSources() { return uniqueSources; }
    public int getUniqueLoggers() { return uniqueLoggers; }
    public Map<String, Integer> getLogsByLevel() { return logsByLevel; }
    public Map<String, Integer> getLogsBySource() { return logsBySource; }
    public long getIndexSizeBytes() { return indexSizeBytes; }
    public int getTotalKeywords() { return totalKeywords; }
    public double getAverageLogLength() { return averageLogLength; }
    public String getOldestLogTime() { return oldestLogTime; }
    public String getNewestLogTime() { return newestLogTime; }
    
    @Override
    public String toString() {
        return String.format(
            "LogSearchStats{logs=%d, sources=%d, loggers=%d, keywords=%d, " +
            "avgLength=%.1f, indexSize=%d bytes, timeRange=%s to %s}",
            totalLogs, uniqueSources, uniqueLoggers, totalKeywords,
            averageLogLength, indexSizeBytes, oldestLogTime, newestLogTime
        );
    }
}