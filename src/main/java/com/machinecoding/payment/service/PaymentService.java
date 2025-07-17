package com.machinecoding.payment.service;

import com.machinecoding.payment.model.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Main service interface for payment processing operations.
 */
public interface PaymentService {
    
    // Payment Method Management
    PaymentMethod addPaymentMethod(String customerId, PaymentMethodType type, String cardNumber,
                                 String expiryMonth, String expiryYear, String cardHolderName,
                                 String billingAddress, boolean isDefault);
    Optional<PaymentMethod> getPaymentMethod(String paymentMethodId);
    List<PaymentMethod> getPaymentMethodsByCustomer(String customerId);
    boolean updatePaymentMethodStatus(String paymentMethodId, PaymentMethodStatus status);
    boolean deletePaymentMethod(String paymentMethodId);
    boolean setDefaultPaymentMethod(String customerId, String paymentMethodId);
    
    // Transaction Processing
    Transaction processPayment(String paymentMethodId, String merchantId, String orderId,
                             BigDecimal amount, String currency, String description);
    Transaction authorizePayment(String paymentMethodId, String merchantId, String orderId,
                               BigDecimal amount, String currency, String description);
    Transaction capturePayment(String authorizationId, BigDecimal amount);
    Transaction refundPayment(String transactionId, BigDecimal amount, String reason);
    Transaction voidTransaction(String transactionId, String reason);
    
    // Transaction Management
    Optional<Transaction> getTransaction(String transactionId);
    List<Transaction> getTransactionsByPaymentMethod(String paymentMethodId);
    List<Transaction> getTransactionsByMerchant(String merchantId);
    List<Transaction> getTransactionsByOrder(String orderId);
    List<Transaction> getTransactionsByStatus(TransactionStatus status);
    
    // Fraud Detection
    boolean isFraudulent(String paymentMethodId, BigDecimal amount, String merchantId);
    void reportFraud(String transactionId, String reason);
    
    // Dispute Management
    Transaction createDispute(String transactionId, String reason, BigDecimal disputeAmount);
    boolean resolveDispute(String disputeId, boolean inFavorOfCustomer, String resolution);
    
    // Statistics and Reporting
    PaymentStats getPaymentStats();
    MerchantStats getMerchantStats(String merchantId);
    PaymentMethodStats getPaymentMethodStats(String paymentMethodId);
    
    // Webhook and Notifications
    void registerWebhook(String merchantId, String webhookUrl, List<String> events);
    void sendWebhookNotification(String merchantId, String event, Transaction transaction);
}