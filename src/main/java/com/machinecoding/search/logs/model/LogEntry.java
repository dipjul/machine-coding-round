package com.machinecoding.search.logs.model;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a log entry with metadata and content.
 */
public class LogEntry {
    private final String id;
    private final LocalDateTime timestamp;
    private final LogLevel level;
    private final String source;
    private final String message;
    private final String thread;
    private final String logger;
    private final Map<String, String> metadata;
    private final Throwable exception;
    
    public LogEntry(LocalDateTime timestamp, LogLevel level, String source, String message) {
        this(timestamp, level, source, message, null, null, null, null);
    }
    
    public LogEntry(LocalDateTime timestamp, LogLevel level, String source, String message,
                   String thread, String logger, Map<String, String> metadata, Throwable exception) {
        this.id = UUID.randomUUID().toString();
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.level = level != null ? level : LogLevel.INFO;
        this.source = source != null ? source : "unknown";
        this.message = message != null ? message : "";
        this.thread = thread;
        this.logger = logger;
        this.metadata = metadata;
        this.exception = exception;
    }
    
    // Getters
    public String getId() { return id; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public LogLevel getLevel() { return level; }
    public String getSource() { return source; }
    public String getMessage() { return message; }
    public String getThread() { return thread; }
    public String getLogger() { return logger; }
    public Map<String, String> getMetadata() { return metadata; }
    public Throwable getException() { return exception; }
    
    /**
     * Gets the full text content for indexing (message + metadata + exception).
     */
    public String getFullText() {
        StringBuilder fullText = new StringBuilder(message);
        
        if (metadata != null && !metadata.isEmpty()) {
            fullText.append(" ");
            metadata.forEach((key, value) -> 
                fullText.append(key).append(":").append(value).append(" "));
        }
        
        if (exception != null) {
            fullText.append(" ").append(exception.getClass().getSimpleName());
            if (exception.getMessage() != null) {
                fullText.append(" ").append(exception.getMessage());
            }
        }
        
        return fullText.toString();
    }
    
    /**
     * Checks if this log entry matches the given time range.
     */
    public boolean isInTimeRange(LocalDateTime start, LocalDateTime end) {
        if (start != null && timestamp.isBefore(start)) {
            return false;
        }
        if (end != null && timestamp.isAfter(end)) {
            return false;
        }
        return true;
    }
    
    /**
     * Checks if this log entry matches the given log level or higher severity.
     */
    public boolean matchesLevel(LogLevel minLevel) {
        return level.getSeverity() >= minLevel.getSeverity();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LogEntry logEntry = (LogEntry) o;
        return Objects.equals(id, logEntry.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s %s - %s", 
                           timestamp.toString().substring(11, 19), 
                           level, source, message);
    }
}