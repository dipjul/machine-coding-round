package com.machinecoding.payment.model;

/**
 * Status of a payment method.
 */
public enum PaymentMethodStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    EXPIRED("Expired"),
    BLOCKED("Blocked"),
    PENDING_VERIFICATION("Pending Verification");
    
    private final String displayName;
    
    PaymentMethodStatus(String displayName) {
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