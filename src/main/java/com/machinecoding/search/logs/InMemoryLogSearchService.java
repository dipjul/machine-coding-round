package com.machinecoding.search.logs;

import com.machinecoding.search.logs.model.LogEntry;
import com.machinecoding.search.logs.model.LogLevel;
import com.machinecoding.search.logs.query.LogSearchQuery;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * In-memory implementation of log search service.
 * 
 * Features:
 * - Full-text search with keyword indexing
 * - Multi-criteria filtering (level, source, time range)
 * - Efficient indexing with inverted index structure
 * - Real-time search with concurrent access support
 * - Comprehensive statistics and analytics
 */
public class InMemoryLogSearchService implements LogSearchService {
    
    // Primary storage
    private final List<LogEntry> logs;
    private final Map<String, LogEntry> logIndex; // id -> LogEntry
    
    // Inverted indexes for fast searching
    private final Map<String, Set<String>> keywordIndex; // keyword -> set of log IDs
    private final Map<String, Set<String>> sourceIndex; // source -> set of log IDs
    private final Map<LogLevel, Set<String>> levelIndex; // level -> set of log IDs
    private final Map<String, Set<String>> loggerIndex; // logger -> set of log IDs
    private final Set<String> exceptionLogIds; // log IDs with exceptions
    
    // Statistics
    private final Map<String, Integer> logCountByLevel;
    private final Map<String, Integer> logCountBySource;
    
    // Configuration
    private final Set<String> stopWords;
    private final Pattern wordPattern;
    
    public InMemoryLogSearchService() {
        this.logs = new CopyOnWriteArrayList<>();
        this.logIndex = new ConcurrentHashMap<>();
        this.keywordIndex = new ConcurrentHashMap<>();
        this.sourceIndex = new ConcurrentHashMap<>();
        this.levelIndex = new ConcurrentHashMap<>();
        this.loggerIndex = new ConcurrentHashMap<>();
        this.exceptionLogIds = ConcurrentHashMap.newKeySet();
        this.logCountByLevel = new ConcurrentHashMap<>();
        this.logCountBySource = new ConcurrentHashMap<>();
        
        // Initialize stop words (common words to ignore in indexing)
        this.stopWords = new HashSet<>(Arrays.asList(
            "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by",
            "is", "are", "was", "were", "be", "been", "have", "has", "had", "do", "does", "did",
            "will", "would", "could", "should", "may", "might", "can", "must", "shall"
        ));
        
        // Pattern for extracting words (alphanumeric sequences)
        this.wordPattern = Pattern.compile("\\b\\w+\\b");
    }
    
    @Override
    public void indexLog(LogEntry logEntry) {
        if (logEntry == null) {
            return;
        }
        
        // Add to primary storage
        logs.add(logEntry);
        logIndex.put(logEntry.getId(), logEntry);
        
        // Update inverted indexes
        indexKeywords(logEntry);
        indexSource(logEntry);
        indexLevel(logEntry);
        indexLogger(logEntry);
        indexException(logEntry);
        
        // Update statistics
        updateStatistics(logEntry);
    }
    
    @Override
    public void indexLogs(List<LogEntry> logEntries) {
        if (logEntries == null || logEntries.isEmpty()) {
            return;
        }
        
        for (LogEntry logEntry : logEntries) {
            indexLog(logEntry);
        }
    }
    
    @Override
    public List<LogEntry> search(LogSearchQuery query) {
        if (query == null || !query.hasFilters()) {
            return getRecentLogs(query != null ? query.getLimit() : 100);
        }
        
        Set<String> candidateIds = null;
        
        // Apply text/keyword filters
        if (query.getTextQuery() != null && !query.getTextQuery().trim().isEmpty()) {
            candidateIds = searchByText(query.getTextQuery());
        } else if (query.getKeywords() != null && !query.getKeywords().isEmpty()) {
            candidateIds = searchByKeywords(query.getKeywords());
        }
        
        // Apply source filter
        if (query.getSources() != null && !query.getSources().isEmpty()) {
            Set<String> sourceIds = new HashSet<>();
            for (String source : query.getSources()) {
                Set<String> ids = sourceIndex.get(source);
                if (ids != null) {
                    sourceIds.addAll(ids);
                }
            }
            candidateIds = intersect(candidateIds, sourceIds);
        }
        
        // Apply level filter
        if (query.getLevels() != null && !query.getLevels().isEmpty()) {
            Set<String> levelIds = new HashSet<>();
            for (LogLevel level : query.getLevels()) {
                Set<String> ids = levelIndex.get(level);
                if (ids != null) {
                    levelIds.addAll(ids);
                }
            }
            candidateIds = intersect(candidateIds, levelIds);
        }
        
        // Apply logger filter
        if (query.getLogger() != null && !query.getLogger().trim().isEmpty()) {
            Set<String> loggerIds = loggerIndex.get(query.getLogger());
            candidateIds = intersect(candidateIds, loggerIds);
        }
        
        // Apply exception filter
        if (query.isIncludeExceptions()) {
            candidateIds = intersect(candidateIds, exceptionLogIds);
        }
        
        // If no candidates found, return empty list
        if (candidateIds == null || candidateIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Convert IDs to LogEntry objects and apply remaining filters
        List<LogEntry> results = candidateIds.stream()
                .map(logIndex::get)
                .filter(Objects::nonNull)
                .filter(log -> matchesTimeRange(log, query.getStartTime(), query.getEndTime()))
                .filter(log -> matchesThread(log, query.getThread()))
                .collect(Collectors.toList());
        
        // Sort results
        sortResults(results, query.getSortOrder());
        
        // Apply limit
        return results.stream()
                .limit(query.getLimit())
                .collect(Collectors.toList());
    }
    
    @Override
    public List<LogEntry> searchText(String text, int limit) {
        if (text == null || text.trim().isEmpty()) {
            return getRecentLogs(limit);
        }
        
        Set<String> candidateIds = searchByText(text);
        
        return candidateIds.stream()
                .map(logIndex::get)
                .filter(Objects::nonNull)
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<LogEntry> searchByLevelAndTime(LogLevel level, LocalDateTime startTime, 
                                              LocalDateTime endTime, int limit) {
        return logs.stream()
                .filter(log -> log.matchesLevel(level))
                .filter(log -> log.isInTimeRange(startTime, endTime))
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<LogEntry> searchBySource(String source, int limit) {
        Set<String> sourceIds = sourceIndex.get(source);
        if (sourceIds == null || sourceIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        return sourceIds.stream()
                .map(logIndex::get)
                .filter(Objects::nonNull)
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<LogEntry> getLogsWithExceptions(int limit) {
        return exceptionLogIds.stream()
                .map(logIndex::get)
                .filter(Objects::nonNull)
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<LogEntry> getRecentLogs(int limit) {
        return logs.stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    @Override
    public Set<String> getAllSources() {
        return new HashSet<>(sourceIndex.keySet());
    }
    
    @Override
    public Set<String> getAllLoggers() {
        return new HashSet<>(loggerIndex.keySet());
    }
    
    @Override
    public LogSearchStats getStats() {
        int totalLogs = logs.size();
        int uniqueSources = sourceIndex.size();
        int uniqueLoggers = loggerIndex.size();
        
        Map<String, Integer> logsByLevel = new HashMap<>(logCountByLevel);
        Map<String, Integer> logsBySource = new HashMap<>(logCountBySource);
        
        long indexSize = estimateIndexSize();
        int totalKeywords = keywordIndex.size();
        
        double averageLogLength = logs.stream()
                .mapToInt(log -> log.getMessage().length())
                .average()
                .orElse(0.0);
        
        String oldestTime = logs.stream()
                .min(Comparator.comparing(LogEntry::getTimestamp))
                .map(log -> log.getTimestamp().toString())
                .orElse("N/A");
        
        String newestTime = logs.stream()
                .max(Comparator.comparing(LogEntry::getTimestamp))
                .map(log -> log.getTimestamp().toString())
                .orElse("N/A");
        
        return new LogSearchStats(totalLogs, uniqueSources, uniqueLoggers, logsByLevel, logsBySource,
                                indexSize, totalKeywords, averageLogLength, oldestTime, newestTime);
    }
    
    @Override
    public void clear() {
        logs.clear();
        logIndex.clear();
        keywordIndex.clear();
        sourceIndex.clear();
        levelIndex.clear();
        loggerIndex.clear();
        exceptionLogIds.clear();
        logCountByLevel.clear();
        logCountBySource.clear();
    }
    
    @Override
    public int getTotalLogs() {
        return logs.size();
    }
    
    @Override
    public void rebuildIndex() {
        // Clear indexes but keep logs
        List<LogEntry> currentLogs = new ArrayList<>(logs);
        clear();
        
        // Rebuild indexes
        for (LogEntry log : currentLogs) {
            indexLog(log);
        }
    }
    
    // Private helper methods
    
    private void indexKeywords(LogEntry logEntry) {
        String fullText = logEntry.getFullText().toLowerCase();
        Set<String> words = extractWords(fullText);
        
        for (String word : words) {
            if (!stopWords.contains(word) && word.length() > 2) {
                keywordIndex.computeIfAbsent(word, k -> ConcurrentHashMap.newKeySet())
                           .add(logEntry.getId());
            }
        }
    }
    
    private void indexSource(LogEntry logEntry) {
        String source = logEntry.getSource();
        if (source != null && !source.trim().isEmpty()) {
            sourceIndex.computeIfAbsent(source, k -> ConcurrentHashMap.newKeySet())
                      .add(logEntry.getId());
        }
    }
    
    private void indexLevel(LogEntry logEntry) {
        LogLevel level = logEntry.getLevel();
        levelIndex.computeIfAbsent(level, k -> ConcurrentHashMap.newKeySet())
                  .add(logEntry.getId());
    }
    
    private void indexLogger(LogEntry logEntry) {
        String logger = logEntry.getLogger();
        if (logger != null && !logger.trim().isEmpty()) {
            loggerIndex.computeIfAbsent(logger, k -> ConcurrentHashMap.newKeySet())
                      .add(logEntry.getId());
        }
    }
    
    private void indexException(LogEntry logEntry) {
        if (logEntry.getException() != null) {
            exceptionLogIds.add(logEntry.getId());
        }
    }
    
    private void updateStatistics(LogEntry logEntry) {
        logCountByLevel.merge(logEntry.getLevel().toString(), 1, Integer::sum);
        logCountBySource.merge(logEntry.getSource(), 1, Integer::sum);
    }
    
    private Set<String> extractWords(String text) {
        Set<String> words = new HashSet<>();
        String[] tokens = text.split("\\W+");
        
        for (String token : tokens) {
            if (token.length() > 0) {
                words.add(token.toLowerCase());
            }
        }
        
        return words;
    }
    
    private Set<String> searchByText(String text) {
        Set<String> words = extractWords(text.toLowerCase());
        Set<String> result = null;
        
        for (String word : words) {
            if (!stopWords.contains(word) && word.length() > 2) {
                Set<String> wordIds = keywordIndex.get(word);
                if (wordIds != null) {
                    if (result == null) {
                        result = new HashSet<>(wordIds);
                    } else {
                        result.retainAll(wordIds); // Intersection (AND operation)
                    }
                }
            }
        }
        
        return result != null ? result : new HashSet<>();
    }
    
    private Set<String> searchByKeywords(List<String> keywords) {
        Set<String> result = null;
        
        for (String keyword : keywords) {
            Set<String> keywordIds = keywordIndex.get(keyword.toLowerCase());
            if (keywordIds != null) {
                if (result == null) {
                    result = new HashSet<>(keywordIds);
                } else {
                    result.retainAll(keywordIds); // Intersection (AND operation)
                }
            }
        }
        
        return result != null ? result : new HashSet<>();
    }
    
    private Set<String> intersect(Set<String> set1, Set<String> set2) {
        if (set1 == null) return set2;
        if (set2 == null) return set1;
        
        Set<String> result = new HashSet<>(set1);
        result.retainAll(set2);
        return result;
    }
    
    private boolean matchesTimeRange(LogEntry log, LocalDateTime startTime, LocalDateTime endTime) {
        return log.isInTimeRange(startTime, endTime);
    }
    
    private boolean matchesThread(LogEntry log, String thread) {
        if (thread == null || thread.trim().isEmpty()) {
            return true;
        }
        return Objects.equals(log.getThread(), thread);
    }
    
    private void sortResults(List<LogEntry> results, LogSearchQuery.SortOrder sortOrder) {
        switch (sortOrder) {
            case TIMESTAMP_ASC:
                results.sort(Comparator.comparing(LogEntry::getTimestamp));
                break;
            case TIMESTAMP_DESC:
                results.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
                break;
            case LEVEL_ASC:
                results.sort(Comparator.comparing(log -> log.getLevel().getSeverity()));
                break;
            case LEVEL_DESC:
                results.sort((a, b) -> Integer.compare(b.getLevel().getSeverity(), a.getLevel().getSeverity()));
                break;
            case SOURCE_ASC:
                results.sort(Comparator.comparing(LogEntry::getSource));
                break;
            case SOURCE_DESC:
                results.sort((a, b) -> b.getSource().compareTo(a.getSource()));
                break;
        }
    }
    
    private long estimateIndexSize() {
        // Rough estimation of index memory usage
        long size = 0;
        size += keywordIndex.size() * 50; // Keyword index
        size += sourceIndex.size() * 30; // Source index
        size += levelIndex.size() * 20; // Level index
        size += loggerIndex.size() * 30; // Logger index
        size += logs.size() * 200; // Log entries
        return size;
    }
}