package com.machinecoding.splitwise.service;

import java.math.BigDecimal;

/**
 * Overall statistics for the Splitwise system.
 */
public class SplitwiseStats {
    private final int totalUsers;
    private final int activeUsers;
    private final int totalGroups;
    private final int activeGroups;
    private final int totalExpenses;
    private final int settledExpenses;
    private final BigDecimal totalExpenseAmount;
    private final BigDecimal totalSettledAmount;
    private final int totalSettlements;
    private final int pendingSettlements;
    
    public SplitwiseStats(int totalUsers, int activeUsers, int totalGroups, int activeGroups,
                         int totalExpenses, int settledExpenses, BigDecimal totalExpenseAmount,
                         BigDecimal totalSettledAmount, int totalSettlements, int pendingSettlements) {
        this.totalUsers = totalUsers;
        this.activeUsers = activeUsers;
        this.totalGroups = totalGroups;
        this.activeGroups = activeGroups;
        this.totalExpenses = totalExpenses;
        this.settledExpenses = settledExpenses;
        this.totalExpenseAmount = totalExpenseAmount != null ? totalExpenseAmount : BigDecimal.ZERO;
        this.totalSettledAmount = totalSettledAmount != null ? totalSettledAmount : BigDecimal.ZERO;
        this.totalSettlements = totalSettlements;
        this.pendingSettlements = pendingSettlements;
    }
    
    // Getters
    public int getTotalUsers() { return totalUsers; }
    public int getActiveUsers() { return activeUsers; }
    public int getTotalGroups() { return totalGroups; }
    public int getActiveGroups() { return activeGroups; }
    public int getTotalExpenses() { return totalExpenses; }
    public int getSettledExpenses() { return settledExpenses; }
    public BigDecimal getTotalExpenseAmount() { return totalExpenseAmount; }
    public BigDecimal getTotalSettledAmount() { return totalSettledAmount; }
    public int getTotalSettlements() { return totalSettlements; }
    public int getPendingSettlements() { return pendingSettlements; }
    
    public double getSettlementRate() {
        return totalExpenses == 0 ? 0.0 : (double) settledExpenses / totalExpenses * 100.0;
    }
    
    public double getUserActivityRate() {
        return totalUsers == 0 ? 0.0 : (double) activeUsers / totalUsers * 100.0;
    }
    
    public double getGroupActivityRate() {
        return totalGroups == 0 ? 0.0 : (double) activeGroups / totalGroups * 100.0;
    }
    
    public BigDecimal getAverageExpenseAmount() {
        return totalExpenses == 0 ? BigDecimal.ZERO :
               totalExpenseAmount.divide(BigDecimal.valueOf(totalExpenses), 2, BigDecimal.ROUND_HALF_UP);
    }
    
    public BigDecimal getPendingAmount() {
        return totalExpenseAmount.subtract(totalSettledAmount);
    }
    
    @Override
    public String toString() {
        return String.format("SplitwiseStats{users=%d/%d (%.1f%%), groups=%d/%d (%.1f%%), " +
                           "expenses=%d (settled=%d, %.1f%%), totalAmount=%s, pendingAmount=%s}",
                           activeUsers, totalUsers, getUserActivityRate(),
                           activeGroups, totalGroups, getGroupActivityRate(),
                           totalExpenses, settledExpenses, getSettlementRate(),
                           totalExpenseAmount, getPendingAmount());
    }
}