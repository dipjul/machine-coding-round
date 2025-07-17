package com.machinecoding.games.snakeladder.service;

/**
 * Statistics for a Snake & Ladder game.
 */
public class GameStats {
    private final String gameId;
    private final int playerCount;
    private final int totalTurns;
    private final long duration;
    private final int totalSnakeHits;
    private final int totalLadderHits;
    private final double averageMovesPerPlayer;
    private final int leadingPlayerPosition;
    private final GameStatus status;
    
    public GameStats(String gameId, int playerCount, int totalTurns, long duration,
                    int totalSnakeHits, int totalLadderHits, double averageMovesPerPlayer,
                    int leadingPlayerPosition, GameStatus status) {
        this.gameId = gameId;
        this.playerCount = playerCount;
        this.totalTurns = totalTurns;
        this.duration = duration;
        this.totalSnakeHits = totalSnakeHits;
        this.totalLadderHits = totalLadderHits;
        this.averageMovesPerPlayer = averageMovesPerPlayer;
        this.leadingPlayerPosition = leadingPlayerPosition;
        this.status = status;
    }
    
    // Getters
    public String getGameId() { return gameId; }
    public int getPlayerCount() { return playerCount; }
    public int getTotalTurns() { return totalTurns; }
    public long getDuration() { return duration; }
    public int getTotalSnakeHits() { return totalSnakeHits; }
    public int getTotalLadderHits() { return totalLadderHits; }
    public double getAverageMovesPerPlayer() { return averageMovesPerPlayer; }
    public int getLeadingPlayerPosition() { return leadingPlayerPosition; }
    public GameStatus getStatus() { return status; }
    
    // Derived statistics
    public double getSnakeHitRate() {
        return totalTurns == 0 ? 0.0 : (double) totalSnakeHits / totalTurns;
    }
    
    public double getLadderHitRate() {
        return totalTurns == 0 ? 0.0 : (double) totalLadderHits / totalTurns;
    }
    
    public double getAverageTurnsPerMinute() {
        return duration == 0 ? 0.0 : (double) totalTurns / (duration / 60000.0);
    }
    
    public String getFormattedDuration() {
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        
        if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }
    
    @Override
    public String toString() {
        return String.format("GameStats{players=%d, turns=%d, duration=%s, snakes=%d, ladders=%d, status=%s}",
                           playerCount, totalTurns, getFormattedDuration(), 
                           totalSnakeHits, totalLadderHits, status);
    }
}