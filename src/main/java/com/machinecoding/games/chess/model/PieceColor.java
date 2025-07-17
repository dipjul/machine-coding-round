package com.machinecoding.games.chess.model;

/**
 * Colors of chess pieces.
 */
public enum PieceColor {
    WHITE("White"),
    BLACK("Black");
    
    private final String displayName;
    
    PieceColor(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public PieceColor opposite() {
        return this == WHITE ? BLACK : WHITE;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}