package com.machinecoding.games.chess.service;

import com.machinecoding.games.chess.model.*;
import java.util.*;

/**
 * Main chess game engine that manages game state and enforces rules.
 */
public class ChessGame {
    private final String gameId;
    private final ChessBoard board;
    private final Map<PieceColor, String> players;
    private PieceColor currentPlayer;
    private GameState gameState;
    private String winner;
    private final List<String> gameLog;
    private final long startTime;
    
    public ChessGame(String gameId) {
        this.gameId = gameId != null ? gameId.trim() : "";
        this.board = new ChessBoard();
        this.players = new HashMap<>();
        this.currentPlayer = PieceColor.WHITE; // White always starts
        this.gameState = GameState.WAITING_FOR_PLAYERS;
        this.winner = null;
        this.gameLog = new ArrayList<>();
        this.startTime = System.currentTimeMillis();
    }
    
    // Game setup
    public boolean addPlayer(String playerId, PieceColor color) {
        if (playerId == null || playerId.trim().isEmpty()) {
            return false;
        }
        
        if (gameState != GameState.WAITING_FOR_PLAYERS) {
            return false;
        }
        
        if (players.containsKey(color)) {
            return false; // Color already taken
        }
        
        players.put(color, playerId.trim());
        
        // Start game when both players are added
        if (players.size() == 2) {
            gameState = GameState.IN_PROGRESS;
            logEvent("Game started. White to move.");
        }
        
        return true;
    }
    
    public boolean startGame() {
        if (players.size() != 2) {
            return false;
        }
        
        gameState = GameState.IN_PROGRESS;
        currentPlayer = PieceColor.WHITE;
        logEvent("Game started. White to move.");
        return true;
    }
    
    // Move execution
    public MoveResult makeMove(String playerId, String moveNotation) {
        // Validate player and game state
        if (!isValidPlayerTurn(playerId)) {
            return new MoveResult(false, "Not your turn or invalid player", null);
        }
        
        if (gameState != GameState.IN_PROGRESS) {
            return new MoveResult(false, "Game is not in progress", null);
        }
        
        try {
            // Parse move notation
            Move move = parseMove(moveNotation);
            if (move == null) {
                return new MoveResult(false, "Invalid move notation", null);
            }
            
            // Validate and execute move
            if (!board.isValidMove(move)) {
                return new MoveResult(false, "Invalid move", null);
            }
            
            // Make the move
            boolean success = board.makeMove(move);
            if (!success) {
                return new MoveResult(false, "Failed to execute move", null);
            }
            
            // Update move with check/checkmate information
            updateMoveStatus(move);
            
            // Log the move
            logMove(move);
            
            // Switch players
            currentPlayer = currentPlayer.opposite();
            
            // Check game end conditions
            checkGameEnd();
            
            return new MoveResult(true, "Move executed successfully", move);
            
        } catch (Exception e) {
            return new MoveResult(false, "Error processing move: " + e.getMessage(), null);
        }
    }
    
    public MoveResult makeMove(String playerId, Position from, Position to) {
        return makeMove(playerId, from.toAlgebraic() + to.toAlgebraic());
    }
    
    public MoveResult makeMove(String playerId, Position from, Position to, PieceType promotionPiece) {
        String notation = from.toAlgebraic() + to.toAlgebraic();
        if (promotionPiece != null) {
            notation += Character.toLowerCase(promotionPiece.name().charAt(0));
        }
        return makeMove(playerId, notation);
    }
    
    private Move parseMove(String notation) {
        if (notation == null || notation.trim().isEmpty()) {
            return null;
        }
        
        notation = notation.trim();
        
        // Handle castling
        if ("O-O".equals(notation) || "0-0".equals(notation)) {
            return createCastlingMove(true); // Kingside
        }
        if ("O-O-O".equals(notation) || "0-0-0".equals(notation)) {
            return createCastlingMove(false); // Queenside
        }
        
        // Handle UCI notation (e2e4, e7e8q)
        if (notation.length() >= 4 && notation.charAt(1) >= '1' && notation.charAt(1) <= '8') {
            return parseUCIMove(notation);
        }
        
        // Handle algebraic notation (simplified)
        return parseAlgebraicMove(notation);
    }
    
    private Move createCastlingMove(boolean kingside) {
        int rank = currentPlayer == PieceColor.WHITE ? 1 : 8;
        Position kingFrom = new Position(rank, 5);
        Position kingTo = new Position(rank, kingside ? 7 : 3);
        
        Piece king = board.getPiece(kingFrom);
        if (king != null && king.getType() == PieceType.KING) {
            Move move = new Move(kingFrom, kingTo, king);
            move.setCastling(true);
            return move;
        }
        
        return null;
    }
    
    private Move parseUCIMove(String notation) {
        try {
            Position from = new Position(notation.substring(0, 2));
            Position to = new Position(notation.substring(2, 4));
            
            Piece piece = board.getPiece(from);
            if (piece == null) {
                return null;
            }
            
            Move move = new Move(from, to, piece);
            
            // Handle promotion
            if (notation.length() > 4) {
                char promotionChar = notation.charAt(4);
                PieceType promotionType = switch (Character.toLowerCase(promotionChar)) {
                    case 'q' -> PieceType.QUEEN;
                    case 'r' -> PieceType.ROOK;
                    case 'b' -> PieceType.BISHOP;
                    case 'n' -> PieceType.KNIGHT;
                    default -> null;
                };
                if (promotionType != null) {
                    move.setPromotionPiece(promotionType);
                }
            }
            
            return move;
        } catch (Exception e) {
            return null;
        }
    }
    
    private Move parseAlgebraicMove(String notation) {
        // Simplified algebraic notation parsing
        // This would need to be much more sophisticated for full algebraic notation
        try {
            // Remove check/checkmate indicators
            notation = notation.replace("+", "").replace("#", "");
            
            // Find destination square (last two characters that look like a square)
            String destSquare = null;
            for (int i = notation.length() - 2; i >= 0; i--) {
                String candidate = notation.substring(i, i + 2);
                if (candidate.matches("[a-h][1-8]")) {
                    destSquare = candidate;
                    break;
                }
            }
            
            if (destSquare == null) {
                return null;
            }
            
            Position to = new Position(destSquare);
            
            // Find the piece that can move to this square
            List<Move> legalMoves = board.getLegalMoves(currentPlayer);
            for (Move move : legalMoves) {
                if (move.getTo().equals(to)) {
                    // Additional disambiguation would be needed for full algebraic notation
                    return move;
                }
            }
            
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    private void updateMoveStatus(Move move) {
        PieceColor opponent = currentPlayer.opposite();
        
        if (board.isInCheck(opponent)) {
            move.setCheck(true);
            
            if (board.isCheckmate(opponent)) {
                move.setCheckmate(true);
            }
        }
    }
    
    private boolean isValidPlayerTurn(String playerId) {
        return playerId != null && playerId.equals(players.get(currentPlayer));
    }
    
    private void checkGameEnd() {
        PieceColor opponent = currentPlayer.opposite();
        
        if (board.isCheckmate(opponent)) {
            gameState = GameState.CHECKMATE;
            winner = players.get(currentPlayer);
            logEvent(currentPlayer + " wins by checkmate!");
        } else if (board.isStalemate(opponent)) {
            gameState = GameState.STALEMATE;
            logEvent("Game ends in stalemate - draw!");
        } else if (board.isDraw()) {
            gameState = GameState.DRAW;
            logEvent("Game ends in draw!");
        }
    }
    
    // Game queries
    public List<Move> getLegalMoves(String playerId) {
        if (!isValidPlayer(playerId)) {
            return new ArrayList<>();
        }
        
        PieceColor color = getPlayerColor(playerId);
        return color != null ? board.getLegalMoves(color) : new ArrayList<>();
    }
    
    public List<Move> getLegalMovesForPiece(String playerId, Position position) {
        if (!isValidPlayer(playerId)) {
            return new ArrayList<>();
        }
        
        Piece piece = board.getPiece(position);
        if (piece == null || piece.getColor() != getPlayerColor(playerId)) {
            return new ArrayList<>();
        }
        
        return board.getLegalMovesForPiece(piece);
    }
    
    public boolean isInCheck(String playerId) {
        PieceColor color = getPlayerColor(playerId);
        return color != null && board.isInCheck(color);
    }
    
    public boolean canCastle(String playerId, boolean kingside) {
        PieceColor color = getPlayerColor(playerId);
        if (color == null) return false;
        
        Piece king = board.getKing(color);
        if (king == null || king.hasMoved() || board.isInCheck(color)) {
            return false;
        }
        
        int rank = color == PieceColor.WHITE ? 1 : 8;
        Position kingTo = new Position(rank, kingside ? 7 : 3);
        Move castlingMove = new Move(king.getPosition(), kingTo, king);
        
        return board.isValidMove(castlingMove);
    }
    
    // Game state queries
    public GameState getGameState() { return gameState; }
    public String getWinner() { return winner; }
    public PieceColor getCurrentPlayer() { return currentPlayer; }
    public String getCurrentPlayerId() { return players.get(currentPlayer); }
    public ChessBoard getBoard() { return board; }
    public List<String> getGameLog() { return new ArrayList<>(gameLog); }
    public long getGameDuration() { return System.currentTimeMillis() - startTime; }
    
    public String getPlayer(PieceColor color) {
        return players.get(color);
    }
    
    public PieceColor getPlayerColor(String playerId) {
        for (Map.Entry<PieceColor, String> entry : players.entrySet()) {
            if (entry.getValue().equals(playerId)) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    public boolean isValidPlayer(String playerId) {
        return players.containsValue(playerId);
    }
    
    public int getMoveCount() {
        return board.getMoveHistory().size();
    }
    
    public Move getLastMove() {
        return board.getLastMove();
    }
    
    // Game control
    public boolean offerDraw(String playerId) {
        if (!isValidPlayer(playerId)) {
            return false;
        }
        
        logEvent(getPlayerColor(playerId) + " offers a draw");
        return true;
    }
    
    public boolean acceptDraw(String playerId) {
        if (!isValidPlayer(playerId)) {
            return false;
        }
        
        gameState = GameState.DRAW;
        logEvent("Draw accepted by " + getPlayerColor(playerId));
        return true;
    }
    
    public boolean resign(String playerId) {
        if (!isValidPlayer(playerId)) {
            return false;
        }
        
        PieceColor resigningColor = getPlayerColor(playerId);
        gameState = GameState.RESIGNATION;
        winner = players.get(resigningColor.opposite());
        logEvent(resigningColor + " resigns. " + resigningColor.opposite() + " wins!");
        return true;
    }
    
    // Logging
    private void logMove(Move move) {
        String moveStr = String.format("%d. %s", 
                                     (getMoveCount() + 1) / 2 + 1, 
                                     move.toAlgebraicNotation());
        gameLog.add(moveStr);
    }
    
    private void logEvent(String event) {
        gameLog.add(event);
    }
    
    // Game analysis
    public int getMaterialBalance() {
        int whiteValue = 0;
        int blackValue = 0;
        
        for (Piece piece : board.getPieces(PieceColor.WHITE)) {
            whiteValue += piece.getType().getValue();
        }
        
        for (Piece piece : board.getPieces(PieceColor.BLACK)) {
            blackValue += piece.getType().getValue();
        }
        
        return whiteValue - blackValue;
    }
    
    public Map<PieceType, Integer> getPieceCounts(PieceColor color) {
        Map<PieceType, Integer> counts = new HashMap<>();
        
        for (PieceType type : PieceType.values()) {
            counts.put(type, 0);
        }
        
        for (Piece piece : board.getPieces(color)) {
            counts.put(piece.getType(), counts.get(piece.getType()) + 1);
        }
        
        return counts;
    }
    
    @Override
    public String toString() {
        return String.format("ChessGame{id='%s', state=%s, currentPlayer=%s, moves=%d}",
                           gameId, gameState, currentPlayer, getMoveCount());
    }
}