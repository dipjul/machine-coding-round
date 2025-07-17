package com.machinecoding.ecommerce.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Statistics for individual customers.
 */
public class CustomerStats {
    private final String customerId;
    private final int totalOrders;
    private final int deliveredOrders;
    private final int cancelledOrders;
    private final BigDecimal totalSpent;
    private final BigDecimal averageOrderValue;
    private final LocalDateTime lastOrderDate;
    private final LocalDateTime registrationDate;
    
    public CustomerStats(String customerId, int totalOrders, int deliveredOrders, int cancelledOrders,
                        BigDecimal totalSpent, BigDecimal averageOrderValue,
                        LocalDateTime lastOrderDate, LocalDateTime registrationDate) {
        this.customerId = customerId;
        this.totalOrders = totalOrders;
        this.deliveredOrders = deliveredOrders;
        this.cancelledOrders = cancelledOrders;
        this.totalSpent = totalSpent != null ? totalSpent : BigDecimal.ZERO;
        this.averageOrderValue = averageOrderValue != null ? averageOrderValue : BigDecimal.ZERO;
        this.lastOrderDate = lastOrderDate;
        this.registrationDate = registrationDate;
    }
    
    // Getters
    public String getCustomerId() { return customerId; }
    public int getTotalOrders() { return totalOrders; }
    public int getDeliveredOrders() { return deliveredOrders; }
    public int getCancelledOrders() { return cancelledOrders; }
    public BigDecimal getTotalSpent() { return totalSpent; }
    public BigDecimal getAverageOrderValue() { return averageOrderValue; }
    public LocalDateTime getLastOrderDate() { return lastOrderDate; }
    public LocalDateTime getRegistrationDate() { return registrationDate; }
    
    public double getOrderCompletionRate() {
        return totalOrders == 0 ? 0.0 : (double) deliveredOrders / totalOrders * 100.0;
    }
    
    public double getOrderCancellationRate() {
        return totalOrders == 0 ? 0.0 : (double) cancelledOrders / totalOrders * 100.0;
    }
    
    public boolean isActiveCustomer() {
        return lastOrderDate != null && lastOrderDate.isAfter(LocalDateTime.now().minusMonths(3));
    }
    
    @Override
    public String toString() {
        return String.format("CustomerStats{id='%s', orders=%d, spent=%s, avgOrder=%s, completion=%.1f%%}",
                           customerId, totalOrders, totalSpent, averageOrderValue, getOrderCompletionRate());
    }
}