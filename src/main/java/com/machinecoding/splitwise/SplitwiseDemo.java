package com.machinecoding.splitwise;

import com.machinecoding.splitwise.model.*;
import com.machinecoding.splitwise.service.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * Comprehensive demonstration of the Splitwise Application.
 * Shows user management, group creation, expense splitting, and settlements.
 */
public class SplitwiseDemo {
    
    public static void main(String[] args) {
        System.out.println("=== Splitwise Application Demo ===\n");
        
        // Demo 1: User Registration and Management
        System.out.println("=== Demo 1: User Registration and Management ===");
        demonstrateUserManagement();
        
        // Demo 2: Group Creation and Management
        System.out.println("\n=== Demo 2: Group Creation and Management ===");
        demonstrateGroupManagement();
        
        // Demo 3: Expense Creation and Splitting
        System.out.println("\n=== Demo 3: Expense Creation and Splitting ===");
        demonstrateExpenseManagement();
        
        // Demo 4: Balance Calculations
        System.out.println("\n=== Demo 4: Balance Calculations ===");
        demonstrateBalanceCalculations();
        
        // Demo 5: Settlement Management
        System.out.println("\n=== Demo 5: Settlement Management ===");
        demonstrateSettlements();
        
        // Demo 6: Statistics and Reporting
        System.out.println("\n=== Demo 6: Statistics and Reporting ===");
        demonstrateStatistics();
        
        System.out.println("\n=== Demo Complete ===");
    }
    
    private static void demonstrateUserManagement() {
        System.out.println("1. Creating Splitwise service:");
        SplitwiseService splitwiseService = new InMemorySplitwiseService();
        
        System.out.println("\n2. Registering users:");
        
        // Register users
        User alice = splitwiseService.registerUser("Alice Johnson", "alice@example.com", "+1-555-0101");
        System.out.println("   Registered: " + alice);
        
        User bob = splitwiseService.registerUser("Bob Smith", "bob@example.com", "+1-555-0102");
        System.out.println("   Registered: " + bob);
        
        User charlie = splitwiseService.registerUser("Charlie Brown", "charlie@example.com", "+1-555-0103");
        System.out.println("   Registered: " + charlie);
        
        User diana = splitwiseService.registerUser("Diana Wilson", "diana@example.com", "+1-555-0104");
        System.out.println("   Registered: " + diana);
        
        System.out.println("\n3. User lookup and management:");
        
        // Get all users
        List<User> allUsers = splitwiseService.getAllUsers();
        System.out.println("   Total active users: " + allUsers.size());
        
        // Get specific user
        Optional<User> foundUser = splitwiseService.getUser(alice.getUserId());
        if (foundUser.isPresent()) {
            System.out.println("   Found user: " + foundUser.get().getName());
        }
        
        // Update user
        boolean updated = splitwiseService.updateUser(bob.getUserId(), "Robert Smith", 
                                                    "robert@example.com", "+1-555-0202");
        System.out.println("   Updated Bob's info: " + updated);
        
        // Test duplicate email registration
        try {
            splitwiseService.registerUser("Another Alice", "alice@example.com", "+1-555-0999");
        } catch (IllegalArgumentException e) {
            System.out.println("   Duplicate email rejected: " + e.getMessage());
        }
    }
    
    private static void demonstrateGroupManagement() {
        SplitwiseService splitwiseService = new InMemorySplitwiseService();
        setupUsers(splitwiseService);
        
        System.out.println("1. Creating groups:");
        
        // Create groups
        Group roommates = splitwiseService.createGroup("Roommates", "Apartment expenses", "USER0001");
        System.out.println("   Created group: " + roommates);
        
        Group vacation = splitwiseService.createGroup("Europe Trip", "Vacation expenses for Europe", "USER0002");
        System.out.println("   Created group: " + vacation);
        
        Group dinner = splitwiseService.createGroup("Dinner Club", "Monthly dinner expenses", "USER0003");
        System.out.println("   Created group: " + dinner);
        
        System.out.println("\n2. Adding members to groups:");
        
        // Add members to roommates group
        splitwiseService.addMemberToGroup(roommates.getGroupId(), "USER0002");
        splitwiseService.addMemberToGroup(roommates.getGroupId(), "USER0003");
        System.out.println("   Added members to Roommates group");
        
        // Add members to vacation group
        splitwiseService.addMemberToGroup(vacation.getGroupId(), "USER0001");
        splitwiseService.addMemberToGroup(vacation.getGroupId(), "USER0003");
        splitwiseService.addMemberToGroup(vacation.getGroupId(), "USER0004");
        System.out.println("   Added members to Europe Trip group");
        
        // Add members to dinner group
        splitwiseService.addMemberToGroup(dinner.getGroupId(), "USER0001");
        splitwiseService.addMemberToGroup(dinner.getGroupId(), "USER0002");
        splitwiseService.addMemberToGroup(dinner.getGroupId(), "USER0004");
        System.out.println("   Added members to Dinner Club group");
        
        System.out.println("\n3. Group information:");
        
        // Show group details
        Optional<Group> roommatesGroup = splitwiseService.getGroup(roommates.getGroupId());
        if (roommatesGroup.isPresent()) {
            Group group = roommatesGroup.get();
            System.out.println("   " + group.getName() + " has " + group.getMemberCount() + " members");
            System.out.println("   Members: " + group.getMemberIds());
        }
        
        // Show user's groups
        List<Group> aliceGroups = splitwiseService.getGroupsByUser("USER0001");
        System.out.println("   Alice is in " + aliceGroups.size() + " groups:");
        for (Group group : aliceGroups) {
            System.out.println("     - " + group.getName());
        }
    }
    
    private static void demonstrateExpenseManagement() {
        SplitwiseService splitwiseService = new InMemorySplitwiseService();
        setupUsers(splitwiseService);
        setupGroups(splitwiseService);
        
        System.out.println("1. Creating expenses with different split types:");
        
        // Equal split expense
        Map<String, BigDecimal> equalSplits = splitwiseService.splitEqually(
            new BigDecimal("120.00"), Arrays.asList("USER0001", "USER0002", "USER0003")
        );
        Expense groceries = splitwiseService.addExpense(
            "Grocery shopping", new BigDecimal("120.00"), "USER0001", "GROUP0001",
            ExpenseType.FOOD, SplitType.EQUAL, equalSplits, "Groceries", "Weekly grocery run"
        );
        System.out.println("   Equal split expense: " + groceries);
        System.out.println("   Splits: " + equalSplits);
        
        // Exact amount split
        Map<String, BigDecimal> exactSplits = new HashMap<>();
        exactSplits.put("USER0001", new BigDecimal("50.00"));
        exactSplits.put("USER0002", new BigDecimal("75.00"));
        exactSplits.put("USER0003", new BigDecimal("25.00"));
        Expense utilities = splitwiseService.addExpense(
            "Electricity bill", new BigDecimal("150.00"), "USER0002", "GROUP0001",
            ExpenseType.UTILITIES, SplitType.EXACT, exactSplits, "Utilities", "Monthly electricity"
        );
        System.out.println("   Exact split expense: " + utilities);
        System.out.println("   Splits: " + exactSplits);
        
        // Percentage split
        Map<String, BigDecimal> percentages = new HashMap<>();
        percentages.put("USER0002", new BigDecimal("40"));
        percentages.put("USER0001", new BigDecimal("35"));
        percentages.put("USER0003", new BigDecimal("15"));
        percentages.put("USER0004", new BigDecimal("10"));
        Map<String, BigDecimal> percentageSplits = splitwiseService.splitByPercentages(
            new BigDecimal("800.00"), percentages
        );
        Expense vacation = splitwiseService.addExpense(
            "Hotel booking", new BigDecimal("800.00"), "USER0002", "GROUP0002",
            ExpenseType.ACCOMMODATION, SplitType.PERCENTAGE, percentageSplits, "Travel", "Paris hotel"
        );
        System.out.println("   Percentage split expense: " + vacation);
        System.out.println("   Splits: " + percentageSplits);
        
        // Shares split
        Map<String, Integer> shares = new HashMap<>();
        shares.put("USER0001", 2);
        shares.put("USER0002", 2);
        shares.put("USER0003", 1);
        shares.put("USER0004", 1);
        Map<String, BigDecimal> sharesSplits = splitwiseService.splitByShares(
            new BigDecimal("240.00"), shares
        );
        Expense dinner = splitwiseService.addExpense(
            "Group dinner", new BigDecimal("240.00"), "USER0003", "GROUP0003",
            ExpenseType.FOOD, SplitType.SHARES, sharesSplits, "Dining", "Birthday celebration"
        );
        System.out.println("   Shares split expense: " + dinner);
        System.out.println("   Splits: " + sharesSplits);
        
        System.out.println("\n2. Expense management:");
        
        // Get expenses by group
        List<Expense> roommateExpenses = splitwiseService.getExpensesByGroup("GROUP0001");
        System.out.println("   Roommates group has " + roommateExpenses.size() + " expenses");
        
        // Get expenses by user
        List<Expense> aliceExpenses = splitwiseService.getExpensesByUser("USER0001");
        System.out.println("   Alice is involved in " + aliceExpenses.size() + " expenses");
        
        // Update expense
        boolean updated = splitwiseService.updateExpense(groceries.getExpenseId(), 
                                                       "Weekly groceries", "Food & Dining", "Costco run");
        System.out.println("   Updated expense: " + updated);
    }
    
    private static void demonstrateBalanceCalculations() {
        SplitwiseService splitwiseService = new InMemorySplitwiseService();
        setupUsers(splitwiseService);
        setupGroups(splitwiseService);
        createSampleExpenses(splitwiseService);
        
        System.out.println("1. Group balance calculations:");
        
        // Get group balances
        Map<String, BigDecimal> roommateBalances = splitwiseService.getGroupBalances("GROUP0001");
        System.out.println("   Roommates group balances:");
        for (Map.Entry<String, BigDecimal> entry : roommateBalances.entrySet()) {
            String status = entry.getValue().compareTo(BigDecimal.ZERO) > 0 ? "is owed" : 
                           entry.getValue().compareTo(BigDecimal.ZERO) < 0 ? "owes" : "is settled";
            System.out.println("     " + entry.getKey() + " " + status + " $" + entry.getValue().abs());
        }
        
        Map<String, BigDecimal> vacationBalances = splitwiseService.getGroupBalances("GROUP0002");
        System.out.println("   Vacation group balances:");
        for (Map.Entry<String, BigDecimal> entry : vacationBalances.entrySet()) {
            String status = entry.getValue().compareTo(BigDecimal.ZERO) > 0 ? "is owed" : 
                           entry.getValue().compareTo(BigDecimal.ZERO) < 0 ? "owes" : "is settled";
            System.out.println("     " + entry.getKey() + " " + status + " $" + entry.getValue().abs());
        }
        
        System.out.println("\n2. User balance summary:");
        
        // Get user balances across all groups
        Map<String, BigDecimal> aliceBalances = splitwiseService.getUserBalances("USER0001");
        System.out.println("   Alice's balances across groups:");
        for (Map.Entry<String, BigDecimal> entry : aliceBalances.entrySet()) {
            String status = entry.getValue().compareTo(BigDecimal.ZERO) > 0 ? "is owed" : 
                           entry.getValue().compareTo(BigDecimal.ZERO) < 0 ? "owes" : "is settled";
            System.out.println("     Group " + entry.getKey() + ": " + status + " $" + entry.getValue().abs());
        }
        
        System.out.println("\n3. Balance between specific users:");
        
        // Get balance between two users
        BigDecimal balanceBetween = splitwiseService.getBalanceBetweenUsers("USER0001", "USER0002");
        if (balanceBetween.compareTo(BigDecimal.ZERO) > 0) {
            System.out.println("   USER0002 owes USER0001 $" + balanceBetween);
        } else if (balanceBetween.compareTo(BigDecimal.ZERO) < 0) {
            System.out.println("   USER0001 owes USER0002 $" + balanceBetween.abs());
        } else {
            System.out.println("   USER0001 and USER0002 are settled");
        }
    }
    
    private static void demonstrateSettlements() {
        SplitwiseService splitwiseService = new InMemorySplitwiseService();
        setupUsers(splitwiseService);
        setupGroups(splitwiseService);
        createSampleExpenses(splitwiseService);
        
        System.out.println("1. Calculating optimal settlements:");
        
        // Calculate optimal settlements for roommates group
        List<Settlement> roommateSettlements = splitwiseService.calculateOptimalSettlements("GROUP0001");
        System.out.println("   Optimal settlements for Roommates group:");
        for (Settlement settlement : roommateSettlements) {
            System.out.println("     " + settlement.getFromUserId() + " should pay " + 
                             settlement.getToUserId() + " $" + settlement.getAmount());
        }
        
        // Calculate optimal settlements for vacation group
        List<Settlement> vacationSettlements = splitwiseService.calculateOptimalSettlements("GROUP0002");
        System.out.println("   Optimal settlements for Vacation group:");
        for (Settlement settlement : vacationSettlements) {
            System.out.println("     " + settlement.getFromUserId() + " should pay " + 
                             settlement.getToUserId() + " $" + settlement.getAmount());
        }
        
        System.out.println("\n2. Creating and managing settlements:");
        
        // Create manual settlement
        Settlement manualSettlement = splitwiseService.createSettlement(
            "USER0003", "USER0001", new BigDecimal("50.00"), "GROUP0001", "Partial payment for groceries"
        );
        System.out.println("   Created settlement: " + manualSettlement);
        
        // Settle payment
        boolean settled = splitwiseService.settlePayment(manualSettlement.getSettlementId());
        System.out.println("   Settlement completed: " + settled);
        
        // Create another settlement and reject it
        Settlement rejectedSettlement = splitwiseService.createSettlement(
            "USER0002", "USER0001", new BigDecimal("25.00"), "GROUP0001", "Disputed amount"
        );
        boolean rejected = splitwiseService.rejectSettlement(rejectedSettlement.getSettlementId());
        System.out.println("   Settlement rejected: " + rejected);
        
        System.out.println("\n3. Settlement history:");
        
        // Get settlements by user
        List<Settlement> aliceSettlements = splitwiseService.getSettlementsByUser("USER0001");
        System.out.println("   Alice's settlements: " + aliceSettlements.size());
        for (Settlement settlement : aliceSettlements) {
            System.out.println("     " + settlement + " - Status: " + settlement.getStatus());
        }
        
        // Get settlements by group
        List<Settlement> groupSettlements = splitwiseService.getSettlementsByGroup("GROUP0001");
        System.out.println("   Roommates group settlements: " + groupSettlements.size());
    }
    
    private static void demonstrateStatistics() {
        SplitwiseService splitwiseService = new InMemorySplitwiseService();
        setupUsers(splitwiseService);
        setupGroups(splitwiseService);
        createSampleExpenses(splitwiseService);
        createSampleSettlements(splitwiseService);
        
        System.out.println("1. Overall system statistics:");
        
        SplitwiseStats overallStats = splitwiseService.getOverallStats();
        System.out.println("   " + overallStats);
        
        System.out.println("\n2. Detailed system metrics:");
        System.out.println("   Total users: " + overallStats.getTotalUsers());
        System.out.println("   Active users: " + overallStats.getActiveUsers());
        System.out.println("   User activity rate: " + String.format("%.1f%%", overallStats.getUserActivityRate()));
        System.out.println("   Total groups: " + overallStats.getTotalGroups());
        System.out.println("   Active groups: " + overallStats.getActiveGroups());
        System.out.println("   Group activity rate: " + String.format("%.1f%%", overallStats.getGroupActivityRate()));
        System.out.println("   Total expenses: " + overallStats.getTotalExpenses());
        System.out.println("   Settlement rate: " + String.format("%.1f%%", overallStats.getSettlementRate()));
        System.out.println("   Total expense amount: $" + overallStats.getTotalExpenseAmount());
        System.out.println("   Average expense: $" + overallStats.getAverageExpenseAmount());
        System.out.println("   Pending amount: $" + overallStats.getPendingAmount());
        
        System.out.println("\n3. Group statistics:");
        
        GroupStats roommateStats = splitwiseService.getGroupStats("GROUP0001");
        if (roommateStats != null) {
            System.out.println("   " + roommateStats);
            System.out.println("   Settlement rate: " + String.format("%.1f%%", roommateStats.getSettlementRate()));
            System.out.println("   Average expense: $" + roommateStats.getAverageExpenseAmount());
            System.out.println("   Most active user: " + roommateStats.getMostActiveUser());
            System.out.println("   Largest expense: $" + roommateStats.getLargestExpense());
            System.out.println("   Is active group: " + roommateStats.isActiveGroup());
        }
        
        System.out.println("\n4. User statistics:");
        
        UserStats aliceStats = splitwiseService.getUserStats("USER0001");
        if (aliceStats != null) {
            System.out.println("   " + aliceStats);
            System.out.println("   Payment ratio: " + String.format("%.1f%%", aliceStats.getPaymentRatio()));
            System.out.println("   Average expense paid: $" + aliceStats.getAverageExpenseAmount());
            System.out.println("   Is creditor: " + aliceStats.isCreditor());
            System.out.println("   Is debtor: " + aliceStats.isDebtor());
            System.out.println("   Is settled: " + aliceStats.isSettled());
        }
    }
    
    // Helper methods
    private static void setupUsers(SplitwiseService service) {
        service.registerUser("Alice Johnson", "alice@example.com", "+1-555-0101");
        service.registerUser("Bob Smith", "bob@example.com", "+1-555-0102");
        service.registerUser("Charlie Brown", "charlie@example.com", "+1-555-0103");
        service.registerUser("Diana Wilson", "diana@example.com", "+1-555-0104");
    }
    
    private static void setupGroups(SplitwiseService service) {
        // Create groups
        Group roommates = service.createGroup("Roommates", "Apartment expenses", "USER0001");
        service.addMemberToGroup(roommates.getGroupId(), "USER0002");
        service.addMemberToGroup(roommates.getGroupId(), "USER0003");
        
        Group vacation = service.createGroup("Europe Trip", "Vacation expenses", "USER0002");
        service.addMemberToGroup(vacation.getGroupId(), "USER0001");
        service.addMemberToGroup(vacation.getGroupId(), "USER0003");
        service.addMemberToGroup(vacation.getGroupId(), "USER0004");
        
        Group dinner = service.createGroup("Dinner Club", "Monthly dinners", "USER0003");
        service.addMemberToGroup(dinner.getGroupId(), "USER0001");
        service.addMemberToGroup(dinner.getGroupId(), "USER0002");
        service.addMemberToGroup(dinner.getGroupId(), "USER0004");
    }
    
    private static void createSampleExpenses(SplitwiseService service) {
        // Roommates expenses
        Map<String, BigDecimal> grocerySplits = service.splitEqually(
            new BigDecimal("120.00"), Arrays.asList("USER0001", "USER0002", "USER0003")
        );
        service.addExpense("Grocery shopping", new BigDecimal("120.00"), "USER0001", "GROUP0001",
                         ExpenseType.FOOD, SplitType.EQUAL, grocerySplits, "Groceries", "Weekly groceries");
        
        Map<String, BigDecimal> utilitySplits = new HashMap<>();
        utilitySplits.put("USER0001", new BigDecimal("50.00"));
        utilitySplits.put("USER0002", new BigDecimal("75.00"));
        utilitySplits.put("USER0003", new BigDecimal("25.00"));
        service.addExpense("Electricity bill", new BigDecimal("150.00"), "USER0002", "GROUP0001",
                         ExpenseType.UTILITIES, SplitType.EXACT, utilitySplits, "Utilities", "Monthly bill");
        
        // Vacation expenses
        Map<String, BigDecimal> hotelSplits = service.splitEqually(
            new BigDecimal("800.00"), Arrays.asList("USER0001", "USER0002", "USER0003", "USER0004")
        );
        service.addExpense("Hotel booking", new BigDecimal("800.00"), "USER0002", "GROUP0002",
                         ExpenseType.ACCOMMODATION, SplitType.EQUAL, hotelSplits, "Travel", "Paris hotel");
        
        // Dinner expenses
        Map<String, BigDecimal> dinnerSplits = service.splitEqually(
            new BigDecimal("240.00"), Arrays.asList("USER0001", "USER0002", "USER0003", "USER0004")
        );
        service.addExpense("Group dinner", new BigDecimal("240.00"), "USER0003", "GROUP0003",
                         ExpenseType.FOOD, SplitType.EQUAL, dinnerSplits, "Dining", "Birthday dinner");
    }
    
    private static void createSampleSettlements(SplitwiseService service) {
        // Create some settlements
        Settlement settlement1 = service.createSettlement("USER0003", "USER0001", new BigDecimal("40.00"), 
                                                         "GROUP0001", "Grocery payment");
        service.settlePayment(settlement1.getSettlementId());
        
        Settlement settlement2 = service.createSettlement("USER0002", "USER0001", new BigDecimal("25.00"), 
                                                         "GROUP0001", "Utility payment");
        service.settlePayment(settlement2.getSettlementId());
    }
}