package com.machinecoding.games.snakeladder.model;

/**
 * Types of special squares in Snake & Ladder game.
 */
public enum SpecialSquareType {
    NORMAL("Normal", "Regular square with no special effects"),
    BONUS("Bonus", "Provides positive effects like extra moves or rolls"),
    PENALTY("Penalty", "Provides negative effects like skipping turns"),
    SAFE("Safe", "Protected from negative effects like snakes"),
    TRAP("Trap", "Temporary negative effect that can be escaped"),
    TELEPORT("Teleport", "Moves player to a different position"),
    MULTIPLIER("Multiplier", "Multiplies dice roll or movement");
    
    private final String displayName;
    private final String description;
    
    SpecialSquareType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isPositive() {
        return this == BONUS || this == SAFE || this == MULTIPLIER;
    }
    
    public boolean isNegative() {
        return this == PENALTY || this == TRAP;
    }
    
    public boolean isNeutral() {
        return this == NORMAL || this == TELEPORT;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}