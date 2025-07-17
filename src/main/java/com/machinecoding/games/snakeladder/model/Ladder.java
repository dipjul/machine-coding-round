package com.machinecoding.games.snakeladder.model;

/**
 * Represents a ladder on the game board.
 */
public class Ladder {
    private final int bottom;
    private final int top;
    private final int length;
    private final String name;
    
    public Ladder(int bottom, int top) {
        this(bottom, top, "Ladder");
    }
    
    public Ladder(int bottom, int top, String name) {
        if (bottom >= top) {
            throw new IllegalArgumentException("Ladder bottom must be less than top");
        }
        
        this.bottom = bottom;
        this.top = top;
        this.length = top - bottom;
        this.name = name != null ? name.trim() : "Ladder";
    }
    
    // Getters
    public int getBottom() { return bottom; }
    public int getTop() { return top; }
    public int getLength() { return length; }
    public String getName() { return name; }
    
    // Utility methods
    public boolean isAt(int position) {
        return position == bottom;
    }
    
    public int getDestination() {
        return top;
    }
    
    public boolean isLongerThan(Ladder other) {
        return other != null && this.length > other.length;
    }
    
    public boolean isShorterThan(Ladder other) {
        return other != null && this.length < other.length;
    }
    
    // Ladder categories based on length
    public LadderSize getSize() {
        if (length <= 10) return LadderSize.SHORT;
        if (length <= 30) return LadderSize.MEDIUM;
        if (length <= 50) return LadderSize.TALL;
        return LadderSize.GIANT;
    }
    
    public enum LadderSize {
        SHORT("Short"),
        MEDIUM("Medium"),
        TALL("Tall"),
        GIANT("Giant");
        
        private final String displayName;
        
        LadderSize(String displayName) {
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
    
    @Override
    public String toString() {
        return String.format("Ladder{name='%s', bottom=%d, top=%d, length=%d, size=%s}",
                           name, bottom, top, length, getSize());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Ladder ladder = (Ladder) obj;
        return bottom == ladder.bottom && top == ladder.top;
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(bottom, top);
    }
}