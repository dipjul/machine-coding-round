package com.machinecoding.games.snakeladder.service;

import com.machinecoding.games.snakeladder.model.Player;

/**
 * Represents the result of a player's turn in Snake & Ladder game.
 */
public class TurnResult {
    private final Player player;
    private final int diceRoll;
    private final int startPosition;
    private final int endPosition;
    private final String description;
    private final long timestamp;
    
    public TurnResult(Player player, int diceRoll, int startPosition, int endPosition, String description) {
        this.player = player;
        this.diceRoll = diceRoll;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.description = description != null ? description : "";
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters
    public Player getPlayer() { return player; }
    public int getDiceRoll() { return diceRoll; }
    public int getStartPosition() { return startPosition; }
    public int getEndPosition() { return endPosition; }
    public String getDescription() { return description; }
    public long getTimestamp() { return timestamp; }
    
    // Derived properties
    public int getMovement() {
        return endPosition - startPosition;
    }
    
    public boolean isWinningMove() {
        return player != null && player.hasWon();
    }
    
    public boolean isSnakeEncounter() {
        return getMovement() < 0 && Math.abs(getMovement()) > diceRoll;
    }
    
    public boolean isLadderClimb() {
        return getMovement() > diceRoll;
    }
    
    public boolean isNormalMove() {
        return getMovement() == diceRoll;
    }
    
    @Override
    public String toString() {
        return String.format("%s rolled %d: %d → %d (%s)",
                           player != null ? player.getName() : "Unknown",
                           diceRoll, startPosition, endPosition, description);
    }
}