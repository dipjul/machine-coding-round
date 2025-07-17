package com.machinecoding.search.logs.model;

/**
 * Log levels with severity ordering.
 */
public enum LogLevel {
    TRACE(1, "TRACE"),
    DEBUG(2, "DEBUG"),
    INFO(3, "INFO"),
    WARN(4, "WARN"),
    ERROR(5, "ERROR"),
    FATAL(6, "FATAL");
    
    private final int severity;
    private final String displayName;
    
    LogLevel(int severity, String displayName) {
        this.severity = severity;
        this.displayName = displayName;
    }
    
    public int getSeverity() {
        return severity;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Parses a log level from string, case-insensitive.
     */
    public static LogLevel fromString(String level) {
        if (level == null || level.trim().isEmpty()) {
            return INFO;
        }
        
        try {
            return LogLevel.valueOf(level.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return INFO;
        }
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}