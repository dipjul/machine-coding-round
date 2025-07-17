package com.machinecoding.games.monopoly.model;

/**
 * Types of properties in Monopoly.
 */
public enum PropertyType {
    STREET("Street"),
    RAILROAD("Railroad"),
    UTILITY("Utility"),
    SPECIAL("Special"); // GO, Jail, Free Parking, etc.
    
    private final String displayName;
    
    PropertyType(String displayName) {
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