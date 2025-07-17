package com.machinecoding.payment.service;

import java.math.BigDecimal;

/**
 * Overall statistics for the payment system.
 */
public class PaymentStats {
    private final int totalTransactions;
    private final int successfulTransactions;
    private final int failedTransactions;
    private final int refundedTransactions;
    private final int disputedTransactions;
    private final BigDecimal totalVolume;
    private final BigDecimal totalFees;
    private final BigDecimal totalRefunds;
    private final int totalPaymentMethods;
    private final int activePaymentMethods;
    
    public PaymentStats(int totalTransactions, int successfulTransactions, int failedTransactions,
                       int refundedTransactions, int disputedTransactions, BigDecimal totalVolume,
                       BigDecimal totalFees, BigDecimal totalRefunds, int totalPaymentMethods,
                       int activePaymentMethods) {
        this.totalTransactions = totalTransactions;
        this.successfulTransactions = successfulTransactions;
        this.failedTransactions = failedTransactions;
        this.refundedTransactions = refundedTransactions;
        this.disputedTransactions = disputedTransactions;
        this.totalVolume = totalVolume != null ? totalVolume : BigDecimal.ZERO;
        this.totalFees = totalFees != null ? totalFees : BigDecimal.ZERO;
        this.totalRefunds = totalRefunds != null ? totalRefunds : BigDecimal.ZERO;
        this.totalPaymentMethods = totalPaymentMethods;
        this.activePaymentMethods = activePaymentMethods;
    }
    
    // Getters
    public int getTotalTransactions() { return totalTransactions; }
    public int getSuccessfulTransactions() { return successfulTransactions; }
    public int getFailedTransactions() { return failedTransactions; }
    public int getRefundedTransactions() { return refundedTransactions; }
    public int getDisputedTransactions() { return disputedTransactions; }
    public BigDecimal getTotalVolume() { return totalVolume; }
    public BigDecimal getTotalFees() { return totalFees; }
    public BigDecimal getTotalRefunds() { return totalRefunds; }
    public int getTotalPaymentMethods() { return totalPaymentMethods; }
    public int getActivePaymentMethods() { return activePaymentMethods; }
    
    public double getSuccessRate() {
        return totalTransactions == 0 ? 0.0 : (double) successfulTransactions / totalTransactions * 100.0;
    }
    
    public double getFailureRate() {
        return totalTransactions == 0 ? 0.0 : (double) failedTransactions / totalTransactions * 100.0;
    }
    
    public double getRefundRate() {
        return successfulTransactions == 0 ? 0.0 : (double) refundedTransactions / successfulTransactions * 100.0;
    }
    
    public double getDisputeRate() {
        return successfulTransactions == 0 ? 0.0 : (double) disputedTransactions / successfulTransactions * 100.0;
    }
    
    public BigDecimal getAverageTransactionAmount() {
        return successfulTransactions == 0 ? BigDecimal.ZERO :
               totalVolume.divide(BigDecimal.valueOf(successfulTransactions), 2, BigDecimal.ROUND_HALF_UP);
    }
    
    public BigDecimal getNetRevenue() {
        return totalVolume.subtract(totalRefunds).subtract(totalFees);
    }
    
    @Override
    public String toString() {
        return String.format("PaymentStats{transactions=%d (success=%d, failed=%d), volume=%s, " +
                           "successRate=%.1f%%, refundRate=%.1f%%, disputeRate=%.1f%%, netRevenue=%s}",
                           totalTransactions, successfulTransactions, failedTransactions, totalVolume,
                           getSuccessRate(), getRefundRate(), getDisputeRate(), getNetRevenue());
    }
}