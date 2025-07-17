package com.machinecoding.payment.model;

/**
 * Types of payment methods supported by the system.
 */
public enum PaymentMethodType {
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    DIGITAL_WALLET("Digital Wallet"),
    BANK_TRANSFER("Bank Transfer"),
    PAYPAL("PayPal"),
    APPLE_PAY("Apple Pay"),
    GOOGLE_PAY("Google Pay"),
    CRYPTOCURRENCY("Cryptocurrency");
    
    private final String displayName;
    
    PaymentMethodType(String displayName) {
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