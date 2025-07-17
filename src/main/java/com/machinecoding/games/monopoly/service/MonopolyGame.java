package com.machinecoding.games.monopoly.service;

import com.machinecoding.games.monopoly.model.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Main game engine for Monopoly.
 * Manages game state, player turns, and game rules.
 */
public class MonopolyGame {
    private final String gameId;
    private final GameBoard board;
    private final Dice dice;
    private final List<Player> players;
    private final Map<String, Player> playerMap;
    private final AtomicInteger playerIdCounter;
    private int currentPlayerIndex;
    private GameStatus status;
    private Player winner;
    private int turnCount;
    private final int maxTurns;
    
    public MonopolyGame(String gameId) {
        this(gameId, 1000); // Default max turns
    }
    
    public MonopolyGame(String gameId, int maxTurns) {
        this.gameId = gameId != null ? gameId.trim() : "";
        this.board = new GameBoard();
        this.dice = new Dice();
        this.players = new ArrayList<>();
        this.playerMap = new HashMap<>();
        this.playerIdCounter = new AtomicInteger(1);
        this.currentPlayerIndex = 0;
        this.status = GameStatus.WAITING_FOR_PLAYERS;
        this.winner = null;
        this.turnCount = 0;
        this.maxTurns = maxTurns;
    }
    
    // Game setup methods
    public Player addPlayer(String name) {
        if (status != GameStatus.WAITING_FOR_PLAYERS) {
            throw new IllegalStateException("Cannot add players after game has started");
        }
        
        if (players.size() >= 8) {
            throw new IllegalStateException("Maximum 8 players allowed");
        }
        
        String playerId = "PLAYER" + String.format("%02d", playerIdCounter.getAndIncrement());
        Player player = new Player(playerId, name, new BigDecimal("1500")); // Starting money
        
        players.add(player);
        playerMap.put(playerId, player);
        
        return player;
    }
    
    public boolean startGame() {
        if (players.size() < 2) {
            return false; // Need at least 2 players
        }
        
        status = GameStatus.IN_PROGRESS;
        currentPlayerIndex = 0;
        return true;
    }
    
    // Game play methods
    public TurnResult takeTurn(String playerId) {
        if (status != GameStatus.IN_PROGRESS) {
            throw new IllegalStateException("Game is not in progress");
        }
        
        Player currentPlayer = getCurrentPlayer();
        if (!currentPlayer.getPlayerId().equals(playerId)) {
            throw new IllegalArgumentException("Not this player's turn");
        }
        
        if (currentPlayer.isBankrupt()) {
            nextPlayer();
            return new TurnResult(currentPlayer, 0, 0, false, "Player is bankrupt");
        }
        
        TurnResult result = executePlayerTurn(currentPlayer);
        
        // Check for game end conditions
        checkGameEnd();
        
        // Move to next player if no doubles or player is in jail
        if (!result.isDoubles() || currentPlayer.isInJail()) {
            nextPlayer();
        }
        
        turnCount++;
        if (turnCount >= maxTurns) {
            endGameByTurnLimit();
        }
        
        return result;
    }
    
    private TurnResult executePlayerTurn(Player player) {
        if (player.isInJail()) {
            return handleJailTurn(player);
        }
        
        // Roll dice
        int diceRoll = dice.roll();
        boolean isDoubles = dice.isDoubles();
        
        // Handle consecutive doubles
        if (isDoubles) {
            player.incrementConsecutiveDoubles();
            if (player.getConsecutiveDoubles() >= 3) {
                // Go to jail for rolling 3 doubles in a row
                return new TurnResult(player, dice.getDie1(), dice.getDie2(), false, 
                                    "Sent to jail for rolling 3 doubles in a row");
            }
        } else {
            player.resetConsecutiveDoubles();
        }
        
        // Move player
        int oldPosition = player.getPosition();
        player.moveBy(diceRoll);
        int newPosition = player.getPosition();
        
        // Handle landing on space
        BoardSpace space = board.getSpace(newPosition);
        String message = handleLandingOnSpace(player, space, diceRoll);
        
        return new TurnResult(player, dice.getDie1(), dice.getDie2(), isDoubles, message);
    }
    
    private TurnResult handleJailTurn(Player player) {
        // Player can try to roll doubles, pay fine, or use get out of jail card
        int diceRoll = dice.roll();
        boolean isDoubles = dice.isDoubles();
        
        if (isDoubles) {
            // Got out of jail with doubles
            player.releaseFromJail();
            player.moveBy(diceRoll);
            BoardSpace space = board.getSpace(player.getPosition());
            String message = "Got out of jail with doubles! " + handleLandingOnSpace(player, space, diceRoll);
            return new TurnResult(player, dice.getDie1(), dice.getDie2(), false, message);
        } else {
            // Didn't roll doubles, increment jail turns
            player.incrementJailTurns();
            if (player.getJailTurns() >= 3) {
                // Must pay fine after 3 turns
                if (player.canAfford(new BigDecimal("50"))) {
                    player.subtractMoney(new BigDecimal("50"));
                    player.releaseFromJail();
                    player.moveBy(diceRoll);
                    BoardSpace space = board.getSpace(player.getPosition());
                    String message = "Paid $50 fine to get out of jail. " + handleLandingOnSpace(player, space, diceRoll);
                    return new TurnResult(player, dice.getDie1(), dice.getDie2(), false, message);
                } else {
                    // Can't afford fine, declare bankruptcy
                    player.setBankrupt();
                    return new TurnResult(player, dice.getDie1(), dice.getDie2(), false, 
                                        "Cannot afford jail fine - declared bankrupt");
                }
            } else {
                return new TurnResult(player, dice.getDie1(), dice.getDie2(), false, 
                                    "Still in jail (turn " + player.getJailTurns() + "/3)");
            }
        }
    }
    
    private String handleLandingOnSpace(Player player, BoardSpace space, int diceRoll) {
        switch (space.getType()) {
            case GO:
                return "Landed on GO - collect $200";
                
            case PROPERTY:
                return handlePropertySpace(player, space.getProperty(), diceRoll);
                
            case CHANCE:
                Card chanceCard = board.drawChanceCard();
                return "Drew Chance card: " + executeCard(player, chanceCard);
                
            case COMMUNITY_CHEST:
                Card ccCard = board.drawCommunityChestCard();
                return "Drew Community Chest card: " + executeCard(player, ccCard);
                
            case TAX:
                BigDecimal taxAmount = space.getTaxAmount();
                if (player.subtractMoney(taxAmount)) {
                    return "Paid " + space.getName() + " of $" + taxAmount;
                } else {
                    player.setBankrupt();
                    return "Cannot afford " + space.getName() + " - declared bankrupt";
                }
                
            case GO_TO_JAIL:
                player.sendToJail();
                return "Go to Jail!";
                
            case JAIL:
                return "Just visiting jail";
                
            case FREE_PARKING:
                return "Free parking - nothing happens";
                
            default:
                return "Landed on " + space.getName();
        }
    }
    
    private String handlePropertySpace(Player player, Property property, int diceRoll) {
        if (!property.isOwned()) {
            return "Property " + property.getName() + " is available for purchase ($" + property.getPurchasePrice() + ")";
        } else if (property.isOwnedBy(player)) {
            return "You own " + property.getName();
        } else {
            // Pay rent
            BigDecimal rent = property.calculateRent(diceRoll);
            if (player.subtractMoney(rent)) {
                property.getOwner().addMoney(rent);
                return "Paid $" + rent + " rent to " + property.getOwner().getName() + " for " + property.getName();
            } else {
                player.setBankrupt();
                return "Cannot afford rent of $" + rent + " - declared bankrupt";
            }
        }
    }
    
    private String executeCard(Player player, Card card) {
        switch (card.getType()) {
            case MOVE:
                if ("GO".equals(card.getValue())) {
                    player.setPosition(0);
                    player.addMoney(new BigDecimal("200"));
                    return card.getDescription() + " - collected $200";
                } else {
                    int targetPosition = Integer.parseInt(card.getValue());
                    boolean passedGo = targetPosition < player.getPosition();
                    player.setPosition(targetPosition);
                    if (passedGo) {
                        player.addMoney(new BigDecimal("200"));
                        return card.getDescription() + " - passed GO, collected $200";
                    }
                    return card.getDescription();
                }
                
            case MOVE_RELATIVE:
                int spaces = Integer.parseInt(card.getValue());
                player.moveBy(spaces);
                return card.getDescription();
                
            case MOVE_NEAREST:
                if ("RAILROAD".equals(card.getValue())) {
                    int nearestRailroad = board.findNearestRailroad(player.getPosition());
                    boolean passedGo = nearestRailroad < player.getPosition();
                    player.setPosition(nearestRailroad);
                    if (passedGo) {
                        player.addMoney(new BigDecimal("200"));
                        return card.getDescription() + " - passed GO, collected $200";
                    }
                    return card.getDescription();
                } else if ("UTILITY".equals(card.getValue())) {
                    int nearestUtility = board.findNearestUtility(player.getPosition());
                    boolean passedGo = nearestUtility < player.getPosition();
                    player.setPosition(nearestUtility);
                    if (passedGo) {
                        player.addMoney(new BigDecimal("200"));
                        return card.getDescription() + " - passed GO, collected $200";
                    }
                    return card.getDescription();
                }
                return card.getDescription();
                
            case MONEY:
                BigDecimal amount = new BigDecimal(card.getValue());
                if (amount.compareTo(BigDecimal.ZERO) > 0) {
                    player.addMoney(amount);
                    return card.getDescription() + " - received $" + amount;
                } else {
                    if (player.subtractMoney(amount.abs())) {
                        return card.getDescription() + " - paid $" + amount.abs();
                    } else {
                        player.setBankrupt();
                        return card.getDescription() + " - cannot afford payment, declared bankrupt";
                    }
                }
                
            case GO_TO_JAIL:
                player.sendToJail();
                return card.getDescription();
                
            case GET_OUT_OF_JAIL:
                player.giveGetOutOfJailCard();
                return card.getDescription() + " - keep this card";
                
            case COLLECT_FROM_PLAYERS:
                BigDecimal collectAmount = new BigDecimal(card.getValue());
                BigDecimal totalCollected = BigDecimal.ZERO;
                for (Player otherPlayer : players) {
                    if (!otherPlayer.equals(player) && !otherPlayer.isBankrupt()) {
                        if (otherPlayer.subtractMoney(collectAmount)) {
                            totalCollected = totalCollected.add(collectAmount);
                        } else {
                            totalCollected = totalCollected.add(otherPlayer.getMoney());
                            otherPlayer.setBankrupt();
                        }
                    }
                }
                player.addMoney(totalCollected);
                return card.getDescription() + " - collected $" + totalCollected + " from other players";
                
            default:
                return card.getDescription();
        }
    }
    
    // Property transaction methods
    public boolean buyProperty(String playerId, String propertyId) {
        Player player = playerMap.get(playerId);
        Property property = board.getProperty(propertyId);
        
        if (player == null || property == null || property.isOwned()) {
            return false;
        }
        
        if (player.getPosition() != getPropertyPosition(property)) {
            return false; // Player must be on the property to buy it
        }
        
        return player.buyProperty(property, property.getPurchasePrice());
    }
    
    public boolean buildHouse(String playerId, String propertyId) {
        Player player = playerMap.get(playerId);
        Property property = board.getProperty(propertyId);
        
        if (player == null || property == null || !property.isOwnedBy(player)) {
            return false;
        }
        
        return property.buildHouse();
    }
    
    public boolean buildHotel(String playerId, String propertyId) {
        Player player = playerMap.get(playerId);
        Property property = board.getProperty(propertyId);
        
        if (player == null || property == null || !property.isOwnedBy(player)) {
            return false;
        }
        
        return property.buildHotel();
    }
    
    public boolean mortgageProperty(String playerId, String propertyId) {
        Player player = playerMap.get(playerId);
        Property property = board.getProperty(propertyId);
        
        if (player == null || property == null || !property.isOwnedBy(player)) {
            return false;
        }
        
        return property.mortgage();
    }
    
    public boolean unmortgageProperty(String playerId, String propertyId) {
        Player player = playerMap.get(playerId);
        Property property = board.getProperty(propertyId);
        
        if (player == null || property == null || !property.isOwnedBy(player)) {
            return false;
        }
        
        return property.unmortgage();
    }
    
    // Game state methods
    private void nextPlayer() {
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        } while (getCurrentPlayer().isBankrupt() && getActivePlayers().size() > 1);
    }
    
    private void checkGameEnd() {
        List<Player> activePlayers = getActivePlayers();
        
        if (activePlayers.size() <= 1) {
            status = GameStatus.FINISHED;
            if (activePlayers.size() == 1) {
                winner = activePlayers.get(0);
            }
        }
    }
    
    private void endGameByTurnLimit() {
        status = GameStatus.FINISHED;
        // Winner is player with highest net worth
        winner = players.stream()
                .filter(p -> !p.isBankrupt())
                .max(Comparator.comparing(Player::getNetWorth))
                .orElse(null);
    }
    
    // Utility methods
    private int getPropertyPosition(Property property) {
        for (int i = 0; i < 40; i++) {
            BoardSpace space = board.getSpace(i);
            if (space.isProperty() && space.getProperty().equals(property)) {
                return i;
            }
        }
        return -1;
    }
    
    // Getters
    public String getGameId() { return gameId; }
    public GameBoard getBoard() { return board; }
    public List<Player> getPlayers() { return new ArrayList<>(players); }
    public List<Player> getActivePlayers() {
        return players.stream()
                .filter(p -> !p.isBankrupt())
                .collect(ArrayList::new, (list, p) -> list.add(p), ArrayList::addAll);
    }
    public Player getCurrentPlayer() { 
        return players.isEmpty() ? null : players.get(currentPlayerIndex); 
    }
    public GameStatus getStatus() { return status; }
    public Player getWinner() { return winner; }
    public int getTurnCount() { return turnCount; }
    public int getMaxTurns() { return maxTurns; }
    
    public Player getPlayer(String playerId) {
        return playerMap.get(playerId);
    }
    
    @Override
    public String toString() {
        return String.format("MonopolyGame{id='%s', players=%d, status=%s, turn=%d, currentPlayer=%s}",
                           gameId, players.size(), status, turnCount, 
                           getCurrentPlayer() != null ? getCurrentPlayer().getName() : "None");
    }
}