package com.machinecoding.games.chess.model;

/**
 * Represents a chess piece with its type, color, and position.
 */
public class Piece {
    private final PieceType type;
    private final PieceColor color;
    private Position position;
    private boolean hasMoved;
    
    public Piece(PieceType type, PieceColor color, Position position) {
        this.type = type != null ? type : PieceType.PAWN;
        this.color = color != null ? color : PieceColor.WHITE;
        this.position = position;
        this.hasMoved = false;
    }
    
    // Getters
    public PieceType getType() { return type; }
    public PieceColor getColor() { return color; }
    public Position getPosition() { return position; }
    public boolean hasMoved() { return hasMoved; }
    
    // Position management
    public void setPosition(Position newPosition) {
        this.position = newPosition;
        this.hasMoved = true;
    }
    
    public void markAsMoved() {
        this.hasMoved = true;
    }
    
    // Utility methods
    public boolean isWhite() {
        return color == PieceColor.WHITE;
    }
    
    public boolean isBlack() {
        return color == PieceColor.BLACK;
    }
    
    public boolean isSameColor(Piece other) {
        return other != null && this.color == other.color;
    }
    
    public boolean isOpponentColor(Piece other) {
        return other != null && this.color != other.color;
    }
    
    public char getSymbol() {
        char symbol = switch (type) {
            case KING -> 'K';
            case QUEEN -> 'Q';
            case ROOK -> 'R';
            case BISHOP -> 'B';
            case KNIGHT -> 'N';
            case PAWN -> 'P';
        };
        
        return isWhite() ? symbol : Character.toLowerCase(symbol);
    }
    
    public String getUnicodeSymbol() {
        return switch (type) {
            case KING -> isWhite() ? "♔" : "♚";
            case QUEEN -> isWhite() ? "♕" : "♛";
            case ROOK -> isWhite() ? "♖" : "♜";
            case BISHOP -> isWhite() ? "♗" : "♝";
            case KNIGHT -> isWhite() ? "♘" : "♞";
            case PAWN -> isWhite() ? "♙" : "♟";
        };
    }
    
    @Override
    public String toString() {
        return String.format("%s %s at %s", color, type, position);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Piece piece = (Piece) obj;
        return type == piece.type && color == piece.color && 
               position != null && position.equals(piece.position);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(type, color, position);
    }
}