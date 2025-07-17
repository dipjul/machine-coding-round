package com.machinecoding.games.snakeladder.service;

/**
 * Represents the current status of a Snake & Ladder game.
 */
public enum GameStatus {
    WAITING_FOR_PLAYERS("Waiting for Players"),
    IN_PROGRESS("In Progress"),
    FINISHED("Finished"),
    PAUSED("Paused"),
    ABANDONED("Abandoned");
    
    private final String displayName;
    
    GameStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isActive() {
        return this == IN_PROGRESS;
    }
    
    public boolean isGameOver() {
        return this == FINISHED || this == ABANDONED;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}