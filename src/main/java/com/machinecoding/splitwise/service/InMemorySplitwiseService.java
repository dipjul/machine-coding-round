package com.machinecoding.splitwise.service;

import com.machinecoding.splitwise.model.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * In-memory implementation of the Splitwise service.
 * Thread-safe implementation using concurrent collections.
 */
public class InMemorySplitwiseService implements SplitwiseService {
    
    private final Map<String, User> users;
    private final Map<String, Group> groups;
    private final Map<String, Expense> expenses;
    private final Map<String, Settlement> settlements;
    private final AtomicInteger userIdCounter;
    private final AtomicInteger groupIdCounter;
    private final AtomicInteger expenseIdCounter;
    private final AtomicInteger settlementIdCounter;
    
    public InMemorySplitwiseService() {
        this.users = new ConcurrentHashMap<>();
        this.groups = new ConcurrentHashMap<>();
        this.expenses = new ConcurrentHashMap<>();
        this.settlements = new ConcurrentHashMap<>();
        this.userIdCounter = new AtomicInteger(1);
        this.groupIdCounter = new AtomicInteger(1);
        this.expenseIdCounter = new AtomicInteger(1);
        this.settlementIdCounter = new AtomicInteger(1);
    }
    
    // User Management
    @Override
    public User registerUser(String name, String email, String phoneNumber) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        
        // Check if email already exists
        boolean emailExists = users.values().stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email.trim()));
        if (emailExists) {
            throw new IllegalArgumentException("Email already registered");
        }
        
        String userId = "USER" + String.format("%04d", userIdCounter.getAndIncrement());
        User user = new User(userId, name, email, phoneNumber);
        users.put(userId, user);
        return user;
    }
    
    @Override
    public Optional<User> getUser(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(users.get(userId.trim()));
    }
    
    @Override
    public List<User> getAllUsers() {
        return users.values().stream()
                .filter(User::isActive)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean updateUser(String userId, String name, String email, String phoneNumber) {
        User user = users.get(userId);
        if (user == null) {
            return false;
        }
        
        // Create updated user (in a real implementation, we'd handle immutability properly)
        User updatedUser = new User(userId, name, email, phoneNumber);
        users.put(userId, updatedUser);
        return true;
    }
    
    @Override
    public boolean deactivateUser(String userId) {
        User user = users.get(userId);
        if (user != null) {
            user.setActive(false);
            return true;
        }
        return false;
    }
    
    // Group Management
    @Override
    public Group createGroup(String name, String description, String createdBy) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Group name is required");
        }
        if (createdBy == null || !users.containsKey(createdBy.trim())) {
            throw new IllegalArgumentException("Valid creator user ID is required");
        }
        
        String groupId = "GROUP" + String.format("%04d", groupIdCounter.getAndIncrement());
        Group group = new Group(groupId, name, description, createdBy);
        groups.put(groupId, group);
        return group;
    }
    
    @Override
    public Optional<Group> getGroup(String groupId) {
        if (groupId == null || groupId.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(groups.get(groupId.trim()));
    }
    
    @Override
    public List<Group> getGroupsByUser(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        return groups.values().stream()
                .filter(Group::isActive)
                .filter(group -> group.isMember(userId.trim()))
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean addMemberToGroup(String groupId, String userId) {
        Group group = groups.get(groupId);
        User user = users.get(userId);
        
        if (group != null && user != null && group.isActive() && user.isActive()) {
            return group.addMember(userId);
        }
        return false;
    }
    
    @Override
    public boolean removeMemberFromGroup(String groupId, String userId) {
        Group group = groups.get(groupId);
        if (group != null) {
            return group.removeMember(userId);
        }
        return false;
    }
    
    @Override
    public boolean updateGroup(String groupId, String name, String description) {
        Group group = groups.get(groupId);
        if (group != null) {
            // Create updated group (in a real implementation, we'd handle immutability properly)
            Group updatedGroup = new Group(groupId, name, description, group.getCreatedBy());
            // Copy members
            for (String memberId : group.getMemberIds()) {
                if (!memberId.equals(group.getCreatedBy())) {
                    updatedGroup.addMember(memberId);
                }
            }
            groups.put(groupId, updatedGroup);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean deleteGroup(String groupId) {
        Group group = groups.get(groupId);
        if (group != null) {
            group.setActive(false);
            return true;
        }
        return false;
    }
    
    // Expense Management
    @Override
    public Expense addExpense(String description, BigDecimal totalAmount, String paidBy, String groupId,
                            ExpenseType type, SplitType splitType, Map<String, BigDecimal> splits,
                            String category, String notes) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Expense description is required");
        }
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valid total amount is required");
        }
        if (paidBy == null || !users.containsKey(paidBy.trim())) {
            throw new IllegalArgumentException("Valid payer user ID is required");
        }
        if (groupId == null || !groups.containsKey(groupId.trim())) {
            throw new IllegalArgumentException("Valid group ID is required");
        }
        
        Group group = groups.get(groupId.trim());
        if (!group.isMember(paidBy.trim())) {
            throw new IllegalArgumentException("Payer must be a member of the group");
        }
        
        String expenseId = "EXP" + String.format("%06d", expenseIdCounter.getAndIncrement());
        Expense expense = new Expense(expenseId, description, totalAmount, paidBy, groupId,
                                    type, splitType, category, notes);
        
        // Add splits
        if (splits != null && !splits.isEmpty()) {
            for (Map.Entry<String, BigDecimal> split : splits.entrySet()) {
                if (group.isMember(split.getKey())) {
                    expense.addSplit(split.getKey(), split.getValue());
                }
            }
        }
        
        // Validate splits
        if (!expense.isValidSplit()) {
            throw new IllegalArgumentException("Split amounts must equal total expense amount");
        }
        
        expenses.put(expenseId, expense);
        return expense;
    }
    
    @Override
    public Optional<Expense> getExpense(String expenseId) {
        if (expenseId == null || expenseId.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(expenses.get(expenseId.trim()));
    }
    
    @Override
    public List<Expense> getExpensesByGroup(String groupId) {
        if (groupId == null || groupId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        return expenses.values().stream()
                .filter(expense -> expense.getGroupId().equals(groupId.trim()))
                .sorted(Comparator.comparing(Expense::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Expense> getExpensesByUser(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        return expenses.values().stream()
                .filter(expense -> expense.getInvolvedUsers().contains(userId.trim()))
                .sorted(Comparator.comparing(Expense::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean updateExpense(String expenseId, String description, String category, String notes) {
        Expense expense = expenses.get(expenseId);
        if (expense != null) {
            // Create updated expense (in a real implementation, we'd handle immutability properly)
            Expense updatedExpense = new Expense(expenseId, description, expense.getTotalAmount(),
                                               expense.getPaidBy(), expense.getGroupId(), expense.getType(),
                                               expense.getSplitType(), category, notes);
            // Copy splits
            for (Map.Entry<String, BigDecimal> split : expense.getSplits().entrySet()) {
                updatedExpense.addSplit(split.getKey(), split.getValue());
            }
            expenses.put(expenseId, updatedExpense);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean deleteExpense(String expenseId) {
        return expenses.remove(expenseId) != null;
    }    

    // Balance Calculations
    @Override
    public Map<String, BigDecimal> getGroupBalances(String groupId) {
        if (groupId == null || groupId.trim().isEmpty()) {
            return new HashMap<>();
        }
        
        Group group = groups.get(groupId.trim());
        if (group == null) {
            return new HashMap<>();
        }
        
        Map<String, BigDecimal> balances = new HashMap<>();
        
        // Initialize balances for all group members
        for (String memberId : group.getMemberIds()) {
            balances.put(memberId, BigDecimal.ZERO);
        }
        
        // Calculate balances from expenses
        List<Expense> groupExpenses = getExpensesByGroup(groupId);
        for (Expense expense : groupExpenses) {
            String paidBy = expense.getPaidBy();
            BigDecimal totalAmount = expense.getTotalAmount();
            
            // Add the total amount to the payer's balance
            balances.put(paidBy, balances.getOrDefault(paidBy, BigDecimal.ZERO).add(totalAmount));
            
            // Subtract each person's share from their balance
            for (Map.Entry<String, BigDecimal> split : expense.getSplits().entrySet()) {
                String userId = split.getKey();
                BigDecimal amount = split.getValue();
                balances.put(userId, balances.getOrDefault(userId, BigDecimal.ZERO).subtract(amount));
            }
        }
        
        // Adjust for settlements
        List<Settlement> groupSettlements = getSettlementsByGroup(groupId);
        for (Settlement settlement : groupSettlements) {
            if (settlement.isSettled()) {
                String fromUser = settlement.getFromUserId();
                String toUser = settlement.getToUserId();
                BigDecimal amount = settlement.getAmount();
                
                balances.put(fromUser, balances.getOrDefault(fromUser, BigDecimal.ZERO).add(amount));
                balances.put(toUser, balances.getOrDefault(toUser, BigDecimal.ZERO).subtract(amount));
            }
        }
        
        return balances;
    }
    
    @Override
    public Map<String, BigDecimal> getUserBalances(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return new HashMap<>();
        }
        
        Map<String, BigDecimal> userBalances = new HashMap<>();
        List<Group> userGroups = getGroupsByUser(userId);
        
        for (Group group : userGroups) {
            Map<String, BigDecimal> groupBalances = getGroupBalances(group.getGroupId());
            BigDecimal userBalance = groupBalances.getOrDefault(userId, BigDecimal.ZERO);
            if (userBalance.compareTo(BigDecimal.ZERO) != 0) {
                userBalances.put(group.getGroupId(), userBalance);
            }
        }
        
        return userBalances;
    }
    
    @Override
    public BigDecimal getBalanceBetweenUsers(String userId1, String userId2) {
        if (userId1 == null || userId2 == null || userId1.equals(userId2)) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal balance = BigDecimal.ZERO;
        
        // Find common groups
        List<Group> user1Groups = getGroupsByUser(userId1);
        List<Group> user2Groups = getGroupsByUser(userId2);
        
        Set<String> commonGroupIds = user1Groups.stream()
                .map(Group::getGroupId)
                .collect(Collectors.toSet());
        commonGroupIds.retainAll(user2Groups.stream()
                .map(Group::getGroupId)
                .collect(Collectors.toSet()));
        
        // Calculate balance across common groups
        for (String groupId : commonGroupIds) {
            Map<String, BigDecimal> groupBalances = getGroupBalances(groupId);
            BigDecimal user1Balance = groupBalances.getOrDefault(userId1, BigDecimal.ZERO);
            BigDecimal user2Balance = groupBalances.getOrDefault(userId2, BigDecimal.ZERO);
            
            // If user1 has positive balance and user2 has negative balance (or vice versa)
            if (user1Balance.compareTo(BigDecimal.ZERO) > 0 && user2Balance.compareTo(BigDecimal.ZERO) < 0) {
                balance = balance.add(user1Balance.min(user2Balance.abs()));
            } else if (user1Balance.compareTo(BigDecimal.ZERO) < 0 && user2Balance.compareTo(BigDecimal.ZERO) > 0) {
                balance = balance.subtract(user1Balance.abs().min(user2Balance));
            }
        }
        
        return balance;
    }
    
    @Override
    public List<Settlement> calculateOptimalSettlements(String groupId) {
        if (groupId == null || groupId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        Map<String, BigDecimal> balances = getGroupBalances(groupId);
        List<Settlement> settlements = new ArrayList<>();
        
        // Separate creditors and debtors
        List<Map.Entry<String, BigDecimal>> creditors = new ArrayList<>();
        List<Map.Entry<String, BigDecimal>> debtors = new ArrayList<>();
        
        for (Map.Entry<String, BigDecimal> entry : balances.entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                creditors.add(entry);
            } else if (entry.getValue().compareTo(BigDecimal.ZERO) < 0) {
                debtors.add(entry);
            }
        }
        
        // Sort creditors by amount (descending) and debtors by amount (ascending)
        creditors.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        debtors.sort((a, b) -> a.getValue().compareTo(b.getValue()));
        
        // Create settlements using greedy algorithm
        int creditorIndex = 0;
        int debtorIndex = 0;
        
        while (creditorIndex < creditors.size() && debtorIndex < debtors.size()) {
            String creditor = creditors.get(creditorIndex).getKey();
            BigDecimal creditAmount = creditors.get(creditorIndex).getValue();
            
            String debtor = debtors.get(debtorIndex).getKey();
            BigDecimal debtAmount = debtors.get(debtorIndex).getValue().abs();
            
            BigDecimal settlementAmount = creditAmount.min(debtAmount);
            
            if (settlementAmount.compareTo(BigDecimal.ZERO) > 0) {
                String settlementId = "SETTLE" + String.format("%06d", settlementIdCounter.getAndIncrement());
                Settlement settlement = new Settlement(settlementId, debtor, creditor, settlementAmount,
                                                    groupId, "Optimal settlement");
                settlements.add(settlement);
                
                // Update remaining amounts
                creditAmount = creditAmount.subtract(settlementAmount);
                debtAmount = debtAmount.subtract(settlementAmount);
                
                creditors.get(creditorIndex).setValue(creditAmount);
                debtors.get(debtorIndex).setValue(debtAmount.negate());
            }
            
            // Move to next creditor or debtor
            if (creditAmount.compareTo(BigDecimal.ZERO) == 0) {
                creditorIndex++;
            }
            if (debtAmount.compareTo(BigDecimal.ZERO) == 0) {
                debtorIndex++;
            }
        }
        
        return settlements;
    }
    
    // Settlement Management
    @Override
    public Settlement createSettlement(String fromUserId, String toUserId, BigDecimal amount,
                                     String groupId, String description) {
        if (fromUserId == null || toUserId == null || fromUserId.equals(toUserId)) {
            throw new IllegalArgumentException("Valid from and to user IDs are required");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valid settlement amount is required");
        }
        if (!users.containsKey(fromUserId) || !users.containsKey(toUserId)) {
            throw new IllegalArgumentException("Both users must exist");
        }
        
        String settlementId = "SETTLE" + String.format("%06d", settlementIdCounter.getAndIncrement());
        Settlement settlement = new Settlement(settlementId, fromUserId, toUserId, amount, groupId, description);
        settlements.put(settlementId, settlement);
        return settlement;
    }
    
    @Override
    public Optional<Settlement> getSettlement(String settlementId) {
        if (settlementId == null || settlementId.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(settlements.get(settlementId.trim()));
    }
    
    @Override
    public List<Settlement> getSettlementsByUser(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        return settlements.values().stream()
                .filter(settlement -> settlement.getFromUserId().equals(userId.trim()) ||
                                    settlement.getToUserId().equals(userId.trim()))
                .sorted(Comparator.comparing(Settlement::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Settlement> getSettlementsByGroup(String groupId) {
        if (groupId == null || groupId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        return settlements.values().stream()
                .filter(settlement -> groupId.trim().equals(settlement.getGroupId()))
                .sorted(Comparator.comparing(Settlement::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean settlePayment(String settlementId) {
        Settlement settlement = settlements.get(settlementId);
        if (settlement != null && settlement.isPending()) {
            settlement.markAsSettled();
            return true;
        }
        return false;
    }
    
    @Override
    public boolean rejectSettlement(String settlementId) {
        Settlement settlement = settlements.get(settlementId);
        if (settlement != null && settlement.isPending()) {
            settlement.markAsRejected();
            return true;
        }
        return false;
    } 
   
    // Expense Splitting Utilities
    @Override
    public Map<String, BigDecimal> splitEqually(BigDecimal totalAmount, List<String> userIds) {
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0 || 
            userIds == null || userIds.isEmpty()) {
            return new HashMap<>();
        }
        
        Map<String, BigDecimal> splits = new HashMap<>();
        BigDecimal splitAmount = totalAmount.divide(BigDecimal.valueOf(userIds.size()), 2, RoundingMode.HALF_UP);
        
        for (String userId : userIds) {
            if (userId != null && !userId.trim().isEmpty()) {
                splits.put(userId.trim(), splitAmount);
            }
        }
        
        // Handle rounding differences
        BigDecimal totalSplit = splits.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal difference = totalAmount.subtract(totalSplit);
        
        if (difference.compareTo(BigDecimal.ZERO) != 0 && !splits.isEmpty()) {
            String firstUser = splits.keySet().iterator().next();
            splits.put(firstUser, splits.get(firstUser).add(difference));
        }
        
        return splits;
    }
    
    @Override
    public Map<String, BigDecimal> splitByExactAmounts(Map<String, BigDecimal> exactAmounts) {
        if (exactAmounts == null || exactAmounts.isEmpty()) {
            return new HashMap<>();
        }
        
        Map<String, BigDecimal> splits = new HashMap<>();
        for (Map.Entry<String, BigDecimal> entry : exactAmounts.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null && 
                entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                splits.put(entry.getKey().trim(), entry.getValue());
            }
        }
        
        return splits;
    }
    
    @Override
    public Map<String, BigDecimal> splitByPercentages(BigDecimal totalAmount, Map<String, BigDecimal> percentages) {
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0 || 
            percentages == null || percentages.isEmpty()) {
            return new HashMap<>();
        }
        
        Map<String, BigDecimal> splits = new HashMap<>();
        
        for (Map.Entry<String, BigDecimal> entry : percentages.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null && 
                entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal percentage = entry.getValue().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                BigDecimal splitAmount = totalAmount.multiply(percentage).setScale(2, RoundingMode.HALF_UP);
                splits.put(entry.getKey().trim(), splitAmount);
            }
        }
        
        // Handle rounding differences
        BigDecimal totalSplit = splits.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal difference = totalAmount.subtract(totalSplit);
        
        if (difference.compareTo(BigDecimal.ZERO) != 0 && !splits.isEmpty()) {
            String firstUser = splits.keySet().iterator().next();
            splits.put(firstUser, splits.get(firstUser).add(difference));
        }
        
        return splits;
    }
    
    @Override
    public Map<String, BigDecimal> splitByShares(BigDecimal totalAmount, Map<String, Integer> shares) {
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0 || 
            shares == null || shares.isEmpty()) {
            return new HashMap<>();
        }
        
        Map<String, BigDecimal> splits = new HashMap<>();
        int totalShares = shares.values().stream().mapToInt(Integer::intValue).sum();
        
        if (totalShares == 0) {
            return splits;
        }
        
        for (Map.Entry<String, Integer> entry : shares.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null && entry.getValue() > 0) {
                BigDecimal shareRatio = BigDecimal.valueOf(entry.getValue())
                        .divide(BigDecimal.valueOf(totalShares), 4, RoundingMode.HALF_UP);
                BigDecimal splitAmount = totalAmount.multiply(shareRatio).setScale(2, RoundingMode.HALF_UP);
                splits.put(entry.getKey().trim(), splitAmount);
            }
        }
        
        // Handle rounding differences
        BigDecimal totalSplit = splits.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal difference = totalAmount.subtract(totalSplit);
        
        if (difference.compareTo(BigDecimal.ZERO) != 0 && !splits.isEmpty()) {
            String firstUser = splits.keySet().iterator().next();
            splits.put(firstUser, splits.get(firstUser).add(difference));
        }
        
        return splits;
    }
    
    // Statistics and Reporting
    @Override
    public SplitwiseStats getOverallStats() {
        int totalUsers = users.size();
        int activeUsers = (int) users.values().stream().filter(User::isActive).count();
        
        int totalGroups = groups.size();
        int activeGroups = (int) groups.values().stream().filter(Group::isActive).count();
        
        int totalExpenses = expenses.size();
        int settledExpenses = (int) expenses.values().stream().filter(Expense::isSettled).count();
        
        BigDecimal totalExpenseAmount = expenses.values().stream()
                .map(Expense::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalSettledAmount = settlements.values().stream()
                .filter(Settlement::isSettled)
                .map(Settlement::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int totalSettlements = settlements.size();
        int pendingSettlements = (int) settlements.values().stream()
                .filter(Settlement::isPending).count();
        
        return new SplitwiseStats(totalUsers, activeUsers, totalGroups, activeGroups,
                                totalExpenses, settledExpenses, totalExpenseAmount, totalSettledAmount,
                                totalSettlements, pendingSettlements);
    }
    
    @Override
    public GroupStats getGroupStats(String groupId) {
        if (groupId == null || groupId.trim().isEmpty()) {
            return null;
        }
        
        Group group = groups.get(groupId.trim());
        if (group == null) {
            return null;
        }
        
        List<Expense> groupExpenses = getExpensesByGroup(groupId);
        
        int memberCount = group.getMemberCount();
        int totalExpenses = groupExpenses.size();
        int settledExpenses = (int) groupExpenses.stream().filter(Expense::isSettled).count();
        
        BigDecimal totalExpenseAmount = groupExpenses.stream()
                .map(Expense::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        List<Settlement> groupSettlements = getSettlementsByGroup(groupId);
        BigDecimal totalSettledAmount = groupSettlements.stream()
                .filter(Settlement::isSettled)
                .map(Settlement::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        LocalDateTime lastExpenseDate = groupExpenses.stream()
                .map(Expense::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        
        // Find most active user (user who paid for most expenses)
        String mostActiveUser = groupExpenses.stream()
                .collect(Collectors.groupingBy(Expense::getPaidBy, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
        
        BigDecimal largestExpense = groupExpenses.stream()
                .map(Expense::getTotalAmount)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        
        return new GroupStats(groupId, memberCount, totalExpenses, settledExpenses,
                            totalExpenseAmount, totalSettledAmount, lastExpenseDate,
                            mostActiveUser, largestExpense);
    }
    
    @Override
    public UserStats getUserStats(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return null;
        }
        
        User user = users.get(userId.trim());
        if (user == null) {
            return null;
        }
        
        List<Group> userGroups = getGroupsByUser(userId);
        List<Expense> userExpenses = getExpensesByUser(userId);
        
        int groupCount = userGroups.size();
        int totalExpenses = userExpenses.size();
        int expensesPaid = (int) userExpenses.stream()
                .filter(expense -> expense.getPaidBy().equals(userId.trim())).count();
        
        BigDecimal totalAmountPaid = userExpenses.stream()
                .filter(expense -> expense.getPaidBy().equals(userId.trim()))
                .map(Expense::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalAmountOwed = userExpenses.stream()
                .map(expense -> expense.getSplitAmount(userId.trim()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal netBalance = totalAmountPaid.subtract(totalAmountOwed);
        
        LocalDateTime lastExpenseDate = userExpenses.stream()
                .map(Expense::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        
        List<Settlement> userSettlements = getSettlementsByUser(userId);
        int settlementsCompleted = (int) userSettlements.stream()
                .filter(Settlement::isSettled).count();
        int pendingSettlements = (int) userSettlements.stream()
                .filter(Settlement::isPending).count();
        
        return new UserStats(userId, groupCount, totalExpenses, expensesPaid,
                           totalAmountPaid, totalAmountOwed, netBalance, lastExpenseDate,
                           settlementsCompleted, pendingSettlements);
    }
    
    // Notifications
    @Override
    public void sendPaymentReminder(String fromUserId, String toUserId, BigDecimal amount) {
        // In a real implementation, this would send actual notifications
        System.out.println("Payment reminder sent: " + fromUserId + " owes " + toUserId + " $" + amount);
    }
    
    @Override
    public void sendExpenseNotification(String expenseId, List<String> userIds) {
        // In a real implementation, this would send actual notifications
        System.out.println("Expense notification sent for " + expenseId + " to users: " + userIds);
    }
}