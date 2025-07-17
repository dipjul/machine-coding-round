package com.machinecoding.payment.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Statistics for individual payment methods.
 */
public class PaymentMethodStats {
    private final String paymentMethodId;
    private final int totalTransactions;
    private final int successfulTransactions;
    private final int failedTransactions;
    private final BigDecimal totalAmount;
    private final LocalDateTime firstUsed;
    private final LocalDateTime lastUsed;
    
    public PaymentMethodStats(String paymentMethodId, int totalTransactions, int successfulTransactions,
                             int failedTransactions, BigDecimal totalAmount, LocalDateTime firstUsed,
                             LocalDateTime lastUsed) {
        this.paymentMethodId = paymentMethodId;
        this.totalTransactions = totalTransactions;
        this.successfulTransactions = successfulTransactions;
        this.failedTransactions = failedTransactions;
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
        this.firstUsed = firstUsed;
        this.lastUsed = lastUsed;
    }
    
    // Getters
    public String getPaymentMethodId() { return paymentMethodId; }
    public int getTotalTransactions() { return totalTransactions; }
    public int getSuccessfulTransactions() { return successfulTransactions; }
    public int getFailedTransactions() { return failedTransactions; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public LocalDateTime getFirstUsed() { return firstUsed; }
    public LocalDateTime getLastUsed() { return lastUsed; }
    
    public double getSuccessRate() {
        return totalTransactions == 0 ? 0.0 : (double) successfulTransactions / totalTransactions * 100.0;
    }
    
    public double getFailureRate() {
        return totalTransactions == 0 ? 0.0 : (double) failedTransactions / totalTransactions * 100.0;
    }
    
    public BigDecimal getAverageTransactionAmount() {
        return successfulTransactions == 0 ? BigDecimal.ZERO :
               totalAmount.divide(BigDecimal.valueOf(successfulTransactions), 2, BigDecimal.ROUND_HALF_UP);
    }
    
    public boolean isFrequentlyUsed() {
        return totalTransactions > 10; // Arbitrary threshold
    }
    
    public boolean isRecentlyUsed() {
        return lastUsed != null && lastUsed.isAfter(LocalDateTime.now().minusDays(30));
    }
    
    @Override
    public String toString() {
        return String.format("PaymentMethodStats{id='%s', transactions=%d, amount=%s, successRate=%.1f%%}",
                           paymentMethodId, totalTransactions, totalAmount, getSuccessRate());
    }
}