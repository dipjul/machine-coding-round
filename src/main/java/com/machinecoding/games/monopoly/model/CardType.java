package com.machinecoding.games.monopoly.model;

/**
 * Types of Chance and Community Chest cards.
 */
public enum CardType {
    MOVE("Move to Position"),
    MOVE_RELATIVE("Move Relative"),
    MOVE_NEAREST("Move to Nearest"),
    MONEY("Money Transaction"),
    GO_TO_JAIL("Go to Jail"),
    GET_OUT_OF_JAIL("Get Out of Jail Free"),
    COLLECT_FROM_PLAYERS("Collect from All Players"),
    PAY_TO_PLAYERS("Pay to All Players"),
    PROPERTY_TAX("Property Tax");
    
    private final String displayName;
    
    CardType(String displayName) {
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