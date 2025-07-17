package com.machinecoding.ecommerce.model;

import java.math.BigDecimal;

/**
 * Represents an item within an order.
 */
public class OrderItem {
    private final String productId;
    private final String productName;
    private final int quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal totalPrice;
    
    public OrderItem(String productId, String productName, int quantity, BigDecimal unitPrice) {
        this.productId = productId != null ? productId.trim() : "";
        this.productName = productName != null ? productName.trim() : "";
        this.quantity = Math.max(1, quantity);
        this.unitPrice = unitPrice != null ? unitPrice : BigDecimal.ZERO;
        this.totalPrice = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
    }
    
    // Getters
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    
    @Override
    public String toString() {
        return String.format("OrderItem{productId='%s', name='%s', quantity=%d, unitPrice=%s, total=%s}", 
                           productId, productName, quantity, unitPrice, totalPrice);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        OrderItem orderItem = (OrderItem) obj;
        return productId.equals(orderItem.productId);
    }
    
    @Override
    public int hashCode() {
        return productId.hashCode();
    }
}