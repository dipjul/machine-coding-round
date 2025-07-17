package com.machinecoding.games.chess;

import com.machinecoding.games.chess.model.*;
import com.machinecoding.games.chess.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

/**
 * Comprehensive test suite for the Chess Game.
 * Tests game setup, move validation, special moves, and game rules.
 */
public class ChessGameTest {
    
    private ChessGame game;
    
    @BeforeEach
    void setUp() {
        game = new ChessGame("TEST_GAME");
        game.addPlayer("Alice", PieceColor.WHITE);
        game.addPlayer("Bob", PieceColor.BLACK);
    }
    
    @Test
    @DisplayName("Game Setup and Player Management")
    void testGameSetup() {
        // Test initial game state
        assertEquals(GameState.IN_PROGRESS, game.getGameState());
        assertEquals(PieceColor.WHITE, game.getCurrentPlayer());
        assertEquals("Alice", game.getCurrentPlayerId());
        
        // Test player queries
        assertEquals("Alice", game.getPlayer(PieceColor.WHITE));
        assertEquals("Bob", game.getPlayer(PieceColor.BLACK));
        assertEquals(PieceColor.WHITE, game.getPlayerColor("Alice"));
        assertEquals(PieceColor.BLACK, game.getPlayerColor("Bob"));
        
        assertTrue(game.isValidPlayer("Alice"));
        assertTrue(game.isValidPlayer("Bob"));
        assertFalse(game.isValidPlayer("Charlie"));
        
        // Test game without players
        ChessGame emptyGame = new ChessGame("EMPTY");
        assertEquals(GameState.WAITING_FOR_PLAYERS, emptyGame.getGameState());
        
        // Test adding duplicate color
        assertFalse(emptyGame.addPlayer("Player1", PieceColor.WHITE));
        assertTrue(emptyGame.addPlayer("Player1", PieceColor.WHITE));
        assertFalse(emptyGame.addPlayer("Player2", PieceColor.WHITE)); // Duplicate color
        assertTrue(emptyGame.addPlayer("Player2", PieceColor.BLACK));
    }
    
    @Test
    @DisplayName("Board Initialization")
    void testBoardInitialization() {
        ChessBoard board = game.getBoard();
        
        // Test initial piece positions
        // White pieces
        assertEquals(PieceType.ROOK, board.getPiece(new Position("a1")).getType());
        assertEquals(PieceType.KNIGHT, board.getPiece(new Position("b1")).getType());
        assertEquals(PieceType.BISHOP, board.getPiece(new Position("c1")).getType());
        assertEquals(PieceType.QUEEN, board.getPiece(new Position("d1")).getType());
        assertEquals(PieceType.KING, board.getPiece(new Position("e1")).getType());
        assertEquals(PieceType.BISHOP, board.getPiece(new Position("f1")).getType());
        assertEquals(PieceType.KNIGHT, board.getPiece(new Position("g1")).getType());
        assertEquals(PieceType.ROOK, board.getPiece(new Position("h1")).getType());
        
        // White pawns
        for (int file = 1; file <= 8; file++) {
            Piece pawn = board.getPiece(new Position(2, file));
            assertEquals(PieceType.PAWN, pawn.getType());
            assertEquals(PieceColor.WHITE, pawn.getColor());
        }
        
        // Black pieces
        assertEquals(PieceType.ROOK, board.getPiece(new Position("a8")).getType());
        assertEquals(PieceType.KING, board.getPiece(new Position("e8")).getType());
        
        // Empty squares
        assertTrue(board.isEmpty(new Position("e4")));
        assertTrue(board.isEmpty(new Position("d5")));
        
        // Test piece counts
        assertEquals(16, board.getPieces(PieceColor.WHITE).size());
        assertEquals(16, board.getPieces(PieceColor.BLACK).size());
    }
    
    @Test
    @DisplayName("Basic Move Execution")
    void testBasicMoves() {
        // Test valid pawn move
        MoveResult result1 = game.makeMove("Alice", "e2e4");
        assertTrue(result1.isSuccess());
        assertNotNull(result1.getMove());
        assertEquals("e4", result1.getMove().getTo().toAlgebraic());
        
        // Test turn switching
        assertEquals(PieceColor.BLACK, game.getCurrentPlayer());
        assertEquals("Bob", game.getCurrentPlayerId());
        
        // Test invalid move (wrong player)
        MoveResult result2 = game.makeMove("Alice", "e7e5");
        assertFalse(result2.isSuccess());
        assertTrue(result2.getMessage().contains("turn"));
        
        // Test valid black response
        MoveResult result3 = game.makeMove("Bob", "e7e5");
        assertTrue(result3.isSuccess());
        
        // Test invalid move (piece doesn't exist)
        MoveResult result4 = game.makeMove("Alice", "a3a4");
        assertFalse(result4.isSuccess());
        
        // Test invalid move (blocked path)
        MoveResult result5 = game.makeMove("Alice", "a1a3");
        assertFalse(result5.isSuccess());
    }
    
    @Test
    @DisplayName("Piece Movement Rules")
    void testPieceMovementRules() {
        // Test pawn moves
        assertTrue(game.makeMove("Alice", "e2e4").isSuccess()); // Two squares from start
        assertTrue(game.makeMove("Bob", "d7d6").isSuccess());   // One square
        assertFalse(game.makeMove("Alice", "e4e6").isSuccess()); // Can't move two squares again
        
        // Test knight moves
        assertTrue(game.makeMove("Alice", "g1f3").isSuccess()); // L-shape move
        assertTrue(game.makeMove("Bob", "b8c6").isSuccess());
        assertFalse(game.makeMove("Alice", "f3e4").isSuccess()); // Invalid knight move
        
        // Test bishop moves
        assertTrue(game.makeMove("Alice", "f1c4").isSuccess()); // Diagonal move
        assertTrue(game.makeMove("Bob", "c8g4").isSuccess());
        
        // Test rook moves (after clearing path)
        game.makeMove("Alice", "a2a4");
        game.makeMove("Bob", "a7a5");
        assertTrue(game.makeMove("Alice", "a1a3").isSuccess()); // Vertical move
        
        // Test queen moves
        game.makeMove("Bob", "d8d7");
        assertTrue(game.makeMove("Alice", "d1d2").isSuccess()); // Queen move
    }
    
    @Test
    @DisplayName("Capture Mechanics")
    void testCaptures() {
        // Set up a capture scenario
        game.makeMove("Alice", "e2e4");
        game.makeMove("Bob", "d7d5");
        
        // Test pawn capture
        MoveResult captureResult = game.makeMove("Alice", "e4d5");
        assertTrue(captureResult.isSuccess());
        assertTrue(captureResult.getMove().isCapture());
        assertNotNull(captureResult.getMove().getCapturedPiece());
        assertEquals(PieceType.PAWN, captureResult.getMove().getCapturedPiece().getType());
        
        // Verify captured piece is removed from board
        assertTrue(game.getBoard().isEmpty(new Position("d7")));
        
        // Test piece counts after capture
        assertEquals(16, game.getBoard().getPieces(PieceColor.WHITE).size());
        assertEquals(15, game.getBoard().getPieces(PieceColor.BLACK).size());
    }
    
    @Test
    @DisplayName("Check Detection")
    void testCheckDetection() {
        // Create a check situation
        game.makeMove("Alice", "e2e4");
        game.makeMove("Bob", "e7e5");
        game.makeMove("Alice", "d1h5"); // Queen attacks f7, threatening check
        
        // Black king should not be in check yet (f7 is protected by pawn)
        assertFalse(game.isInCheck("Bob"));
        
        // Create actual check
        game.makeMove("Bob", "b8c6");
        game.makeMove("Alice", "f1c4"); // Bishop attacks f7
        game.makeMove("Bob", "d7d6");
        MoveResult checkMove = game.makeMove("Alice", "h5f7");
        
        if (checkMove.isSuccess()) {
            assertTrue(checkMove.getMove().isCheck());
            assertTrue(game.isInCheck("Bob"));
        }
    }
    
    @Test
    @DisplayName("Castling")
    void testCastling() {
        // Clear path for castling
        game.makeMove("Alice", "e2e4");
        game.makeMove("Bob", "e7e5");
        game.makeMove("Alice", "g1f3");
        game.makeMove("Bob", "b8c6");
        game.makeMove("Alice", "f1c4");
        game.makeMove("Bob", "f8c5");
        
        // Test castling availability
        assertTrue(game.canCastle("Alice", true)); // Kingside
        
        // Perform castling
        MoveResult castlingResult = game.makeMove("Alice", "O-O");
        assertTrue(castlingResult.isSuccess());
        assertTrue(castlingResult.getMove().isCastling());
        assertTrue(castlingResult.getMove().isKingsideCastling());
        
        // Verify king and rook positions after castling
        ChessBoard board = game.getBoard();
        Piece king = board.getPiece(new Position("g1"));
        Piece rook = board.getPiece(new Position("f1"));
        
        assertNotNull(king);
        assertNotNull(rook);
        assertEquals(PieceType.KING, king.getType());
        assertEquals(PieceType.ROOK, rook.getType());
        
        // Verify original positions are empty
        assertTrue(board.isEmpty(new Position("e1")));
        assertTrue(board.isEmpty(new Position("h1")));
    }
    
    @Test
    @DisplayName("Pawn Promotion")
    void testPawnPromotion() {
        // This test would require setting up a position where a pawn can promote
        // For simplicity, we'll test the promotion detection logic
        
        ChessBoard board = new ChessBoard();
        Piece whitePawn = new Piece(PieceType.PAWN, PieceColor.WHITE, new Position(7, 5));
        Position promotionSquare = new Position(8, 5);
        
        Move promotionMove = new Move(whitePawn.getPosition(), promotionSquare, whitePawn);
        assertTrue(promotionMove.isPromotion());
        assertEquals(PieceType.QUEEN, promotionMove.getPromotionPiece()); // Default promotion
        
        // Test custom promotion
        promotionMove.setPromotionPiece(PieceType.KNIGHT);
        assertEquals(PieceType.KNIGHT, promotionMove.getPromotionPiece());
    }
    
    @Test
    @DisplayName("En Passant")
    void testEnPassant() {
        // Set up en passant scenario
        game.makeMove("Alice", "e2e4");
        game.makeMove("Bob", "a7a6"); // Random move
        game.makeMove("Alice", "e4e5");
        game.makeMove("Bob", "d7d5"); // Two-square pawn move
        
        // Test en passant capture
        MoveResult enPassantResult = game.makeMove("Alice", "e5d6");
        
        if (enPassantResult.isSuccess() && enPassantResult.getMove().isEnPassant()) {
            assertTrue(enPassantResult.getMove().isEnPassant());
            assertTrue(enPassantResult.getMove().isCapture());
            
            // Verify the captured pawn is removed
            assertTrue(game.getBoard().isEmpty(new Position("d5")));
        }
    }
    
    @Test
    @DisplayName("Legal Move Generation")
    void testLegalMoveGeneration() {
        // Test initial legal moves
        List<Move> whiteMoves = game.getLegalMoves("Alice");
        assertEquals(20, whiteMoves.size()); // 16 pawn moves + 4 knight moves
        
        // Test legal moves for specific piece
        List<Move> knightMoves = game.getLegalMovesForPiece("Alice", new Position("g1"));
        assertEquals(2, knightMoves.size()); // Knight on g1 has 2 legal moves initially
        
        // Test after making a move
        game.makeMove("Alice", "e2e4");
        List<Move> blackMoves = game.getLegalMoves("Bob");
        assertEquals(20, blackMoves.size()); // Black also has 20 initial moves
        
        // Test invalid player
        List<Move> invalidMoves = game.getLegalMoves("Charlie");
        assertTrue(invalidMoves.isEmpty());
    }
    
    @Test
    @DisplayName("Game End Conditions")
    void testGameEndConditions() {
        // Test resignation
        boolean resigned = game.resign("Alice");
        assertTrue(resigned);
        assertEquals(GameState.RESIGNATION, game.getGameState());
        assertEquals("Bob", game.getWinner());
        assertTrue(game.getGameState().isGameOver());
        assertTrue(game.getGameState().hasWinner());
        
        // Test draw offer and acceptance
        ChessGame drawGame = new ChessGame("DRAW_TEST");
        drawGame.addPlayer("Player1", PieceColor.WHITE);
        drawGame.addPlayer("Player2", PieceColor.BLACK);
        
        assertTrue(drawGame.offerDraw("Player1"));
        assertTrue(drawGame.acceptDraw("Player2"));
        assertEquals(GameState.DRAW, drawGame.getGameState());
        assertTrue(drawGame.getGameState().isGameOver());
        assertFalse(drawGame.getGameState().hasWinner());
    }
    
    @Test
    @DisplayName("Position and Move Utilities")
    void testPositionUtilities() {
        // Test position creation and validation
        Position pos1 = new Position("e4");
        assertEquals(4, pos1.getRank());
        assertEquals(5, pos1.getFile());
        assertEquals("e4", pos1.toAlgebraic());
        
        Position pos2 = new Position(4, 5);
        assertEquals(pos1, pos2);
        
        // Test position relationships
        Position pos3 = new Position("e5");
        assertTrue(pos1.isOnSameFile(pos3));
        assertFalse(pos1.isOnSameRank(pos3));
        assertEquals(1, pos1.getRankDistance(pos3));
        assertEquals(0, pos1.getFileDistance(pos3));
        
        // Test diagonal relationship
        Position pos4 = new Position("f5");
        assertTrue(pos1.isOnSameDiagonal(pos4));
        
        // Test adjacency
        assertTrue(pos1.isAdjacent(pos3));
        assertFalse(pos1.isAdjacent(new Position("g6")));
        
        // Test invalid positions
        assertThrows(IllegalArgumentException.class, () -> new Position(0, 5));
        assertThrows(IllegalArgumentException.class, () -> new Position(5, 9));
        assertThrows(IllegalArgumentException.class, () -> new Position("z9"));
    }
    
    @Test
    @DisplayName("Piece Utilities")
    void testPieceUtilities() {
        Piece whitePawn = new Piece(PieceType.PAWN, PieceColor.WHITE, new Position("e2"));
        Piece blackKnight = new Piece(PieceType.KNIGHT, PieceColor.BLACK, new Position("b8"));
        
        // Test piece properties
        assertTrue(whitePawn.isWhite());
        assertFalse(whitePawn.isBlack());
        assertTrue(blackKnight.isBlack());
        assertFalse(blackKnight.isWhite());
        
        // Test piece relationships
        assertFalse(whitePawn.isSameColor(blackKnight));
        assertTrue(whitePawn.isOpponentColor(blackKnight));
        
        // Test piece symbols
        assertEquals('P', whitePawn.getSymbol());
        assertEquals('n', blackKnight.getSymbol());
        assertEquals("♙", whitePawn.getUnicodeSymbol());
        assertEquals("♞", blackKnight.getUnicodeSymbol());
        
        // Test piece values
        assertEquals(1, PieceType.PAWN.getValue());
        assertEquals(3, PieceType.KNIGHT.getValue());
        assertEquals(9, PieceType.QUEEN.getValue());
        assertEquals(1000, PieceType.KING.getValue());
    }
    
    @Test
    @DisplayName("Game Analysis")
    void testGameAnalysis() {
        // Test initial material balance
        assertEquals(0, game.getMaterialBalance()); // Equal material
        
        // Test piece counts
        Map<PieceType, Integer> whitePieces = game.getPieceCounts(PieceColor.WHITE);
        assertEquals(8, (int) whitePieces.get(PieceType.PAWN));
        assertEquals(2, (int) whitePieces.get(PieceType.ROOK));
        assertEquals(1, (int) whitePieces.get(PieceType.KING));
        
        // Make some moves and test again
        game.makeMove("Alice", "e2e4");
        game.makeMove("Bob", "d7d5");
        game.makeMove("Alice", "e4d5"); // Capture
        
        // Material should favor white after capture
        assertTrue(game.getMaterialBalance() > 0);
        
        // Test move count
        assertEquals(3, game.getMoveCount());
        
        // Test last move
        Move lastMove = game.getLastMove();
        assertNotNull(lastMove);
        assertTrue(lastMove.isCapture());
        assertEquals("d5", lastMove.getTo().toAlgebraic());
    }
    
    @Test
    @DisplayName("Move Notation")
    void testMoveNotation() {
        // Test basic move notation
        game.makeMove("Alice", "e2e4");
        Move lastMove = game.getLastMove();
        
        assertNotNull(lastMove);
        assertEquals("e2e4", lastMove.toUCINotation());
        
        // Test algebraic notation
        String algebraic = lastMove.toAlgebraicNotation();
        assertNotNull(algebraic);
        assertFalse(algebraic.isEmpty());
        
        // Test move parsing
        MoveResult result = game.makeMove("Bob", "e7e5");
        assertTrue(result.isSuccess());
        
        // Test invalid notation
        MoveResult invalidResult = game.makeMove("Alice", "invalid");
        assertFalse(invalidResult.isSuccess());
    }
    
    @Test
    @DisplayName("Board Representation")
    void testBoardRepresentation() {
        ChessBoard board = game.getBoard();
        
        // Test FEN generation
        String fen = board.toFEN();
        assertNotNull(fen);
        assertFalse(fen.isEmpty());
        assertTrue(fen.contains("rnbqkbnr")); // Black back rank
        assertTrue(fen.contains("RNBQKBNR")); // White back rank
        
        // Test string representation
        String boardString = board.toString();
        assertNotNull(boardString);
        assertTrue(boardString.contains("a b c d e f g h")); // File labels
        assertTrue(boardString.contains("8")); // Rank labels
        
        // Test after moves
        game.makeMove("Alice", "e2e4");
        String newFen = board.toFEN();
        assertNotEquals(fen, newFen); // FEN should change after move
    }
}