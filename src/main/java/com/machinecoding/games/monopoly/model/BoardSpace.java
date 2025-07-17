package com.machinecoding.games.monopoly.model;

import java.math.BigDecimal;

/**
 * Represents a space on the Monopoly board.
 */
public class BoardSpace {
    private final int position;
    private final String name;
    private final BoardSpaceType type;
    private final Property property; // null for non-property spaces
    private final BigDecimal taxAmount; // for tax spaces
    
    // Constructor for property spaces
    public BoardSpace(int position, String name, BoardSpaceType type, Property property) {
        this.position = position;
        this.name = name != null ? name.trim() : "";
        this.type = type != null ? type : BoardSpaceType.SPECIAL;
        this.property = property;
        this.taxAmount = BigDecimal.ZERO;
    }
    
    // Constructor for tax spaces
    public BoardSpace(int position, String name, BoardSpaceType type, BigDecimal taxAmount) {
        this.position = position;
        this.name = name != null ? name.trim() : "";
        this.type = type != null ? type : BoardSpaceType.SPECIAL;
        this.property = null;
        this.taxAmount = taxAmount != null ? taxAmount : BigDecimal.ZERO;
    }
    
    // Constructor for special spaces (GO, Jail, Free Parking, etc.)
    public BoardSpace(int position, String name, BoardSpaceType type) {
        this.position = position;
        this.name = name != null ? name.trim() : "";
        this.type = type != null ? type : BoardSpaceType.SPECIAL;
        this.property = null;
        this.taxAmount = BigDecimal.ZERO;
    }
    
    // Getters
    public int getPosition() { return position; }
    public String getName() { return name; }
    public BoardSpaceType getType() { return type; }
    public Property getProperty() { return property; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    
    // Utility methods
    public boolean isProperty() {
        return property != null;
    }
    
    public boolean isTax() {
        return type == BoardSpaceType.TAX;
    }
    
    public boolean isSpecial() {
        return type == BoardSpaceType.GO || type == BoardSpaceType.JAIL || 
               type == BoardSpaceType.FREE_PARKING || type == BoardSpaceType.GO_TO_JAIL;
    }
    
    public boolean isCard() {
        return type == BoardSpaceType.CHANCE || type == BoardSpaceType.COMMUNITY_CHEST;
    }
    
    @Override
    public String toString() {
        return String.format("BoardSpace{position=%d, name='%s', type=%s}", 
                           position, name, type);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BoardSpace space = (BoardSpace) obj;
        return position == space.position;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(position);
    }
}