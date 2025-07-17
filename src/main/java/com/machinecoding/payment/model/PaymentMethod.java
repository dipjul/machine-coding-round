package com.machinecoding.payment.model;

import java.time.LocalDateTime;

/**
 * Represents a payment method (credit card, debit card, digital wallet, etc.)
 */
public class PaymentMethod {
    private final String paymentMethodId;
    private final String customerId;
    private final PaymentMethodType type;
    private final String maskedNumber;
    private final String expiryMonth;
    private final String expiryYear;
    private final String cardHolderName;
    private final String billingAddress;
    private final PaymentMethodStatus status;
    private final LocalDateTime createdAt;
    private final boolean isDefault;
    
    public PaymentMethod(String paymentMethodId, String customerId, PaymentMethodType type,
                        String maskedNumber, String expiryMonth, String expiryYear,
                        String cardHolderName, String billingAddress, boolean isDefault) {
        this.paymentMethodId = paymentMethodId != null ? paymentMethodId.trim() : "";
        this.customerId = customerId != null ? customerId.trim() : "";
        this.type = type != null ? type : PaymentMethodType.CREDIT_CARD;
        this.maskedNumber = maskedNumber != null ? maskedNumber.trim() : "";
        this.expiryMonth = expiryMonth != null ? expiryMonth.trim() : "";
        this.expiryYear = expiryYear != null ? expiryYear.trim() : "";
        this.cardHolderName = cardHolderName != null ? cardHolderName.trim() : "";
        this.billingAddress = billingAddress != null ? billingAddress.trim() : "";
        this.status = PaymentMethodStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.isDefault = isDefault;
    }
    
    // Getters
    public String getPaymentMethodId() { return paymentMethodId; }
    public String getCustomerId() { return customerId; }
    public PaymentMethodType getType() { return type; }
    public String getMaskedNumber() { return maskedNumber; }
    public String getExpiryMonth() { return expiryMonth; }
    public String getExpiryYear() { return expiryYear; }
    public String getCardHolderName() { return cardHolderName; }
    public String getBillingAddress() { return billingAddress; }
    public PaymentMethodStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isDefault() { return isDefault; }
    
    public boolean isActive() {
        return status == PaymentMethodStatus.ACTIVE;
    }
    
    public boolean isExpired() {
        if (expiryMonth.isEmpty() || expiryYear.isEmpty()) {
            return false;
        }
        
        try {
            int month = Integer.parseInt(expiryMonth);
            int year = Integer.parseInt(expiryYear);
            LocalDateTime now = LocalDateTime.now();
            
            if (year < now.getYear()) {
                return true;
            } else if (year == now.getYear() && month < now.getMonthValue()) {
                return true;
            }
            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public PaymentMethod withStatus(PaymentMethodStatus newStatus) {
        PaymentMethod updated = new PaymentMethod(paymentMethodId, customerId, type, maskedNumber,
                                                expiryMonth, expiryYear, cardHolderName, billingAddress, isDefault);
        return updated; // In a real implementation, we'd properly handle immutability
    }
    
    @Override
    public String toString() {
        return String.format("PaymentMethod{id='%s', type=%s, number='%s', holder='%s', status=%s}",
                           paymentMethodId, type, maskedNumber, cardHolderName, status);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PaymentMethod that = (PaymentMethod) obj;
        return paymentMethodId.equals(that.paymentMethodId);
    }
    
    @Override
    public int hashCode() {
        return paymentMethodId.hashCode();
    }
}