package com.machinecoding.splitwise.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Represents an expense that can be split among users.
 */
public class Expense {
    private final String expenseId;
    private final String description;
    private final BigDecimal totalAmount;
    private final String paidBy;
    private final String groupId;
    private final ExpenseType type;
    private final SplitType splitType;
    private final Map<String, BigDecimal> splits;
    private final LocalDateTime createdAt;
    private final String category;
    private final String notes;
    private boolean isSettled;
    
    public Expense(String expenseId, String description, BigDecimal totalAmount, String paidBy,
                  String groupId, ExpenseType type, SplitType splitType, String category, String notes) {
        this.expenseId = expenseId != null ? expenseId.trim() : "";
        this.description = description != null ? description.trim() : "";
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
        this.paidBy = paidBy != null ? paidBy.trim() : "";
        this.groupId = groupId != null ? groupId.trim() : "";
        this.type = type != null ? type : ExpenseType.GENERAL;
        this.splitType = splitType != null ? splitType : SplitType.EQUAL;
        this.splits = new HashMap<>();
        this.createdAt = LocalDateTime.now();
        this.category = category != null ? category.trim() : "General";
        this.notes = notes != null ? notes.trim() : "";
        this.isSettled = false;
    }
    
    // Getters
    public String getExpenseId() { return expenseId; }
    public String getDescription() { return description; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getPaidBy() { return paidBy; }
    public String getGroupId() { return groupId; }
    public ExpenseType getType() { return type; }
    public SplitType getSplitType() { return splitType; }
    public Map<String, BigDecimal> getSplits() { return new HashMap<>(splits); }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getCategory() { return category; }
    public String getNotes() { return notes; }
    public boolean isSettled() { return isSettled; }
    
    public void setSettled(boolean settled) {
        this.isSettled = settled;
    }
    
    public void addSplit(String userId, BigDecimal amount) {
        if (userId != null && amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            splits.put(userId.trim(), amount);
        }
    }
    
    public BigDecimal getSplitAmount(String userId) {
        return splits.getOrDefault(userId, BigDecimal.ZERO);
    }
    
    public List<String> getInvolvedUsers() {
        List<String> users = new ArrayList<>(splits.keySet());
        if (!users.contains(paidBy)) {
            users.add(paidBy);
        }
        return users;
    }
    
    public BigDecimal getTotalSplitAmount() {
        return splits.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public boolean isValidSplit() {
        return getTotalSplitAmount().compareTo(totalAmount) == 0;
    }
    
    @Override
    public String toString() {
        return String.format("Expense{id='%s', description='%s', amount=%s, paidBy='%s', splitType=%s, settled=%s}", 
                           expenseId, description, totalAmount, paidBy, splitType, isSettled);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Expense expense = (Expense) obj;
        return expenseId.equals(expense.expenseId);
    }
    
    @Override
    public int hashCode() {
        return expenseId.hashCode();
    }
}