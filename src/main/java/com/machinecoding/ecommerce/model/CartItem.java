package com.machinecoding.ecommerce.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents an item in a shopping cart.
 */
public class CartItem {
    private final String productId;
    private int quantity;
    private final BigDecimal unitPrice;
    private final LocalDateTime addedAt;
    private LocalDateTime updatedAt;
    
    public CartItem(String productId, int quantity, BigDecimal unitPrice) {
        this.productId = productId != null ? productId.trim() : "";
        this.quantity = Math.max(1, quantity);
        this.unitPrice = unitPrice != null ? unitPrice : BigDecimal.ZERO;
        this.addedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters
    public String getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public LocalDateTime getAddedAt() { return addedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    public BigDecimal getTotalPrice() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
    
    public void updateQuantity(int newQuantity) {
        this.quantity = Math.max(1, newQuantity);
        this.updatedAt = LocalDateTime.now();
    }
    
    public void incrementQuantity(int amount) {
        this.quantity += Math.max(0, amount);
        this.updatedAt = LocalDateTime.now();
    }
    
    public void decrementQuantity(int amount) {
        this.quantity = Math.max(1, this.quantity - Math.max(0, amount));
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return String.format("CartItem{productId='%s', quantity=%d, unitPrice=%s, total=%s}", 
                           productId, quantity, unitPrice, getTotalPrice());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CartItem cartItem = (CartItem) obj;
        return productId.equals(cartItem.productId);
    }
    
    @Override
    public int hashCode() {
        return productId.hashCode();
    }
}