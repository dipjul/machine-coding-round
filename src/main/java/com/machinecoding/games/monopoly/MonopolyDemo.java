package com.machinecoding.games.monopoly;

import com.machinecoding.games.monopoly.model.*;
import com.machinecoding.games.monopoly.service.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Comprehensive demonstration of the Monopoly Game.
 * Shows game setup, player turns, property transactions, and game mechanics.
 */
public class MonopolyDemo {
    
    public static void main(String[] args) {
        System.out.println("=== Monopoly Game Demo ===\n");
        
        // Demo 1: Game Setup
        System.out.println("=== Demo 1: Game Setup ===");
        demonstrateGameSetup();
        
        // Demo 2: Basic Gameplay
        System.out.println("\n=== Demo 2: Basic Gameplay ===");
        demonstrateBasicGameplay();
        
        // Demo 3: Property Transactions
        System.out.println("\n=== Demo 3: Property Transactions ===");
        demonstratePropertyTransactions();
        
        // Demo 4: Advanced Game Mechanics
        System.out.println("\n=== Demo 4: Advanced Game Mechanics ===");
        demonstrateAdvancedMechanics();
        
        // Demo 5: Complete Game Simulation
        System.out.println("\n=== Demo 5: Complete Game Simulation ===");
        demonstrateCompleteGame();
        
        System.out.println("\n=== Demo Complete ===");
    }
    
    private static void demonstrateGameSetup() {
        System.out.println("1. Creating new Monopoly game:");
        MonopolyGame game = new MonopolyGame("GAME001");
        System.out.println("   Created game: " + game);
        
        System.out.println("\n2. Adding players:");
        Player alice = game.addPlayer("Alice");
        System.out.println("   Added player: " + alice);
        
        Player bob = game.addPlayer("Bob");
        System.out.println("   Added player: " + bob);
        
        Player charlie = game.addPlayer("Charlie");
        System.out.println("   Added player: " + charlie);
        
        Player diana = game.addPlayer("Diana");
        System.out.println("   Added player: " + diana);
        
        System.out.println("\n3. Game state before starting:");
        System.out.println("   Status: " + game.getStatus());
        System.out.println("   Players: " + game.getPlayers().size());
        System.out.println("   Current player: " + (game.getCurrentPlayer() != null ? game.getCurrentPlayer().getName() : "None"));
        
        System.out.println("\n4. Starting the game:");
        boolean started = game.startGame();
        System.out.println("   Game started: " + started);
        System.out.println("   Status: " + game.getStatus());
        System.out.println("   Current player: " + game.getCurrentPlayer().getName());
        
        System.out.println("\n5. Game board information:");
        GameBoard board = game.getBoard();
        System.out.println("   Board: " + board);
        
        // Show some properties
        System.out.println("   Sample properties:");
        System.out.println("     " + board.getSpace(1).getProperty());
        System.out.println("     " + board.getSpace(6).getProperty());
        System.out.println("     " + board.getSpace(11).getProperty());
    }
    
    private static void demonstrateBasicGameplay() {
        MonopolyGame game = new MonopolyGame("GAME002");
        setupTestGame(game);
        
        System.out.println("1. Taking player turns:");
        
        // Take several turns to show basic gameplay
        for (int i = 0; i < 8; i++) {
            Player currentPlayer = game.getCurrentPlayer();
            System.out.println("\\n   Turn " + (i + 1) + " - " + currentPlayer.getName() + "'s turn:");
            System.out.println("     Position: " + currentPlayer.getPosition() + " (" + 
                             game.getBoard().getSpace(currentPlayer.getPosition()).getName() + ")");
            System.out.println("     Money: $" + currentPlayer.getMoney());
            
            TurnResult result = game.takeTurn(currentPlayer.getPlayerId());
            System.out.println("     Rolled: [" + result.getDie1() + "," + result.getDie2() + "] = " + result.getTotal());
            System.out.println("     Doubles: " + result.isDoubles());
            System.out.println("     New position: " + result.getPlayer().getPosition() + " (" + 
                             game.getBoard().getSpace(result.getPlayer().getPosition()).getName() + ")");
            System.out.println("     Result: " + result.getMessage());
            System.out.println("     Money after: $" + result.getPlayer().getMoney());
        }
        
        System.out.println("\n2. Game state after turns:");
        System.out.println("   Status: " + game.getStatus());
        System.out.println("   Turn count: " + game.getTurnCount());
        System.out.println("   Active players: " + game.getActivePlayers().size());
        
        for (Player player : game.getPlayers()) {
            System.out.println("     " + player.getName() + ": Position " + player.getPosition() + 
                             ", Money $" + player.getMoney() + ", Properties: " + player.getOwnedProperties().size());
        }
    }
    
    private static void demonstratePropertyTransactions() {
        MonopolyGame game = new MonopolyGame("GAME003");
        setupTestGame(game);
        
        System.out.println("1. Simulating property purchases:");
        
        // Move players to properties and demonstrate purchases
        Player alice = game.getPlayer("PLAYER01");
        Player bob = game.getPlayer("PLAYER02");
        
        // Move Alice to Mediterranean Avenue (position 1)
        alice.setPosition(1);
        Property mediterraneanAve = game.getBoard().getSpace(1).getProperty();
        
        System.out.println("   Alice is at " + mediterraneanAve.getName());
        System.out.println("   Property price: $" + mediterraneanAve.getPurchasePrice());
        System.out.println("   Alice's money: $" + alice.getMoney());
        
        boolean purchased = game.buyProperty(alice.getPlayerId(), mediterraneanAve.getPropertyId());
        System.out.println("   Purchase successful: " + purchased);
        System.out.println("   Alice's money after: $" + alice.getMoney());
        System.out.println("   Property owner: " + (mediterraneanAve.getOwner() != null ? mediterraneanAve.getOwner().getName() : "None"));
        
        System.out.println("\n2. Demonstrating rent payment:");
        
        // Move Bob to Mediterranean Avenue
        bob.setPosition(1);
        BigDecimal bobMoneyBefore = bob.getMoney();
        BigDecimal aliceMoneyBefore = alice.getMoney();
        
        System.out.println("   Bob lands on " + mediterraneanAve.getName() + " (owned by Alice)");
        System.out.println("   Bob's money before: $" + bobMoneyBefore);
        System.out.println("   Alice's money before: $" + aliceMoneyBefore);
        
        BigDecimal rent = mediterraneanAve.calculateRent(7); // Simulate dice roll of 7
        System.out.println("   Rent amount: $" + rent);
        
        if (bob.subtractMoney(rent)) {
            alice.addMoney(rent);
            System.out.println("   Rent paid successfully");
        }
        
        System.out.println("   Bob's money after: $" + bob.getMoney());
        System.out.println("   Alice's money after: $" + alice.getMoney());
        
        System.out.println("\n3. Demonstrating property development:");
        
        // Try to build houses (need complete color group)
        Property balticAve = game.getBoard().getSpace(3).getProperty();
        alice.setPosition(3);
        game.buyProperty(alice.getPlayerId(), balticAve.getPropertyId());
        
        System.out.println("   Alice now owns both Brown properties");
        System.out.println("   Mediterranean Ave can build house: " + mediterraneanAve.canBuildHouse());
        System.out.println("   Baltic Ave can build house: " + balticAve.canBuildHouse());
        
        boolean houseBuilt = game.buildHouse(alice.getPlayerId(), mediterraneanAve.getPropertyId());
        System.out.println("   Built house on Mediterranean Ave: " + houseBuilt);
        System.out.println("   Houses on Mediterranean Ave: " + mediterraneanAve.getHouses());
        System.out.println("   Alice's money after building: $" + alice.getMoney());
        
        // Show increased rent
        BigDecimal newRent = mediterraneanAve.calculateRent(7);
        System.out.println("   New rent with house: $" + newRent);
    }
    
    private static void demonstrateAdvancedMechanics() {
        MonopolyGame game = new MonopolyGame("GAME004");
        setupTestGame(game);
        
        System.out.println("1. Demonstrating jail mechanics:");
        
        Player alice = game.getPlayer("PLAYER01");
        System.out.println("   Alice before jail: Position " + alice.getPosition() + ", In jail: " + alice.isInJail());
        
        // Send Alice to jail
        alice.sendToJail();
        System.out.println("   Alice sent to jail: Position " + alice.getPosition() + ", In jail: " + alice.isInJail());
        System.out.println("   Jail turns: " + alice.getJailTurns());
        
        // Simulate jail turns
        for (int i = 0; i < 3; i++) {
            System.out.println("\\n   Jail turn " + (i + 1) + ":");
            TurnResult result = game.takeTurn(alice.getPlayerId());
            System.out.println("     Rolled: [" + result.getDie1() + "," + result.getDie2() + "] = " + result.getTotal());
            System.out.println("     Doubles: " + result.isDoubles());
            System.out.println("     Still in jail: " + alice.isInJail());
            System.out.println("     Result: " + result.getMessage());
            
            if (!alice.isInJail()) {
                break;
            }
        }
        
        System.out.println("\n2. Demonstrating card mechanics:");
        
        // Draw some cards to show card effects
        GameBoard board = game.getBoard();
        
        System.out.println("   Drawing Chance cards:");
        for (int i = 0; i < 3; i++) {
            Card chanceCard = board.drawChanceCard();
            System.out.println("     " + chanceCard);
        }
        
        System.out.println("   Drawing Community Chest cards:");
        for (int i = 0; i < 3; i++) {
            Card ccCard = board.drawCommunityChestCard();
            System.out.println("     " + ccCard);
        }
        
        System.out.println("\n3. Demonstrating property groups:");
        
        List<Property> brownProperties = board.getPropertiesByGroup(PropertyGroup.BROWN);
        System.out.println("   Brown properties: " + brownProperties.size());
        for (Property property : brownProperties) {
            System.out.println("     " + property.getName() + " - $" + property.getPurchasePrice());
        }
        
        List<Property> railroads = board.getPropertiesByGroup(PropertyGroup.RAILROAD);
        System.out.println("   Railroads: " + railroads.size());
        for (Property property : railroads) {
            System.out.println("     " + property.getName() + " - $" + property.getPurchasePrice());
        }
    }
    
    private static void demonstrateCompleteGame() {
        MonopolyGame game = new MonopolyGame("GAME005", 50); // Limit to 50 turns for demo
        setupTestGame(game);
        
        System.out.println("1. Running complete game simulation:");
        System.out.println("   Initial state:");
        for (Player player : game.getPlayers()) {
            System.out.println("     " + player.getName() + ": $" + player.getMoney());
        }
        
        int turnCount = 0;
        while (game.getStatus() == GameStatus.IN_PROGRESS && turnCount < 30) { // Limit for demo
            Player currentPlayer = game.getCurrentPlayer();
            
            if (turnCount % 10 == 0) {
                System.out.println("\\n   Turn " + turnCount + " - Game state:");
                for (Player player : game.getPlayers()) {
                    if (!player.isBankrupt()) {
                        System.out.println("     " + player.getName() + ": Position " + player.getPosition() + 
                                         ", Money $" + player.getMoney() + ", Properties: " + player.getOwnedProperties().size() +
                                         ", Net Worth: $" + player.getNetWorth());
                    }
                }
            }
            
            try {
                TurnResult result = game.takeTurn(currentPlayer.getPlayerId());
                
                // Auto-buy properties if affordable and available
                BoardSpace currentSpace = game.getBoard().getSpace(result.getPlayer().getPosition());
                if (currentSpace.isProperty() && !currentSpace.getProperty().isOwned() && 
                    result.getPlayer().canAfford(currentSpace.getProperty().getPurchasePrice()) &&
                    result.getPlayer().getMoney().compareTo(new BigDecimal("500")) > 0) { // Keep some cash
                    game.buyProperty(result.getPlayer().getPlayerId(), currentSpace.getProperty().getPropertyId());
                }
                
                turnCount++;
            } catch (Exception e) {
                System.out.println("     Error in turn: " + e.getMessage());
                break;
            }
        }
        
        System.out.println("\n2. Final game state:");
        System.out.println("   Status: " + game.getStatus());
        System.out.println("   Total turns: " + game.getTurnCount());
        System.out.println("   Winner: " + (game.getWinner() != null ? game.getWinner().getName() : "None"));
        
        System.out.println("\\n   Final player standings:");
        List<Player> players = game.getPlayers();
        players.sort((a, b) -> b.getNetWorth().compareTo(a.getNetWorth()));
        
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            System.out.println("     " + (i + 1) + ". " + player.getName() + 
                             " - Net Worth: $" + player.getNetWorth() + 
                             " (Money: $" + player.getMoney() + ", Properties: " + player.getOwnedProperties().size() + ")" +
                             (player.isBankrupt() ? " [BANKRUPT]" : ""));
        }
        
        System.out.println("\n3. Property ownership summary:");
        GameBoard board = game.getBoard();
        int ownedProperties = 0;
        for (Property property : board.getAllProperties()) {
            if (property.isOwned()) {
                ownedProperties++;
                if (ownedProperties <= 10) { // Show first 10 owned properties
                    System.out.println("     " + property.getName() + " - Owner: " + property.getOwner().getName() +
                                     " (Houses: " + property.getHouses() + ", Hotel: " + property.hasHotel() + ")");
                }
            }
        }
        System.out.println("   Total properties owned: " + ownedProperties + "/" + board.getAllProperties().size());
    }
    
    // Helper method to set up a test game
    private static void setupTestGame(MonopolyGame game) {
        game.addPlayer("Alice");
        game.addPlayer("Bob");
        game.addPlayer("Charlie");
        game.addPlayer("Diana");
        game.startGame();
    }
}