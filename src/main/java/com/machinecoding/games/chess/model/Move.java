package com.machinecoding.games.chess.model;

/**
 * Represents a chess move with all necessary information.
 */
public class Move {
    private final Position from;
    private final Position to;
    private final Piece movingPiece;
    private Piece capturedPiece;
    private boolean isCastling;
    private boolean isEnPassant;
    private boolean isPromotion;
    private PieceType promotionPiece;
    private boolean isCheck;
    private boolean isCheckmate;
    private final long timestamp;
    
    public Move(Position from, Position to, Piece movingPiece) {
        this.from = from;
        this.to = to;
        this.movingPiece = movingPiece;
        this.capturedPiece = null;
        this.isCastling = false;
        this.isEnPassant = false;
        this.isPromotion = false;
        this.promotionPiece = null;
        this.isCheck = false;
        this.isCheckmate = false;
        this.timestamp = System.currentTimeMillis();
        
        // Auto-detect promotion for pawns reaching the end
        if (movingPiece != null && movingPiece.getType() == PieceType.PAWN) {
            int targetRank = to.getRank();
            if ((movingPiece.isWhite() && targetRank == 8) || 
                (movingPiece.isBlack() && targetRank == 1)) {
                this.isPromotion = true;
                this.promotionPiece = PieceType.QUEEN; // Default promotion
            }
        }
    }
    
    public Move(String algebraic, Piece movingPiece) {
        this(parseFromAlgebraic(algebraic), parseToAlgebraic(algebraic), movingPiece);
    }
    
    private static Position parseFromAlgebraic(String algebraic) {
        // Simplified parsing - would need more complex logic for full algebraic notation
        if (algebraic.length() >= 4) {
            return new Position(algebraic.substring(0, 2));
        }
        throw new IllegalArgumentException("Invalid algebraic notation: " + algebraic);
    }
    
    private static Position parseToAlgebraic(String algebraic) {
        // Simplified parsing - would need more complex logic for full algebraic notation
        if (algebraic.length() >= 4) {
            return new Position(algebraic.substring(2, 4));
        }
        throw new IllegalArgumentException("Invalid algebraic notation: " + algebraic);
    }
    
    // Getters
    public Position getFrom() { return from; }
    public Position getTo() { return to; }
    public Piece getMovingPiece() { return movingPiece; }
    public Piece getCapturedPiece() { return capturedPiece; }
    public boolean isCastling() { return isCastling; }
    public boolean isEnPassant() { return isEnPassant; }
    public boolean isPromotion() { return isPromotion; }
    public PieceType getPromotionPiece() { return promotionPiece; }
    public boolean isCheck() { return isCheck; }
    public boolean isCheckmate() { return isCheckmate; }
    public long getTimestamp() { return timestamp; }
    
    // Setters for move properties
    public void setCapturedPiece(Piece capturedPiece) {
        this.capturedPiece = capturedPiece;
    }
    
    public void setCastling(boolean castling) {
        this.isCastling = castling;
    }
    
    public void setEnPassant(boolean enPassant) {
        this.isEnPassant = enPassant;
    }
    
    public void setPromotion(boolean promotion) {
        this.isPromotion = promotion;
    }
    
    public void setPromotionPiece(PieceType promotionPiece) {
        this.promotionPiece = promotionPiece;
        this.isPromotion = promotionPiece != null;
    }
    
    public void setCheck(boolean check) {
        this.isCheck = check;
    }
    
    public void setCheckmate(boolean checkmate) {
        this.isCheckmate = checkmate;
        if (checkmate) {
            this.isCheck = true;
        }
    }
    
    // Move type queries
    public boolean isCapture() {
        return capturedPiece != null;
    }
    
    public boolean isKingsideCastling() {
        return isCastling && to.getFile() > from.getFile();
    }
    
    public boolean isQueensideCastling() {
        return isCastling && to.getFile() < from.getFile();
    }
    
    public boolean isPawnMove() {
        return movingPiece != null && movingPiece.getType() == PieceType.PAWN;
    }
    
    public boolean isPawnDoubleMove() {
        return isPawnMove() && Math.abs(to.getRank() - from.getRank()) == 2;
    }
    
    // Algebraic notation
    public String toAlgebraicNotation() {
        StringBuilder notation = new StringBuilder();
        
        if (isCastling) {
            return isKingsideCastling() ? "O-O" : "O-O-O";
        }
        
        // Add piece symbol (except for pawns)
        if (movingPiece.getType() != PieceType.PAWN) {
            notation.append(Character.toUpperCase(movingPiece.getSymbol()));
        }
        
        // Add capture notation
        if (isCapture() || isEnPassant) {
            if (movingPiece.getType() == PieceType.PAWN) {
                notation.append((char)('a' + from.getFile() - 1));
            }
            notation.append('x');
        }
        
        // Add destination square
        notation.append(to.toAlgebraic());
        
        // Add promotion
        if (isPromotion && promotionPiece != null) {
            notation.append('=').append(Character.toUpperCase(promotionPiece.name().charAt(0)));
        }
        
        // Add en passant notation
        if (isEnPassant) {
            notation.append(" e.p.");
        }
        
        // Add check/checkmate
        if (isCheckmate) {
            notation.append('#');
        } else if (isCheck) {
            notation.append('+');
        }
        
        return notation.toString();
    }
    
    public String toUCINotation() {
        StringBuilder uci = new StringBuilder();
        uci.append(from.toAlgebraic());
        uci.append(to.toAlgebraic());
        
        if (isPromotion && promotionPiece != null) {
            uci.append(Character.toLowerCase(promotionPiece.name().charAt(0)));
        }
        
        return uci.toString();
    }
    
    @Override
    public String toString() {
        return toAlgebraicNotation();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Move move = (Move) obj;
        return from.equals(move.from) && to.equals(move.to) && 
               movingPiece.equals(move.movingPiece) &&
               isCastling == move.isCastling && isEnPassant == move.isEnPassant &&
               isPromotion == move.isPromotion && promotionPiece == move.promotionPiece;
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(from, to, movingPiece, isCastling, isEnPassant, isPromotion, promotionPiece);
    }
}