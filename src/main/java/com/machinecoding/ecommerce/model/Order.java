package com.machinecoding.ecommerce.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * Represents an order in the e-commerce system.
 */
public class Order {
    private final String orderId;
    private final String customerId;
    private final List<OrderItem> items;
    private final Address shippingAddress;
    private final Address billingAddress;
    private final BigDecimal subtotal;
    private final BigDecimal taxAmount;
    private final BigDecimal shippingCost;
    private final BigDecimal totalAmount;
    private OrderStatus status;
    private final String paymentMethodId;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private String trackingNumber;
    
    public Order(String orderId, String customerId, List<OrderItem> items,
                Address shippingAddress, Address billingAddress,
                BigDecimal subtotal, BigDecimal taxAmount, BigDecimal shippingCost,
                String paymentMethodId) {
        this.orderId = orderId != null ? orderId.trim() : "";
        this.customerId = customerId != null ? customerId.trim() : "";
        this.items = new ArrayList<>(items != null ? items : new ArrayList<>());
        this.shippingAddress = shippingAddress;
        this.billingAddress = billingAddress;
        this.subtotal = subtotal != null ? subtotal : BigDecimal.ZERO;
        this.taxAmount = taxAmount != null ? taxAmount : BigDecimal.ZERO;
        this.shippingCost = shippingCost != null ? shippingCost : BigDecimal.ZERO;
        this.totalAmount = this.subtotal.add(this.taxAmount).add(this.shippingCost);
        this.status = OrderStatus.PENDING;
        this.paymentMethodId = paymentMethodId != null ? paymentMethodId.trim() : "";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters
    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public List<OrderItem> getItems() { return new ArrayList<>(items); }
    public Address getShippingAddress() { return shippingAddress; }
    public Address getBillingAddress() { return billingAddress; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public BigDecimal getShippingCost() { return shippingCost; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public OrderStatus getStatus() { return status; }
    public String getPaymentMethodId() { return paymentMethodId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getShippedAt() { return shippedAt; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public String getTrackingNumber() { return trackingNumber; }
    
    public int getTotalItems() {
        return items.stream().mapToInt(OrderItem::getQuantity).sum();
    }
    
    public boolean canCancel() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }
    
    public boolean canShip() {
        return status == OrderStatus.CONFIRMED || status == OrderStatus.PROCESSING;
    }
    
    public boolean isActive() {
        return status != OrderStatus.CANCELLED && status != OrderStatus.DELIVERED;
    }
    
    public void updateStatus(OrderStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
        
        if (newStatus == OrderStatus.SHIPPED && shippedAt == null) {
            this.shippedAt = LocalDateTime.now();
        } else if (newStatus == OrderStatus.DELIVERED && deliveredAt == null) {
            this.deliveredAt = LocalDateTime.now();
        }
    }
    
    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber != null ? trackingNumber.trim() : "";
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return String.format("Order{id='%s', customer='%s', items=%d, total=%s, status=%s}", 
                           orderId, customerId, items.size(), totalAmount, status);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Order order = (Order) obj;
        return orderId.equals(order.orderId);
    }
    
    @Override
    public int hashCode() {
        return orderId.hashCode();
    }
}