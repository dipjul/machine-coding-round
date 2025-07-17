package com.machinecoding.splitwise.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Statistics for individual groups.
 */
public class GroupStats {
    private final String groupId;
    private final int memberCount;
    private final int totalExpenses;
    private final int settledExpenses;
    private final BigDecimal totalExpenseAmount;
    private final BigDecimal totalSettledAmount;
    private final LocalDateTime lastExpenseDate;
    private final String mostActiveUser;
    private final BigDecimal largestExpense;
    
    public GroupStats(String groupId, int memberCount, int totalExpenses, int settledExpenses,
                     BigDecimal totalExpenseAmount, BigDecimal totalSettledAmount,
                     LocalDateTime lastExpenseDate, String mostActiveUser, BigDecimal largestExpense) {
        this.groupId = groupId;
        this.memberCount = memberCount;
        this.totalExpenses = totalExpenses;
        this.settledExpenses = settledExpenses;
        this.totalExpenseAmount = totalExpenseAmount != null ? totalExpenseAmount : BigDecimal.ZERO;
        this.totalSettledAmount = totalSettledAmount != null ? totalSettledAmount : BigDecimal.ZERO;
        this.lastExpenseDate = lastExpenseDate;
        this.mostActiveUser = mostActiveUser;
        this.largestExpense = largestExpense != null ? largestExpense : BigDecimal.ZERO;
    }
    
    // Getters
    public String getGroupId() { return groupId; }
    public int getMemberCount() { return memberCount; }
    public int getTotalExpenses() { return totalExpenses; }
    public int getSettledExpenses() { return settledExpenses; }
    public BigDecimal getTotalExpenseAmount() { return totalExpenseAmount; }
    public BigDecimal getTotalSettledAmount() { return totalSettledAmount; }
    public LocalDateTime getLastExpenseDate() { return lastExpenseDate; }
    public String getMostActiveUser() { return mostActiveUser; }
    public BigDecimal getLargestExpense() { return largestExpense; }
    
    public double getSettlementRate() {
        return totalExpenses == 0 ? 0.0 : (double) settledExpenses / totalExpenses * 100.0;
    }
    
    public BigDecimal getAverageExpenseAmount() {
        return totalExpenses == 0 ? BigDecimal.ZERO :
               totalExpenseAmount.divide(BigDecimal.valueOf(totalExpenses), 2, BigDecimal.ROUND_HALF_UP);
    }
    
    public BigDecimal getPendingAmount() {
        return totalExpenseAmount.subtract(totalSettledAmount);
    }
    
    public boolean isActiveGroup() {
        return lastExpenseDate != null && 
               lastExpenseDate.isAfter(LocalDateTime.now().minusDays(30));
    }
    
    @Override
    public String toString() {
        return String.format("GroupStats{id='%s', members=%d, expenses=%d, totalAmount=%s, " +
                           "settlementRate=%.1f%%, pendingAmount=%s}",
                           groupId, memberCount, totalExpenses, totalExpenseAmount,
                           getSettlementRate(), getPendingAmount());
    }
}