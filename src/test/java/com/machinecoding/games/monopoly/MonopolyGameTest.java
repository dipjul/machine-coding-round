package com.machinecoding.games.monopoly;

import com.machinecoding.games.monopoly.model.*;
import com.machinecoding.games.monopoly.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Comprehensive test suite for the Monopoly Game.
 * Tests game setup, player mechanics, property transactions, and game rules.
 */
public class MonopolyGameTest {
    
    private MonopolyGame game;
    
    @BeforeEach
    void setUp() {
        game = new MonopolyGame("TEST_GAME");
    }
    
    @Test
    @DisplayName("Game Setup and Player Management")
    void testGameSetup() {
        // Test initial game state
        assertEquals(GameStatus.WAITING_FOR_PLAYERS, game.getStatus());
        assertEquals(0, game.getPlayers().size());
        assertNull(game.getCurrentPlayer());
        
        // Test adding players
        Player alice = game.addPlayer("Alice");
        assertNotNull(alice);
        assertEquals("Alice", alice.getName());
        assertEquals(new BigDecimal("1500"), alice.getMoney());
        assertEquals(0, alice.getPosition());
        assertFalse(alice.isInJail());
        assertTrue(alice.isActive());
        
        Player bob = game.addPlayer("Bob");
        assertNotNull(bob);
        assertEquals(2, game.getPlayers().size());
        
        // Test starting game with insufficient players
        game = new MonopolyGame("TEST_GAME2");
        game.addPlayer("Solo");
        assertFalse(game.startGame());
        assertEquals(GameStatus.WAITING_FOR_PLAYERS, game.getStatus());
        
        // Test starting game with sufficient players
        game.addPlayer("Player2");
        assertTrue(game.startGame());
        assertEquals(GameStatus.IN_PROGRESS, game.getStatus());
        assertNotNull(game.getCurrentPlayer());
        
        // Test adding players after game started
        assertThrows(IllegalStateException.class, () -> {
            game.addPlayer("Late Player");
        });
        
        // Test maximum players
        MonopolyGame maxGame = new MonopolyGame("MAX_GAME");
        for (int i = 1; i <= 8; i++) {
            maxGame.addPlayer("Player" + i);
        }
        assertEquals(8, maxGame.getPlayers().size());
        
        assertThrows(IllegalStateException.class, () -> {
            maxGame.addPlayer("Player9");
        });
    }
    
    @Test
    @DisplayName("Player Movement and Dice Rolling")
    void testPlayerMovement() {
        setupTestGame();
        
        Player alice = game.getPlayer("PLAYER01");
        int initialPosition = alice.getPosition();
        BigDecimal initialMoney = alice.getMoney();
        
        // Test basic turn
        TurnResult result = game.takeTurn(alice.getPlayerId());
        assertNotNull(result);
        assertEquals(alice, result.getPlayer());
        assertTrue(result.getDie1() >= 1 && result.getDie1() <= 6);
        assertTrue(result.getDie2() >= 1 && result.getDie2() <= 6);
        assertEquals(result.getDie1() + result.getDie2(), result.getTotal());
        assertEquals(result.getDie1() == result.getDie2(), result.isDoubles());
        
        // Player should have moved
        assertTrue(alice.getPosition() != initialPosition || result.getTotal() == 0);
        
        // Test passing GO
        alice.setPosition(38); // Near GO
        BigDecimal moneyBeforeGO = alice.getMoney();
        alice.moveBy(4); // Should pass GO
        assertEquals(2, alice.getPosition());
        assertEquals(moneyBeforeGO.add(new BigDecimal("200")), alice.getMoney());
        
        // Test wrong player turn
        Player bob = game.getPlayer("PLAYER02");
        if (!game.getCurrentPlayer().equals(bob)) {
            assertThrows(IllegalArgumentException.class, () -> {
                game.takeTurn(bob.getPlayerId());
            });
        }
    }
    
    @Test
    @DisplayName("Property Purchase and Ownership")
    void testPropertyTransactions() {
        setupTestGame();
        
        Player alice = game.getPlayer("PLAYER01");
        Property mediterraneanAve = game.getBoard().getSpace(1).getProperty();
        
        // Test property purchase when not on property
        assertFalse(game.buyProperty(alice.getPlayerId(), mediterraneanAve.getPropertyId()));
        
        // Move to property and purchase
        alice.setPosition(1);
        BigDecimal moneyBefore = alice.getMoney();
        assertTrue(game.buyProperty(alice.getPlayerId(), mediterraneanAve.getPropertyId()));
        
        // Verify purchase
        assertTrue(mediterraneanAve.isOwned());
        assertEquals(alice, mediterraneanAve.getOwner());
        assertEquals(moneyBefore.subtract(mediterraneanAve.getPurchasePrice()), alice.getMoney());
        assertTrue(alice.getOwnedProperties().contains(mediterraneanAve));
        
        // Test buying already owned property
        assertFalse(game.buyProperty(alice.getPlayerId(), mediterraneanAve.getPropertyId()));
        
        // Test buying property without enough money
        alice.subtractMoney(alice.getMoney().subtract(new BigDecimal("50"))); // Leave only $50
        Property boardwalk = game.getBoard().getSpace(39).getProperty();
        alice.setPosition(39);
        assertFalse(game.buyProperty(alice.getPlayerId(), boardwalk.getPropertyId()));
    }
    
    @Test
    @DisplayName("Rent Calculation and Payment")
    void testRentMechanics() {
        setupTestGame();
        
        Player alice = game.getPlayer("PLAYER01");
        Player bob = game.getPlayer("PLAYER02");
        Property mediterraneanAve = game.getBoard().getSpace(1).getProperty();
        
        // Alice buys property
        alice.setPosition(1);
        game.buyProperty(alice.getPlayerId(), mediterraneanAve.getPropertyId());
        
        // Test rent calculation
        BigDecimal baseRent = mediterraneanAve.calculateRent(7);
        assertEquals(mediterraneanAve.getBaseRent(), baseRent);
        
        // Test rent with complete color group
        Property balticAve = game.getBoard().getSpace(3).getProperty();
        alice.setPosition(3);
        game.buyProperty(alice.getPlayerId(), balticAve.getPropertyId());
        
        BigDecimal doubleRent = mediterraneanAve.calculateRent(7);
        assertEquals(mediterraneanAve.getBaseRent().multiply(new BigDecimal("2")), doubleRent);
        
        // Test rent payment
        BigDecimal bobMoneyBefore = bob.getMoney();
        BigDecimal aliceMoneyBefore = alice.getMoney();
        
        bob.setPosition(1);
        BigDecimal rent = mediterraneanAve.calculateRent(7);
        assertTrue(bob.subtractMoney(rent));
        alice.addMoney(rent);
        
        assertEquals(bobMoneyBefore.subtract(rent), bob.getMoney());
        assertEquals(aliceMoneyBefore.add(rent), alice.getMoney());
    }
    
    @Test
    @DisplayName("Property Development")
    void testPropertyDevelopment() {
        setupTestGame();
        
        Player alice = game.getPlayer("PLAYER01");
        Property mediterraneanAve = game.getBoard().getSpace(1).getProperty();
        Property balticAve = game.getBoard().getSpace(3).getProperty();
        
        // Buy both brown properties
        alice.setPosition(1);
        game.buyProperty(alice.getPlayerId(), mediterraneanAve.getPropertyId());
        alice.setPosition(3);
        game.buyProperty(alice.getPlayerId(), balticAve.getPropertyId());
        
        // Test building houses
        assertTrue(mediterraneanAve.canBuildHouse());
        assertTrue(game.buildHouse(alice.getPlayerId(), mediterraneanAve.getPropertyId()));
        assertEquals(1, mediterraneanAve.getHouses());
        
        // Test building multiple houses
        for (int i = 2; i <= 4; i++) {
            assertTrue(game.buildHouse(alice.getPlayerId(), mediterraneanAve.getPropertyId()));
            assertEquals(i, mediterraneanAve.getHouses());
        }
        
        // Test building hotel
        assertTrue(mediterraneanAve.canBuildHotel());
        assertTrue(game.buildHotel(alice.getPlayerId(), mediterraneanAve.getPropertyId()));
        assertTrue(mediterraneanAve.hasHotel());
        assertEquals(0, mediterraneanAve.getHouses()); // Houses removed when hotel built
        
        // Test can't build more after hotel
        assertFalse(mediterraneanAve.canBuildHouse());
        assertFalse(mediterraneanAve.canBuildHotel());
        
        // Test rent with development
        BigDecimal hotelRent = mediterraneanAve.calculateRent(7);
        assertTrue(hotelRent.compareTo(mediterraneanAve.getBaseRent()) > 0);
    }
    
    @Test
    @DisplayName("Jail Mechanics")
    void testJailMechanics() {
        setupTestGame();
        
        Player alice = game.getPlayer("PLAYER01");
        
        // Test sending to jail
        assertFalse(alice.isInJail());
        alice.sendToJail();
        assertTrue(alice.isInJail());
        assertEquals(10, alice.getPosition()); // Jail position
        assertEquals(0, alice.getJailTurns());
        assertEquals(0, alice.getConsecutiveDoubles());
        
        // Test jail turns
        alice.incrementJailTurns();
        assertEquals(1, alice.getJailTurns());
        
        alice.incrementJailTurns();
        alice.incrementJailTurns();
        assertFalse(alice.isInJail()); // Released after 3 turns
        
        // Test get out of jail card
        alice.sendToJail();
        alice.giveGetOutOfJailCard();
        assertTrue(alice.hasGetOutOfJailCard());
        
        alice.useGetOutOfJailCard();
        assertFalse(alice.isInJail());
        assertFalse(alice.hasGetOutOfJailCard());
        
        // Test consecutive doubles leading to jail
        alice.resetConsecutiveDoubles();
        assertEquals(0, alice.getConsecutiveDoubles());
        
        alice.incrementConsecutiveDoubles();
        alice.incrementConsecutiveDoubles();
        assertFalse(alice.isInJail());
        
        alice.incrementConsecutiveDoubles(); // Third double
        assertTrue(alice.isInJail());
    }
    
    @Test
    @DisplayName("Mortgage System")
    void testMortgageSystem() {
        setupTestGame();
        
        Player alice = game.getPlayer("PLAYER01");
        Property mediterraneanAve = game.getBoard().getSpace(1).getProperty();
        
        // Buy property
        alice.setPosition(1);
        game.buyProperty(alice.getPlayerId(), mediterraneanAve.getPropertyId());
        
        // Test mortgage
        BigDecimal moneyBefore = alice.getMoney();
        assertTrue(game.mortgageProperty(alice.getPlayerId(), mediterraneanAve.getPropertyId()));
        assertTrue(mediterraneanAve.isMortgaged());
        assertEquals(moneyBefore.add(mediterraneanAve.getMortgageValue()), alice.getMoney());
        
        // Test rent on mortgaged property
        assertEquals(BigDecimal.ZERO, mediterraneanAve.calculateRent(7));
        
        // Test unmortgage
        BigDecimal unmortgageCost = mediterraneanAve.getMortgageValue().multiply(new BigDecimal("1.1"));
        moneyBefore = alice.getMoney();
        assertTrue(game.unmortgageProperty(alice.getPlayerId(), mediterraneanAve.getPropertyId()));
        assertFalse(mediterraneanAve.isMortgaged());
        assertEquals(moneyBefore.subtract(unmortgageCost), alice.getMoney());
        
        // Test can't mortgage with houses
        Property balticAve = game.getBoard().getSpace(3).getProperty();
        alice.setPosition(3);
        game.buyProperty(alice.getPlayerId(), balticAve.getPropertyId());
        game.buildHouse(alice.getPlayerId(), mediterraneanAve.getPropertyId());
        
        assertFalse(game.mortgageProperty(alice.getPlayerId(), mediterraneanAve.getPropertyId()));
    }
    
    @Test
    @DisplayName("Railroad and Utility Rent")
    void testSpecialPropertyRent() {
        setupTestGame();
        
        Player alice = game.getPlayer("PLAYER01");
        
        // Test railroad rent
        Property readingRR = game.getBoard().getSpace(5).getProperty();
        alice.setPosition(5);
        game.buyProperty(alice.getPlayerId(), readingRR.getPropertyId());
        
        BigDecimal oneRailroadRent = readingRR.calculateRent(7);
        assertEquals(new BigDecimal("25"), oneRailroadRent);
        
        // Buy second railroad
        Property pennsylvaniaRR = game.getBoard().getSpace(15).getProperty();
        alice.setPosition(15);
        game.buyProperty(alice.getPlayerId(), pennsylvaniaRR.getPropertyId());
        
        BigDecimal twoRailroadRent = readingRR.calculateRent(7);
        assertEquals(new BigDecimal("50"), twoRailroadRent);
        
        // Test utility rent
        Property electricCompany = game.getBoard().getSpace(12).getProperty();
        alice.setPosition(12);
        game.buyProperty(alice.getPlayerId(), electricCompany.getPropertyId());
        
        BigDecimal oneUtilityRent = electricCompany.calculateRent(8);
        assertEquals(new BigDecimal("32"), oneUtilityRent); // 4 * 8
        
        // Buy second utility
        Property waterWorks = game.getBoard().getSpace(28).getProperty();
        alice.setPosition(28);
        game.buyProperty(alice.getPlayerId(), waterWorks.getPropertyId());
        
        BigDecimal twoUtilityRent = electricCompany.calculateRent(8);
        assertEquals(new BigDecimal("80"), twoUtilityRent); // 10 * 8
    }
    
    @Test
    @DisplayName("Game End Conditions")
    void testGameEndConditions() {
        setupTestGame();
        
        // Test bankruptcy
        Player alice = game.getPlayer("PLAYER01");
        Player bob = game.getPlayer("PLAYER02");
        
        alice.setBankrupt();
        assertTrue(alice.isBankrupt());
        assertFalse(alice.isActive());
        
        // Test active players count
        List<Player> activePlayers = game.getActivePlayers();
        assertEquals(3, activePlayers.size()); // 4 total - 1 bankrupt
        assertFalse(activePlayers.contains(alice));
        
        // Test game doesn't end with multiple active players
        assertEquals(GameStatus.IN_PROGRESS, game.getStatus());
        assertNull(game.getWinner());
        
        // Test turn limit
        MonopolyGame limitedGame = new MonopolyGame("LIMITED", 5);
        limitedGame.addPlayer("Player1");
        limitedGame.addPlayer("Player2");
        limitedGame.startGame();
        
        // Take turns until limit
        for (int i = 0; i < 6; i++) {
            if (limitedGame.getStatus() == GameStatus.IN_PROGRESS) {
                limitedGame.takeTurn(limitedGame.getCurrentPlayer().getPlayerId());
            }
        }
        
        assertEquals(GameStatus.FINISHED, limitedGame.getStatus());
        assertNotNull(limitedGame.getWinner());
    }
    
    @Test
    @DisplayName("Card System")
    void testCardSystem() {
        GameBoard board = new GameBoard();
        
        // Test drawing cards
        Card chanceCard = board.drawChanceCard();
        assertNotNull(chanceCard);
        assertNotNull(chanceCard.getCardId());
        assertNotNull(chanceCard.getDescription());
        assertNotNull(chanceCard.getType());
        
        Card ccCard = board.drawCommunityChestCard();
        assertNotNull(ccCard);
        assertNotNull(ccCard.getCardId());
        assertNotNull(ccCard.getDescription());
        assertNotNull(ccCard.getType());
        
        // Test card return
        board.returnChanceCard(chanceCard);
        board.returnCommunityChestCard(ccCard);
        
        // Cards should be shuffled back in
        assertNotNull(board.drawChanceCard());
        assertNotNull(board.drawCommunityChestCard());
    }
    
    @Test
    @DisplayName("Board Navigation")
    void testBoardNavigation() {
        GameBoard board = new GameBoard();
        
        // Test space retrieval
        BoardSpace go = board.getSpace(0);
        assertEquals("GO", go.getName());
        assertEquals(BoardSpaceType.GO, go.getType());
        
        BoardSpace mediterraneanAve = board.getSpace(1);
        assertEquals("Mediterranean Avenue", mediterraneanAve.getName());
        assertEquals(BoardSpaceType.PROPERTY, mediterraneanAve.getType());
        assertTrue(mediterraneanAve.isProperty());
        
        // Test wrapping around board
        BoardSpace wrapped = board.getSpace(40);
        assertEquals(go, wrapped);
        
        // Test property groups
        List<Property> brownProperties = board.getPropertiesByGroup(PropertyGroup.BROWN);
        assertEquals(2, brownProperties.size());
        
        List<Property> railroads = board.getPropertiesByGroup(PropertyGroup.RAILROAD);
        assertEquals(4, railroads.size());
        
        List<Property> utilities = board.getPropertiesByGroup(PropertyGroup.UTILITY);
        assertEquals(2, utilities.size());
        
        // Test nearest property finding
        int nearestRailroad = board.findNearestRailroad(3);
        assertEquals(5, nearestRailroad); // Reading Railroad
        
        int nearestUtility = board.findNearestUtility(20);
        assertEquals(28, nearestUtility); // Water Works
    }
    
    @Test
    @DisplayName("Player Net Worth Calculation")
    void testNetWorthCalculation() {
        setupTestGame();
        
        Player alice = game.getPlayer("PLAYER01");
        BigDecimal initialNetWorth = alice.getNetWorth();
        assertEquals(alice.getMoney(), initialNetWorth); // No properties initially
        
        // Buy property
        Property mediterraneanAve = game.getBoard().getSpace(1).getProperty();
        alice.setPosition(1);
        game.buyProperty(alice.getPlayerId(), mediterraneanAve.getPropertyId());
        
        BigDecimal newNetWorth = alice.getNetWorth();
        assertTrue(newNetWorth.compareTo(initialNetWorth) > 0);
        
        // Build house
        Property balticAve = game.getBoard().getSpace(3).getProperty();
        alice.setPosition(3);
        game.buyProperty(alice.getPlayerId(), balticAve.getPropertyId());
        game.buildHouse(alice.getPlayerId(), mediterraneanAve.getPropertyId());
        
        BigDecimal developedNetWorth = alice.getNetWorth();
        assertTrue(developedNetWorth.compareTo(newNetWorth) > 0);
    }
    
    // Helper method to set up a test game
    private void setupTestGame() {
        game.addPlayer("Alice");
        game.addPlayer("Bob");
        game.addPlayer("Charlie");
        game.addPlayer("Diana");
        game.startGame();
    }
}