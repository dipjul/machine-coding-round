package com.machinecoding.games.monopoly.model;

/**
 * Property groups (color groups) in Monopoly.
 */
public enum PropertyGroup {
    BROWN("Brown", 2),
    LIGHT_BLUE("Light Blue", 3),
    PINK("Pink", 3),
    ORANGE("Orange", 3),
    RED("Red", 3),
    YELLOW("Yellow", 3),
    GREEN("Green", 3),
    DARK_BLUE("Dark Blue", 2),
    RAILROAD("Railroad", 4),
    UTILITY("Utility", 2),
    SPECIAL("Special", 0);
    
    private final String displayName;
    private final int propertyCount;
    
    PropertyGroup(String displayName, int propertyCount) {
        this.displayName = displayName;
        this.propertyCount = propertyCount;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getPropertyCount() {
        return propertyCount;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}