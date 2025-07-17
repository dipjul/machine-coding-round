package com.machinecoding.games.monopoly.service;

/**
 * Status of a Monopoly game.
 */
public enum GameStatus {
    WAITING_FOR_PLAYERS("Waiting for Players"),
    IN_PROGRESS("In Progress"),
    PAUSED("Paused"),
    FINISHED("Finished"),
    CANCELLED("Cancelled");
    
    private final String displayName;
    
    GameStatus(String displayName) {
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