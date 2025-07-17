package com.machinecoding.payment.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a payment transaction.
 */
public class Transaction {
    private final String transactionId;
    private final String paymentMethodId;
    private final String merchantId;
    private final String orderId;
    private final BigDecimal amount;
    private final String currency;
    private final TransactionType type;
    private TransactionStatus status;
    private final String description;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String failureReason;
    private String gatewayTransactionId;
    private String authorizationCode;
    private BigDecimal processingFee;
    
    public Transaction(String transactionId, String paymentMethodId, String merchantId,
                      String orderId, BigDecimal amount, String currency, TransactionType type,
                      String description) {
        this.transactionId = transactionId != null ? transactionId.trim() : "";
        this.paymentMethodId = paymentMethodId != null ? paymentMethodId.trim() : "";
        this.merchantId = merchantId != null ? merchantId.trim() : "";
        this.orderId = orderId != null ? orderId.trim() : "";
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.currency = currency != null ? currency.trim() : "USD";
        this.type = type != null ? type : TransactionType.PAYMENT;
        this.status = TransactionStatus.PENDING;
        this.description = description != null ? description.trim() : "";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.processingFee = BigDecimal.ZERO;
    }
    
    // Getters
    public String getTransactionId() { return transactionId; }
    public String getPaymentMethodId() { return paymentMethodId; }
    public String getMerchantId() { return merchantId; }
    public String getOrderId() { return orderId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public TransactionType getType() { return type; }
    public TransactionStatus getStatus() { return status; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getFailureReason() { return failureReason; }
    public String getGatewayTransactionId() { return gatewayTransactionId; }
    public String getAuthorizationCode() { return authorizationCode; }
    public BigDecimal getProcessingFee() { return processingFee; }
    
    public BigDecimal getNetAmount() {
        return amount.subtract(processingFee);
    }
    
    public boolean isSuccessful() {
        return status == TransactionStatus.COMPLETED;
    }
    
    public boolean isFailed() {
        return status == TransactionStatus.FAILED;
    }
    
    public boolean isPending() {
        return status == TransactionStatus.PENDING || status == TransactionStatus.PROCESSING;
    }
    
    public boolean canRefund() {
        return status == TransactionStatus.COMPLETED && type == TransactionType.PAYMENT;
    }
    
    public boolean canCancel() {
        return status == TransactionStatus.PENDING || status == TransactionStatus.AUTHORIZED;
    }
    
    public void updateStatus(TransactionStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void setFailureReason(String reason) {
        this.failureReason = reason != null ? reason.trim() : "";
        this.updatedAt = LocalDateTime.now();
    }
    
    public void setGatewayTransactionId(String gatewayId) {
        this.gatewayTransactionId = gatewayId != null ? gatewayId.trim() : "";
        this.updatedAt = LocalDateTime.now();
    }
    
    public void setAuthorizationCode(String authCode) {
        this.authorizationCode = authCode != null ? authCode.trim() : "";
        this.updatedAt = LocalDateTime.now();
    }
    
    public void setProcessingFee(BigDecimal fee) {
        this.processingFee = fee != null ? fee : BigDecimal.ZERO;
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return String.format("Transaction{id='%s', amount=%s %s, type=%s, status=%s, merchant='%s'}",
                           transactionId, amount, currency, type, status, merchantId);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Transaction that = (Transaction) obj;
        return transactionId.equals(that.transactionId);
    }
    
    @Override
    public int hashCode() {
        return transactionId.hashCode();
    }
}