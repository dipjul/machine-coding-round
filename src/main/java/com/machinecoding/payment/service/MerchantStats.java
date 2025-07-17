package com.machinecoding.payment.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Statistics for individual merchants.
 */
public class MerchantStats {
    private final String merchantId;
    private final int totalTransactions;
    private final int successfulTransactions;
    private final int failedTransactions;
    private final BigDecimal totalVolume;
    private final BigDecimal totalFees;
    private final BigDecimal averageTransactionAmount;
    private final LocalDateTime firstTransactionDate;
    private final LocalDateTime lastTransactionDate;
    
    public MerchantStats(String merchantId, int totalTransactions, int successfulTransactions,
                        int failedTransactions, BigDecimal totalVolume, BigDecimal totalFees,
                        BigDecimal averageTransactionAmount, LocalDateTime firstTransactionDate,
                        LocalDateTime lastTransactionDate) {
        this.merchantId = merchantId;
        this.totalTransactions = totalTransactions;
        this.successfulTransactions = successfulTransactions;
        this.failedTransactions = failedTransactions;
        this.totalVolume = totalVolume != null ? totalVolume : BigDecimal.ZERO;
        this.totalFees = totalFees != null ? totalFees : BigDecimal.ZERO;
        this.averageTransactionAmount = averageTransactionAmount != null ? averageTransactionAmount : BigDecimal.ZERO;
        this.firstTransactionDate = firstTransactionDate;
        this.lastTransactionDate = lastTransactionDate;
    }
    
    // Getters
    public String getMerchantId() { return merchantId; }
    public int getTotalTransactions() { return totalTransactions; }
    public int getSuccessfulTransactions() { return successfulTransactions; }
    public int getFailedTransactions() { return failedTransactions; }
    public BigDecimal getTotalVolume() { return totalVolume; }
    public BigDecimal getTotalFees() { return totalFees; }
    public BigDecimal getAverageTransactionAmount() { return averageTransactionAmount; }
    public LocalDateTime getFirstTransactionDate() { return firstTransactionDate; }
    public LocalDateTime getLastTransactionDate() { return lastTransactionDate; }
    
    public double getSuccessRate() {
        return totalTransactions == 0 ? 0.0 : (double) successfulTransactions / totalTransactions * 100.0;
    }
    
    public double getFailureRate() {
        return totalTransactions == 0 ? 0.0 : (double) failedTransactions / totalTransactions * 100.0;
    }
    
    public BigDecimal getNetRevenue() {
        return totalVolume.subtract(totalFees);
    }
    
    public boolean isActiveMerchant() {
        return lastTransactionDate != null && 
               lastTransactionDate.isAfter(LocalDateTime.now().minusDays(30));
    }
    
    @Override
    public String toString() {
        return String.format("MerchantStats{id='%s', transactions=%d, volume=%s, successRate=%.1f%%, netRevenue=%s}",
                           merchantId, totalTransactions, totalVolume, getSuccessRate(), getNetRevenue());
    }
}