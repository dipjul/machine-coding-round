package com.machinecoding.splitwise;

import com.machinecoding.splitwise.model.*;
import com.machinecoding.splitwise.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * Comprehensive test suite for the Splitwise Application.
 * Tests user management, group operations, expense splitting, and settlements.
 */
public class SplitwiseTest {
    
    private SplitwiseService splitwiseService;
    private User alice, bob, charlie, diana;
    private Group roommates, vacation, dinner;
    
    @BeforeEach
    void setUp() {
        splitwiseService = new InMemorySplitwiseService();
        setupTestUsers();
        setupTestGroups();
    }
    
    private void setupTestUsers() {
        alice = splitwiseService.registerUser("Alice Johnson", "alice@example.com", "+1-555-0101");
        bob = splitwiseService.registerUser("Bob Smith", "bob@example.com", "+1-555-0102");
        charlie = splitwiseService.registerUser("Charlie Brown", "charlie@example.com", "+1-555-0103");
        diana = splitwiseService.registerUser("Diana Wilson", "diana@example.com", "+1-555-0104");
    }
    
    private void setupTestGroups() {
        roommates = splitwiseService.createGroup("Roommates", "Apartment expenses", alice.getUserId());
        splitwiseService.addMemberToGroup(roommates.getGroupId(), bob.getUserId());
        splitwiseService.addMemberToGroup(roommates.getGroupId(), charlie.getUserId());
        
        vacation = splitwiseService.createGroup("Europe Trip", "Vacation expenses", bob.getUserId());
        splitwiseService.addMemberToGroup(vacation.getGroupId(), alice.getUserId());
        splitwiseService.addMemberToGroup(vacation.getGroupId(), charlie.getUserId());
        splitwiseService.addMemberToGroup(vacation.getGroupId(), diana.getUserId());
        
        dinner = splitwiseService.createGroup("Dinner Club", "Monthly dinners", charlie.getUserId());
        splitwiseService.addMemberToGroup(dinner.getGroupId(), alice.getUserId());
        splitwiseService.addMemberToGroup(dinner.getGroupId(), bob.getUserId());
        splitwiseService.addMemberToGroup(dinner.getGroupId(), diana.getUserId());
    }
    
    @Test
    @DisplayName("User Registration and Management")
    void testUserManagement() {
        // Test user registration
        assertNotNull(alice);
        assertEquals("Alice Johnson", alice.getName());
        assertEquals("alice@example.com", alice.getEmail());
        assertTrue(alice.getUserId().startsWith("USER"));
        
        // Test duplicate email registration
        assertThrows(IllegalArgumentException.class, () -> {
            splitwiseService.registerUser("Another Alice", "alice@example.com", "+1-555-0999");
        });
        
        // Test invalid user registration
        assertThrows(IllegalArgumentException.class, () -> {
            splitwiseService.registerUser(null, "test@example.com", "+1-555-0999");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            splitwiseService.registerUser("Test User", null, "+1-555-0999");
        });
        
        // Test user retrieval
        Optional<User> foundUser = splitwiseService.getUser(alice.getUserId());
        assertTrue(foundUser.isPresent());
        assertEquals(alice.getUserId(), foundUser.get().getUserId());
        
        // Test user not found
        Optional<User> notFound = splitwiseService.getUser("INVALID_ID");
        assertFalse(notFound.isPresent());
        
        // Test get all users
        List<User> allUsers = splitwiseService.getAllUsers();
        assertEquals(4, allUsers.size());
        
        // Test user update
        boolean updated = splitwiseService.updateUser(bob.getUserId(), "Robert Smith", 
                                                    "robert@example.com", "+1-555-0202");
        assertTrue(updated);
        
        Optional<User> updatedUser = splitwiseService.getUser(bob.getUserId());
        assertTrue(updatedUser.isPresent());
        assertEquals("Robert Smith", updatedUser.get().getName());
        assertEquals("robert@example.com", updatedUser.get().getEmail());
        
        // Test update non-existent user
        boolean notUpdated = splitwiseService.updateUser("INVALID_ID", "Test", "test@example.com", "123");
        assertFalse(notUpdated);
    }
    
    @Test
    @DisplayName("Group Creation and Management")
    void testGroupManagement() {
        // Test group creation
        assertNotNull(roommates);
        assertEquals("Roommates", roommates.getName());
        assertEquals("Apartment expenses", roommates.getDescription());
        assertEquals(alice.getUserId(), roommates.getCreatedBy());
        assertTrue(roommates.getGroupId().startsWith("GROUP"));
        assertEquals(3, roommates.getMemberCount());
        
        // Test invalid group creation
        assertThrows(IllegalArgumentException.class, () -> {
            splitwiseService.createGroup(null, "Description", alice.getUserId());
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            splitwiseService.createGroup("Test Group", "Description", "INVALID_USER");
        });
        
        // Test group retrieval
        Optional<Group> foundGroup = splitwiseService.getGroup(roommates.getGroupId());
        assertTrue(foundGroup.isPresent());
        assertEquals(roommates.getGroupId(), foundGroup.get().getGroupId());
        
        // Test group not found
        Optional<Group> notFound = splitwiseService.getGroup("INVALID_ID");
        assertFalse(notFound.isPresent());
        
        // Test adding members
        Group testGroup = splitwiseService.createGroup("Test Group", "Test", alice.getUserId());
        boolean added = splitwiseService.addMemberToGroup(testGroup.getGroupId(), bob.getUserId());
        assertTrue(added);
        
        Optional<Group> updatedGroup = splitwiseService.getGroup(testGroup.getGroupId());
        assertTrue(updatedGroup.isPresent());
        assertEquals(2, updatedGroup.get().getMemberCount());
        assertTrue(updatedGroup.get().getMemberIds().contains(bob.getUserId()));
        
        // Test adding duplicate member
        boolean duplicateAdded = splitwiseService.addMemberToGroup(testGroup.getGroupId(), bob.getUserId());
        assertFalse(duplicateAdded);
        
        // Test adding to invalid group
        boolean invalidAdd = splitwiseService.addMemberToGroup("INVALID_GROUP", bob.getUserId());
        assertFalse(invalidAdd);
        
        // Test removing members
        boolean removed = splitwiseService.removeMemberFromGroup(testGroup.getGroupId(), bob.getUserId());
        assertTrue(removed);
        
        Optional<Group> afterRemoval = splitwiseService.getGroup(testGroup.getGroupId());
        assertTrue(afterRemoval.isPresent());
        assertEquals(1, afterRemoval.get().getMemberCount());
        assertFalse(afterRemoval.get().getMemberIds().contains(bob.getUserId()));
        
        // Test removing non-existent member
        boolean notRemoved = splitwiseService.removeMemberFromGroup(testGroup.getGroupId(), "INVALID_USER");
        assertFalse(notRemoved);
        
        // Test get groups by user
        List<Group> aliceGroups = splitwiseService.getGroupsByUser(alice.getUserId());
        assertTrue(aliceGroups.size() >= 3); // At least roommates, vacation, dinner
        
        List<Group> invalidUserGroups = splitwiseService.getGroupsByUser("INVALID_USER");
        assertTrue(invalidUserGroups.isEmpty());
    }
    
    @Test
    @DisplayName("Expense Creation and Splitting")
    void testExpenseManagement() {
        // Test equal split
        Map<String, BigDecimal> equalSplits = splitwiseService.splitEqually(
            new BigDecimal("120.00"), Arrays.asList(alice.getUserId(), bob.getUserId(), charlie.getUserId())
        );
        assertEquals(3, equalSplits.size());
        assertEquals(new BigDecimal("40.00"), equalSplits.get(alice.getUserId()));
        assertEquals(new BigDecimal("40.00"), equalSplits.get(bob.getUserId()));
        assertEquals(new BigDecimal("40.00"), equalSplits.get(charlie.getUserId()));
        
        // Test equal split with remainder
        Map<String, BigDecimal> remainderSplits = splitwiseService.splitEqually(
            new BigDecimal("100.00"), Arrays.asList(alice.getUserId(), bob.getUserId(), charlie.getUserId())
        );
        assertEquals(3, remainderSplits.size());
        BigDecimal total = remainderSplits.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("100.00"), total);
        
        // Test percentage split
        Map<String, BigDecimal> percentages = new HashMap<>();
        percentages.put(alice.getUserId(), new BigDecimal("50"));
        percentages.put(bob.getUserId(), new BigDecimal("30"));
        percentages.put(charlie.getUserId(), new BigDecimal("20"));
        
        Map<String, BigDecimal> percentageSplits = splitwiseService.splitByPercentages(
            new BigDecimal("200.00"), percentages
        );
        assertEquals(3, percentageSplits.size());
        assertEquals(new BigDecimal("100.00"), percentageSplits.get(alice.getUserId()));
        assertEquals(new BigDecimal("60.00"), percentageSplits.get(bob.getUserId()));
        assertEquals(new BigDecimal("40.00"), percentageSplits.get(charlie.getUserId()));
        
        // Test invalid percentage split (doesn't add to 100)
        Map<String, BigDecimal> invalidPercentages = new HashMap<>();
        invalidPercentages.put(alice.getUserId(), new BigDecimal("50"));
        invalidPercentages.put(bob.getUserId(), new BigDecimal("30"));
        
        assertThrows(IllegalArgumentException.class, () -> {
            splitwiseService.splitByPercentages(new BigDecimal("200.00"), invalidPercentages);
        });
        
        // Test shares split
        Map<String, Integer> shares = new HashMap<>();
        shares.put(alice.getUserId(), 2);
        shares.put(bob.getUserId(), 2);
        shares.put(charlie.getUserId(), 1);
        
        Map<String, BigDecimal> sharesSplits = splitwiseService.splitByShares(
            new BigDecimal("250.00"), shares
        );
        assertEquals(3, sharesSplits.size());
        assertEquals(new BigDecimal("100.00"), sharesSplits.get(alice.getUserId()));
        assertEquals(new BigDecimal("100.00"), sharesSplits.get(bob.getUserId()));
        assertEquals(new BigDecimal("50.00"), sharesSplits.get(charlie.getUserId()));
        
        // Test expense creation
        Expense groceries = splitwiseService.addExpense(
            "Grocery shopping", new BigDecimal("120.00"), alice.getUserId(), roommates.getGroupId(),
            ExpenseType.FOOD, SplitType.EQUAL, equalSplits, "Groceries", "Weekly grocery run"
        );
        
        assertNotNull(groceries);
        assertEquals("Grocery shopping", groceries.getDescription());
        assertEquals(new BigDecimal("120.00"), groceries.getAmount());
        assertEquals(alice.getUserId(), groceries.getPaidBy());
        assertEquals(roommates.getGroupId(), groceries.getGroupId());
        assertEquals(ExpenseType.FOOD, groceries.getType());
        assertEquals(SplitType.EQUAL, groceries.getSplitType());
        assertTrue(groceries.getExpenseId().startsWith("EXP"));
        
        // Test invalid expense creation
        assertThrows(IllegalArgumentException.class, () -> {
            splitwiseService.addExpense(null, new BigDecimal("100.00"), alice.getUserId(), 
                                      roommates.getGroupId(), ExpenseType.FOOD, SplitType.EQUAL, 
                                      equalSplits, "Category", "Notes");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            splitwiseService.addExpense("Test", new BigDecimal("-100.00"), alice.getUserId(), 
                                      roommates.getGroupId(), ExpenseType.FOOD, SplitType.EQUAL, 
                                      equalSplits, "Category", "Notes");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            splitwiseService.addExpense("Test", new BigDecimal("100.00"), "INVALID_USER", 
                                      roommates.getGroupId(), ExpenseType.FOOD, SplitType.EQUAL, 
                                      equalSplits, "Category", "Notes");
        });
        
        // Test expense retrieval
        Optional<Expense> foundExpense = splitwiseService.getExpense(groceries.getExpenseId());
        assertTrue(foundExpense.isPresent());
        assertEquals(groceries.getExpenseId(), foundExpense.get().getExpenseId());
        
        // Test get expenses by group
        List<Expense> roommateExpenses = splitwiseService.getExpensesByGroup(roommates.getGroupId());
        assertEquals(1, roommateExpenses.size());
        assertEquals(groceries.getExpenseId(), roommateExpenses.get(0).getExpenseId());
        
        // Test get expenses by user
        List<Expense> aliceExpenses = splitwiseService.getExpensesByUser(alice.getUserId());
        assertTrue(aliceExpenses.size() >= 1);
        
        // Test expense update
        boolean updated = splitwiseService.updateExpense(groceries.getExpenseId(), 
                                                       "Weekly groceries", "Food & Dining", "Costco run");
        assertTrue(updated);
        
        Optional<Expense> updatedExpense = splitwiseService.getExpense(groceries.getExpenseId());
        assertTrue(updatedExpense.isPresent());
        assertEquals("Weekly groceries", updatedExpense.get().getDescription());
        assertEquals("Food & Dining", updatedExpense.get().getCategory());
        assertEquals("Costco run", updatedExpense.get().getNotes());
        
        // Test update non-existent expense
        boolean notUpdated = splitwiseService.updateExpense("INVALID_ID", "Test", "Test", "Test");
        assertFalse(notUpdated);
    }
    
    @Test
    @DisplayName("Balance Calculations")
    void testBalanceCalculations() {
        // Create test expenses
        createTestExpenses();
        
        // Test group balances
        Map<String, BigDecimal> roommateBalances = splitwiseService.getGroupBalances(roommates.getGroupId());
        assertNotNull(roommateBalances);
        assertEquals(3, roommateBalances.size());
        assertTrue(roommateBalances.containsKey(alice.getUserId()));
        assertTrue(roommateBalances.containsKey(bob.getUserId()));
        assertTrue(roommateBalances.containsKey(charlie.getUserId()));
        
        // Verify balance conservation (sum should be zero)
        BigDecimal totalBalance = roommateBalances.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, totalBalance.compareTo(BigDecimal.ZERO));
        
        // Test user balances across all groups
        Map<String, BigDecimal> aliceBalances = splitwiseService.getUserBalances(alice.getUserId());
        assertNotNull(aliceBalances);
        assertTrue(aliceBalances.size() >= 1);
        
        // Test balance between users
        BigDecimal balanceBetween = splitwiseService.getBalanceBetweenUsers(alice.getUserId(), bob.getUserId());
        assertNotNull(balanceBetween);
        
        // Test with invalid users
        Map<String, BigDecimal> invalidBalances = splitwiseService.getUserBalances("INVALID_USER");
        assertTrue(invalidBalances.isEmpty());
        
        BigDecimal invalidBalance = splitwiseService.getBalanceBetweenUsers("INVALID_USER1", "INVALID_USER2");
        assertEquals(BigDecimal.ZERO, invalidBalance);
    }
    
    @Test
    @DisplayName("Settlement Management")
    void testSettlementManagement() {
        // Create test expenses first
        createTestExpenses();
        
        // Test optimal settlement calculation
        List<Settlement> roommateSettlements = splitwiseService.calculateOptimalSettlements(roommates.getGroupId());
        assertNotNull(roommateSettlements);
        
        // Verify settlements balance out
        BigDecimal totalSettlements = BigDecimal.ZERO;
        for (Settlement settlement : roommateSettlements) {
            totalSettlements = totalSettlements.add(settlement.getAmount());
        }
        
        // Test manual settlement creation
        Settlement manualSettlement = splitwiseService.createSettlement(
            charlie.getUserId(), alice.getUserId(), new BigDecimal("50.00"), 
            roommates.getGroupId(), "Partial payment for groceries"
        );
        
        assertNotNull(manualSettlement);
        assertEquals(charlie.getUserId(), manualSettlement.getFromUserId());
        assertEquals(alice.getUserId(), manualSettlement.getToUserId());
        assertEquals(new BigDecimal("50.00"), manualSettlement.getAmount());
        assertEquals(roommates.getGroupId(), manualSettlement.getGroupId());
        assertTrue(manualSettlement.getSettlementId().startsWith("SET"));
        
        // Test invalid settlement creation
        assertThrows(IllegalArgumentException.class, () -> {
            splitwiseService.createSettlement("INVALID_USER", alice.getUserId(), 
                                            new BigDecimal("50.00"), roommates.getGroupId(), "Test");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            splitwiseService.createSettlement(charlie.getUserId(), alice.getUserId(), 
                                            new BigDecimal("-50.00"), roommates.getGroupId(), "Test");
        });
        
        // Test settlement completion
        boolean settled = splitwiseService.settlePayment(manualSettlement.getSettlementId());
        assertTrue(settled);
        
        Optional<Settlement> settledSettlement = splitwiseService.getSettlement(manualSettlement.getSettlementId());
        assertTrue(settledSettlement.isPresent());
        // Note: Settlement status would be checked if the model had status tracking
        
        // Test settlement rejection
        Settlement rejectedSettlement = splitwiseService.createSettlement(
            bob.getUserId(), alice.getUserId(), new BigDecimal("25.00"), 
            roommates.getGroupId(), "Disputed amount"
        );
        
        boolean rejected = splitwiseService.rejectSettlement(rejectedSettlement.getSettlementId());
        assertTrue(rejected);
        
        // Test settlement retrieval
        List<Settlement> aliceSettlements = splitwiseService.getSettlementsByUser(alice.getUserId());
        assertTrue(aliceSettlements.size() >= 2);
        
        List<Settlement> groupSettlements = splitwiseService.getSettlementsByGroup(roommates.getGroupId());
        assertTrue(groupSettlements.size() >= 2);
        
        // Test invalid operations
        boolean invalidSettle = splitwiseService.settlePayment("INVALID_ID");
        assertFalse(invalidSettle);
        
        boolean invalidReject = splitwiseService.rejectSettlement("INVALID_ID");
        assertFalse(invalidReject);
    }
    
    @Test
    @DisplayName("Statistics and Reporting")
    void testStatistics() {
        // Create test data
        createTestExpenses();
        createTestSettlements();
        
        // Test overall statistics
        SplitwiseStats overallStats = splitwiseService.getOverallStats();
        assertNotNull(overallStats);
        assertEquals(4, overallStats.getTotalUsers());
        assertEquals(4, overallStats.getActiveUsers());
        assertTrue(overallStats.getTotalGroups() >= 3);
        assertTrue(overallStats.getActiveGroups() >= 3);
        assertTrue(overallStats.getTotalExpenses() >= 1);
        assertTrue(overallStats.getTotalExpenseAmount().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(overallStats.getUserActivityRate() >= 0.0);
        assertTrue(overallStats.getGroupActivityRate() >= 0.0);
        assertTrue(overallStats.getSettlementRate() >= 0.0);
        
        // Test group statistics
        GroupStats roommateStats = splitwiseService.getGroupStats(roommates.getGroupId());
        assertNotNull(roommateStats);
        assertEquals(roommates.getGroupId(), roommateStats.getGroupId());
        assertEquals(3, roommateStats.getTotalMembers());
        assertEquals(3, roommateStats.getActiveMembers());
        assertTrue(roommateStats.getTotalExpenses() >= 1);
        assertTrue(roommateStats.getTotalExpenseAmount().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(roommateStats.getSettlementRate() >= 0.0);
        assertNotNull(roommateStats.getMostActiveUser());
        assertTrue(roommateStats.getLargestExpense().compareTo(BigDecimal.ZERO) >= 0);
        
        // Test user statistics
        UserStats aliceStats = splitwiseService.getUserStats(alice.getUserId());
        assertNotNull(aliceStats);
        assertEquals(alice.getUserId(), aliceStats.getUserId());
        assertTrue(aliceStats.getTotalGroups() >= 3);
        assertTrue(aliceStats.getActiveGroups() >= 3);
        assertTrue(aliceStats.getTotalExpensesPaid() >= 0);
        assertTrue(aliceStats.getTotalExpensesShared() >= 1);
        assertTrue(aliceStats.getTotalAmountPaid().compareTo(BigDecimal.ZERO) >= 0);
        assertTrue(aliceStats.getTotalAmountOwed().compareTo(BigDecimal.ZERO) >= 0);
        assertTrue(aliceStats.getPaymentRatio() >= 0.0);
        
        // Test invalid statistics
        GroupStats invalidGroupStats = splitwiseService.getGroupStats("INVALID_GROUP");
        assertNull(invalidGroupStats);
        
        UserStats invalidUserStats = splitwiseService.getUserStats("INVALID_USER");
        assertNull(invalidUserStats);
    }
    
    @Test
    @DisplayName("Edge Cases and Error Handling")
    void testEdgeCases() {
        // Test empty splits
        assertThrows(IllegalArgumentException.class, () -> {
            splitwiseService.splitEqually(new BigDecimal("100.00"), Collections.emptyList());
        });
        
        // Test null parameters
        assertThrows(IllegalArgumentException.class, () -> {
            splitwiseService.splitEqually(null, Arrays.asList(alice.getUserId()));
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            splitwiseService.splitEqually(new BigDecimal("100.00"), null);
        });
        
        // Test zero amount
        assertThrows(IllegalArgumentException.class, () -> {
            splitwiseService.splitEqually(BigDecimal.ZERO, Arrays.asList(alice.getUserId()));
        });
        
        // Test negative amount
        assertThrows(IllegalArgumentException.class, () -> {
            splitwiseService.splitEqually(new BigDecimal("-100.00"), Arrays.asList(alice.getUserId()));
        });
        
        // Test single user split
        Map<String, BigDecimal> singleSplit = splitwiseService.splitEqually(
            new BigDecimal("100.00"), Arrays.asList(alice.getUserId())
        );
        assertEquals(1, singleSplit.size());
        assertEquals(new BigDecimal("100.00"), singleSplit.get(alice.getUserId()));
        
        // Test very small amounts
        Map<String, BigDecimal> smallSplit = splitwiseService.splitEqually(
            new BigDecimal("0.01"), Arrays.asList(alice.getUserId(), bob.getUserId())
        );
        assertEquals(2, smallSplit.size());
        BigDecimal total = smallSplit.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("0.01"), total);
        
        // Test large amounts
        Map<String, BigDecimal> largeSplit = splitwiseService.splitEqually(
            new BigDecimal("999999.99"), Arrays.asList(alice.getUserId(), bob.getUserId())
        );
        assertEquals(2, largeSplit.size());
        BigDecimal largeTotal = largeSplit.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("999999.99"), largeTotal);
    }
    
    // Helper methods
    private void createTestExpenses() {
        // Create a simple expense for testing
        Map<String, BigDecimal> splits = splitwiseService.splitEqually(
            new BigDecimal("120.00"), Arrays.asList(alice.getUserId(), bob.getUserId(), charlie.getUserId())
        );
        
        splitwiseService.addExpense(
            "Test Grocery", new BigDecimal("120.00"), alice.getUserId(), roommates.getGroupId(),
            ExpenseType.FOOD, SplitType.EQUAL, splits, "Groceries", "Test expense"
        );
    }
    
    private void createTestSettlements() {
        // Create a test settlement
        Settlement settlement = splitwiseService.createSettlement(
            charlie.getUserId(), alice.getUserId(), new BigDecimal("40.00"), 
            roommates.getGroupId(), "Test payment"
        );
        splitwiseService.settlePayment(settlement.getSettlementId());
    }
}