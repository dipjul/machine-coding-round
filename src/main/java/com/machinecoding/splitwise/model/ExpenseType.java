package com.machinecoding.splitwise.model;

/**
 * Types of expenses in the system.
 */
public enum ExpenseType {
    GENERAL("General"),
    FOOD("Food & Dining"),
    TRANSPORTATION("Transportation"),
    ACCOMMODATION("Accommodation"),
    ENTERTAINMENT("Entertainment"),
    SHOPPING("Shopping"),
    UTILITIES("Utilities"),
    HEALTHCARE("Healthcare"),
    EDUCATION("Education"),
    TRAVEL("Travel"),
    OTHER("Other");
    
    private final String displayName;
    
    ExpenseType(String displayName) {
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