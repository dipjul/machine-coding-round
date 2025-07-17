package com.machinecoding.games.snakeladder.service;

import com.machinecoding.games.snakeladder.model.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Main game engine for Snake & Ladder game.
 * Manages game state, player turns, and game rules.
 */
public class SnakeLadderGame {
    private final String gameId;
    private final GameBoard board;
    private final List<Player> players;
    private final Map<String, Player> playerMap;
    private final AtomicInteger playerIdCounter;
    private final Random dice;
    private int currentPlayerIndex;
    private GameStatus status;
    private Player winner;
    private int totalTurns;
    private final List<String> gameLog;
    private final long startTime;
    private final GameSettings settings;
    
    public SnakeLadderGame(String gameId) {
        this(gameId, new GameBoard(), new GameSettings());
    }
    
    public SnakeLadderGame(String gameId, GameBoard board) {
        this(gameId, board, new GameSettings());
    }
    
    public SnakeLadderGame(String gameId, GameBoard board, GameSettings settings) {
        this.gameId = gameId != null ? gameId.trim() : "";
        this.board = board != null ? board : new GameBoard();
        this.settings = settings != null ? settings : new GameSettings();
        this.players = new ArrayList<>();
        this.playerMap = new HashMap<>();
        this.playerIdCounter = new AtomicInteger(1);
        this.dice = new Random();
        this.currentPlayerIndex = 0;
        this.status = GameStatus.WAITING_FOR_PLAYERS;
        this.winner = null;
        this.totalTurns = 0;
        this.gameLog = new ArrayList<>();
        this.startTime = System.currentTimeMillis();
    }
    
    // Game setup
    public Player addPlayer(String name) {
        if (status != GameStatus.WAITING_FOR_PLAYERS) {
            throw new IllegalStateException("Cannot add players after game has started");
        }
        
        if (players.size() >= settings.getMaxPlayers()) {
            throw new IllegalStateException("Maximum " + settings.getMaxPlayers() + " players allowed");
        }
        
        String playerId = "PLAYER" + String.format("%02d", playerIdCounter.getAndIncrement());
        Player player = new Player(playerId, name);
        
        players.add(player);
        playerMap.put(playerId, player);
        
        logEvent("Player " + name + " joined the game");
        return player;
    }
    
    public boolean startGame() {
        if (players.size() < settings.getMinPlayers()) {
            return false;
        }
        
        status = GameStatus.IN_PROGRESS;
        currentPlayerIndex = 0;
        logEvent("Game started with " + players.size() + " players");
        return true;
    }
    
    // Game play
    public TurnResult takeTurn(String playerId) {
        if (status != GameStatus.IN_PROGRESS) {
            throw new IllegalStateException("Game is not in progress");
        }
        
        Player currentPlayer = getCurrentPlayer();
        if (!currentPlayer.getPlayerId().equals(playerId)) {
            throw new IllegalArgumentException("Not this player's turn");
        }
        
        if (!currentPlayer.isActive()) {
            nextPlayer();
            return new TurnResult(currentPlayer, 0, currentPlayer.getPosition(), 
                                currentPlayer.getPosition(), "Player is not active");
        }
        
        return executePlayerTurn(currentPlayer);
    }
    
    private TurnResult executePlayerTurn(Player player) {
        int diceRoll = rollDice();
        int oldPosition = player.getPosition();
        int newPosition = oldPosition + diceRoll;
        
        // Check if player would exceed board size
        if (newPosition > board.getSize()) {
            if (settings.isExactWinRequired()) {
                // Player must roll exact number to win
                logEvent(player.getName() + " rolled " + diceRoll + " but needs exact roll to win");
                nextPlayer();
                return new TurnResult(player, diceRoll, oldPosition, oldPosition, 
                                    "Need exact roll to win (rolled " + diceRoll + ")");
            } else {
                // Player wins by reaching or exceeding the final square
                newPosition = board.getSize();
            }
        }
        
        // Move player
        player.moveTo(newPosition);
        player.incrementMoves();
        totalTurns++;
        
        String moveDescription = player.getName() + " rolled " + diceRoll + 
                               " and moved from " + oldPosition + " to " + newPosition;
        
        // Check for win condition
        if (newPosition >= board.getSize()) {
            player.setStatus(PlayerStatus.WON);
            winner = player;
            status = GameStatus.FINISHED;
            logEvent(moveDescription + " - WINNER!");
            return new TurnResult(player, diceRoll, oldPosition, newPosition, "Winner!");
        }
        
        // Handle special squares, snakes, and ladders
        String specialEffect = handleSpecialSquares(player, newPosition);
        int finalPosition = player.getPosition();
        
        String fullDescription = moveDescription;
        if (!specialEffect.isEmpty()) {
            fullDescription += " - " + specialEffect;
        }
        
        logEvent(fullDescription);
        
        // Check for additional turn (if enabled and player rolled max)
        boolean additionalTurn = settings.isAdditionalTurnOnMax() && diceRoll == settings.getDiceSides();
        
        if (!additionalTurn) {
            nextPlayer();
        }
        
        return new TurnResult(player, diceRoll, oldPosition, finalPosition, 
                            specialEffect.isEmpty() ? "Normal move" : specialEffect);
    }
    
    private String handleSpecialSquares(Player player, int position) {
        List<String> effects = new ArrayList<>();
        
        // Handle snakes
        if (board.hasSnake(position)) {
            Snake snake = board.getSnake(position);
            player.moveTo(snake.getTail());
            player.encounterSnake();
            effects.add("Hit snake! Slid down to " + snake.getTail());
        }
        
        // Handle ladders
        else if (board.hasLadder(position)) {
            Ladder ladder = board.getLadder(position);
            player.moveTo(ladder.getTop());
            player.encounterLadder();
            effects.add("Found ladder! Climbed up to " + ladder.getTop());
        }
        
        // Handle special squares
        if (board.hasSpecialSquare(position)) {
            SpecialSquare special = board.getSpecialSquare(position);
            String specialEffect = applySpecialSquareEffect(player, special);
            if (!specialEffect.isEmpty()) {
                effects.add(specialEffect);
            }
        }
        
        return String.join(", ", effects);
    }
    
    private String applySpecialSquareEffect(Player player, SpecialSquare special) {
        switch (special.getType()) {
            case BONUS:
                if (special.getDescription().contains("Roll again")) {
                    // This would require additional game state management
                    return "Bonus: " + special.getDescription();
                } else if (special.getDescription().contains("forward")) {
                    int extraMoves = 3; // Default extra moves
                    int newPos = Math.min(player.getPosition() + extraMoves, board.getSize());
                    player.moveTo(newPos);
                    return "Bonus: Moved forward " + extraMoves + " spaces to " + newPos;
                }
                return "Bonus: " + special.getDescription();
                
            case PENALTY:
                return "Penalty: " + special.getDescription();
                
            case SAFE:
                return "Safe zone: " + special.getDescription();
                
            case TRAP:
                return "Trap: " + special.getDescription();
                
            default:
                return special.getDescription();
        }
    }
    
    private int rollDice() {
        return dice.nextInt(settings.getDiceSides()) + 1;
    }
    
    private void nextPlayer() {
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        } while (!getCurrentPlayer().isActive() && getActivePlayers().size() > 1);
        
        // Check if only one player remains
        if (getActivePlayers().size() <= 1 && status == GameStatus.IN_PROGRESS) {
            List<Player> activePlayers = getActivePlayers();
            if (!activePlayers.isEmpty()) {
                winner = activePlayers.get(0);
                winner.setStatus(PlayerStatus.WON);
                status = GameStatus.FINISHED;
                logEvent(winner.getName() + " wins by elimination!");
            }
        }
    }
    
    // Game queries
    public List<Player> getActivePlayers() {
        return players.stream()
                .filter(Player::isActive)
                .collect(ArrayList::new, (list, p) -> list.add(p), ArrayList::addAll);
    }
    
    public Player getCurrentPlayer() {
        return players.isEmpty() ? null : players.get(currentPlayerIndex);
    }
    
    public List<Player> getLeaderboard() {
        List<Player> leaderboard = new ArrayList<>(players);
        leaderboard.sort((a, b) -> {
            // Winners first
            if (a.hasWon() && !b.hasWon()) return -1;
            if (!a.hasWon() && b.hasWon()) return 1;
            
            // Then by position (descending)
            if (a.getPosition() != b.getPosition()) {
                return Integer.compare(b.getPosition(), a.getPosition());
            }
            
            // Then by fewer moves (ascending)
            return Integer.compare(a.getTotalMoves(), b.getTotalMoves());
        });
        
        return leaderboard;
    }
    
    // Game control
    public boolean eliminatePlayer(String playerId) {
        Player player = playerMap.get(playerId);
        if (player != null && player.isActive()) {
            player.setStatus(PlayerStatus.ELIMINATED);
            logEvent(player.getName() + " has been eliminated");
            
            // Check if game should end
            if (getActivePlayers().size() <= 1) {
                List<Player> remaining = getActivePlayers();
                if (!remaining.isEmpty()) {
                    winner = remaining.get(0);
                    winner.setStatus(PlayerStatus.WON);
                    status = GameStatus.FINISHED;
                    logEvent(winner.getName() + " wins by elimination!");
                }
            }
            return true;
        }
        return false;
    }
    
    public boolean forfeitGame(String playerId) {
        return eliminatePlayer(playerId);
    }
    
    // Game statistics
    public GameStats getGameStats() {
        long duration = System.currentTimeMillis() - startTime;
        
        int totalSnakeHits = players.stream()
                .mapToInt(Player::getSnakeEncounters)
                .sum();
        
        int totalLadderHits = players.stream()
                .mapToInt(Player::getLadderEncounters)
                .sum();
        
        double averageMovesPerPlayer = players.isEmpty() ? 0.0 :
                players.stream().mapToInt(Player::getTotalMoves).average().orElse(0.0);
        
        Player leadingPlayer = getLeaderboard().get(0);
        
        return new GameStats(gameId, players.size(), totalTurns, duration,
                           totalSnakeHits, totalLadderHits, averageMovesPerPlayer,
                           leadingPlayer.getPosition(), status);
    }
    
    // Getters
    public String getGameId() { return gameId; }
    public GameBoard getBoard() { return board; }
    public List<Player> getPlayers() { return new ArrayList<>(players); }
    public GameStatus getStatus() { return status; }
    public Player getWinner() { return winner; }
    public int getTotalTurns() { return totalTurns; }
    public List<String> getGameLog() { return new ArrayList<>(gameLog); }
    public long getGameDuration() { return System.currentTimeMillis() - startTime; }
    public GameSettings getSettings() { return settings; }
    
    public Player getPlayer(String playerId) {
        return playerMap.get(playerId);
    }
    
    // Logging
    private void logEvent(String event) {
        gameLog.add("[Turn " + totalTurns + "] " + event);
    }
    
    @Override
    public String toString() {
        return String.format("SnakeLadderGame{id='%s', players=%d, status=%s, turns=%d, winner=%s}",
                           gameId, players.size(), status, totalTurns, 
                           winner != null ? winner.getName() : "None");
    }
}