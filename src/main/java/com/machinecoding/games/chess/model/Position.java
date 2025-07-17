package com.machinecoding.games.chess.model;

/**
 * Represents a position on the chess board using rank and file.
 */
public class Position {
    private final int rank; // 1-8 (rows)
    private final int file; // 1-8 (columns, a-h)
    
    public Position(int rank, int file) {
        if (rank < 1 || rank > 8 || file < 1 || file > 8) {
            throw new IllegalArgumentException("Invalid position: rank and file must be between 1 and 8");
        }
        this.rank = rank;
        this.file = file;
    }
    
    public Position(String algebraic) {
        if (algebraic == null || algebraic.length() != 2) {
            throw new IllegalArgumentException("Invalid algebraic notation: " + algebraic);
        }
        
        char fileChar = algebraic.charAt(0);
        char rankChar = algebraic.charAt(1);
        
        if (fileChar < 'a' || fileChar > 'h' || rankChar < '1' || rankChar > '8') {
            throw new IllegalArgumentException("Invalid algebraic notation: " + algebraic);
        }
        
        this.file = fileChar - 'a' + 1;
        this.rank = rankChar - '1' + 1;
    }
    
    // Getters
    public int getRank() { return rank; }
    public int getFile() { return file; }
    
    // Utility methods
    public String toAlgebraic() {
        return String.valueOf((char)('a' + file - 1)) + rank;
    }
    
    public boolean isValid() {
        return rank >= 1 && rank <= 8 && file >= 1 && file <= 8;
    }
    
    public boolean isLightSquare() {
        return (rank + file) % 2 == 0;
    }
    
    public boolean isDarkSquare() {
        return (rank + file) % 2 == 1;
    }
    
    // Movement calculations
    public Position add(int rankOffset, int fileOffset) {
        int newRank = rank + rankOffset;
        int newFile = file + fileOffset;
        
        if (newRank < 1 || newRank > 8 || newFile < 1 || newFile > 8) {
            return null; // Invalid position
        }
        
        return new Position(newRank, newFile);
    }
    
    public int getRankDistance(Position other) {
        return Math.abs(this.rank - other.rank);
    }
    
    public int getFileDistance(Position other) {
        return Math.abs(this.file - other.file);
    }
    
    public int getManhattanDistance(Position other) {
        return getRankDistance(other) + getFileDistance(other);
    }
    
    public int getChebyshevDistance(Position other) {
        return Math.max(getRankDistance(other), getFileDistance(other));
    }
    
    public boolean isOnSameRank(Position other) {
        return this.rank == other.rank;
    }
    
    public boolean isOnSameFile(Position other) {
        return this.file == other.file;
    }
    
    public boolean isOnSameDiagonal(Position other) {
        return Math.abs(this.rank - other.rank) == Math.abs(this.file - other.file);
    }
    
    public boolean isAdjacent(Position other) {
        return getChebyshevDistance(other) == 1;
    }
    
    // Direction calculations
    public int getRankDirection(Position other) {
        return Integer.compare(other.rank, this.rank);
    }
    
    public int getFileDirection(Position other) {
        return Integer.compare(other.file, this.file);
    }
    
    @Override
    public String toString() {
        return toAlgebraic();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Position position = (Position) obj;
        return rank == position.rank && file == position.file;
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(rank, file);
    }
}