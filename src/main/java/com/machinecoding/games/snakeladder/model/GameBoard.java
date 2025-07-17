package com.machinecoding.games.snakeladder.model;

import java.util.*;

/**
 * Represents the Snake & Ladder game board with snakes and ladders.
 */
public class GameBoard {
    private final int size;
    private final Map<Integer, Snake> snakes;
    private final Map<Integer, Ladder> ladders;
    private final Map<Integer, SpecialSquare> specialSquares;
    
    public GameBoard(int size) {
        this.size = Math.max(10, size); // Minimum board size of 10
        this.snakes = new HashMap<>();
        this.ladders = new HashMap<>();
        this.specialSquares = new HashMap<>();
        initializeDefaultBoard();
    }
    
    public GameBoard() {
        this(100); // Default 10x10 board (100 squares)
    }
    
    private void initializeDefaultBoard() {
        // Add default snakes (head -> tail)
        addSnake(99, 78);  // Near the end
        addSnake(95, 75);
        addSnake(92, 88);
        addSnake(87, 24);
        addSnake(64, 60);
        addSnake(62, 19);
        addSnake(56, 53);
        addSnake(49, 11);
        addSnake(47, 26);
        addSnake(16, 6);
        
        // Add default ladders (bottom -> top)
        addLadder(2, 38);
        addLadder(7, 14);
        addLadder(8, 31);
        addLadder(15, 26);
        addLadder(21, 42);
        addLadder(28, 84);
        addLadder(36, 44);
        addLadder(51, 67);
        addLadder(71, 91);
        addLadder(78, 98);
        
        // Add some special squares
        addSpecialSquare(13, SpecialSquareType.BONUS, "Roll again!");
        addSpecialSquare(33, SpecialSquareType.PENALTY, "Skip next turn");
        addSpecialSquare(66, SpecialSquareType.BONUS, "Move forward 3 spaces");
        addSpecialSquare(77, SpecialSquareType.SAFE, "Safe zone - no snakes");
    }
    
    // Snake management
    public boolean addSnake(int head, int tail) {
        if (!isValidPosition(head) || !isValidPosition(tail) || head <= tail) {
            return false;
        }
        
        // Check if positions are already occupied by ladders
        if (ladders.containsKey(head) || ladders.containsKey(tail)) {
            return false;
        }
        
        Snake snake = new Snake(head, tail);
        snakes.put(head, snake);
        return true;
    }
    
    public boolean removeSnake(int head) {
        return snakes.remove(head) != null;
    }
    
    public Snake getSnake(int position) {
        return snakes.get(position);
    }
    
    public boolean hasSnake(int position) {
        return snakes.containsKey(position);
    }
    
    // Ladder management
    public boolean addLadder(int bottom, int top) {
        if (!isValidPosition(bottom) || !isValidPosition(top) || bottom >= top) {
            return false;
        }
        
        // Check if positions are already occupied by snakes
        if (snakes.containsKey(bottom) || snakes.containsKey(top)) {
            return false;
        }
        
        Ladder ladder = new Ladder(bottom, top);
        ladders.put(bottom, ladder);
        return true;
    }
    
    public boolean removeLadder(int bottom) {
        return ladders.remove(bottom) != null;
    }
    
    public Ladder getLadder(int position) {
        return ladders.get(position);
    }
    
    public boolean hasLadder(int position) {
        return ladders.containsKey(position);
    }
    
    // Special square management
    public boolean addSpecialSquare(int position, SpecialSquareType type, String description) {
        if (!isValidPosition(position)) {
            return false;
        }
        
        SpecialSquare specialSquare = new SpecialSquare(position, type, description);
        specialSquares.put(position, specialSquare);
        return true;
    }
    
    public SpecialSquare getSpecialSquare(int position) {
        return specialSquares.get(position);
    }
    
    public boolean hasSpecialSquare(int position) {
        return specialSquares.containsKey(position);
    }
    
    // Position validation and movement
    public boolean isValidPosition(int position) {
        return position >= 1 && position <= size;
    }
    
    public boolean isWinningPosition(int position) {
        return position >= size;
    }
    
    public int getValidPosition(int position) {
        if (position < 1) return 1;
        if (position > size) return size;
        return position;
    }
    
    // Board queries
    public int getSize() { return size; }
    
    public Set<Integer> getSnakeHeads() {
        return new HashSet<>(snakes.keySet());
    }
    
    public Set<Integer> getLadderBottoms() {
        return new HashSet<>(ladders.keySet());
    }
    
    public Set<Integer> getSpecialSquarePositions() {
        return new HashSet<>(specialSquares.keySet());
    }
    
    public int getSnakeCount() { return snakes.size(); }
    public int getLadderCount() { return ladders.size(); }
    public int getSpecialSquareCount() { return specialSquares.size(); }
    
    // Board analysis
    public List<Snake> getAllSnakes() {
        return new ArrayList<>(snakes.values());
    }
    
    public List<Ladder> getAllLadders() {
        return new ArrayList<>(ladders.values());
    }
    
    public List<SpecialSquare> getAllSpecialSquares() {
        return new ArrayList<>(specialSquares.values());
    }
    
    public Snake getLongestSnake() {
        return snakes.values().stream()
                .max(Comparator.comparingInt(Snake::getLength))
                .orElse(null);
    }
    
    public Ladder getLongestLadder() {
        return ladders.values().stream()
                .max(Comparator.comparingInt(Ladder::getLength))
                .orElse(null);
    }
    
    // Board visualization helpers
    public String getSquareDescription(int position) {
        if (!isValidPosition(position)) {
            return "Invalid position";
        }
        
        List<String> descriptions = new ArrayList<>();
        
        if (hasSnake(position)) {
            Snake snake = getSnake(position);
            descriptions.add("Snake head (goes to " + snake.getTail() + ")");
        }
        
        if (hasLadder(position)) {
            Ladder ladder = getLadder(position);
            descriptions.add("Ladder bottom (goes to " + ladder.getTop() + ")");
        }
        
        if (hasSpecialSquare(position)) {
            SpecialSquare special = getSpecialSquare(position);
            descriptions.add(special.getType() + ": " + special.getDescription());
        }
        
        if (descriptions.isEmpty()) {
            return "Normal square";
        }
        
        return String.join(", ", descriptions);
    }
    
    // Board statistics
    public BoardStats getStatistics() {
        int totalSnakeLength = snakes.values().stream()
                .mapToInt(Snake::getLength)
                .sum();
        
        int totalLadderLength = ladders.values().stream()
                .mapToInt(Ladder::getLength)
                .sum();
        
        double averageSnakeLength = snakes.isEmpty() ? 0.0 : 
                (double) totalSnakeLength / snakes.size();
        
        double averageLadderLength = ladders.isEmpty() ? 0.0 : 
                (double) totalLadderLength / ladders.size();
        
        return new BoardStats(size, snakes.size(), ladders.size(), specialSquares.size(),
                            totalSnakeLength, totalLadderLength, averageSnakeLength, averageLadderLength);
    }
    
    @Override
    public String toString() {
        return String.format("GameBoard{size=%d, snakes=%d, ladders=%d, specialSquares=%d}",
                           size, snakes.size(), ladders.size(), specialSquares.size());
    }
    
    // Inner class for board statistics
    public static class BoardStats {
        private final int boardSize;
        private final int snakeCount;
        private final int ladderCount;
        private final int specialSquareCount;
        private final int totalSnakeLength;
        private final int totalLadderLength;
        private final double averageSnakeLength;
        private final double averageLadderLength;
        
        public BoardStats(int boardSize, int snakeCount, int ladderCount, int specialSquareCount,
                         int totalSnakeLength, int totalLadderLength, 
                         double averageSnakeLength, double averageLadderLength) {
            this.boardSize = boardSize;
            this.snakeCount = snakeCount;
            this.ladderCount = ladderCount;
            this.specialSquareCount = specialSquareCount;
            this.totalSnakeLength = totalSnakeLength;
            this.totalLadderLength = totalLadderLength;
            this.averageSnakeLength = averageSnakeLength;
            this.averageLadderLength = averageLadderLength;
        }
        
        // Getters
        public int getBoardSize() { return boardSize; }
        public int getSnakeCount() { return snakeCount; }
        public int getLadderCount() { return ladderCount; }
        public int getSpecialSquareCount() { return specialSquareCount; }
        public int getTotalSnakeLength() { return totalSnakeLength; }
        public int getTotalLadderLength() { return totalLadderLength; }
        public double getAverageSnakeLength() { return averageSnakeLength; }
        public double getAverageLadderLength() { return averageLadderLength; }
        
        @Override
        public String toString() {
            return String.format("BoardStats{size=%d, snakes=%d(avg=%.1f), ladders=%d(avg=%.1f), special=%d}",
                               boardSize, snakeCount, averageSnakeLength, ladderCount, averageLadderLength, specialSquareCount);
        }
    }
}