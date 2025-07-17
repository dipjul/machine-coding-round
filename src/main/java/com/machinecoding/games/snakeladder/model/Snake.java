package com.machinecoding.games.snakeladder.model;

/**
 * Represents a snake on the game board.
 */
public class Snake {
    private final int head;
    private final int tail;
    private final int length;
    private final String name;
    
    public Snake(int head, int tail) {
        this(head, tail, "Snake");
    }
    
    public Snake(int head, int tail, String name) {
        if (head <= tail) {
            throw new IllegalArgumentException("Snake head must be greater than tail");
        }
        
        this.head = head;
        this.tail = tail;
        this.length = head - tail;
        this.name = name != null ? name.trim() : "Snake";
    }
    
    // Getters
    public int getHead() { return head; }
    public int getTail() { return tail; }
    public int getLength() { return length; }
    public String getName() { return name; }
    
    // Utility methods
    public boolean isAt(int position) {
        return position == head;
    }
    
    public int getDestination() {
        return tail;
    }
    
    public boolean isLongerThan(Snake other) {
        return other != null && this.length > other.length;
    }
    
    public boolean isShorterThan(Snake other) {
        return other != null && this.length < other.length;
    }
    
    // Snake categories based on length
    public SnakeSize getSize() {
        if (length <= 10) return SnakeSize.SMALL;
        if (length <= 30) return SnakeSize.MEDIUM;
        if (length <= 50) return SnakeSize.LARGE;
        return SnakeSize.GIANT;
    }
    
    public enum SnakeSize {
        SMALL("Small"),
        MEDIUM("Medium"),
        LARGE("Large"),
        GIANT("Giant");
        
        private final String displayName;
        
        SnakeSize(String displayName) {
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
        return String.format("Snake{name='%s', head=%d, tail=%d, length=%d, size=%s}",
                           name, head, tail, length, getSize());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Snake snake = (Snake) obj;
        return head == snake.head && tail == snake.tail;
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(head, tail);
    }
}