package com.machinecoding.splitwise.service;

import com.machinecoding.splitwise.model.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Main service interface for Splitwise operations.
 */
public interface SplitwiseService {
    
    // User Management
    User registerUser(String name, String email, String phoneNumber);
    Optional<User> getUser(String userId);
    List<User> getAllUsers();
    boolean updateUser(String userId, String name, String email, String phoneNumber);
    boolean deactivateUser(String userId);
    
    // Group Management
    Group createGroup(String name, String description, String createdBy);
    Optional<Group> getGroup(String groupId);
    List<Group> getGroupsByUser(String userId);
    boolean addMemberToGroup(String groupId, String userId);
    boolean removeMemberFromGroup(String groupId, String userId);
    boolean updateGroup(String groupId, String name, String description);
    boolean deleteGroup(String groupId);
    
    // Expense Management
    Expense addExpense(String description, BigDecimal totalAmount, String paidBy, String groupId,
                      ExpenseType type, SplitType splitType, Map<String, BigDecimal> splits,
                      String category, String notes);
    Optional<Expense> getExpense(String expenseId);
    List<Expense> getExpensesByGroup(String groupId);
    List<Expense> getExpensesByUser(String userId);
    boolean updateExpense(String expenseId, String description, String category, String notes);
    boolean deleteExpense(String expenseId);
    
    // Balance Calculations
    Map<String, BigDecimal> getGroupBalances(String groupId);
    Map<String, BigDecimal> getUserBalances(String userId);
    BigDecimal getBalanceBetweenUsers(String userId1, String userId2);
    List<Settlement> calculateOptimalSettlements(String groupId);
    
    // Settlement Management
    Settlement createSettlement(String fromUserId, String toUserId, BigDecimal amount, 
                              String groupId, String description);
    Optional<Settlement> getSettlement(String settlementId);
    List<Settlement> getSettlementsByUser(String userId);
    List<Settlement> getSettlementsByGroup(String groupId);
    boolean settlePayment(String settlementId);
    boolean rejectSettlement(String settlementId);
    
    // Expense Splitting Utilities
    Map<String, BigDecimal> splitEqually(BigDecimal totalAmount, List<String> userIds);
    Map<String, BigDecimal> splitByExactAmounts(Map<String, BigDecimal> exactAmounts);
    Map<String, BigDecimal> splitByPercentages(BigDecimal totalAmount, Map<String, BigDecimal> percentages);
    Map<String, BigDecimal> splitByShares(BigDecimal totalAmount, Map<String, Integer> shares);
    
    // Statistics and Reporting
    SplitwiseStats getOverallStats();
    GroupStats getGroupStats(String groupId);
    UserStats getUserStats(String userId);
    
    // Notifications
    void sendPaymentReminder(String fromUserId, String toUserId, BigDecimal amount);
    void sendExpenseNotification(String expenseId, List<String> userIds);
}