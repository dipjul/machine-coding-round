package com.machinecoding.games.snakeladder.service;

/**
 * Configuration settings for Snake & Ladder game.
 */
public class GameSettings {
    private final int minPlayers;
    private final int maxPlayers;
    private final int diceSides;
    private final boolean exactWinRequired;
    private final boolean additionalTurnOnMax;
    private final int maxTurnsPerGame;
    private final boolean allowPlayerElimination;
    
    public GameSettings() {
        this(2, 6, 6, true, false, 1000, false);
    }
    
    public GameSettings(int minPlayers, int maxPlayers, int diceSides, 
                       boolean exactWinRequired, boolean additionalTurnOnMax,
                       int maxTurnsPerGame, boolean allowPlayerElimination) {
        this.minPlayers = Math.max(2, minPlayers);
        this.maxPlayers = Math.max(this.minPlayers, maxPlayers);
        this.diceSides = Math.max(2, diceSides);
        this.exactWinRequired = exactWinRequired;
        this.additionalTurnOnMax = additionalTurnOnMax;
        this.maxTurnsPerGame = Math.max(100, maxTurnsPerGame);
        this.allowPlayerElimination = allowPlayerElimination;
    }
    
    // Getters
    public int getMinPlayers() { return minPlayers; }
    public int getMaxPlayers() { return maxPlayers; }
    public int getDiceSides() { return diceSides; }
    public boolean isExactWinRequired() { return exactWinRequired; }
    public boolean isAdditionalTurnOnMax() { return additionalTurnOnMax; }
    public int getMaxTurnsPerGame() { return maxTurnsPerGame; }
    public boolean isAllowPlayerElimination() { return allowPlayerElimination; }
    
    @Override
    public String toString() {
        return String.format("GameSettings{players=%d-%d, dice=%d, exactWin=%s, bonusTurn=%s}",
                           minPlayers, maxPlayers, diceSides, exactWinRequired, additionalTurnOnMax);
    }
}