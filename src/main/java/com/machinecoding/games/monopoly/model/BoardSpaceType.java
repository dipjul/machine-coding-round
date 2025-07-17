package com.machinecoding.games.monopoly.model;

/**
 * Types of spaces on the Monopoly board.
 */
public enum BoardSpaceType {
    GO("GO"),
    PROPERTY("Property"),
    CHANCE("Chance"),
    COMMUNITY_CHEST("Community Chest"),
    TAX("Tax"),
    RAILROAD("Railroad"),
    UTILITY("Utility"),
    JAIL("Jail"),
    FREE_PARKING("Free Parking"),
    GO_TO_JAIL("Go to Jail"),
    SPECIAL("Special");
    
    private final String displayName;
    
    BoardSpaceType(String displayName) {
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