package com.machinecoding.games.snakeladder;

import com.machinecoding.games.snakeladder.model.*;
import com.machinecoding.games.snakeladder.service.*;

/**
 * Demonstration of the Snake and Ladder game using the existing implementation.
 */
public class SnakeAndLadderDemo {
    
    public static void main(String[] args) {
        System.out.println("=== Snake and Ladder Game Demo ===\n");
        
        // Demo 1: Basic Game Setup
        System.out.println("=== Demo 1: Basic Game Setup ===");
        demonstrateGameSetup();
        
        // Demo 2: Playing a Complete Game
        System.out.println("\n=== Demo 2: Playing a Complete Game ===");
        demonstrateCompleteGame();
        
        // Demo 3: Game Statistics
        System.out.println("\n=== Demo 3: Game Statistics ===");
        demonstrateGameStatistics();
        
        System.out.println("\n=== Demo Complete ===");
    }
    
    private static void demonstrateGameSetup() {
        System.out.println("1. Creating a new Snake and Ladder game:");
        SnakeLadderGame game = new SnakeLadderGame("DEMO_GAME_001");
        
        System.out.println("   Created game: " + game);
        System.out.println("   Board: " + game.getBoard());
        System.out.println("   Game status: " + game.getStatus());
        
        System.out.println("\n2. Adding players:");
        Player alice = game.addPlayer("Alice");
        System.out.println("   Added player: " + alice);
        
        Player bob = game.addPlayer("Bob");
        System.out.println("   Added player: " + bob);
        
        Player charlie = game.addPlayer("Charlie");
        System.out.println("   Added player: " + charlie);
        
        System.out.println("\n3. Game state before starting:");
        System.out.println("   Status: " + game.getStatus());
        System.out.println("   Players: " + game.getPlayers().size());
        System.out.println("   Active players: " + game.getActivePlayers().size());
        
        System.out.println("\n4. Starting the game:");
        boolean started = game.startGame();
        System.out.println("   Game started: " + started);
        System.out.println("   Status: " + game.getStatus());
        System.out.println("   Current player: " + game.getCurrentPlayer().getName());
        
        System.out.println("\n5. Board information:");
        GameBoard board = game.getBoard();
        System.out.println("   Board size: " + board.getSize());
        System.out.println("   Snakes: " + board.getSnakeCount());
        System.out.println("   Ladders: " + board.getLadderCount());
        System.out.println("   Special squares: " + board.getSpecialSquareCount());
        
        // Show some board details
        System.out.println("\n6. Some board features:");
        if (board.getSnakeCount() > 0) {
            Snake longestSnake = board.getLongestSnake();
            if (longestSnake != null) {
                System.out.println("   Longest snake: " + longestSnake.getHead() + " -> " + longestSnake.getTail() + 
                                 " (length: " + longestSnake.getLength() + ")");
            }
        }
        
        if (board.getLadderCount() > 0) {
            Ladder longestLadder = board.getLongestLadder();
            if (longestLadder != null) {
                System.out.println("   Longest ladder: " + longestLadder.getBottom() + " -> " + longestLadder.getTop() + 
                                 " (length: " + longestLadder.getLength() + ")");
            }
        }
    }
    
    private static void demonstrateCompleteGame() {
        System.out.println("1. Setting up a complete game:");
        SnakeLadderGame game = new SnakeLadderGame("COMPLETE_GAME");
        
        game.addPlayer("Alice");
        game.addPlayer("Bob");
        game.addPlayer("Charlie");
        game.addPlayer("Diana");
        
        System.out.println("   Players added: " + game.getPlayers().size());
        System.out.println("   Starting game...");
        
        game.startGame();
        System.out.println("   Game started with status: " + game.getStatus());
        
        System.out.println("\n2. Playing until completion:");
        int turnCount = 0;
        int maxTurns = 200; // Safety limit
        
        while (game.getStatus() == GameStatus.IN_PROGRESS && turnCount < maxTurns) {
            Player currentPlayer = game.getCurrentPlayer();
            
            try {
                TurnResult result = game.takeTurn(currentPlayer.getPlayerId());
                turnCount++;
                
                // Print significant moves or every 10th move
                if (result.getDescription().contains("snake") || 
                    result.getDescription().contains("ladder") || 
                    result.getDescription().contains("Winner") ||
                    turnCount % 10 == 0) {
                    System.out.println("   Turn " + turnCount + ": " + result);
                }
                
                // Check if someone won
                if (result.isWinningMove()) {
                    System.out.println("   🎉 " + currentPlayer.getName() + " WINS! 🎉");
                    break;
                }
                
            } catch (Exception e) {
                System.out.println("   Error on turn " + turnCount + ": " + e.getMessage());
                break;
            }
        }
        
        System.out.println("\n3. Game completed:");
        System.out.println("   Final status: " + game.getStatus());
        System.out.println("   Total turns: " + game.getTotalTurns());
        System.out.println("   Game duration: " + game.getGameDuration() + "ms");
        
        if (game.getWinner() != null) {
            System.out.println("   Winner: " + game.getWinner().getName());
        }
        
        System.out.println("\n4. Final leaderboard:");
        java.util.List<Player> leaderboard = game.getLeaderboard();
        for (int i = 0; i < leaderboard.size(); i++) {
            Player player = leaderboard.get(i);
            String position = (i == 0) ? "1st" : (i == 1) ? "2nd" : (i == 2) ? "3rd" : (i + 1) + "th";
            System.out.println("   " + position + " place: " + player.getName() + 
                             " (Position: " + player.getPosition() + 
                             ", Moves: " + player.getTotalMoves() + 
                             ", Status: " + player.getStatus() + ")");
        }
    }
    
    private static void demonstrateGameStatistics() {
        System.out.println("1. Creating a game for statistics demo:");
        SnakeLadderGame game = new SnakeLadderGame("STATS_GAME");
        
        game.addPlayer("Alice");
        game.addPlayer("Bob");
        game.startGame();
        
        System.out.println("   Playing some turns...");
        
        // Play 15 turns
        for (int i = 0; i < 15 && game.getStatus() == GameStatus.IN_PROGRESS; i++) {
            Player currentPlayer = game.getCurrentPlayer();
            try {
                TurnResult result = game.takeTurn(currentPlayer.getPlayerId());
                System.out.println("   Turn " + (i + 1) + ": " + result);
                
                if (result.isWinningMove()) {
                    break;
                }
            } catch (Exception e) {
                System.out.println("   Error: " + e.getMessage());
                break;
            }
        }
        
        System.out.println("\n2. Game statistics:");
        GameStats stats = game.getGameStats();
        System.out.println("   " + stats);
        System.out.println("   Snake hit rate: " + String.format("%.2f%%", stats.getSnakeHitRate() * 100));
        System.out.println("   Ladder hit rate: " + String.format("%.2f%%", stats.getLadderHitRate() * 100));
        System.out.println("   Average turns per minute: " + String.format("%.1f", stats.getAverageTurnsPerMinute()));
        
        System.out.println("\n3. Individual player statistics:");
        for (Player player : game.getPlayers()) {
            System.out.println("   " + player.getName() + ":");
            System.out.println("     Position: " + player.getPosition());
            System.out.println("     Total moves: " + player.getTotalMoves());
            System.out.println("     Snake encounters: " + player.getSnakeEncounters());
            System.out.println("     Ladder encounters: " + player.getLadderEncounters());
            System.out.println("     Status: " + player.getStatus());
        }
        
        System.out.println("\n4. Board statistics:");
        GameBoard.BoardStats boardStats = game.getBoard().getStatistics();
        System.out.println("   " + boardStats);
        
        System.out.println("\n5. Game log (last 5 entries):");
        java.util.List<String> gameLog = game.getGameLog();
        int startIndex = Math.max(0, gameLog.size() - 5);
        for (int i = startIndex; i < gameLog.size(); i++) {
            System.out.println("   " + gameLog.get(i));
        }
    }
}