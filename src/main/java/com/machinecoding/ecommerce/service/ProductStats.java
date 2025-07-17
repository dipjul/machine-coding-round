package com.machinecoding.ecommerce.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Statistics for individual products.
 */
public class ProductStats {
    private final String productId;
    private final int totalSold;
    private final BigDecimal totalRevenue;
    private final int currentStock;
    private final LocalDateTime createdAt;
    
    public ProductStats(String productId, int totalSold, BigDecimal totalRevenue, 
                       int currentStock, LocalDateTime createdAt) {
        this.productId = productId;
        this.totalSold = totalSold;
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        this.currentStock = currentStock;
        this.createdAt = createdAt;
    }
    
    // Getters
    public String getProductId() { return productId; }
    public int getTotalSold() { return totalSold; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public int getCurrentStock() { return currentStock; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    public BigDecimal getAverageSellingPrice() {
        return totalSold > 0 ? 
               totalRevenue.divide(BigDecimal.valueOf(totalSold), 2, BigDecimal.ROUND_HALF_UP) : 
               BigDecimal.ZERO;
    }
    
    public boolean isPopularProduct() {
        return totalSold > 10; // Arbitrary threshold for popularity
    }
    
    public boolean isLowStock() {
        return currentStock < 10; // Arbitrary threshold for low stock
    }
    
    @Override
    public String toString() {
        return String.format("ProductStats{id='%s', sold=%d, revenue=%s, stock=%d, avgPrice=%s}",
                           productId, totalSold, totalRevenue, currentStock, getAverageSellingPrice());
    }
}