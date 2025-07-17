package com.machinecoding.payment.model;

/**
 * Types of payment transactions.
 */
public enum TransactionType {
    PAYMENT("Payment"),
    REFUND("Refund"),
    AUTHORIZATION("Authorization"),
    CAPTURE("Capture"),
    VOID("Void"),
    CHARGEBACK("Chargeback"),
    DISPUTE("Dispute"),
    SETTLEMENT("Settlement");
    
    private final String displayName;
    
    TransactionType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}