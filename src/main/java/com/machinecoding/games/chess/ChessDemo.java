package com.machinecoding.games.chess;

import com.machinecoding.games.chess.model.*;
import com.machinecoding.games.chess.service.*;
import java.util.List;
import java.util.Map;

/**
 * Comprehensive demonstration of the Chess Game.
 * Shows game setup, move execution, special moves, and game mechanics.
 */
public class ChessDemo {
    
    public static void main(String[] args) {
        System.out.println("=== Chess Game Demo ===\n");
        
        // Demo 1: Game Setup and Basic Moves
        System.out.println("=== Demo 1: Game Setup and Basic Moves ===");
        demonstrateGameSetup();
        
        // Demo 2: Special Moves
        System.out.println("\n=== Demo 2: Special Moves ===");
        demonstrateSpecialMoves();
        
        // Demo 3: Check and Checkmate
        System.out.println("\n=== Demo 3: Check and Checkmate ===");
        demonstrateCheckAndCheckmate();
        
        // Demo 4: Game Analysis
        System.out.println("\n=== Demo 4: Game Analysis ===");
        demonstrateGameAnalysis();
        
        // Demo 5: Complete Game Simulation
        System.out.println("\n=== Demo 5: Complete Game Simulation ===");
        demonstrateCompleteGame();
        
        System.out.println("\n=== Demo Complete ===");
    }
    
    private static void demonstrateGameSetup() {
        System.out.println("1. Creating new chess game:");
        ChessGame game = new ChessGame("CHESS001");
        System.out.println("   Created game: " + game);
        
        System.out.println("\n2. Adding players:");
        boolean whiteAdded = game.addPlayer("Alice", PieceColor.WHITE);
        System.out.println("   Added White player (Alice): " + whiteAdded);
        
        boolean blackAdded = game.addPlayer("Bob", PieceColor.BLACK);
        System.out.println("   Added Black player (Bob): " + blackAdded);
        
        System.out.println("\n3. Game state after setup:");
        System.out.println("   Status: " + game.getGameState());
        System.out.println("   Current player: " + game.getCurrentPlayer());
        System.out.println("   Current player ID: " + game.getCurrentPlayerId());
        
        System.out.println("\n4. Initial board position:");
        System.out.println(game.getBoard());
        
        System.out.println("\n5. Making basic moves:");
        
        // White's first move: e2-e4
        MoveResult result1 = game.makeMove("Alice", "e2e4");
        System.out.println("   White plays e2-e4: " + result1.isSuccess());
        if (result1.getMove() != null) {
            System.out.println("   Move: " + result1.getMove().toAlgebraicNotation());
        }
        
        // Black's response: e7-e5
        MoveResult result2 = game.makeMove("Bob", "e7e5");
        System.out.println("   Black plays e7-e5: " + result2.isSuccess());
        if (result2.getMove() != null) {
            System.out.println("   Move: " + result2.getMove().toAlgebraicNotation());
        }
        
        // White's second move: Nf3
        MoveResult result3 = game.makeMove("Alice", "g1f3");
        System.out.println("   White plays Nf3: " + result3.isSuccess());
        if (result3.getMove() != null) {
            System.out.println("   Move: " + result3.getMove().toAlgebraicNotation());
        }
        
        System.out.println("\n6. Board after opening moves:");
        System.out.println(game.getBoard());
        
        System.out.println("\n7. Game log:");
        for (String logEntry : game.getGameLog()) {
            System.out.println("   " + logEntry);
        }
    }
    
    private static void demonstrateSpecialMoves() {
        System.out.println("1. Setting up position for castling:");
        ChessGame game = new ChessGame("CHESS002");
        game.addPlayer("Alice", PieceColor.WHITE);
        game.addPlayer("Bob", PieceColor.BLACK);
        
        // Set up a position where castling is possible
        // This would require moving pieces to clear the path
        System.out.println("   Initial position:");
        System.out.println(game.getBoard());
        
        // Make some moves to clear castling path
        game.makeMove("Alice", "e2e4");
        game.makeMove("Bob", "e7e5");
        game.makeMove("Alice", "g1f3");
        game.makeMove("Bob", "b8c6");
        game.makeMove("Alice", "f1c4");
        game.makeMove("Bob", "f8c5");
        
        System.out.println("\n2. Position after clearing castling path:");
        System.out.println(game.getBoard());
        
        System.out.println("\n3. Checking castling availability:");
        boolean canCastleKingside = game.canCastle("Alice", true);
        boolean canCastleQueenside = game.canCastle("Alice", false);
        System.out.println("   White can castle kingside: " + canCastleKingside);
        System.out.println("   White can castle queenside: " + canCastleQueenside);
        
        // Attempt castling
        if (canCastleKingside) {
            MoveResult castlingResult = game.makeMove("Alice", "O-O");
            System.out.println("   Castling result: " + castlingResult.isSuccess());
            if (castlingResult.getMove() != null) {
                System.out.println("   Castling move: " + castlingResult.getMove().toAlgebraicNotation());
            }
        }
        
        System.out.println("\n4. Board after castling:");
        System.out.println(game.getBoard());
        
        System.out.println("\n5. Demonstrating pawn promotion:");
        // This would require setting up a position where a pawn can promote
        ChessGame promotionGame = new ChessGame("CHESS003");
        promotionGame.addPlayer("Alice", PieceColor.WHITE);
        promotionGame.addPlayer("Bob", PieceColor.BLACK);
        
        System.out.println("   Pawn promotion would be demonstrated with a pawn reaching the 8th rank");
        System.out.println("   Example: e7e8q (promote to queen)");
    }
    
    private static void demonstrateCheckAndCheckmate() {
        System.out.println("1. Setting up a check position:");
        ChessGame game = new ChessGame("CHESS004");
        game.addPlayer("Alice", PieceColor.WHITE);
        game.addPlayer("Bob", PieceColor.BLACK);
        
        // Play some moves to create a check situation
        game.makeMove("Alice", "e2e4");
        game.makeMove("Bob", "e7e5");
        game.makeMove("Alice", "d1h5"); // Queen attacks f7
        
        System.out.println("   Current position:");
        System.out.println(game.getBoard());
        
        // Check if black king is in check
        boolean blackInCheck = game.isInCheck("Bob");
        System.out.println("   Black king in check: " + blackInCheck);
        
        System.out.println("\n2. Legal moves when in check:");
        List<Move> legalMoves = game.getLegalMoves("Bob");
        System.out.println("   Black has " + legalMoves.size() + " legal moves:");
        for (int i = 0; i < Math.min(5, legalMoves.size()); i++) {
            Move move = legalMoves.get(i);
            System.out.println("     " + move.toAlgebraicNotation() + 
                             " (" + move.getFrom() + " to " + move.getTo() + ")");
        }
        
        System.out.println("\n3. Game state information:");
        System.out.println("   Game state: " + game.getGameState());
        System.out.println("   Current player: " + game.getCurrentPlayer());
        System.out.println("   Move count: " + game.getMoveCount());
        
        System.out.println("\n4. Demonstrating Scholar's Mate (quick checkmate):");
        ChessGame mateGame = new ChessGame("MATE_DEMO");
        mateGame.addPlayer("White", PieceColor.WHITE);
        mateGame.addPlayer("Black", PieceColor.BLACK);
        
        // Scholar's Mate sequence
        mateGame.makeMove("White", "e2e4");
        mateGame.makeMove("Black", "e7e5");
        mateGame.makeMove("White", "f1c4");
        mateGame.makeMove("Black", "b8c6");
        mateGame.makeMove("White", "d1h5");
        mateGame.makeMove("Black", "g8f6");
        MoveResult mateMove = mateGame.makeMove("White", "h5f7");
        
        System.out.println("   Final move result: " + mateMove.isSuccess());
        if (mateMove.getMove() != null) {
            System.out.println("   Checkmate move: " + mateMove.getMove().toAlgebraicNotation());
            System.out.println("   Is checkmate: " + mateMove.getMove().isCheckmate());
        }
        System.out.println("   Game state: " + mateGame.getGameState());
        System.out.println("   Winner: " + mateGame.getWinner());
    }
    
    private static void demonstrateGameAnalysis() {
        System.out.println("1. Creating game for analysis:");
        ChessGame game = new ChessGame("ANALYSIS");
        game.addPlayer("Alice", PieceColor.WHITE);
        game.addPlayer("Bob", PieceColor.BLACK);
        
        // Play several moves
        game.makeMove("Alice", "e2e4");
        game.makeMove("Bob", "e7e5");
        game.makeMove("Alice", "g1f3");
        game.makeMove("Bob", "b8c6");
        game.makeMove("Alice", "f1c4");
        game.makeMove("Bob", "f8c5");
        game.makeMove("Alice", "d2d3");
        game.makeMove("Bob", "d7d6");
        
        System.out.println("   Current position:");
        System.out.println(game.getBoard());
        
        System.out.println("\n2. Material analysis:");
        int materialBalance = game.getMaterialBalance();
        System.out.println("   Material balance: " + materialBalance + 
                         " (positive = White advantage, negative = Black advantage)");
        
        Map<PieceType, Integer> whitePieces = game.getPieceCounts(PieceColor.WHITE);
        Map<PieceType, Integer> blackPieces = game.getPieceCounts(PieceColor.BLACK);
        
        System.out.println("   White pieces:");
        for (Map.Entry<PieceType, Integer> entry : whitePieces.entrySet()) {
            if (entry.getValue() > 0) {
                System.out.println("     " + entry.getKey() + ": " + entry.getValue());
            }
        }
        
        System.out.println("   Black pieces:");
        for (Map.Entry<PieceType, Integer> entry : blackPieces.entrySet()) {
            if (entry.getValue() > 0) {
                System.out.println("     " + entry.getKey() + ": " + entry.getValue());
            }
        }
        
        System.out.println("\n3. Position analysis:");
        System.out.println("   White legal moves: " + game.getLegalMoves("Alice").size());
        System.out.println("   Black legal moves: " + game.getLegalMoves("Bob").size());
        System.out.println("   White in check: " + game.isInCheck("Alice"));
        System.out.println("   Black in check: " + game.isInCheck("Bob"));
        
        System.out.println("\n4. Game statistics:");
        System.out.println("   Total moves played: " + game.getMoveCount());
        System.out.println("   Game duration: " + game.getGameDuration() + "ms");
        System.out.println("   Current player: " + game.getCurrentPlayer());
        
        System.out.println("\n5. FEN representation:");
        System.out.println("   FEN: " + game.getBoard().toFEN());
    }
    
    private static void demonstrateCompleteGame() {
        System.out.println("1. Playing a complete short game:");
        ChessGame game = new ChessGame("COMPLETE_GAME");
        game.addPlayer("Alice", PieceColor.WHITE);
        game.addPlayer("Bob", PieceColor.BLACK);
        
        // Play a series of moves
        String[] moves = {
            "e2e4", "e7e5",     // 1. e4 e5
            "g1f3", "b8c6",     // 2. Nf3 Nc6
            "f1c4", "f8c5",     // 3. Bc4 Bc5
            "d2d3", "d7d6",     // 4. d3 d6
            "c1g5", "f7f6",     // 5. Bg5 f6
            "g5h4", "g7g5",     // 6. Bh4 g5
            "f3g5", "f6g5",     // 7. Nxg5 fxg5
            "h4g5", "d8g5"      // 8. Bxg5 Qxg5
        };
        
        System.out.println("   Playing moves:");
        for (int i = 0; i < moves.length; i++) {
            String playerId = (i % 2 == 0) ? "Alice" : "Bob";
            String color = (i % 2 == 0) ? "White" : "Black";
            
            MoveResult result = game.makeMove(playerId, moves[i]);
            System.out.println("     " + (i/2 + 1) + ". " + color + " plays " + moves[i] + 
                             ": " + (result.isSuccess() ? "Success" : "Failed - " + result.getMessage()));
            
            if (!result.isSuccess()) {
                break;
            }
            
            if (game.getGameState().isGameOver()) {
                System.out.println("     Game ended: " + game.getGameState());
                break;
            }
        }
        
        System.out.println("\n2. Final position:");
        System.out.println(game.getBoard());
        
        System.out.println("\n3. Game summary:");
        System.out.println("   Final state: " + game.getGameState());
        System.out.println("   Winner: " + (game.getWinner() != null ? game.getWinner() : "Draw/No winner"));
        System.out.println("   Total moves: " + game.getMoveCount());
        System.out.println("   Material balance: " + game.getMaterialBalance());
        
        System.out.println("\n4. Complete game log:");
        List<String> gameLog = game.getGameLog();
        for (String logEntry : gameLog) {
            System.out.println("   " + logEntry);
        }
        
        System.out.println("\n5. Testing game control features:");
        
        // Test draw offer
        ChessGame drawGame = new ChessGame("DRAW_TEST");
        drawGame.addPlayer("Player1", PieceColor.WHITE);
        drawGame.addPlayer("Player2", PieceColor.BLACK);
        
        boolean drawOffered = drawGame.offerDraw("Player1");
        System.out.println("   Draw offered: " + drawOffered);
        
        boolean drawAccepted = drawGame.acceptDraw("Player2");
        System.out.println("   Draw accepted: " + drawAccepted);
        System.out.println("   Game state after draw: " + drawGame.getGameState());
        
        // Test resignation
        ChessGame resignGame = new ChessGame("RESIGN_TEST");
        resignGame.addPlayer("Player1", PieceColor.WHITE);
        resignGame.addPlayer("Player2", PieceColor.BLACK);
        
        boolean resigned = resignGame.resign("Player1");
        System.out.println("   Resignation: " + resigned);
        System.out.println("   Game state after resignation: " + resignGame.getGameState());
        System.out.println("   Winner after resignation: " + resignGame.getWinner());
    }
}