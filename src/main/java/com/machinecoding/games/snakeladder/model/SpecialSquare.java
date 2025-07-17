package com.machinecoding.games.snakeladder.model;

/**
 * Represents a special square on the game board with unique effects.
 */
public class SpecialSquare {
    private final int position;
    private final SpecialSquareType type;
    private final String description;
    private final int effectValue;
    
    public SpecialSquare(int position, SpecialSquareType type, String description) {
        this(position, type, description, 0);
    }
    
    public SpecialSquare(int position, SpecialSquareType type, String description, int effectValue) {
        this.position = position;
        this.type = type != null ? type : SpecialSquareType.NORMAL;
        this.description = description != null ? description.trim() : "";
        this.effectValue = effectValue;
    }
    
    // Getters
    public int getPosition() { return position; }
    public SpecialSquareType getType() { return type; }
    public String getDescription() { return description; }
    public int getEffectValue() { return effectValue; }
    
    // Utility methods
    public boolean hasEffect() {
        return type != SpecialSquareType.NORMAL;
    }
    
    public boolean isPositive() {
        return type == SpecialSquareType.BONUS || type == SpecialSquareType.SAFE;
    }
    
    public boolean isNegative() {
        return type == SpecialSquareType.PENALTY || type == SpecialSquareType.TRAP;
    }
    
    public boolean isNeutral() {
        return type == SpecialSquareType.NORMAL || type == SpecialSquareType.SAFE;
    }
    
    @Override
    public String toString() {
        return String.format("SpecialSquare{position=%d, type=%s, description='%s', effect=%d}",
                           position, type, description, effectValue);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SpecialSquare that = (SpecialSquare) obj;
        return position == that.position && type == that.type;
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(position, type);
    }
}