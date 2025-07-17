package com.machinecoding.payment.model;

/**
 * Status of a payment transaction.
 */
public enum TransactionStatus {
    PENDING("Pending"),
    AUTHORIZED("Authorized"),
    PROCESSING("Processing"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    CANCELLED("Cancelled"),
    REFUNDED("Refunded"),
    DISPUTED("Disputed"),
    SETTLED("Settled");
    
    private final String displayName;
    
    TransactionStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean canTransitionTo(TransactionStatus newStatus) {
        switch (this) {
            case PENDING:
                return newStatus == AUTHORIZED || newStatus == PROCESSING || 
                       newStatus == COMPLETED || newStatus == FAILED || newStatus == CANCELLED;
            case AUTHORIZED:
                return newStatus == PROCESSING || newStatus == COMPLETED || 
                       newStatus == CANCELLED || newStatus == FAILED;
            case PROCESSING:
                return newStatus == COMPLETED || newStatus == FAILED;
            case COMPLETED:
                return newStatus == REFUNDED || newStatus == DISPUTED;
            case REFUNDED:
                return newStatus == DISPUTED;
            case DISPUTED:
                return newStatus == SETTLED;
            default:
                return false;
        }
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}