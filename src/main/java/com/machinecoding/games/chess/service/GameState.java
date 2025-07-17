package com.machinecoding.games.chess.service;

/**
 * Represents the current state of a chess game.
 */
public enum GameState {
    WAITING_FOR_PLAYERS("Waiting for Players"),
    IN_PROGRESS("In Progress"),
    CHECKMATE("Checkmate"),
    STALEMATE("Stalemate"),
    DRAW("Draw"),
    RESIGNATION("Resignation"),
    TIMEOUT("Timeout"),
    ABANDONED("Abandoned");
    
    private final String displayName;
    
    GameState(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isGameOver() {
        return this != WAITING_FOR_PLAYERS && this != IN_PROGRESS;
    }
    
    public boolean hasWinner() {
        return this == CHECKMATE || this == RESIGNATION || this == TIMEOUT;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}