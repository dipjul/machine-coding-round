package com.machinecoding.messagequeues.events;

import com.machinecoding.messagequeues.events.model.Event;
import com.machinecoding.messagequeues.events.model.EventPriority;

import java.math.BigDecimal;

/**
 * Event fired when a new order is created.
 */
public class OrderCreatedEvent extends Event {
    private final String orderId;
    private final String customerId;
    private final BigDecimal amount;
    private final String currency;
    
    public OrderCreatedEvent(String orderId, String customerId, BigDecimal amount, String currency) {
        super("ORDER_CREATED", "OrderService");
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
        this.currency = currency;
    }
    
    @Override
    public Object getPayload() {
        return new OrderData(orderId, customerId, amount, currency);
    }
    
    @Override
    public EventPriority getPriority() {
        // High-value orders get higher priority
        return amount.compareTo(new BigDecimal("1000")) > 0 ? 
               EventPriority.HIGH : EventPriority.NORMAL;
    }
    
    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    
    public static class OrderData {
        private final String orderId;
        private final String customerId;
        private final BigDecimal amount;
        private final String currency;
        
        public OrderData(String orderId, String customerId, BigDecimal amount, String currency) {
            this.orderId = orderId;
            this.customerId = customerId;
            this.amount = amount;
            this.currency = currency;
        }
        
        public String getOrderId() { return orderId; }
        public String getCustomerId() { return customerId; }
        public BigDecimal getAmount() { return amount; }
        public String getCurrency() { return currency; }
        
        @Override
        public String toString() {
            return String.format("OrderData{orderId='%s', customerId='%s', amount=%s %s}", 
                               orderId, customerId, amount, currency);
        }
    }
}