package com.machinecoding.games.monopoly.model;

/**
 * Status of a player in the Monopoly game.
 */
public enum PlayerStatus {
    ACTIVE("Active"),
    BANKRUPT("Bankrupt"),
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