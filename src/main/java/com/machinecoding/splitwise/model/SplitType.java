package com.machinecoding.splitwise.model;

/**
 * Types of expense splitting methods.
 */
public enum SplitType {
    EQUAL("Equal Split"),
    EXACT("Exact Amount"),
    PERCENTAGE("Percentage"),
    SHARES("Shares");
    
    private final String displayName;
    
    SplitType(String displayName) {
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