package com.machinecoding.payment.service;

import com.machinecoding.payment.model.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * In-memory implementation of the payment service.
 * Thread-safe implementation using concurrent collections.
 */
public class InMemoryPaymentService implements PaymentService {
    
    private final Map<String, PaymentMethod> paymentMethods;
    private final Map<String, Transaction> transactions;
    private final Map<String, List<String>> customerPaymentMethods;
    private final Map<String, String> webhooks; // merchantId -> webhookUrl
    private final AtomicInteger paymentMethodIdCounter;
    private final AtomicInteger transactionIdCounter;
    private final FraudDetectionEngine fraudDetectionEngine;
    private final PaymentGateway paymentGateway;
    
    public InMemoryPaymentService() {
        this.paymentMethods = new ConcurrentHashMap<>();
        this.transactions = new ConcurrentHashMap<>();
        this.customerPaymentMethods = new ConcurrentHashMap<>();
        this.webhooks = new ConcurrentHashMap<>();
        this.paymentMethodIdCounter = new AtomicInteger(1);
        this.transactionIdCounter = new AtomicInteger(1);
        this.fraudDetectionEngine = new FraudDetectionEngine();
        this.paymentGateway = new PaymentGateway();
    }
    
    // Payment Method Management
    @Override
    public PaymentMethod addPaymentMethod(String customerId, PaymentMethodType type, String cardNumber,
                                        String expiryMonth, String expiryYear, String cardHolderName,
                                        String billingAddress, boolean isDefault) {
        if (customerId == null || cardNumber == null || cardHolderName == null) {
            throw new IllegalArgumentException("Customer ID, card number, and card holder name are required");
        }
        
        // Validate card number (basic validation)
        if (!isValidCardNumber(cardNumber)) {
            throw new IllegalArgumentException("Invalid card number");
        }
        
        // Generate payment method ID
        String paymentMethodId = "PM" + String.format("%06d", paymentMethodIdCounter.getAndIncrement());
        
        // Mask the card number (show only last 4 digits)
        String maskedNumber = maskCardNumber(cardNumber);
        
        // Create payment method
        PaymentMethod paymentMethod = new PaymentMethod(paymentMethodId, customerId, type, maskedNumber,
                                                      expiryMonth, expiryYear, cardHolderName, billingAddress, isDefault);
        
        paymentMethods.put(paymentMethodId, paymentMethod);
        
        // Add to customer's payment methods
        customerPaymentMethods.computeIfAbsent(customerId, k -> new ArrayList<>()).add(paymentMethodId);
        
        // If this is set as default, update other payment methods for this customer
        if (isDefault) {
            setDefaultPaymentMethod(customerId, paymentMethodId);
        }
        
        return paymentMethod;
    }
    
    @Override
    public Optional<PaymentMethod> getPaymentMethod(String paymentMethodId) {
        if (paymentMethodId == null || paymentMethodId.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(paymentMethods.get(paymentMethodId.trim()));
    }
    
    @Override
    public List<PaymentMethod> getPaymentMethodsByCustomer(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> paymentMethodIds = customerPaymentMethods.get(customerId.trim());
        if (paymentMethodIds == null) {
            return new ArrayList<>();
        }
        
        return paymentMethodIds.stream()
                .map(paymentMethods::get)
                .filter(Objects::nonNull)
                .filter(PaymentMethod::isActive)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean updatePaymentMethodStatus(String paymentMethodId, PaymentMethodStatus status) {
        if (paymentMethodId == null || status == null) {
            return false;
        }
        
        PaymentMethod paymentMethod = paymentMethods.get(paymentMethodId.trim());
        if (paymentMethod != null) {
            PaymentMethod updated = paymentMethod.withStatus(status);
            paymentMethods.put(paymentMethodId.trim(), updated);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean deletePaymentMethod(String paymentMethodId) {
        if (paymentMethodId == null) {
            return false;
        }
        
        PaymentMethod paymentMethod = paymentMethods.get(paymentMethodId.trim());
        if (paymentMethod != null) {
            // Remove from customer's payment methods
            List<String> customerMethods = customerPaymentMethods.get(paymentMethod.getCustomerId());
            if (customerMethods != null) {
                customerMethods.remove(paymentMethodId.trim());
            }
            
            // Remove the payment method
            paymentMethods.remove(paymentMethodId.trim());
            return true;
        }
        return false;
    }
    
    @Override
    public boolean setDefaultPaymentMethod(String customerId, String paymentMethodId) {
        if (customerId == null || paymentMethodId == null) {
            return false;
        }
        
        PaymentMethod targetMethod = paymentMethods.get(paymentMethodId.trim());
        if (targetMethod == null || !targetMethod.getCustomerId().equals(customerId.trim())) {
            return false;
        }
        
        // Update all payment methods for this customer to not be default
        List<String> customerMethods = customerPaymentMethods.get(customerId.trim());
        if (customerMethods != null) {
            for (String methodId : customerMethods) {
                PaymentMethod method = paymentMethods.get(methodId);
                if (method != null && method.isDefault()) {
                    // Create new instance with isDefault = false
                    PaymentMethod updated = new PaymentMethod(method.getPaymentMethodId(), method.getCustomerId(),
                                                            method.getType(), method.getMaskedNumber(), method.getExpiryMonth(),
                                                            method.getExpiryYear(), method.getCardHolderName(),
                                                            method.getBillingAddress(), false);
                    paymentMethods.put(methodId, updated);
                }
            }
        }
        
        // Set the target method as default
        PaymentMethod defaultMethod = new PaymentMethod(targetMethod.getPaymentMethodId(), targetMethod.getCustomerId(),
                                                       targetMethod.getType(), targetMethod.getMaskedNumber(),
                                                       targetMethod.getExpiryMonth(), targetMethod.getExpiryYear(),
                                                       targetMethod.getCardHolderName(), targetMethod.getBillingAddress(), true);
        paymentMethods.put(paymentMethodId.trim(), defaultMethod);
        
        return true;
    }
    
    // Transaction Processing
    @Override
    public Transaction processPayment(String paymentMethodId, String merchantId, String orderId,
                                    BigDecimal amount, String currency, String description) {
        if (paymentMethodId == null || merchantId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid payment parameters");
        }
        
        PaymentMethod paymentMethod = paymentMethods.get(paymentMethodId.trim());
        if (paymentMethod == null || !paymentMethod.isActive()) {
            throw new IllegalArgumentException("Payment method not found or inactive");
        }
        
        if (paymentMethod.isExpired()) {
            throw new IllegalArgumentException("Payment method is expired");
        }
        
        // Check for fraud
        if (isFraudulent(paymentMethodId, amount, merchantId)) {
            throw new IllegalArgumentException("Transaction flagged as fraudulent");
        }
        
        // Generate transaction ID
        String transactionId = "TXN" + String.format("%08d", transactionIdCounter.getAndIncrement());
        
        // Create transaction
        Transaction transaction = new Transaction(transactionId, paymentMethodId, merchantId, orderId,
                                                amount, currency, TransactionType.PAYMENT, description);
        
        // Process through payment gateway
        boolean success = paymentGateway.processPayment(transaction);
        
        if (success) {
            transaction.updateStatus(TransactionStatus.COMPLETED);
            transaction.setGatewayTransactionId("GTW" + transactionId);
            transaction.setAuthorizationCode("AUTH" + System.currentTimeMillis());
            transaction.setProcessingFee(calculateProcessingFee(amount));
        } else {
            transaction.updateStatus(TransactionStatus.FAILED);
            transaction.setFailureReason("Payment gateway declined the transaction");
        }
        
        transactions.put(transactionId, transaction);
        
        // Send webhook notification
        sendWebhookNotification(merchantId, "payment.processed", transaction);
        
        return transaction;
    }
    
    @Override
    public Transaction authorizePayment(String paymentMethodId, String merchantId, String orderId,
                                      BigDecimal amount, String currency, String description) {
        if (paymentMethodId == null || merchantId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid authorization parameters");
        }
        
        PaymentMethod paymentMethod = paymentMethods.get(paymentMethodId.trim());
        if (paymentMethod == null || !paymentMethod.isActive()) {
            throw new IllegalArgumentException("Payment method not found or inactive");
        }
        
        // Generate transaction ID
        String transactionId = "TXN" + String.format("%08d", transactionIdCounter.getAndIncrement());
        
        // Create authorization transaction
        Transaction transaction = new Transaction(transactionId, paymentMethodId, merchantId, orderId,
                                                amount, currency, TransactionType.AUTHORIZATION, description);
        
        // Authorize through payment gateway
        boolean success = paymentGateway.authorizePayment(transaction);
        
        if (success) {
            transaction.updateStatus(TransactionStatus.AUTHORIZED);
            transaction.setGatewayTransactionId("GTW" + transactionId);
            transaction.setAuthorizationCode("AUTH" + System.currentTimeMillis());
        } else {
            transaction.updateStatus(TransactionStatus.FAILED);
            transaction.setFailureReason("Authorization failed");
        }
        
        transactions.put(transactionId, transaction);
        
        return transaction;
    }
    
    @Override
    public Transaction capturePayment(String authorizationId, BigDecimal amount) {
        if (authorizationId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid capture parameters");
        }
        
        Transaction authTransaction = transactions.get(authorizationId.trim());
        if (authTransaction == null || authTransaction.getStatus() != TransactionStatus.AUTHORIZED) {
            throw new IllegalArgumentException("Authorization not found or not in authorized state");
        }
        
        if (amount.compareTo(authTransaction.getAmount()) > 0) {
            throw new IllegalArgumentException("Capture amount cannot exceed authorized amount");
        }
        
        // Generate capture transaction ID
        String captureId = "TXN" + String.format("%08d", transactionIdCounter.getAndIncrement());
        
        // Create capture transaction
        Transaction captureTransaction = new Transaction(captureId, authTransaction.getPaymentMethodId(),
                                                       authTransaction.getMerchantId(), authTransaction.getOrderId(),
                                                       amount, authTransaction.getCurrency(), TransactionType.CAPTURE,
                                                       "Capture for authorization " + authorizationId);
        
        // Process capture
        boolean success = paymentGateway.capturePayment(captureTransaction);
        
        if (success) {
            captureTransaction.updateStatus(TransactionStatus.COMPLETED);
            captureTransaction.setGatewayTransactionId("GTW" + captureId);
            captureTransaction.setProcessingFee(calculateProcessingFee(amount));
            
            // Update original authorization only if full amount is captured
            if (amount.equals(authTransaction.getAmount())) {
                authTransaction.updateStatus(TransactionStatus.COMPLETED);
            }
        } else {
            captureTransaction.updateStatus(TransactionStatus.FAILED);
            captureTransaction.setFailureReason("Capture failed");
        }
        
        transactions.put(captureId, captureTransaction);
        
        return captureTransaction;
    }  
  
    @Override
    public Transaction refundPayment(String transactionId, BigDecimal amount, String reason) {
        if (transactionId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid refund parameters");
        }
        
        Transaction originalTransaction = transactions.get(transactionId.trim());
        if (originalTransaction == null || !originalTransaction.canRefund()) {
            throw new IllegalArgumentException("Original transaction not found or cannot be refunded");
        }
        
        if (amount.compareTo(originalTransaction.getAmount()) > 0) {
            throw new IllegalArgumentException("Refund amount cannot exceed original transaction amount");
        }
        
        // Generate refund transaction ID
        String refundId = "TXN" + String.format("%08d", transactionIdCounter.getAndIncrement());
        
        // Create refund transaction
        Transaction refundTransaction = new Transaction(refundId, originalTransaction.getPaymentMethodId(),
                                                      originalTransaction.getMerchantId(), originalTransaction.getOrderId(),
                                                      amount, originalTransaction.getCurrency(), TransactionType.REFUND,
                                                      reason != null ? reason : "Refund for transaction " + transactionId);
        
        // Process refund
        boolean success = paymentGateway.refundPayment(refundTransaction);
        
        if (success) {
            refundTransaction.updateStatus(TransactionStatus.COMPLETED);
            refundTransaction.setGatewayTransactionId("GTW" + refundId);
            
            // Update original transaction status if full refund
            if (amount.equals(originalTransaction.getAmount())) {
                originalTransaction.updateStatus(TransactionStatus.REFUNDED);
            }
        } else {
            refundTransaction.updateStatus(TransactionStatus.FAILED);
            refundTransaction.setFailureReason("Refund failed");
        }
        
        transactions.put(refundId, refundTransaction);
        
        // Send webhook notification
        sendWebhookNotification(originalTransaction.getMerchantId(), "payment.refunded", refundTransaction);
        
        return refundTransaction;
    }
    
    @Override
    public Transaction voidTransaction(String transactionId, String reason) {
        if (transactionId == null) {
            throw new IllegalArgumentException("Transaction ID is required");
        }
        
        Transaction originalTransaction = transactions.get(transactionId.trim());
        if (originalTransaction == null || !originalTransaction.canCancel()) {
            throw new IllegalArgumentException("Transaction not found or cannot be voided");
        }
        
        // Generate void transaction ID
        String voidId = "TXN" + String.format("%08d", transactionIdCounter.getAndIncrement());
        
        // Create void transaction
        Transaction voidTransaction = new Transaction(voidId, originalTransaction.getPaymentMethodId(),
                                                    originalTransaction.getMerchantId(), originalTransaction.getOrderId(),
                                                    originalTransaction.getAmount(), originalTransaction.getCurrency(),
                                                    TransactionType.VOID, reason != null ? reason : "Void transaction " + transactionId);
        
        // Process void
        boolean success = paymentGateway.voidTransaction(voidTransaction);
        
        if (success) {
            voidTransaction.updateStatus(TransactionStatus.COMPLETED);
            voidTransaction.setGatewayTransactionId("GTW" + voidId);
            
            // Update original transaction
            originalTransaction.updateStatus(TransactionStatus.CANCELLED);
        } else {
            voidTransaction.updateStatus(TransactionStatus.FAILED);
            voidTransaction.setFailureReason("Void failed");
        }
        
        transactions.put(voidId, voidTransaction);
        
        return voidTransaction;
    }
    
    // Transaction Management
    @Override
    public Optional<Transaction> getTransaction(String transactionId) {
        if (transactionId == null || transactionId.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(transactions.get(transactionId.trim()));
    }
    
    @Override
    public List<Transaction> getTransactionsByPaymentMethod(String paymentMethodId) {
        if (paymentMethodId == null || paymentMethodId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        return transactions.values().stream()
                .filter(transaction -> transaction.getPaymentMethodId().equals(paymentMethodId.trim()))
                .sorted(Comparator.comparing(Transaction::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Transaction> getTransactionsByMerchant(String merchantId) {
        if (merchantId == null || merchantId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        return transactions.values().stream()
                .filter(transaction -> transaction.getMerchantId().equals(merchantId.trim()))
                .sorted(Comparator.comparing(Transaction::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Transaction> getTransactionsByOrder(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        return transactions.values().stream()
                .filter(transaction -> transaction.getOrderId().equals(orderId.trim()))
                .sorted(Comparator.comparing(Transaction::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Transaction> getTransactionsByStatus(TransactionStatus status) {
        if (status == null) {
            return new ArrayList<>();
        }
        
        return transactions.values().stream()
                .filter(transaction -> transaction.getStatus() == status)
                .sorted(Comparator.comparing(Transaction::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }
    
    // Fraud Detection
    @Override
    public boolean isFraudulent(String paymentMethodId, BigDecimal amount, String merchantId) {
        return fraudDetectionEngine.isFraudulent(paymentMethodId, amount, merchantId, transactions);
    }
    
    @Override
    public void reportFraud(String transactionId, String reason) {
        Transaction transaction = transactions.get(transactionId);
        if (transaction != null) {
            transaction.updateStatus(TransactionStatus.DISPUTED);
            transaction.setFailureReason("Reported as fraud: " + reason);
        }
    }
    
    // Dispute Management
    @Override
    public Transaction createDispute(String transactionId, String reason, BigDecimal disputeAmount) {
        if (transactionId == null || reason == null) {
            throw new IllegalArgumentException("Transaction ID and reason are required");
        }
        
        Transaction originalTransaction = transactions.get(transactionId.trim());
        if (originalTransaction == null || originalTransaction.getStatus() != TransactionStatus.COMPLETED) {
            throw new IllegalArgumentException("Original transaction not found or not completed");
        }
        
        // Generate dispute transaction ID
        String disputeId = "TXN" + String.format("%08d", transactionIdCounter.getAndIncrement());
        
        BigDecimal amount = disputeAmount != null ? disputeAmount : originalTransaction.getAmount();
        
        // Create dispute transaction
        Transaction disputeTransaction = new Transaction(disputeId, originalTransaction.getPaymentMethodId(),
                                                       originalTransaction.getMerchantId(), originalTransaction.getOrderId(),
                                                       amount, originalTransaction.getCurrency(), TransactionType.DISPUTE, reason);
        
        disputeTransaction.updateStatus(TransactionStatus.DISPUTED);
        
        // Update original transaction
        originalTransaction.updateStatus(TransactionStatus.DISPUTED);
        
        transactions.put(disputeId, disputeTransaction);
        
        return disputeTransaction;
    }
    
    @Override
    public boolean resolveDispute(String disputeId, boolean inFavorOfCustomer, String resolution) {
        Transaction disputeTransaction = transactions.get(disputeId);
        if (disputeTransaction == null || disputeTransaction.getType() != TransactionType.DISPUTE) {
            return false;
        }
        
        if (inFavorOfCustomer) {
            disputeTransaction.updateStatus(TransactionStatus.REFUNDED);
        } else {
            disputeTransaction.updateStatus(TransactionStatus.SETTLED);
        }
        
        return true;
    }
    
    // Statistics and Reporting
    @Override
    public PaymentStats getPaymentStats() {
        int totalTransactions = transactions.size();
        int successfulTransactions = (int) transactions.values().stream()
                .filter(Transaction::isSuccessful).count();
        int failedTransactions = (int) transactions.values().stream()
                .filter(Transaction::isFailed).count();
        int refundedTransactions = (int) transactions.values().stream()
                .filter(t -> t.getStatus() == TransactionStatus.REFUNDED).count();
        int disputedTransactions = (int) transactions.values().stream()
                .filter(t -> t.getStatus() == TransactionStatus.DISPUTED).count();
        
        BigDecimal totalVolume = transactions.values().stream()
                .filter(Transaction::isSuccessful)
                .filter(t -> t.getType() == TransactionType.PAYMENT)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalFees = transactions.values().stream()
                .filter(Transaction::isSuccessful)
                .map(Transaction::getProcessingFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalRefunds = transactions.values().stream()
                .filter(t -> t.getType() == TransactionType.REFUND && t.isSuccessful())
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int totalPaymentMethods = paymentMethods.size();
        int activePaymentMethods = (int) paymentMethods.values().stream()
                .filter(PaymentMethod::isActive).count();
        
        return new PaymentStats(totalTransactions, successfulTransactions, failedTransactions,
                              refundedTransactions, disputedTransactions, totalVolume, totalFees,
                              totalRefunds, totalPaymentMethods, activePaymentMethods);
    }
    
    @Override
    public MerchantStats getMerchantStats(String merchantId) {
        if (merchantId == null) {
            return null;
        }
        
        List<Transaction> merchantTransactions = getTransactionsByMerchant(merchantId);
        
        int totalTransactions = merchantTransactions.size();
        int successfulTransactions = (int) merchantTransactions.stream()
                .filter(Transaction::isSuccessful).count();
        int failedTransactions = (int) merchantTransactions.stream()
                .filter(Transaction::isFailed).count();
        
        BigDecimal totalVolume = merchantTransactions.stream()
                .filter(Transaction::isSuccessful)
                .filter(t -> t.getType() == TransactionType.PAYMENT)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalFees = merchantTransactions.stream()
                .filter(Transaction::isSuccessful)
                .map(Transaction::getProcessingFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal averageTransactionAmount = successfulTransactions > 0 ?
                totalVolume.divide(BigDecimal.valueOf(successfulTransactions), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;
        
        LocalDateTime firstTransactionDate = merchantTransactions.stream()
                .map(Transaction::getCreatedAt)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        
        LocalDateTime lastTransactionDate = merchantTransactions.stream()
                .map(Transaction::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        
        return new MerchantStats(merchantId, totalTransactions, successfulTransactions, failedTransactions,
                               totalVolume, totalFees, averageTransactionAmount, firstTransactionDate,
                               lastTransactionDate);
    }
    
    @Override
    public PaymentMethodStats getPaymentMethodStats(String paymentMethodId) {
        if (paymentMethodId == null) {
            return null;
        }
        
        List<Transaction> methodTransactions = getTransactionsByPaymentMethod(paymentMethodId);
        
        int totalTransactions = methodTransactions.size();
        int successfulTransactions = (int) methodTransactions.stream()
                .filter(Transaction::isSuccessful).count();
        int failedTransactions = (int) methodTransactions.stream()
                .filter(Transaction::isFailed).count();
        
        BigDecimal totalAmount = methodTransactions.stream()
                .filter(Transaction::isSuccessful)
                .filter(t -> t.getType() == TransactionType.PAYMENT)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        LocalDateTime firstUsed = methodTransactions.stream()
                .map(Transaction::getCreatedAt)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        
        LocalDateTime lastUsed = methodTransactions.stream()
                .map(Transaction::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        
        return new PaymentMethodStats(paymentMethodId, totalTransactions, successfulTransactions,
                                    failedTransactions, totalAmount, firstUsed, lastUsed);
    }
    
    // Webhook and Notifications
    @Override
    public void registerWebhook(String merchantId, String webhookUrl, List<String> events) {
        if (merchantId != null && webhookUrl != null) {
            webhooks.put(merchantId.trim(), webhookUrl.trim());
        }
    }
    
    @Override
    public void sendWebhookNotification(String merchantId, String event, Transaction transaction) {
        String webhookUrl = webhooks.get(merchantId);
        if (webhookUrl != null) {
            // In a real implementation, this would make an HTTP POST request
            System.out.println("Webhook notification sent to " + webhookUrl + 
                             " for event: " + event + ", transaction: " + transaction.getTransactionId());
        }
    }
    
    // Helper methods
    private boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            return false;
        }
        
        String cleanNumber = cardNumber.replaceAll("\\s+", "");
        return cleanNumber.matches("\\d{13,19}"); // Basic validation
    }
    
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        
        String cleanNumber = cardNumber.replaceAll("\\s+", "");
        String lastFour = cleanNumber.substring(cleanNumber.length() - 4);
        return "**** **** **** " + lastFour;
    }
    
    private BigDecimal calculateProcessingFee(BigDecimal amount) {
        // 2.9% + $0.30 processing fee
        BigDecimal percentageFee = amount.multiply(new BigDecimal("0.029"));
        BigDecimal fixedFee = new BigDecimal("0.30");
        return percentageFee.add(fixedFee).setScale(2, RoundingMode.HALF_UP);
    }
    
    // Inner classes for payment processing
    private static class FraudDetectionEngine {
        public boolean isFraudulent(String paymentMethodId, BigDecimal amount, String merchantId,
                                  Map<String, Transaction> transactions) {
            // Simple fraud detection rules
            
            // Check for unusually large amounts
            if (amount.compareTo(new BigDecimal("10000")) > 0) {
                return true;
            }
            
            // Check for too many transactions in short time
            long recentTransactions = transactions.values().stream()
                    .filter(t -> t.getPaymentMethodId().equals(paymentMethodId))
                    .filter(t -> t.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(10)))
                    .count();
            
            if (recentTransactions > 5) {
                return true;
            }
            
            // Check for high failure rate
            List<Transaction> methodTransactions = transactions.values().stream()
                    .filter(t -> t.getPaymentMethodId().equals(paymentMethodId))
                    .filter(t -> t.getCreatedAt().isAfter(LocalDateTime.now().minusHours(1)))
                    .collect(Collectors.toList());
            
            if (methodTransactions.size() > 3) {
                long failedCount = methodTransactions.stream()
                        .filter(Transaction::isFailed)
                        .count();
                
                double failureRate = (double) failedCount / methodTransactions.size();
                if (failureRate > 0.5) {
                    return true;
                }
            }
            
            return false;
        }
    }
    
    private static class PaymentGateway {
        public boolean processPayment(Transaction transaction) {
            // Simulate payment processing
            // 95% success rate
            return Math.random() > 0.05;
        }
        
        public boolean authorizePayment(Transaction transaction) {
            // Simulate authorization
            // 98% success rate
            return Math.random() > 0.02;
        }
        
        public boolean capturePayment(Transaction transaction) {
            // Simulate capture
            // 99% success rate
            return Math.random() > 0.01;
        }
        
        public boolean refundPayment(Transaction transaction) {
            // Simulate refund
            // 97% success rate
            return Math.random() > 0.03;
        }
        
        public boolean voidTransaction(Transaction transaction) {
            // Simulate void
            // 99% success rate
            return Math.random() > 0.01;
        }
    }
}