package com.machinecoding.games.chess.model;

/**
 * Types of chess pieces.
 */
public enum PieceType {
    KING("King", 1000),
    QUEEN("Queen", 9),
    ROOK("Rook", 5),
    BISHOP("Bishop", 3),
    KNIGHT("Knight", 3),
    PAWN("Pawn", 1);
    
    private final String displayName;
    private final int value;
    
    PieceType(String displayName, int value) {
        this.displayName = displayName;
        this.value = value;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}