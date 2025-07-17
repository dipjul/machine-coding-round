package com.machinecoding.ecommerce.model;

import java.time.LocalDateTime;

/**
 * Represents inventory information for a product.
 */
public class Inventory {
    private final String productId;
    private int availableQuantity;
    private int reservedQuantity;
    private final int reorderLevel;
    private final int maxStockLevel;
    private final LocalDateTime lastUpdated;
    
    public Inventory(String productId, int availableQuantity, int reservedQuantity, 
                    int reorderLevel, int maxStockLevel) {
        this.productId = productId != null ? productId.trim() : "";
        this.availableQuantity = Math.max(0, availableQuantity);
        this.reservedQuantity = Math.max(0, reservedQuantity);
        this.reorderLevel = Math.max(0, reorderLevel);
        this.maxStockLevel = Math.max(0, maxStockLevel);
        this.lastUpdated = LocalDateTime.now();
    }
    
    // Getters
    public String getProductId() { return productId; }
    public int getAvailableQuantity() { return availableQuantity; }
    public int getReservedQuantity() { return reservedQuantity; }
    public int getReorderLevel() { return reorderLevel; }
    public int getMaxStockLevel() { return maxStockLevel; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    
    public int getTotalQuantity() {
        return availableQuantity + reservedQuantity;
    }
    
    public boolean isAvailable(int quantity) {
        return availableQuantity >= quantity;
    }
    
    public boolean needsReorder() {
        return getTotalQuantity() <= reorderLevel;
    }
    
    public boolean isOutOfStock() {
        return availableQuantity == 0;
    }
    
    public synchronized boolean reserveQuantity(int quantity) {
        if (availableQuantity >= quantity) {
            availableQuantity -= quantity;
            reservedQuantity += quantity;
            return true;
        }
        return false;
    }
    
    public synchronized void releaseReservedQuantity(int quantity) {
        int releaseAmount = Math.min(quantity, reservedQuantity);
        reservedQuantity -= releaseAmount;
        availableQuantity += releaseAmount;
    }
    
    public synchronized void confirmReservedQuantity(int quantity) {
        int confirmAmount = Math.min(quantity, reservedQuantity);
        reservedQuantity -= confirmAmount;
    }
    
    public synchronized void addStock(int quantity) {
        availableQuantity += Math.max(0, quantity);
    }
    
    public synchronized void removeStock(int quantity) {
        availableQuantity = Math.max(0, availableQuantity - quantity);
    }
    
    @Override
    public String toString() {
        return String.format("Inventory{productId='%s', available=%d, reserved=%d, reorderLevel=%d}", 
                           productId, availableQuantity, reservedQuantity, reorderLevel);
    }
}