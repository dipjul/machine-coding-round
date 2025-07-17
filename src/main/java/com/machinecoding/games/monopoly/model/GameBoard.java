package com.machinecoding.games.monopoly.model;

import java.math.BigDecimal;
import java.util.*;

/**
 * Represents the Monopoly game board with all spaces and properties.
 */
public class GameBoard {
    private final Map<Integer, BoardSpace> spaces;
    private final Map<String, Property> properties;
    private final List<Card> chanceCards;
    private final List<Card> communityChestCards;
    
    public GameBoard() {
        this.spaces = new HashMap<>();
        this.properties = new HashMap<>();
        this.chanceCards = new ArrayList<>();
        this.communityChestCards = new ArrayList<>();
        initializeBoard();
        initializeCards();
    }
    
    private void initializeBoard() {
        // Initialize all 40 spaces on the Monopoly board
        
        // GO
        spaces.put(0, new BoardSpace(0, "GO", BoardSpaceType.GO));
        
        // Brown properties
        Property mediterraneanAve = new Property("PROP001", "Mediterranean Avenue", PropertyType.STREET, 
                PropertyGroup.BROWN, new BigDecimal("60"), new BigDecimal("2"), 
                new BigDecimal("50"), new BigDecimal("50"));
        spaces.put(1, new BoardSpace(1, "Mediterranean Avenue", BoardSpaceType.PROPERTY, mediterraneanAve));
        properties.put("PROP001", mediterraneanAve);
        
        // Community Chest
        spaces.put(2, new BoardSpace(2, "Community Chest", BoardSpaceType.COMMUNITY_CHEST));
        
        Property balticAve = new Property("PROP002", "Baltic Avenue", PropertyType.STREET, 
                PropertyGroup.BROWN, new BigDecimal("60"), new BigDecimal("4"), 
                new BigDecimal("50"), new BigDecimal("50"));
        spaces.put(3, new BoardSpace(3, "Baltic Avenue", BoardSpaceType.PROPERTY, balticAve));
        properties.put("PROP002", balticAve);
        
        // Income Tax
        spaces.put(4, new BoardSpace(4, "Income Tax", BoardSpaceType.TAX, new BigDecimal("200")));
        
        // Reading Railroad
        Property readingRR = new Property("PROP003", "Reading Railroad", PropertyType.RAILROAD, 
                PropertyGroup.RAILROAD, new BigDecimal("200"), new BigDecimal("25"), 
                BigDecimal.ZERO, BigDecimal.ZERO);
        spaces.put(5, new BoardSpace(5, "Reading Railroad", BoardSpaceType.PROPERTY, readingRR));
        properties.put("PROP003", readingRR);
        
        // Light Blue properties
        Property orientalAve = new Property("PROP004", "Oriental Avenue", PropertyType.STREET, 
                PropertyGroup.LIGHT_BLUE, new BigDecimal("100"), new BigDecimal("6"), 
                new BigDecimal("50"), new BigDecimal("50"));
        spaces.put(6, new BoardSpace(6, "Oriental Avenue", BoardSpaceType.PROPERTY, orientalAve));
        properties.put("PROP004", orientalAve);
        
        // Chance\n        spaces.put(7, new BoardSpace(7, \"Chance\", BoardSpaceType.CHANCE));\n        \n        Property vermontAve = new Property(\"PROP005\", \"Vermont Avenue\", PropertyType.STREET, \n                PropertyGroup.LIGHT_BLUE, new BigDecimal(\"100\"), new BigDecimal(\"6\"), \n                new BigDecimal(\"50\"), new BigDecimal(\"50\"));\n        spaces.put(8, new BoardSpace(8, \"Vermont Avenue\", BoardSpaceType.PROPERTY, vermontAve));\n        properties.put(\"PROP005\", vermontAve);\n        \n        Property connecticutAve = new Property(\"PROP006\", \"Connecticut Avenue\", PropertyType.STREET, \n                PropertyGroup.LIGHT_BLUE, new BigDecimal(\"120\"), new BigDecimal(\"8\"), \n                new BigDecimal(\"50\"), new BigDecimal(\"50\"));\n        spaces.put(9, new BoardSpace(9, \"Connecticut Avenue\", BoardSpaceType.PROPERTY, connecticutAve));\n        properties.put(\"PROP006\", connecticutAve);\n        \n        // Jail\n        spaces.put(10, new BoardSpace(10, \"Jail\", BoardSpaceType.JAIL));\n        \n        // Pink properties\n        Property stCharlesPlace = new Property(\"PROP007\", \"St. Charles Place\", PropertyType.STREET, \n                PropertyGroup.PINK, new BigDecimal(\"140\"), new BigDecimal(\"10\"), \n                new BigDecimal(\"100\"), new BigDecimal(\"100\"));\n        spaces.put(11, new BoardSpace(11, \"St. Charles Place\", BoardSpaceType.PROPERTY, stCharlesPlace));\n        properties.put(\"PROP007\", stCharlesPlace);\n        \n        // Electric Company\n        Property electricCompany = new Property(\"PROP008\", \"Electric Company\", PropertyType.UTILITY, \n                PropertyGroup.UTILITY, new BigDecimal(\"150\"), BigDecimal.ZERO, \n                BigDecimal.ZERO, BigDecimal.ZERO);\n        spaces.put(12, new BoardSpace(12, \"Electric Company\", BoardSpaceType.PROPERTY, electricCompany));\n        properties.put(\"PROP008\", electricCompany);\n        \n        Property statesAve = new Property(\"PROP009\", \"States Avenue\", PropertyType.STREET, \n                PropertyGroup.PINK, new BigDecimal(\"140\"), new BigDecimal(\"10\"), \n                new BigDecimal(\"100\"), new BigDecimal(\"100\"));\n        spaces.put(13, new BoardSpace(13, \"States Avenue\", BoardSpaceType.PROPERTY, statesAve));\n        properties.put(\"PROP009\", statesAve);\n        \n        Property virginiaAve = new Property(\"PROP010\", \"Virginia Avenue\", PropertyType.STREET, \n                PropertyGroup.PINK, new BigDecimal(\"160\"), new BigDecimal(\"12\"), \n                new BigDecimal(\"100\"), new BigDecimal(\"100\"));\n        spaces.put(14, new BoardSpace(14, \"Virginia Avenue\", BoardSpaceType.PROPERTY, virginiaAve));\n        properties.put(\"PROP010\", virginiaAve);\n        \n        // Pennsylvania Railroad\n        Property pennsylvaniaRR = new Property(\"PROP011\", \"Pennsylvania Railroad\", PropertyType.RAILROAD, \n                PropertyGroup.RAILROAD, new BigDecimal(\"200\"), new BigDecimal(\"25\"), \n                BigDecimal.ZERO, BigDecimal.ZERO);\n        spaces.put(15, new BoardSpace(15, \"Pennsylvania Railroad\", BoardSpaceType.PROPERTY, pennsylvaniaRR));\n        properties.put(\"PROP011\", pennsylvaniaRR);\n        \n        // Orange properties\n        Property stJamesPlace = new Property(\"PROP012\", \"St. James Place\", PropertyType.STREET, \n                PropertyGroup.ORANGE, new BigDecimal(\"180\"), new BigDecimal(\"14\"), \n                new BigDecimal(\"100\"), new BigDecimal(\"100\"));\n        spaces.put(16, new BoardSpace(16, \"St. James Place\", BoardSpaceType.PROPERTY, stJamesPlace));\n        properties.put(\"PROP012\", stJamesPlace);\n        \n        // Community Chest\n        spaces.put(17, new BoardSpace(17, \"Community Chest\", BoardSpaceType.COMMUNITY_CHEST));\n        \n        Property tennesseeAve = new Property(\"PROP013\", \"Tennessee Avenue\", PropertyType.STREET, \n                PropertyGroup.ORANGE, new BigDecimal(\"180\"), new BigDecimal(\"14\"), \n                new BigDecimal(\"100\"), new BigDecimal(\"100\"));\n        spaces.put(18, new BoardSpace(18, \"Tennessee Avenue\", BoardSpaceType.PROPERTY, tennesseeAve));\n        properties.put(\"PROP013\", tennesseeAve);\n        \n        Property newYorkAve = new Property(\"PROP014\", \"New York Avenue\", PropertyType.STREET, \n                PropertyGroup.ORANGE, new BigDecimal(\"200\"), new BigDecimal(\"16\"), \n                new BigDecimal(\"100\"), new BigDecimal(\"100\"));\n        spaces.put(19, new BoardSpace(19, \"New York Avenue\", BoardSpaceType.PROPERTY, newYorkAve));\n        properties.put(\"PROP014\", newYorkAve);\n        \n        // Free Parking\n        spaces.put(20, new BoardSpace(20, \"Free Parking\", BoardSpaceType.FREE_PARKING));\n        \n        // Red properties\n        Property kentuckyAve = new Property(\"PROP015\", \"Kentucky Avenue\", PropertyType.STREET, \n                PropertyGroup.RED, new BigDecimal(\"220\"), new BigDecimal(\"18\"), \n                new BigDecimal(\"150\"), new BigDecimal(\"150\"));\n        spaces.put(21, new BoardSpace(21, \"Kentucky Avenue\", BoardSpaceType.PROPERTY, kentuckyAve));\n        properties.put(\"PROP015\", kentuckyAve);\n        \n        // Chance\n        spaces.put(22, new BoardSpace(22, \"Chance\", BoardSpaceType.CHANCE));\n        \n        Property indianaAve = new Property(\"PROP016\", \"Indiana Avenue\", PropertyType.STREET, \n                PropertyGroup.RED, new BigDecimal(\"220\"), new BigDecimal(\"18\"), \n                new BigDecimal(\"150\"), new BigDecimal(\"150\"));\n        spaces.put(23, new BoardSpace(23, \"Indiana Avenue\", BoardSpaceType.PROPERTY, indianaAve));\n        properties.put(\"PROP016\", indianaAve);\n        \n        Property illinoisAve = new Property(\"PROP017\", \"Illinois Avenue\", PropertyType.STREET, \n                PropertyGroup.RED, new BigDecimal(\"240\"), new BigDecimal(\"20\"), \n                new BigDecimal(\"150\"), new BigDecimal(\"150\"));\n        spaces.put(24, new BoardSpace(24, \"Illinois Avenue\", BoardSpaceType.PROPERTY, illinoisAve));\n        properties.put(\"PROP017\", illinoisAve);\n        \n        // B&O Railroad\n        Property boRR = new Property(\"PROP018\", \"B&O Railroad\", PropertyType.RAILROAD, \n                PropertyGroup.RAILROAD, new BigDecimal(\"200\"), new BigDecimal(\"25\"), \n                BigDecimal.ZERO, BigDecimal.ZERO);\n        spaces.put(25, new BoardSpace(25, \"B&O Railroad\", BoardSpaceType.PROPERTY, boRR));\n        properties.put(\"PROP018\", boRR);\n        \n        // Yellow properties\n        Property atlanticAve = new Property(\"PROP019\", \"Atlantic Avenue\", PropertyType.STREET, \n                PropertyGroup.YELLOW, new BigDecimal(\"260\"), new BigDecimal(\"22\"), \n                new BigDecimal(\"150\"), new BigDecimal(\"150\"));\n        spaces.put(26, new BoardSpace(26, \"Atlantic Avenue\", BoardSpaceType.PROPERTY, atlanticAve));\n        properties.put(\"PROP019\", atlanticAve);\n        \n        Property ventnorAve = new Property(\"PROP020\", \"Ventnor Avenue\", PropertyType.STREET, \n                PropertyGroup.YELLOW, new BigDecimal(\"260\"), new BigDecimal(\"22\"), \n                new BigDecimal(\"150\"), new BigDecimal(\"150\"));\n        spaces.put(27, new BoardSpace(27, \"Ventnor Avenue\", BoardSpaceType.PROPERTY, ventnorAve));\n        properties.put(\"PROP020\", ventnorAve);\n        \n        // Water Works\n        Property waterWorks = new Property(\"PROP021\", \"Water Works\", PropertyType.UTILITY, \n                PropertyGroup.UTILITY, new BigDecimal(\"150\"), BigDecimal.ZERO, \n                BigDecimal.ZERO, BigDecimal.ZERO);\n        spaces.put(28, new BoardSpace(28, \"Water Works\", BoardSpaceType.PROPERTY, waterWorks));\n        properties.put(\"PROP021\", waterWorks);\n        \n        Property marvinGardens = new Property(\"PROP022\", \"Marvin Gardens\", PropertyType.STREET, \n                PropertyGroup.YELLOW, new BigDecimal(\"280\"), new BigDecimal(\"24\"), \n                new BigDecimal(\"150\"), new BigDecimal(\"150\"));\n        spaces.put(29, new BoardSpace(29, \"Marvin Gardens\", BoardSpaceType.PROPERTY, marvinGardens));\n        properties.put(\"PROP022\", marvinGardens);\n        \n        // Go to Jail\n        spaces.put(30, new BoardSpace(30, \"Go to Jail\", BoardSpaceType.GO_TO_JAIL));\n        \n        // Green properties\n        Property pacificAve = new Property(\"PROP023\", \"Pacific Avenue\", PropertyType.STREET, \n                PropertyGroup.GREEN, new BigDecimal(\"300\"), new BigDecimal(\"26\"), \n                new BigDecimal(\"200\"), new BigDecimal(\"200\"));\n        spaces.put(31, new BoardSpace(31, \"Pacific Avenue\", BoardSpaceType.PROPERTY, pacificAve));\n        properties.put(\"PROP023\", pacificAve);\n        \n        Property northCarolinaAve = new Property(\"PROP024\", \"North Carolina Avenue\", PropertyType.STREET, \n                PropertyGroup.GREEN, new BigDecimal(\"300\"), new BigDecimal(\"26\"), \n                new BigDecimal(\"200\"), new BigDecimal(\"200\"));\n        spaces.put(32, new BoardSpace(32, \"North Carolina Avenue\", BoardSpaceType.PROPERTY, northCarolinaAve));\n        properties.put(\"PROP024\", northCarolinaAve);\n        \n        // Community Chest\n        spaces.put(33, new BoardSpace(33, \"Community Chest\", BoardSpaceType.COMMUNITY_CHEST));\n        \n        Property pennsylvaniaAve = new Property(\"PROP025\", \"Pennsylvania Avenue\", PropertyType.STREET, \n                PropertyGroup.GREEN, new BigDecimal(\"320\"), new BigDecimal(\"28\"), \n                new BigDecimal(\"200\"), new BigDecimal(\"200\"));\n        spaces.put(34, new BoardSpace(34, \"Pennsylvania Avenue\", BoardSpaceType.PROPERTY, pennsylvaniaAve));\n        properties.put(\"PROP025\", pennsylvaniaAve);\n        \n        // Short Line Railroad\n        Property shortLineRR = new Property(\"PROP026\", \"Short Line Railroad\", PropertyType.RAILROAD, \n                PropertyGroup.RAILROAD, new BigDecimal(\"200\"), new BigDecimal(\"25\"), \n                BigDecimal.ZERO, BigDecimal.ZERO);\n        spaces.put(35, new BoardSpace(35, \"Short Line Railroad\", BoardSpaceType.PROPERTY, shortLineRR));\n        properties.put(\"PROP026\", shortLineRR);\n        \n        // Chance\n        spaces.put(36, new BoardSpace(36, \"Chance\", BoardSpaceType.CHANCE));\n        \n        // Dark Blue properties\n        Property parkPlace = new Property(\"PROP027\", \"Park Place\", PropertyType.STREET, \n                PropertyGroup.DARK_BLUE, new BigDecimal(\"350\"), new BigDecimal(\"35\"), \n                new BigDecimal(\"200\"), new BigDecimal(\"200\"));\n        spaces.put(37, new BoardSpace(37, \"Park Place\", BoardSpaceType.PROPERTY, parkPlace));\n        properties.put(\"PROP027\", parkPlace);\n        \n        // Luxury Tax\n        spaces.put(38, new BoardSpace(38, \"Luxury Tax\", BoardSpaceType.TAX, new BigDecimal(\"100\")));\n        \n        Property boardwalk = new Property(\"PROP028\", \"Boardwalk\", PropertyType.STREET, \n                PropertyGroup.DARK_BLUE, new BigDecimal(\"400\"), new BigDecimal(\"50\"), \n                new BigDecimal(\"200\"), new BigDecimal(\"200\"));\n        spaces.put(39, new BoardSpace(39, \"Boardwalk\", BoardSpaceType.PROPERTY, boardwalk));\n        properties.put(\"PROP028\", boardwalk);\n    }\n    \n    private void initializeCards() {\n        // Initialize Chance cards\n        chanceCards.add(new Card(\"CHANCE001\", \"Advance to GO\", CardType.MOVE, \"GO\"));\n        chanceCards.add(new Card(\"CHANCE002\", \"Advance to Illinois Ave\", CardType.MOVE, \"24\"));\n        chanceCards.add(new Card(\"CHANCE003\", \"Advance to St. Charles Place\", CardType.MOVE, \"11\"));\n        chanceCards.add(new Card(\"CHANCE004\", \"Advance to nearest Railroad\", CardType.MOVE_NEAREST, \"RAILROAD\"));\n        chanceCards.add(new Card(\"CHANCE005\", \"Advance to nearest Utility\", CardType.MOVE_NEAREST, \"UTILITY\"));\n        chanceCards.add(new Card(\"CHANCE006\", \"Bank pays you $50\", CardType.MONEY, \"50\"));\n        chanceCards.add(new Card(\"CHANCE007\", \"Get out of Jail Free\", CardType.GET_OUT_OF_JAIL, \"\"));\n        chanceCards.add(new Card(\"CHANCE008\", \"Go back 3 spaces\", CardType.MOVE_RELATIVE, \"-3\"));\n        chanceCards.add(new Card(\"CHANCE009\", \"Go to Jail\", CardType.GO_TO_JAIL, \"\"));\n        chanceCards.add(new Card(\"CHANCE010\", \"Pay poor tax of $15\", CardType.MONEY, \"-15\"));\n        chanceCards.add(new Card(\"CHANCE011\", \"Take a trip to Reading Railroad\", CardType.MOVE, \"5\"));\n        chanceCards.add(new Card(\"CHANCE012\", \"Take a walk on the Boardwalk\", CardType.MOVE, \"39\"));\n        chanceCards.add(new Card(\"CHANCE013\", \"You have been elected Chairman of the Board\", CardType.COLLECT_FROM_PLAYERS, \"50\"));\n        chanceCards.add(new Card(\"CHANCE014\", \"Your building loan matures\", CardType.MONEY, \"150\"));\n        chanceCards.add(new Card(\"CHANCE015\", \"You have won a crossword competition\", CardType.MONEY, \"100\"));\n        chanceCards.add(new Card(\"CHANCE016\", \"Speeding fine $15\", CardType.MONEY, \"-15\"));\n        \n        // Initialize Community Chest cards\n        communityChestCards.add(new Card(\"CC001\", \"Advance to GO\", CardType.MOVE, \"GO\"));\n        communityChestCards.add(new Card(\"CC002\", \"Bank error in your favor\", CardType.MONEY, \"200\"));\n        communityChestCards.add(new Card(\"CC003\", \"Doctor's fees\", CardType.MONEY, \"-50\"));\n        communityChestCards.add(new Card(\"CC004\", \"From sale of stock you get $50\", CardType.MONEY, \"50\"));\n        communityChestCards.add(new Card(\"CC005\", \"Get out of Jail Free\", CardType.GET_OUT_OF_JAIL, \"\"));\n        communityChestCards.add(new Card(\"CC006\", \"Go to Jail\", CardType.GO_TO_JAIL, \"\"));\n        communityChestCards.add(new Card(\"CC007\", \"Holiday fund matures\", CardType.MONEY, \"100\"));\n        communityChestCards.add(new Card(\"CC008\", \"Income tax refund\", CardType.MONEY, \"20\"));\n        communityChestCards.add(new Card(\"CC009\", \"It is your birthday\", CardType.COLLECT_FROM_PLAYERS, \"10\"));\n        communityChestCards.add(new Card(\"CC010\", \"Life insurance matures\", CardType.MONEY, \"100\"));\n        communityChestCards.add(new Card(\"CC011\", \"Hospital fees\", CardType.MONEY, \"-100\"));\n        communityChestCards.add(new Card(\"CC012\", \"School fees\", CardType.MONEY, \"-50\"));\n        communityChestCards.add(new Card(\"CC013\", \"Receive $25 consultancy fee\", CardType.MONEY, \"25\"));\n        communityChestCards.add(new Card(\"CC014\", \"You are assessed for street repairs\", CardType.PROPERTY_TAX, \"40:115\"));\n        communityChestCards.add(new Card(\"CC015\", \"You have won second prize in a beauty contest\", CardType.MONEY, \"10\"));\n        communityChestCards.add(new Card(\"CC016\", \"You inherit $100\", CardType.MONEY, \"100\"));\n        \n        // Shuffle the cards\n        Collections.shuffle(chanceCards);\n        Collections.shuffle(communityChestCards);\n    }\n    \n    // Getters\n    public BoardSpace getSpace(int position) {\n        return spaces.get(position % 40);\n    }\n    \n    public Property getProperty(String propertyId) {\n        return properties.get(propertyId);\n    }\n    \n    public List<Property> getAllProperties() {\n        return new ArrayList<>(properties.values());\n    }\n    \n    public List<Property> getPropertiesByGroup(PropertyGroup group) {\n        return properties.values().stream()\n                .filter(p -> p.getGroup() == group)\n                .collect(ArrayList::new, (list, p) -> list.add(p), ArrayList::addAll);\n    }\n    \n    // Card operations\n    public Card drawChanceCard() {\n        if (chanceCards.isEmpty()) {\n            initializeCards(); // Reshuffle if deck is empty\n        }\n        return chanceCards.remove(0);\n    }\n    \n    public Card drawCommunityChestCard() {\n        if (communityChestCards.isEmpty()) {\n            initializeCards(); // Reshuffle if deck is empty\n        }\n        return communityChestCards.remove(0);\n    }\n    \n    public void returnChanceCard(Card card) {\n        chanceCards.add(card);\n        Collections.shuffle(chanceCards);\n    }\n    \n    public void returnCommunityChestCard(Card card) {\n        communityChestCards.add(card);\n        Collections.shuffle(communityChestCards);\n    }\n    \n    // Utility methods\n    public int findNearestRailroad(int currentPosition) {\n        int[] railroads = {5, 15, 25, 35};\n        for (int railroad : railroads) {\n            if (railroad > currentPosition) {\n                return railroad;\n            }\n        }\n        return railroads[0]; // Wrap around to Reading Railroad\n    }\n    \n    public int findNearestUtility(int currentPosition) {\n        int[] utilities = {12, 28};\n        for (int utility : utilities) {\n            if (utility > currentPosition) {\n                return utility;\n            }\n        }\n        return utilities[0]; // Wrap around to Electric Company\n    }\n    \n    @Override\n    public String toString() {\n        return String.format(\"GameBoard{spaces=%d, properties=%d, chanceCards=%d, communityChestCards=%d}\",\n                           spaces.size(), properties.size(), chanceCards.size(), communityChestCards.size());\n    }\n}"
        spaces.put(7, new BoardSpace(7, "Chance", BoardSpaceType.CHANCE));
        
        Property vermontAve = new Property("PROP005", "Vermont Avenue", PropertyType.STREET, 
                PropertyGroup.LIGHT_BLUE, new BigDecimal("100"), new BigDecimal("6"), 
                new BigDecimal("50"), new BigDecimal("50"));
        spaces.put(8, new BoardSpace(8, "Vermont Avenue", BoardSpaceType.PROPERTY, vermontAve));
        properties.put("PROP005", vermontAve);
        
        Property connecticutAve = new Property("PROP006", "Connecticut Avenue", PropertyType.STREET, 
                PropertyGroup.LIGHT_BLUE, new BigDecimal("120"), new BigDecimal("8"), 
                new BigDecimal("50"), new BigDecimal("50"));
        spaces.put(9, new BoardSpace(9, "Connecticut Avenue", BoardSpaceType.PROPERTY, connecticutAve));
        properties.put("PROP006", connecticutAve);
        
        // Jail
        spaces.put(10, new BoardSpace(10, "Jail", BoardSpaceType.JAIL));
        
        // Pink properties
        Property stCharlesPlace = new Property("PROP007", "St. Charles Place", PropertyType.STREET, 
                PropertyGroup.PINK, new BigDecimal("140"), new BigDecimal("10"), 
                new BigDecimal("100"), new BigDecimal("100"));
        spaces.put(11, new BoardSpace(11, "St. Charles Place", BoardSpaceType.PROPERTY, stCharlesPlace));
        properties.put("PROP007", stCharlesPlace);
        
        // Electric Company
        Property electricCompany = new Property("PROP008", "Electric Company", PropertyType.UTILITY, 
                PropertyGroup.UTILITY, new BigDecimal("150"), BigDecimal.ZERO, 
                BigDecimal.ZERO, BigDecimal.ZERO);
        spaces.put(12, new BoardSpace(12, "Electric Company", BoardSpaceType.PROPERTY, electricCompany));
        properties.put("PROP008", electricCompany);
        
        Property statesAve = new Property("PROP009", "States Avenue", PropertyType.STREET, 
                PropertyGroup.PINK, new BigDecimal("140"), new BigDecimal("10"), 
                new BigDecimal("100"), new BigDecimal("100"));
        spaces.put(13, new BoardSpace(13, "States Avenue", BoardSpaceType.PROPERTY, statesAve));
        properties.put("PROP009", statesAve);
        
        Property virginiaAve = new Property("PROP010", "Virginia Avenue", PropertyType.STREET, 
                PropertyGroup.PINK, new BigDecimal("160"), new BigDecimal("12"), 
                new BigDecimal("100"), new BigDecimal("100"));
        spaces.put(14, new BoardSpace(14, "Virginia Avenue", BoardSpaceType.PROPERTY, virginiaAve));
        properties.put("PROP010", virginiaAve);
        
        // Pennsylvania Railroad
        Property pennsylvaniaRR = new Property("PROP011", "Pennsylvania Railroad", PropertyType.RAILROAD, 
                PropertyGroup.RAILROAD, new BigDecimal("200"), new BigDecimal("25"), 
                BigDecimal.ZERO, BigDecimal.ZERO);
        spaces.put(15, new BoardSpace(15, "Pennsylvania Railroad", BoardSpaceType.PROPERTY, pennsylvaniaRR));
        properties.put("PROP011", pennsylvaniaRR);
        
        // Orange properties
        Property stJamesPlace = new Property("PROP012", "St. James Place", PropertyType.STREET, 
                PropertyGroup.ORANGE, new BigDecimal("180"), new BigDecimal("14"), 
                new BigDecimal("100"), new BigDecimal("100"));
        spaces.put(16, new BoardSpace(16, "St. James Place", BoardSpaceType.PROPERTY, stJamesPlace));
        properties.put("PROP012", stJamesPlace);
        
        // Community Chest
        spaces.put(17, new BoardSpace(17, "Community Chest", BoardSpaceType.COMMUNITY_CHEST));
        
        Property tennesseeAve = new Property("PROP013", "Tennessee Avenue", PropertyType.STREET, 
                PropertyGroup.ORANGE, new BigDecimal("180"), new BigDecimal("14"), 
                new BigDecimal("100"), new BigDecimal("100"));
        spaces.put(18, new BoardSpace(18, "Tennessee Avenue", BoardSpaceType.PROPERTY, tennesseeAve));
        properties.put("PROP013", tennesseeAve);
        
        Property newYorkAve = new Property("PROP014", "New York Avenue", PropertyType.STREET, 
                PropertyGroup.ORANGE, new BigDecimal("200"), new BigDecimal("16"), 
                new BigDecimal("100"), new BigDecimal("100"));
        spaces.put(19, new BoardSpace(19, "New York Avenue", BoardSpaceType.PROPERTY, newYorkAve));
        properties.put("PROP014", newYorkAve);
        
        // Free Parking
        spaces.put(20, new BoardSpace(20, "Free Parking", BoardSpaceType.FREE_PARKING));
        
        // Red properties
        Property kentuckyAve = new Property("PROP015", "Kentucky Avenue", PropertyType.STREET, 
                PropertyGroup.RED, new BigDecimal("220"), new BigDecimal("18"), 
                new BigDecimal("150"), new BigDecimal("150"));
        spaces.put(21, new BoardSpace(21, "Kentucky Avenue", BoardSpaceType.PROPERTY, kentuckyAve));
        properties.put("PROP015", kentuckyAve);
        
        // Chance
        spaces.put(22, new BoardSpace(22, "Chance", BoardSpaceType.CHANCE));
        
        Property indianaAve = new Property("PROP016", "Indiana Avenue", PropertyType.STREET, 
                PropertyGroup.RED, new BigDecimal("220"), new BigDecimal("18"), 
                new BigDecimal("150"), new BigDecimal("150"));
        spaces.put(23, new BoardSpace(23, "Indiana Avenue", BoardSpaceType.PROPERTY, indianaAve));
        properties.put("PROP016", indianaAve);
        
        Property illinoisAve = new Property("PROP017", "Illinois Avenue", PropertyType.STREET, 
                PropertyGroup.RED, new BigDecimal("240"), new BigDecimal("20"), 
                new BigDecimal("150"), new BigDecimal("150"));
        spaces.put(24, new BoardSpace(24, "Illinois Avenue", BoardSpaceType.PROPERTY, illinoisAve));
        properties.put("PROP017", illinoisAve);
        
        // B&O Railroad
        Property boRR = new Property("PROP018", "B&O Railroad", PropertyType.RAILROAD, 
                PropertyGroup.RAILROAD, new BigDecimal("200"), new BigDecimal("25"), 
                BigDecimal.ZERO, BigDecimal.ZERO);
        spaces.put(25, new BoardSpace(25, "B&O Railroad", BoardSpaceType.PROPERTY, boRR));
        properties.put("PROP018", boRR);
        
        // Yellow properties
        Property atlanticAve = new Property("PROP019", "Atlantic Avenue", PropertyType.STREET, 
                PropertyGroup.YELLOW, new BigDecimal("260"), new BigDecimal("22"), 
                new BigDecimal("150"), new BigDecimal("150"));
        spaces.put(26, new BoardSpace(26, "Atlantic Avenue", BoardSpaceType.PROPERTY, atlanticAve));
        properties.put("PROP019", atlanticAve);
        
        Property ventnorAve = new Property("PROP020", "Ventnor Avenue", PropertyType.STREET, 
                PropertyGroup.YELLOW, new BigDecimal("260"), new BigDecimal("22"), 
                new BigDecimal("150"), new BigDecimal("150"));
        spaces.put(27, new BoardSpace(27, "Ventnor Avenue", BoardSpaceType.PROPERTY, ventnorAve));
        properties.put("PROP020", ventnorAve);
        
        // Water Works
        Property waterWorks = new Property("PROP021", "Water Works", PropertyType.UTILITY, 
                PropertyGroup.UTILITY, new BigDecimal("150"), BigDecimal.ZERO, 
                BigDecimal.ZERO, BigDecimal.ZERO);
        spaces.put(28, new BoardSpace(28, "Water Works", BoardSpaceType.PROPERTY, waterWorks));
        properties.put("PROP021", waterWorks);
        
        Property marvinGardens = new Property("PROP022", "Marvin Gardens", PropertyType.STREET, 
                PropertyGroup.YELLOW, new BigDecimal("280"), new BigDecimal("24"), 
                new BigDecimal("150"), new BigDecimal("150"));
        spaces.put(29, new BoardSpace(29, "Marvin Gardens", BoardSpaceType.PROPERTY, marvinGardens));
        properties.put("PROP022", marvinGardens);
        
        // Go to Jail
        spaces.put(30, new BoardSpace(30, "Go to Jail", BoardSpaceType.GO_TO_JAIL));
        
        // Green properties
        Property pacificAve = new Property("PROP023", "Pacific Avenue", PropertyType.STREET, 
                PropertyGroup.GREEN, new BigDecimal("300"), new BigDecimal("26"), 
                new BigDecimal("200"), new BigDecimal("200"));
        spaces.put(31, new BoardSpace(31, "Pacific Avenue", BoardSpaceType.PROPERTY, pacificAve));
        properties.put("PROP023", pacificAve);
        
        Property northCarolinaAve = new Property("PROP024", "North Carolina Avenue", PropertyType.STREET, 
                PropertyGroup.GREEN, new BigDecimal("300"), new BigDecimal("26"), 
                new BigDecimal("200"), new BigDecimal("200"));
        spaces.put(32, new BoardSpace(32, "North Carolina Avenue", BoardSpaceType.PROPERTY, northCarolinaAve));
        properties.put("PROP024", northCarolinaAve);
        
        // Community Chest
        spaces.put(33, new BoardSpace(33, "Community Chest", BoardSpaceType.COMMUNITY_CHEST));
        
        Property pennsylvaniaAve = new Property("PROP025", "Pennsylvania Avenue", PropertyType.STREET, 
                PropertyGroup.GREEN, new BigDecimal("320"), new BigDecimal("28"), 
                new BigDecimal("200"), new BigDecimal("200"));
        spaces.put(34, new BoardSpace(34, "Pennsylvania Avenue", BoardSpaceType.PROPERTY, pennsylvaniaAve));
        properties.put("PROP025", pennsylvaniaAve);
        
        // Short Line Railroad
        Property shortLineRR = new Property("PROP026", "Short Line Railroad", PropertyType.RAILROAD, 
                PropertyGroup.RAILROAD, new BigDecimal("200"), new BigDecimal("25"), 
                BigDecimal.ZERO, BigDecimal.ZERO);
        spaces.put(35, new BoardSpace(35, "Short Line Railroad", BoardSpaceType.PROPERTY, shortLineRR));
        properties.put("PROP026", shortLineRR);
        
        // Chance
        spaces.put(36, new BoardSpace(36, "Chance", BoardSpaceType.CHANCE));
        
        // Dark Blue properties
        Property parkPlace = new Property("PROP027", "Park Place", PropertyType.STREET, 
                PropertyGroup.DARK_BLUE, new BigDecimal("350"), new BigDecimal("35"), 
                new BigDecimal("200"), new BigDecimal("200"));
        spaces.put(37, new BoardSpace(37, "Park Place", BoardSpaceType.PROPERTY, parkPlace));
        properties.put("PROP027", parkPlace);
        
        // Luxury Tax
        spaces.put(38, new BoardSpace(38, "Luxury Tax", BoardSpaceType.TAX, new BigDecimal("100")));
        
        Property boardwalk = new Property("PROP028", "Boardwalk", PropertyType.STREET, 
                PropertyGroup.DARK_BLUE, new BigDecimal("400"), new BigDecimal("50"), 
                new BigDecimal("200"), new BigDecimal("200"));
        spaces.put(39, new BoardSpace(39, "Boardwalk", BoardSpaceType.PROPERTY, boardwalk));
        properties.put("PROP028", boardwalk);
    }
    
    private void initializeCards() {
        // Initialize Chance cards
        chanceCards.add(new Card("CHANCE001", "Advance to GO", CardType.MOVE, "GO"));
        chanceCards.add(new Card("CHANCE002", "Advance to Illinois Ave", CardType.MOVE, "24"));
        chanceCards.add(new Card("CHANCE003", "Advance to St. Charles Place", CardType.MOVE, "11"));
        chanceCards.add(new Card("CHANCE004", "Advance to nearest Railroad", CardType.MOVE_NEAREST, "RAILROAD"));
        chanceCards.add(new Card("CHANCE005", "Advance to nearest Utility", CardType.MOVE_NEAREST, "UTILITY"));
        chanceCards.add(new Card("CHANCE006", "Bank pays you $50", CardType.MONEY, "50"));
        chanceCards.add(new Card("CHANCE007", "Get out of Jail Free", CardType.GET_OUT_OF_JAIL, ""));
        chanceCards.add(new Card("CHANCE008", "Go back 3 spaces", CardType.MOVE_RELATIVE, "-3"));
        chanceCards.add(new Card("CHANCE009", "Go to Jail", CardType.GO_TO_JAIL, ""));
        chanceCards.add(new Card("CHANCE010", "Pay poor tax of $15", CardType.MONEY, "-15"));
        chanceCards.add(new Card("CHANCE011", "Take a trip to Reading Railroad", CardType.MOVE, "5"));
        chanceCards.add(new Card("CHANCE012", "Take a walk on the Boardwalk", CardType.MOVE, "39"));
        chanceCards.add(new Card("CHANCE013", "You have been elected Chairman of the Board", CardType.COLLECT_FROM_PLAYERS, "50"));
        chanceCards.add(new Card("CHANCE014", "Your building loan matures", CardType.MONEY, "150"));
        chanceCards.add(new Card("CHANCE015", "You have won a crossword competition", CardType.MONEY, "100"));
        chanceCards.add(new Card("CHANCE016", "Speeding fine $15", CardType.MONEY, "-15"));
        
        // Initialize Community Chest cards
        communityChestCards.add(new Card("CC001", "Advance to GO", CardType.MOVE, "GO"));
        communityChestCards.add(new Card("CC002", "Bank error in your favor", CardType.MONEY, "200"));
        communityChestCards.add(new Card("CC003", "Doctor's fees", CardType.MONEY, "-50"));
        communityChestCards.add(new Card("CC004", "From sale of stock you get $50", CardType.MONEY, "50"));
        communityChestCards.add(new Card("CC005", "Get out of Jail Free", CardType.GET_OUT_OF_JAIL, ""));
        communityChestCards.add(new Card("CC006", "Go to Jail", CardType.GO_TO_JAIL, ""));
        communityChestCards.add(new Card("CC007", "Holiday fund matures", CardType.MONEY, "100"));
        communityChestCards.add(new Card("CC008", "Income tax refund", CardType.MONEY, "20"));
        communityChestCards.add(new Card("CC009", "It is your birthday", CardType.COLLECT_FROM_PLAYERS, "10"));
        communityChestCards.add(new Card("CC010", "Life insurance matures", CardType.MONEY, "100"));
        communityChestCards.add(new Card("CC011", "Hospital fees", CardType.MONEY, "-100"));
        communityChestCards.add(new Card("CC012", "School fees", CardType.MONEY, "-50"));
        communityChestCards.add(new Card("CC013", "Receive $25 consultancy fee", CardType.MONEY, "25"));
        communityChestCards.add(new Card("CC014", "You are assessed for street repairs", CardType.PROPERTY_TAX, "40:115"));
        communityChestCards.add(new Card("CC015", "You have won second prize in a beauty contest", CardType.MONEY, "10"));
        communityChestCards.add(new Card("CC016", "You inherit $100", CardType.MONEY, "100"));
        
        // Shuffle the cards
        Collections.shuffle(chanceCards);
        Collections.shuffle(communityChestCards);
    }
    
    // Getters
    public BoardSpace getSpace(int position) {
        return spaces.get(position % 40);
    }
    
    public Property getProperty(String propertyId) {
        return properties.get(propertyId);
    }
    
    public List<Property> getAllProperties() {
        return new ArrayList<>(properties.values());
    }
    
    public List<Property> getPropertiesByGroup(PropertyGroup group) {
        return properties.values().stream()
                .filter(p -> p.getGroup() == group)
                .collect(ArrayList::new, (list, p) -> list.add(p), ArrayList::addAll);
    }
    
    // Card operations
    public Card drawChanceCard() {
        if (chanceCards.isEmpty()) {
            initializeCards(); // Reshuffle if deck is empty
        }
        return chanceCards.remove(0);
    }
    
    public Card drawCommunityChestCard() {
        if (communityChestCards.isEmpty()) {
            initializeCards(); // Reshuffle if deck is empty
        }
        return communityChestCards.remove(0);
    }
    
    public void returnChanceCard(Card card) {
        chanceCards.add(card);
        Collections.shuffle(chanceCards);
    }
    
    public void returnCommunityChestCard(Card card) {
        communityChestCards.add(card);
        Collections.shuffle(communityChestCards);
    }
    
    // Utility methods
    public int findNearestRailroad(int currentPosition) {
        int[] railroads = {5, 15, 25, 35};
        for (int railroad : railroads) {
            if (railroad > currentPosition) {
                return railroad;
            }
        }
        return railroads[0]; // Wrap around to Reading Railroad
    }
    
    public int findNearestUtility(int currentPosition) {
        int[] utilities = {12, 28};
        for (int utility : utilities) {
            if (utility > currentPosition) {
                return utility;
            }
        }
        return utilities[0]; // Wrap around to Electric Company
    }
    
    @Override
    public String toString() {
        return String.format("GameBoard{spaces=%d, properties=%d, chanceCards=%d, communityChestCards=%d}",
                           spaces.size(), properties.size(), chanceCards.size(), communityChestCards.size());
    }
}