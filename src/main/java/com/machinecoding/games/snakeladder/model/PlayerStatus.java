package com.machinecoding.games.snakeladder.model;

/**
 * Status of a player in the Snake & Ladder game.
 */
public enum PlayerStatus {
    ACTIVE("Active"),
    WON("Won"),
    ELIMINATED("Eliminated");
    
    private final String displayName;
    
    PlayerStatus(String displayName) {
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