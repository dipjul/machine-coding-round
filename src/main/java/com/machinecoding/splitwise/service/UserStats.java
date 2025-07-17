package com.machinecoding.splitwise.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Statistics for individual users.
 */
public class UserStats {
    private final String userId;
    private final int groupCount;
    private final int totalExpenses;
    private final int expensesPaid;
    private final BigDecimal totalAmountPaid;
    private final BigDecimal totalAmountOwed;
    private final BigDecimal netBalance;
    private final LocalDateTime lastExpenseDate;
    private final int settlementsCompleted;
    private final int pendingSettlements;
    
    public UserStats(String userId, int groupCount, int totalExpenses, int expensesPaid,
                    BigDecimal totalAmountPaid, BigDecimal totalAmountOwed, BigDecimal netBalance,
                    LocalDateTime lastExpenseDate, int settlementsCompleted, int pendingSettlements) {
        this.userId = userId;
        this.groupCount = groupCount;
        this.totalExpenses = totalExpenses;
        this.expensesPaid = expensesPaid;
        this.totalAmountPaid = totalAmountPaid != null ? totalAmountPaid : BigDecimal.ZERO;
        this.totalAmountOwed = totalAmountOwed != null ? totalAmountOwed : BigDecimal.ZERO;
        this.netBalance = netBalance != null ? netBalance : BigDecimal.ZERO;
        this.lastExpenseDate = lastExpenseDate;
        this.settlementsCompleted = settlementsCompleted;
        this.pendingSettlements = pendingSettlements;
    }
    
    // Getters
    public String getUserId() { return userId; }
    public int getGroupCount() { return groupCount; }
    public int getTotalExpenses() { return totalExpenses; }
    public int getExpensesPaid() { return expensesPaid; }
    public BigDecimal getTotalAmountPaid() { return totalAmountPaid; }
    public BigDecimal getTotalAmountOwed() { return totalAmountOwed; }
    public BigDecimal getNetBalance() { return netBalance; }
    public LocalDateTime getLastExpenseDate() { return lastExpenseDate; }
    public int getSettlementsCompleted() { return settlementsCompleted; }
    public int getPendingSettlements() { return pendingSettlements; }
    
    public boolean isCreditor() {
        return netBalance.compareTo(BigDecimal.ZERO) > 0;
    }
    
    public boolean isDebtor() {
        return netBalance.compareTo(BigDecimal.ZERO) < 0;
    }
    
    public boolean isSettled() {
        return netBalance.compareTo(BigDecimal.ZERO) == 0;
    }
    
    public double getPaymentRatio() {
        return totalExpenses == 0 ? 0.0 : (double) expensesPaid / totalExpenses * 100.0;
    }
    
    public BigDecimal getAverageExpenseAmount() {
        return expensesPaid == 0 ? BigDecimal.ZERO :
               totalAmountPaid.divide(BigDecimal.valueOf(expensesPaid), 2, BigDecimal.ROUND_HALF_UP);
    }
    
    public boolean isActiveUser() {
        return lastExpenseDate != null && 
               lastExpenseDate.isAfter(LocalDateTime.now().minusDays(30));
    }
    
    @Override
    public String toString() {
        return String.format("UserStats{id='%s', groups=%d, expenses=%d/%d, netBalance=%s, " +
                           "pendingSettlements=%d, active=%s}",
                           userId, groupCount, expensesPaid, totalExpenses, netBalance,
                           pendingSettlements, isActiveUser());
    }
}