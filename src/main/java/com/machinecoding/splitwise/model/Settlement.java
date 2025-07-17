package com.machinecoding.splitwise.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a settlement between two users.
 */
public class Settlement {
    private final String settlementId;
    private final String fromUserId;
    private final String toUserId;
    private final BigDecimal amount;
    private final String groupId;
    private final String description;
    private final LocalDateTime createdAt;
    private SettlementStatus status;
    private LocalDateTime settledAt;
    
    public Settlement(String settlementId, String fromUserId, String toUserId, 
                     BigDecimal amount, String groupId, String description) {
        this.settlementId = settlementId != null ? settlementId.trim() : "";
        this.fromUserId = fromUserId != null ? fromUserId.trim() : "";
        this.toUserId = toUserId != null ? toUserId.trim() : "";
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.groupId = groupId != null ? groupId.trim() : "";
        this.description = description != null ? description.trim() : "";
        this.createdAt = LocalDateTime.now();
        this.status = SettlementStatus.PENDING;
    }
    
    // Getters
    public String getSettlementId() { return settlementId; }
    public String getFromUserId() { return fromUserId; }
    public String getToUserId() { return toUserId; }
    public BigDecimal getAmount() { return amount; }
    public String getGroupId() { return groupId; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public SettlementStatus getStatus() { return status; }
    public LocalDateTime getSettledAt() { return settledAt; }
    
    public void markAsSettled() {
        this.status = SettlementStatus.SETTLED;
        this.settledAt = LocalDateTime.now();
    }
    
    public void markAsRejected() {
        this.status = SettlementStatus.REJECTED;
    }
    
    public boolean isPending() {
        return status == SettlementStatus.PENDING;
    }
    
    public boolean isSettled() {
        return status == SettlementStatus.SETTLED;
    }
    
    @Override
    public String toString() {
        return String.format("Settlement{id='%s', from='%s', to='%s', amount=%s, status=%s}", 
                           settlementId, fromUserId, toUserId, amount, status);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Settlement settlement = (Settlement) obj;
        return settlementId.equals(settlement.settlementId);
    }
    
    @Override
    public int hashCode() {
        return settlementId.hashCode();
    }
}

enum SettlementStatus {
    PENDING("Pending"),
    SETTLED("Settled"),
    REJECTED("Rejected");
    
    private final String displayName;
    
    SettlementStatus(String displayName) {
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