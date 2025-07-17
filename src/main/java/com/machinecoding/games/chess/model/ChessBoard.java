package com.machinecoding.games.chess.model;

import java.util.*;

/**
 * Represents a chess board with pieces and game state.
 */
public class ChessBoard {
    private final Piece[][] board;
    private final Map<PieceColor, Set<Piece>> pieces;
    private final Map<PieceColor, Piece> kings;
    private final List<Move> moveHistory;
    
    public ChessBoard() {
        this.board = new Piece[8][8];
        this.pieces = new HashMap<>();
        this.pieces.put(PieceColor.WHITE, new HashSet<>());
        this.pieces.put(PieceColor.BLACK, new HashSet<>());
        this.kings = new HashMap<>();
        this.moveHistory = new ArrayList<>();
        initializeBoard();
    }
    
    private void initializeBoard() {
        // Initialize white pieces
        placePiece(new Piece(PieceType.ROOK, PieceColor.WHITE, new Position(1, 1)));
        placePiece(new Piece(PieceType.KNIGHT, PieceColor.WHITE, new Position(1, 2)));
        placePiece(new Piece(PieceType.BISHOP, PieceColor.WHITE, new Position(1, 3)));
        placePiece(new Piece(PieceType.QUEEN, PieceColor.WHITE, new Position(1, 4)));
        
        Piece whiteKing = new Piece(PieceType.KING, PieceColor.WHITE, new Position(1, 5));
        placePiece(whiteKing);
        kings.put(PieceColor.WHITE, whiteKing);
        
        placePiece(new Piece(PieceType.BISHOP, PieceColor.WHITE, new Position(1, 6)));
        placePiece(new Piece(PieceType.KNIGHT, PieceColor.WHITE, new Position(1, 7)));
        placePiece(new Piece(PieceType.ROOK, PieceColor.WHITE, new Position(1, 8)));
        
        // White pawns
        for (int file = 1; file <= 8; file++) {
            placePiece(new Piece(PieceType.PAWN, PieceColor.WHITE, new Position(2, file)));
        }
        
        // Initialize black pieces
        placePiece(new Piece(PieceType.ROOK, PieceColor.BLACK, new Position(8, 1)));
        placePiece(new Piece(PieceType.KNIGHT, PieceColor.BLACK, new Position(8, 2)));
        placePiece(new Piece(PieceType.BISHOP, PieceColor.BLACK, new Position(8, 3)));
        placePiece(new Piece(PieceType.QUEEN, PieceColor.BLACK, new Position(8, 4)));
        
        Piece blackKing = new Piece(PieceType.KING, PieceColor.BLACK, new Position(8, 5));
        placePiece(blackKing);
        kings.put(PieceColor.BLACK, blackKing);
        
        placePiece(new Piece(PieceType.BISHOP, PieceColor.BLACK, new Position(8, 6)));
        placePiece(new Piece(PieceType.KNIGHT, PieceColor.BLACK, new Position(8, 7)));
        placePiece(new Piece(PieceType.ROOK, PieceColor.BLACK, new Position(8, 8)));
        
        // Black pawns
        for (int file = 1; file <= 8; file++) {
            placePiece(new Piece(PieceType.PAWN, PieceColor.BLACK, new Position(7, file)));
        }
    }
    
    // Piece management
    public void placePiece(Piece piece) {
        if (piece == null || piece.getPosition() == null) {
            return;
        }
        
        Position pos = piece.getPosition();
        board[pos.getRank() - 1][pos.getFile() - 1] = piece;
        pieces.get(piece.getColor()).add(piece);
    }
    
    public void removePiece(Position position) {
        if (position == null) return;
        
        Piece piece = getPiece(position);
        if (piece != null) {
            board[position.getRank() - 1][position.getFile() - 1] = null;
            pieces.get(piece.getColor()).remove(piece);
        }
    }
    
    public Piece getPiece(Position position) {
        if (position == null || !position.isValid()) {
            return null;
        }
        return board[position.getRank() - 1][position.getFile() - 1];
    }
    
    public boolean isEmpty(Position position) {
        return getPiece(position) == null;
    }
    
    public boolean isOccupied(Position position) {
        return getPiece(position) != null;
    }
    
    public boolean isOccupiedByColor(Position position, PieceColor color) {
        Piece piece = getPiece(position);
        return piece != null && piece.getColor() == color;
    }
    
    public boolean isOccupiedByOpponent(Position position, PieceColor color) {
        Piece piece = getPiece(position);
        return piece != null && piece.getColor() != color;
    }
    
    // Move execution
    public boolean makeMove(Move move) {
        if (move == null || !isValidMove(move)) {
            return false;
        }
        
        Piece movingPiece = getPiece(move.getFrom());
        Piece capturedPiece = getPiece(move.getTo());
        
        // Handle capture
        if (capturedPiece != null) {
            removePiece(move.getTo());
            move.setCapturedPiece(capturedPiece);
        }
        
        // Move the piece
        removePiece(move.getFrom());
        movingPiece.setPosition(move.getTo());
        placePiece(movingPiece);
        
        // Handle special moves
        handleSpecialMoves(move, movingPiece);
        
        // Add to move history
        moveHistory.add(move);
        
        return true;
    }
    
    private void handleSpecialMoves(Move move, Piece movingPiece) {
        // Handle castling
        if (move.isCastling()) {
            handleCastling(move);
        }
        
        // Handle en passant
        if (move.isEnPassant()) {
            handleEnPassant(move);
        }
        
        // Handle pawn promotion
        if (move.isPromotion()) {
            handlePromotion(move, movingPiece);
        }
    }
    
    private void handleCastling(Move move) {
        // Move the rook for castling
        if (move.isKingsideCastling()) {
            Position rookFrom = new Position(move.getFrom().getRank(), 8);
            Position rookTo = new Position(move.getFrom().getRank(), 6);
            Piece rook = getPiece(rookFrom);
            if (rook != null) {
                removePiece(rookFrom);
                rook.setPosition(rookTo);
                placePiece(rook);
            }
        } else if (move.isQueensideCastling()) {
            Position rookFrom = new Position(move.getFrom().getRank(), 1);
            Position rookTo = new Position(move.getFrom().getRank(), 4);
            Piece rook = getPiece(rookFrom);
            if (rook != null) {
                removePiece(rookFrom);
                rook.setPosition(rookTo);
                placePiece(rook);
            }
        }
    }
    
    private void handleEnPassant(Move move) {
        // Remove the captured pawn in en passant
        int capturedPawnRank = move.getMovingPiece().isWhite() ? 
                              move.getTo().getRank() - 1 : move.getTo().getRank() + 1;
        Position capturedPawnPos = new Position(capturedPawnRank, move.getTo().getFile());
        Piece capturedPawn = getPiece(capturedPawnPos);
        if (capturedPawn != null) {
            removePiece(capturedPawnPos);
            move.setCapturedPiece(capturedPawn);
        }
    }
    
    private void handlePromotion(Move move, Piece movingPiece) {
        // Replace pawn with promoted piece
        removePiece(move.getTo());
        pieces.get(movingPiece.getColor()).remove(movingPiece);
        
        PieceType promotionType = move.getPromotionPiece() != null ? 
                                 move.getPromotionPiece() : PieceType.QUEEN;
        Piece promotedPiece = new Piece(promotionType, movingPiece.getColor(), move.getTo());
        placePiece(promotedPiece);
    }
    
    // Move validation
    public boolean isValidMove(Move move) {
        if (move == null) return false;
        
        Piece piece = getPiece(move.getFrom());
        if (piece == null) return false;
        
        // Check if it's a legal move for the piece type
        if (!isLegalMoveForPiece(piece, move)) {
            return false;
        }
        
        // Check if the path is clear (except for knights)
        if (piece.getType() != PieceType.KNIGHT && !isPathClear(move.getFrom(), move.getTo())) {
            return false;
        }
        
        // Check if destination is not occupied by same color
        Piece destinationPiece = getPiece(move.getTo());
        if (destinationPiece != null && destinationPiece.isSameColor(piece)) {
            return false;
        }
        
        // Check if move would leave king in check
        if (wouldLeaveKingInCheck(move, piece.getColor())) {
            return false;
        }
        
        return true;
    }
    
    private boolean isLegalMoveForPiece(Piece piece, Move move) {
        return switch (piece.getType()) {
            case PAWN -> isValidPawnMove(piece, move);
            case ROOK -> isValidRookMove(move);
            case BISHOP -> isValidBishopMove(move);
            case KNIGHT -> isValidKnightMove(move);
            case QUEEN -> isValidQueenMove(move);
            case KING -> isValidKingMove(piece, move);
        };
    }
    
    private boolean isValidPawnMove(Piece pawn, Move move) {
        int direction = pawn.isWhite() ? 1 : -1;
        int rankDiff = move.getTo().getRank() - move.getFrom().getRank();
        int fileDiff = Math.abs(move.getTo().getFile() - move.getFrom().getFile());
        
        // Forward move
        if (fileDiff == 0) {
            if (rankDiff == direction && isEmpty(move.getTo())) {
                return true; // One square forward
            }
            if (rankDiff == 2 * direction && !pawn.hasMoved() && 
                isEmpty(move.getTo()) && isEmpty(move.getFrom().add(direction, 0))) {
                return true; // Two squares forward from starting position
            }
        }
        
        // Diagonal capture
        if (fileDiff == 1 && rankDiff == direction) {
            if (isOccupiedByOpponent(move.getTo(), pawn.getColor())) {
                return true; // Regular capture
            }
            if (isEnPassantCapture(pawn, move)) {
                move.setEnPassant(true);
                return true; // En passant capture
            }
        }
        
        return false;
    }
    
    private boolean isValidRookMove(Move move) {
        return move.getFrom().isOnSameRank(move.getTo()) || 
               move.getFrom().isOnSameFile(move.getTo());
    }
    
    private boolean isValidBishopMove(Move move) {
        return move.getFrom().isOnSameDiagonal(move.getTo());
    }
    
    private boolean isValidKnightMove(Move move) {
        int rankDiff = Math.abs(move.getTo().getRank() - move.getFrom().getRank());
        int fileDiff = Math.abs(move.getTo().getFile() - move.getFrom().getFile());
        return (rankDiff == 2 && fileDiff == 1) || (rankDiff == 1 && fileDiff == 2);
    }
    
    private boolean isValidQueenMove(Move move) {
        return isValidRookMove(move) || isValidBishopMove(move);
    }
    
    private boolean isValidKingMove(Piece king, Move move) {
        // Regular king move (one square in any direction)
        if (move.getFrom().getChebyshevDistance(move.getTo()) == 1) {
            return true;
        }
        
        // Castling
        if (canCastle(king, move)) {
            move.setCastling(true);
            return true;
        }
        
        return false;
    }
    
    private boolean canCastle(Piece king, Move move) {
        if (king.hasMoved() || isInCheck(king.getColor())) {
            return false;
        }
        
        int fileDiff = move.getTo().getFile() - move.getFrom().getFile();
        if (Math.abs(fileDiff) != 2) {
            return false;
        }
        
        boolean isKingside = fileDiff > 0;
        Position rookPos = new Position(king.getPosition().getRank(), isKingside ? 8 : 1);
        Piece rook = getPiece(rookPos);
        
        if (rook == null || rook.getType() != PieceType.ROOK || 
            rook.hasMoved() || !rook.isSameColor(king)) {
            return false;
        }
        
        // Check if path is clear and not under attack
        int startFile = Math.min(king.getPosition().getFile(), rookPos.getFile()) + 1;
        int endFile = Math.max(king.getPosition().getFile(), rookPos.getFile()) - 1;
        
        for (int file = startFile; file <= endFile; file++) {
            Position pos = new Position(king.getPosition().getRank(), file);
            if (!isEmpty(pos)) {
                return false;
            }
        }
        
        // Check if king passes through check
        int direction = isKingside ? 1 : -1;
        for (int i = 0; i <= 2; i++) {
            Position pos = king.getPosition().add(0, i * direction);
            if (pos != null && isSquareUnderAttack(pos, king.getColor().opposite())) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean isEnPassantCapture(Piece pawn, Move move) {
        if (moveHistory.isEmpty()) return false;
        
        Move lastMove = moveHistory.get(moveHistory.size() - 1);
        Piece lastMovedPiece = lastMove.getMovingPiece();
        
        // Check if last move was a two-square pawn move
        if (lastMovedPiece.getType() != PieceType.PAWN) return false;
        if (Math.abs(lastMove.getTo().getRank() - lastMove.getFrom().getRank()) != 2) return false;
        
        // Check if the captured pawn is adjacent
        if (!lastMove.getTo().isOnSameRank(pawn.getPosition()) ||
            Math.abs(lastMove.getTo().getFile() - pawn.getPosition().getFile()) != 1) {
            return false;
        }
        
        // Check if moving to the square the pawn passed over
        int passedOverRank = (lastMove.getFrom().getRank() + lastMove.getTo().getRank()) / 2;
        Position passedOverSquare = new Position(passedOverRank, lastMove.getTo().getFile());
        
        return move.getTo().equals(passedOverSquare);
    }
    
    private boolean isPathClear(Position from, Position to) {
        int rankDir = Integer.compare(to.getRank(), from.getRank());
        int fileDir = Integer.compare(to.getFile(), from.getFile());
        
        Position current = from.add(rankDir, fileDir);
        while (current != null && !current.equals(to)) {
            if (!isEmpty(current)) {
                return false;
            }
            current = current.add(rankDir, fileDir);
        }
        
        return true;
    }
    
    // Check detection
    public boolean isInCheck(PieceColor color) {
        Piece king = kings.get(color);
        return king != null && isSquareUnderAttack(king.getPosition(), color.opposite());
    }
    
    public boolean isSquareUnderAttack(Position position, PieceColor attackingColor) {
        for (Piece piece : pieces.get(attackingColor)) {
            if (canPieceAttackSquare(piece, position)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean canPieceAttackSquare(Piece piece, Position target) {
        Move testMove = new Move(piece.getPosition(), target, piece);
        
        // For pawns, check diagonal attacks only
        if (piece.getType() == PieceType.PAWN) {
            int direction = piece.isWhite() ? 1 : -1;
            int rankDiff = target.getRank() - piece.getPosition().getRank();
            int fileDiff = Math.abs(target.getFile() - piece.getPosition().getFile());
            return rankDiff == direction && fileDiff == 1;
        }
        
        return isLegalMoveForPiece(piece, testMove) && 
               (piece.getType() == PieceType.KNIGHT || isPathClear(piece.getPosition(), target));
    }
    
    private boolean wouldLeaveKingInCheck(Move move, PieceColor color) {
        // Make a temporary move and check if king is in check
        Piece movingPiece = getPiece(move.getFrom());
        Piece capturedPiece = getPiece(move.getTo());
        
        // Temporarily make the move
        removePiece(move.getFrom());
        if (capturedPiece != null) {
            removePiece(move.getTo());
        }
        movingPiece.setPosition(move.getTo());
        placePiece(movingPiece);
        
        boolean inCheck = isInCheck(color);
        
        // Undo the move
        removePiece(move.getTo());
        movingPiece.setPosition(move.getFrom());
        placePiece(movingPiece);
        if (capturedPiece != null) {
            placePiece(capturedPiece);
        }
        
        return inCheck;
    }
    
    // Game state queries
    public List<Move> getLegalMoves(PieceColor color) {
        List<Move> legalMoves = new ArrayList<>();
        
        for (Piece piece : pieces.get(color)) {
            legalMoves.addAll(getLegalMovesForPiece(piece));
        }
        
        return legalMoves;
    }
    
    public List<Move> getLegalMovesForPiece(Piece piece) {
        List<Move> moves = new ArrayList<>();
        
        for (int rank = 1; rank <= 8; rank++) {
            for (int file = 1; file <= 8; file++) {
                Position to = new Position(rank, file);
                Move move = new Move(piece.getPosition(), to, piece);
                
                if (isValidMove(move)) {
                    moves.add(move);
                }
            }
        }
        
        return moves;
    }
    
    public boolean isCheckmate(PieceColor color) {
        return isInCheck(color) && getLegalMoves(color).isEmpty();
    }
    
    public boolean isStalemate(PieceColor color) {
        return !isInCheck(color) && getLegalMoves(color).isEmpty();
    }
    
    public boolean isDraw() {
        return isStalemate(PieceColor.WHITE) || isStalemate(PieceColor.BLACK) ||
               isInsufficientMaterial() || isThreefoldRepetition() || isFiftyMoveRule();
    }
    
    private boolean isInsufficientMaterial() {
        Set<Piece> whitePieces = pieces.get(PieceColor.WHITE);
        Set<Piece> blackPieces = pieces.get(PieceColor.BLACK);
        
        // King vs King
        if (whitePieces.size() == 1 && blackPieces.size() == 1) {
            return true;
        }
        
        // King and Bishop/Knight vs King
        if ((whitePieces.size() == 2 && blackPieces.size() == 1) ||
            (whitePieces.size() == 1 && blackPieces.size() == 2)) {
            
            Set<Piece> largerSet = whitePieces.size() > blackPieces.size() ? whitePieces : blackPieces;
            for (Piece piece : largerSet) {
                if (piece.getType() == PieceType.BISHOP || piece.getType() == PieceType.KNIGHT) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private boolean isThreefoldRepetition() {
        // Simplified implementation - would need position hashing in real implementation
        return false;
    }
    
    private boolean isFiftyMoveRule() {
        // Simplified implementation - would need to track half-moves since last pawn move or capture
        return false;
    }
    
    // Getters
    public Set<Piece> getPieces(PieceColor color) {
        return new HashSet<>(pieces.get(color));
    }
    
    public Piece getKing(PieceColor color) {
        return kings.get(color);
    }
    
    public List<Move> getMoveHistory() {
        return new ArrayList<>(moveHistory);
    }
    
    public Move getLastMove() {
        return moveHistory.isEmpty() ? null : moveHistory.get(moveHistory.size() - 1);
    }
    
    // Board display
    public String toFEN() {
        // Simplified FEN generation
        StringBuilder fen = new StringBuilder();
        
        for (int rank = 8; rank >= 1; rank--) {
            int emptyCount = 0;
            for (int file = 1; file <= 8; file++) {
                Piece piece = getPiece(new Position(rank, file));
                if (piece == null) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }
                    fen.append(piece.getSymbol());
                }
            }
            if (emptyCount > 0) {
                fen.append(emptyCount);
            }
            if (rank > 1) {
                fen.append('/');
            }
        }
        
        return fen.toString();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("  a b c d e f g h\n");
        
        for (int rank = 8; rank >= 1; rank--) {
            sb.append(rank).append(" ");
            for (int file = 1; file <= 8; file++) {
                Piece piece = getPiece(new Position(rank, file));
                if (piece == null) {
                    sb.append(". ");
                } else {
                    sb.append(piece.getSymbol()).append(" ");
                }
            }
            sb.append(rank).append("\n");
        }
        
        sb.append("  a b c d e f g h");
        return sb.toString();
    }
}