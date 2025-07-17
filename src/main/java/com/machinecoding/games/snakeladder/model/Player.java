package com.machinecoding.games.snakeladder.model;

/**
 * Represents a player in the Snake & Ladder game.
 */
public class Player {
    private final String playerId;
    private final String name;
    private int position;
    private int totalMoves;
    private int snakeEncounters;
    private int ladderEncounters;
    private PlayerStatus status;
    
    public Player(String playerId, String name) {
        this.playerId = playerId != null ? playerId.trim() : "";
        this.name = name != null ? name.trim() : "";
        this.position = 1; // Start at position 1
        this.totalMoves = 0;
        this.snakeEncounters = 0;
        this.ladderEncounters = 0;
        this.status = PlayerStatus.ACTIVE;
    }
    
    // Getters
    public String getPlayerId() { return playerId; }
    public String getName() { return name; }
    public int getPosition() { return position; }
    public int getTotalMoves() { return totalMoves; }
    public int getSnakeEncounters() { return snakeEncounters; }
    public int getLadderEncounters() { return ladderEncounters; }
    public PlayerStatus getStatus() { return status; }
    
    // Position management
    public void setPosition(int newPosition) {
        this.position = Math.max(1, newPosition);
    }
    
    public void moveBy(int steps) {
        this.position += steps;
        this.totalMoves++;
    }
    
    public void moveTo(int newPosition) {
        this.position = newPosition;
    }
    
    // Game events
    public void encounterSnake() {
        this.snakeEncounters++;
    }
    
    public void encounterLadder() {
        this.ladderEncounters++;
    }
    
    public void incrementMoves() {
        this.totalMoves++;
    }
    
    // Status management
    public void setStatus(PlayerStatus status) {
        this.status = status;
    }
    
    public boolean isActive() {
        return status == PlayerStatus.ACTIVE;
    }
    
    public boolean hasWon() {
        return status == PlayerStatus.WON;
    }
    
    public boolean isEliminated() {
        return status == PlayerStatus.ELIMINATED;
    }
    
    // Statistics
    public double getAveragePositionGain() {
        return totalMoves > 0 ? (double) (position - 1) / totalMoves : 0.0;
    }
    
    public int getNetSpecialEncounters() {
        return ladderEncounters - snakeEncounters;
    }
    
    @Override
    public String toString() {
        return String.format("Player{id='%s', name='%s', position=%d, moves=%d, status=%s}",
                           playerId, name, position, totalMoves, status);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Player player = (Player) obj;
        return playerId.equals(player.playerId);
    }
    
    @Override
    public int hashCode() {
        return playerId.hashCode();
    }
}