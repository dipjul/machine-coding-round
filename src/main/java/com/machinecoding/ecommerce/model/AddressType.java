package com.machinecoding.ecommerce.model;

/**
 * Type of address (shipping or billing).
 */
public enum AddressType {
    SHIPPING("Shipping"),
    BILLING("Billing"),
    BOTH("Both");
    
    private final String displayName;
    
    AddressType(String displayName) {
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