package com.machinecoding.games.chess.service;

import com.machinecoding.games.chess.model.Move;

/**
 * Result of attempting to make a move in chess.
 */
public class MoveResult {
    private final boolean success;
    private final String message;
    private final Move move;
    
    public MoveResult(boolean success, String message, Move move) {
        this.success = success;
        this.message = message != null ? message : "";
        this.move = move;
    }
    
    // Getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Move getMove() { return move; }
    
    public boolean isFailure() { return !success; }
    
    @Override
    public String toString() {
        return String.format("MoveResult{success=%s, message='%s', move=%s}",
                           success, message, move != null ? move.toString() : "null");
    }
}