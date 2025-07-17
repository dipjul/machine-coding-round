package com.machinecoding.games.snakeladder;

import com.machinecoding.games.snakeladder.model.*;
import com.machinecoding.games.snakeladder.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for the Snake and Ladder game.
 */
public class SnakeAndLadderGameTest {
    
    private Board board;
    private SnakeAndLadderGame game;
    private Dice dice;
    
    @BeforeEach
    void setUp() {
        board = new Board(10);
        dice = new Dice(6, 12345); // Seeded for reproducible tests
        game = new SnakeAndLadderGame("TEST_GAME", board, dice);
    }
    
    @Test
    void testBoardCreation() {
        assertEquals(10, board.getSize());
        assertEquals(100, board.getTotalCells());
        assertTrue(board.isValidPosition(1));
        assertTrue(board.isValidPosition(100));
        assertFalse(board.isValidPosition(0));
        assertFalse(board.isValidPosition(101));
        assertTrue(board.isWinningPosition(100));
        assertFalse(board.isWinningPosition(99));
    }
    
    @Test
    void testSnakeAddition() {
        assertTrue(board.addSnake(16, 6));
        assertTrue(board.hasSnake(16));
        assertEquals(6, board.getSnakeTail(16));
        assertEquals(6, board.getNextPosition(16));
        
        // Cannot add snake with head <= tail
        assertFalse(board.addSnake(5, 10));
        assertFalse(board.addSnake(5, 5));
        
        // Cannot add snake at position 1 or 100
        assertFalse(board.addSnake(1, 0));
        assertFalse(board.addSnake(100, 50));
        
        // Cannot add overlapping snakes
        assertFalse(board.addSnake(16, 3)); // Head already used
        assertFalse(board.addSnake(20, 6)); // Tail already used
    }
    
    @Test
    void testLadderAddition() {
        assertTrue(board.addLadder(4, 14));
        assertTrue(board.hasLadder(4));
        assertEquals(14, board.getLadderTop(4));
        assertEquals(14, board.getNextPosition(4));
        
        // Cannot add ladder with bottom >= top
        assertFalse(board.addLadder(10, 5));
        assertFalse(board.addLadder(10, 10));
        
        // Cannot add ladder at position 1 or 100
        assertFalse(board.addLadder(1, 10));
        assertFalse(board.addLadder(90, 100));
        
        // Cannot add overlapping ladders
        assertFalse(board.addLadder(4, 20)); // Bottom already used
        assertFalse(board.addLadder(8, 14)); // Top already used
    }
    
    @Test
    void testPlayerCreation() {
        Player player = new Player("P1", "Alice");
        assertEquals("P1", player.getId());
        assertEquals("Alice", player.getName());
        assertEquals(0, player.getPosition());
        assertFalse(player.hasWon());
        assertEquals(0, player.getMovesCount());
        assertEquals(0, player.getSnakesEncountered());
        assertEquals(0, player.getLaddersClimbed());
    }
    
    @Test
    void testPlayerReset() {
        Player player = new Player("P1", "Alice");
        player.setPosition(50);
        player.setHasWon(true);
        player.incrementMovesCount();
        player.incrementSnakesEncountered();
        player.incrementLaddersClimbed();
        
        player.reset();
        
        assertEquals(0, player.getPosition());
        assertFalse(player.hasWon());
        assertEquals(0, player.getMovesCount());
        assertEquals(0, player.getSnakesEncountered());
        assertEquals(0, player.getLaddersClimbed());
    }
    
    @Test
    void testDiceRoll() {
        Dice testDice = new Dice(6);
        for (int i = 0; i < 100; i++) {
            int roll = testDice.roll();
            assertTrue(roll >= 1 && roll <= 6);
            assertEquals(roll, testDice.getLastRoll());
        }
    }
    
    @Test
    void testGameInitialization() {
        assertEquals("TEST_GAME", game.getGameId());
        assertEquals(GameStatus.WAITING_FOR_PLAYERS, game.getStatus());
        assertNull(game.getWinner());
        assertEquals(0, game.getMoveCount());
        assertTrue(game.getPlayers().isEmpty());
    }
    
    @Test
    void testPlayerAddition() {
        Player alice = game.addPlayer("Alice");
        assertEquals("Alice", alice.getName());
        assertEquals(1, game.getPlayers().size());
        assertEquals(alice, game.getPlayer(alice.getId()));
        
        Player bob = game.addPlayer("Bob");
        assertEquals(2, game.getPlayers().size());
        
        // Cannot add players after game starts
        game.startGame();
        assertThrows(IllegalStateException.class, () -> game.addPlayer("Charlie"));
    }
    
    @Test
    void testGameStart() {
        // Cannot start with less than 2 players
        assertFalse(game.startGame());
        
        game.addPlayer("Alice");
        assertFalse(game.startGame());
        
        game.addPlayer("Bob");
        assertTrue(game.startGame());
        assertEquals(GameStatus.IN_PROGRESS, game.getStatus());
        assertNotNull(game.getCurrentPlayer());
        
        // Cannot start again
        assertFalse(game.startGame());
    }
    
    @Test
    void testBasicMove() {
        game.addPlayer("Alice");
        game.addPlayer("Bob");
        game.startGame();
        
        Player currentPlayer = game.getCurrentPlayer();
        int initialPosition = currentPlayer.getPosition();
        
        Move move = game.makeMove();
        assertNotNull(move);
        assertEquals(currentPlayer, move.getPlayer());
        assertTrue(move.getDiceValue() >= 1 && move.getDiceValue() <= 6);
        assertEquals(initialPosition, move.getStartPosition());
        assertEquals(initialPosition + move.getDiceValue(), move.getEndPosition());
        assertFalse(move.isSnakeEncountered());
        assertFalse(move.isLadderClimbed());
        assertFalse(move.isWon());
        
        assertEquals(1, game.getMoveCount());
        assertEquals(1, currentPlayer.getMovesCount());
    }
    
    @Test
    void testSnakeEncounter() {
        board.addSnake(16, 6);
        game.addPlayer("Alice");
        game.startGame();
        
        Player player = game.getCurrentPlayer();
        player.setPosition(10); // Set position to land on snake
        
        // Mock a dice roll that would land on the snake
        // We need to make 6 moves to land on position 16
        for (int i = 0; i < 10; i++) {
            Move move = game.makeMove();
            if (move.isSnakeEncountered()) {
                assertTrue(move.getEndPosition() < move.getStartPosition());
                assertEquals(1, player.getSnakesEncountered());
                break;
            }
        }
    }
    
    @Test
    void testLadderClimb() {
        board.addLadder(4, 14);
        game.addPlayer("Alice");
        game.startGame();
        
        Player player = game.getCurrentPlayer();
        
        // Keep playing until we hit a ladder
        for (int i = 0; i < 20; i++) {
            Move move = game.makeMove();
            if (move.isLadderClimbed()) {
                assertTrue(move.getEndPosition() > move.getStartPosition() + move.getDiceValue());
                assertEquals(1, player.getLaddersClimbed());
                break;
            }
        }
    }
    
    @Test
    void testWinCondition() {
        game.addPlayer("Alice");
        game.startGame();
        
        Player player = game.getCurrentPlayer();
        player.setPosition(95); // Close to winning
        
        // Keep playing until someone wins
        for (int i = 0; i < 10; i++) {
            Move move = game.makeMove();
            if (move.isWon()) {
                assertTrue(player.hasWon());
                assertEquals(GameStatus.FINISHED, game.getStatus());
                assertEquals(player, game.getWinner());
                break;
            }
        }
    }
    
    @Test
    void testExactRollToWin() {
        game.addPlayer("Alice");
        game.startGame();
        
        Player player = game.getCurrentPlayer();
        player.setPosition(98); // Need exactly 2 to win
        
        Move move = game.makeMove();
        
        if (move.getDiceValue() == 2) {
            // Should win
            assertTrue(move.isWon());
            assertEquals(100, player.getPosition());
        } else {
            // Should stay at same position
            assertEquals(98, player.getPosition());
            assertFalse(move.isWon());
        }
    }
    
    @Test
    void testPlayerTurns() {
        game.addPlayer("Alice");
        game.addPlayer("Bob");
        game.addPlayer("Charlie");
        game.startGame();
        
        Player firstPlayer = game.getCurrentPlayer();
        game.makeMove();
        
        Player secondPlayer = game.getCurrentPlayer();
        assertNotEquals(firstPlayer, secondPlayer);
        game.makeMove();
        
        Player thirdPlayer = game.getCurrentPlayer();
        assertNotEquals(secondPlayer, thirdPlayer);
        assertNotEquals(firstPlayer, thirdPlayer);
    }
    
    @Test
    void testGameStatistics() {
        board.addSnake(16, 6);
        board.addLadder(4, 14);
        
        game.addPlayer("Alice");
        game.addPlayer("Bob");
        game.startGame();
        
        // Play some moves
        for (int i = 0; i < 10; i++) {
            game.makeMove();
        }
        
        GameStatistics stats = game.getStatistics();
        assertEquals(10, stats.getTotalMoves());
        assertTrue(stats.getGameDuration() > 0);
        assertEquals(2, stats.getAllPlayerStatistics().size());
        
        for (GameStatistics.PlayerStatistics playerStats : stats.getAllPlayerStatistics().values()) {
            assertTrue(playerStats.getMoves() > 0);
        }
    }
    
    @Test
    void testBoardFactory() {
        Board standardBoard = BoardFactory.createStandardBoard();
        assertEquals(10, standardBoard.getSize());
        assertTrue(standardBoard.getSnakes().size() > 0);
        assertTrue(standardBoard.getLadders().size() > 0);
        
        Board randomBoard = BoardFactory.createRandomBoard(8, 5, 5, 12345);
        assertEquals(8, randomBoard.getSize());
        assertTrue(randomBoard.getSnakes().size() <= 5);
        assertTrue(randomBoard.getLadders().size() <= 5);
        
        Board balancedBoard = BoardFactory.createBalancedBoard(10);
        assertEquals(10, balancedBoard.getSize());
        assertTrue(balancedBoard.getSnakes().size() > 0);
        assertTrue(balancedBoard.getLadders().size() > 0);
    }
    
    @Test
    void testMoveHistory() {
        game.addPlayer("Alice");
        game.addPlayer("Bob");
        game.startGame();
        
        assertTrue(game.getMoveHistory().isEmpty());
        assertNull(game.getLastMove());
        
        Move move1 = game.makeMove();
        assertEquals(1, game.getMoveHistory().size());
        assertEquals(move1, game.getLastMove());
        
        Move move2 = game.makeMove();
        assertEquals(2, game.getMoveHistory().size());
        assertEquals(move2, game.getLastMove());
    }
    
    @Test
    void testInvalidMoves() {
        game.addPlayer("Alice");
        game.addPlayer("Bob");
        
        // Cannot make move before starting
        assertThrows(IllegalStateException.class, () -> game.makeMove());
        
        game.startGame();
        
        // Cannot make move for wrong player
        Player currentPlayer = game.getCurrentPlayer();
        String wrongPlayerId = game.getPlayers().stream()
            .filter(p -> !p.equals(currentPlayer))
            .findFirst()
            .get()
            .getId();
        
        assertNull(game.makeMove(wrongPlayerId));
    }
    
    @Test
    void testActivePlayers() {
        game.addPlayer("Alice");
        game.addPlayer("Bob");
        game.addPlayer("Charlie");
        game.startGame();
        
        assertEquals(3, game.getActivePlayers().size());
        
        // Simulate a player winning
        Player winner = game.getPlayers().get(0);
        winner.setHasWon(true);
        
        assertEquals(2, game.getActivePlayers().size());
        assertFalse(game.getActivePlayers().contains(winner));
    }
}