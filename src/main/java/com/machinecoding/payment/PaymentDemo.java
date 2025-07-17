package com.machinecoding.payment;

import com.machinecoding.payment.model.*;
import com.machinecoding.payment.service.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Comprehensive demonstration of the Payment Processing System.
 * Shows payment method management, transaction processing, and fraud detection.
 */
public class PaymentDemo {
    
    public static void main(String[] args) {
        System.out.println("=== Payment Processing System Demo ===\n");
        
        // Demo 1: Payment Method Management
        System.out.println("=== Demo 1: Payment Method Management ===");
        demonstratePaymentMethodManagement();
        
        // Demo 2: Transaction Processing
        System.out.println("\n=== Demo 2: Transaction Processing ===");
        demonstrateTransactionProcessing();
        
        // Demo 3: Authorization and Capture
        System.out.println("\n=== Demo 3: Authorization and Capture ===");
        demonstrateAuthorizationCapture();
        
        // Demo 4: Refunds and Voids
        System.out.println("\n=== Demo 4: Refunds and Voids ===");
        demonstrateRefundsAndVoids();
        
        // Demo 5: Fraud Detection
        System.out.println("\n=== Demo 5: Fraud Detection ===");
        demonstrateFraudDetection();
        
        // Demo 6: Statistics and Reporting
        System.out.println("\n=== Demo 6: Statistics and Reporting ===");
        demonstrateStatistics();
        
        System.out.println("\n=== Demo Complete ===");
    }
    
    private static void demonstratePaymentMethodManagement() {
        System.out.println("1. Creating payment service:");
        PaymentService paymentService = new InMemoryPaymentService();
        
        System.out.println("\n2. Adding payment methods:");
        
        // Add credit cards for different customers
        PaymentMethod creditCard1 = paymentService.addPaymentMethod(
            "CUST001", PaymentMethodType.CREDIT_CARD, "4111111111111111",
            "12", "2025", "John Doe", "123 Main St, New York, NY", true
        );
        System.out.println("   Added: " + creditCard1);
        
        PaymentMethod debitCard = paymentService.addPaymentMethod(
            "CUST001", PaymentMethodType.DEBIT_CARD, "5555555555554444",
            "06", "2024", "John Doe", "123 Main St, New York, NY", false
        );
        System.out.println("   Added: " + debitCard);
        
        PaymentMethod digitalWallet = paymentService.addPaymentMethod(
            "CUST002", PaymentMethodType.DIGITAL_WALLET, "6011111111111117",
            "03", "2026", "Jane Smith", "456 Oak Ave, Boston, MA", true
        );
        System.out.println("   Added: " + digitalWallet);
        
        System.out.println("\n3. Retrieving payment methods:");
        
        // Get payment methods by customer
        List<PaymentMethod> customer1Methods = paymentService.getPaymentMethodsByCustomer("CUST001");
        System.out.println("   Customer CUST001 has " + customer1Methods.size() + " payment methods:");
        for (PaymentMethod method : customer1Methods) {
            System.out.println("     - " + method.getType() + ": " + method.getMaskedNumber() + 
                             " (Default: " + method.isDefault() + ")");
        }
        
        List<PaymentMethod> customer2Methods = paymentService.getPaymentMethodsByCustomer("CUST002");
        System.out.println("   Customer CUST002 has " + customer2Methods.size() + " payment methods:");
        for (PaymentMethod method : customer2Methods) {
            System.out.println("     - " + method.getType() + ": " + method.getMaskedNumber() + 
                             " (Default: " + method.isDefault() + ")");
        }
        
        System.out.println("\n4. Setting default payment method:");
        
        boolean defaultSet = paymentService.setDefaultPaymentMethod("CUST001", debitCard.getPaymentMethodId());
        System.out.println("   Set debit card as default: " + defaultSet);
        
        // Verify default changed
        customer1Methods = paymentService.getPaymentMethodsByCustomer("CUST001");
        for (PaymentMethod method : customer1Methods) {
            System.out.println("     - " + method.getType() + " (Default: " + method.isDefault() + ")");
        }
    }
    
    private static void demonstrateTransactionProcessing() {
        PaymentService paymentService = new InMemoryPaymentService();
        setupPaymentMethods(paymentService);
        
        System.out.println("1. Processing payments:");
        
        // Get payment methods for testing
        List<PaymentMethod> customer1Methods = paymentService.getPaymentMethodsByCustomer("CUST001");
        String paymentMethodId = customer1Methods.get(0).getPaymentMethodId();
        
        // Process different types of payments
        Transaction payment1 = paymentService.processPayment(
            paymentMethodId, "MERCHANT001", "ORDER001",
            new BigDecimal("99.99"), "USD", "Product purchase"
        );
        System.out.println("   Payment 1: " + payment1);
        System.out.println("   Status: " + payment1.getStatus());
        System.out.println("   Processing fee: $" + payment1.getProcessingFee());
        
        Transaction payment2 = paymentService.processPayment(
            paymentMethodId, "MERCHANT002", "ORDER002",
            new BigDecimal("249.50"), "USD", "Service subscription"
        );
        System.out.println("   Payment 2: " + payment2);
        System.out.println("   Status: " + payment2.getStatus());
        
        Transaction payment3 = paymentService.processPayment(
            paymentMethodId, "MERCHANT001", "ORDER003",
            new BigDecimal("1599.99"), "USD", "Electronics purchase"
        );
        System.out.println("   Payment 3: " + payment3);
        System.out.println("   Status: " + payment3.getStatus());
        
        System.out.println("\n2. Transaction lookup:");
        
        // Get transactions by payment method
        List<Transaction> methodTransactions = paymentService.getTransactionsByPaymentMethod(paymentMethodId);
        System.out.println("   Transactions for payment method " + paymentMethodId + ": " + methodTransactions.size());
        
        // Get transactions by merchant
        List<Transaction> merchantTransactions = paymentService.getTransactionsByMerchant("MERCHANT001");
        System.out.println("   Transactions for MERCHANT001: " + merchantTransactions.size());
        
        // Get transactions by order
        List<Transaction> orderTransactions = paymentService.getTransactionsByOrder("ORDER001");
        System.out.println("   Transactions for ORDER001: " + orderTransactions.size());
        
        // Get transactions by status
        List<Transaction> completedTransactions = paymentService.getTransactionsByStatus(TransactionStatus.COMPLETED);
        System.out.println("   Completed transactions: " + completedTransactions.size());
    }
    
    private static void demonstrateAuthorizationCapture() {
        PaymentService paymentService = new InMemoryPaymentService();
        setupPaymentMethods(paymentService);
        
        System.out.println("1. Authorization and capture flow:");
        
        List<PaymentMethod> customer1Methods = paymentService.getPaymentMethodsByCustomer("CUST001");
        String paymentMethodId = customer1Methods.get(0).getPaymentMethodId();
        
        // Step 1: Authorize payment
        Transaction authorization = paymentService.authorizePayment(
            paymentMethodId, "MERCHANT001", "ORDER004",
            new BigDecimal("500.00"), "USD", "Hotel reservation"
        );
        System.out.println("   Authorization: " + authorization);
        System.out.println("   Status: " + authorization.getStatus());
        System.out.println("   Auth code: " + authorization.getAuthorizationCode());
        
        // Step 2: Capture partial amount
        Transaction capture1 = paymentService.capturePayment(
            authorization.getTransactionId(), new BigDecimal("300.00")
        );
        System.out.println("   Partial capture: " + capture1);
        System.out.println("   Status: " + capture1.getStatus());
        System.out.println("   Captured amount: $" + capture1.getAmount());
        
        // Note: In this demo, we'll show that the authorization remains available for additional captures
        // In a real system, you'd track captured amounts vs authorized amounts
        System.out.println("   Authorization status after partial capture: " + 
                         paymentService.getTransaction(authorization.getTransactionId()).get().getStatus());
        
        System.out.println("\n2. Authorization without capture (expires):");
        
        Transaction expiredAuth = paymentService.authorizePayment(
            paymentMethodId, "MERCHANT002", "ORDER005",
            new BigDecimal("150.00"), "USD", "Temporary hold"
        );
        System.out.println("   Authorization: " + expiredAuth);
        
        // Void the authorization instead of capturing
        Transaction voidTransaction = paymentService.voidTransaction(
            expiredAuth.getTransactionId(), "Customer cancelled order"
        );
        System.out.println("   Voided authorization: " + voidTransaction);
        System.out.println("   Original status: " + paymentService.getTransaction(expiredAuth.getTransactionId()).get().getStatus());
    }
    
    private static void demonstrateRefundsAndVoids() {
        PaymentService paymentService = new InMemoryPaymentService();
        setupPaymentMethods(paymentService);
        
        System.out.println("1. Processing refunds:");
        
        List<PaymentMethod> customer1Methods = paymentService.getPaymentMethodsByCustomer("CUST001");
        String paymentMethodId = customer1Methods.get(0).getPaymentMethodId();
        
        // Create a payment to refund
        Transaction originalPayment = paymentService.processPayment(
            paymentMethodId, "MERCHANT001", "ORDER006",
            new BigDecimal("199.99"), "USD", "Product purchase"
        );
        System.out.println("   Original payment: " + originalPayment);
        
        // Full refund
        Transaction fullRefund = paymentService.refundPayment(
            originalPayment.getTransactionId(), originalPayment.getAmount(), "Customer returned product"
        );
        System.out.println("   Full refund: " + fullRefund);
        System.out.println("   Refund status: " + fullRefund.getStatus());
        System.out.println("   Original payment status: " + paymentService.getTransaction(originalPayment.getTransactionId()).get().getStatus());
        
        // Partial refund example
        Transaction anotherPayment = paymentService.processPayment(
            paymentMethodId, "MERCHANT001", "ORDER007",
            new BigDecimal("299.99"), "USD", "Multiple items purchase"
        );
        System.out.println("   Another payment: " + anotherPayment);
        
        Transaction partialRefund = paymentService.refundPayment(
            anotherPayment.getTransactionId(), new BigDecimal("50.00"), "Partial return"
        );
        System.out.println("   Partial refund: " + partialRefund);
        System.out.println("   Partial refund status: " + partialRefund.getStatus());
        
        System.out.println("\n2. Transaction voids:");
        
        // Create authorization to void
        Transaction authToVoid = paymentService.authorizePayment(
            paymentMethodId, "MERCHANT002", "ORDER008",
            new BigDecimal("75.00"), "USD", "Pending order"
        );
        System.out.println("   Authorization to void: " + authToVoid);
        
        // Only void if authorization was successful
        if (authToVoid.getStatus() == TransactionStatus.AUTHORIZED) {
            Transaction voidAuth = paymentService.voidTransaction(
                authToVoid.getTransactionId(), "Order cancelled by customer"
            );
            System.out.println("   Void transaction: " + voidAuth);
            System.out.println("   Original auth status: " + paymentService.getTransaction(authToVoid.getTransactionId()).get().getStatus());
        } else {
            System.out.println("   Authorization failed, cannot void: " + authToVoid.getStatus());
        }
    }
    
    private static void demonstrateFraudDetection() {
        PaymentService paymentService = new InMemoryPaymentService();
        setupPaymentMethods(paymentService);
        
        System.out.println("1. Normal transactions (should pass):");
        
        List<PaymentMethod> customer1Methods = paymentService.getPaymentMethodsByCustomer("CUST001");
        String paymentMethodId = customer1Methods.get(0).getPaymentMethodId();
        
        // Normal transaction
        boolean isFraud1 = paymentService.isFraudulent(paymentMethodId, new BigDecimal("50.00"), "MERCHANT001");
        System.out.println("   $50 transaction fraud check: " + isFraud1);
        
        boolean isFraud2 = paymentService.isFraudulent(paymentMethodId, new BigDecimal("500.00"), "MERCHANT001");
        System.out.println("   $500 transaction fraud check: " + isFraud2);
        
        System.out.println("\n2. Suspicious transactions (should be flagged):");
        
        // Large amount transaction
        boolean isFraud3 = paymentService.isFraudulent(paymentMethodId, new BigDecimal("15000.00"), "MERCHANT001");
        System.out.println("   $15,000 transaction fraud check: " + isFraud3);
        
        System.out.println("\n3. Creating multiple transactions to trigger fraud detection:");
        
        // Create multiple transactions quickly
        for (int i = 0; i < 6; i++) {
            try {
                Transaction tx = paymentService.processPayment(
                    paymentMethodId, "MERCHANT001", "ORDER" + (100 + i),
                    new BigDecimal("25.00"), "USD", "Rapid transaction " + i
                );
                System.out.println("   Transaction " + i + ": " + tx.getStatus());
            } catch (Exception e) {
                System.out.println("   Transaction " + i + " blocked: " + e.getMessage());
            }
        }
        
        System.out.println("\n4. Fraud reporting:");
        
        // Use a different payment method to avoid fraud detection from previous transactions
        List<PaymentMethod> customer2Methods = paymentService.getPaymentMethodsByCustomer("CUST002");
        String cleanPaymentMethodId = customer2Methods.get(0).getPaymentMethodId();
        
        // Create a transaction and report it as fraud
        Transaction suspiciousTransaction = paymentService.processPayment(
            cleanPaymentMethodId, "MERCHANT003", "ORDER200",
            new BigDecimal("100.00"), "USD", "Suspicious purchase"
        );
        System.out.println("   Suspicious transaction: " + suspiciousTransaction);
        
        // Report as fraud
        paymentService.reportFraud(suspiciousTransaction.getTransactionId(), "Unauthorized use of card");
        Transaction reportedTransaction = paymentService.getTransaction(suspiciousTransaction.getTransactionId()).get();
        System.out.println("   After fraud report - Status: " + reportedTransaction.getStatus());
        System.out.println("   Failure reason: " + reportedTransaction.getFailureReason());
    }
    
    private static void demonstrateStatistics() {
        PaymentService paymentService = new InMemoryPaymentService();
        setupPaymentMethods(paymentService);
        createSampleTransactions(paymentService);
        
        System.out.println("1. Overall payment statistics:");
        
        PaymentStats stats = paymentService.getPaymentStats();
        System.out.println("   " + stats);
        
        System.out.println("\n2. Detailed statistics:");
        System.out.println("   Total transactions: " + stats.getTotalTransactions());
        System.out.println("   Successful transactions: " + stats.getSuccessfulTransactions());
        System.out.println("   Failed transactions: " + stats.getFailedTransactions());
        System.out.println("   Success rate: " + String.format("%.1f%%", stats.getSuccessRate()));
        System.out.println("   Failure rate: " + String.format("%.1f%%", stats.getFailureRate()));
        System.out.println("   Refund rate: " + String.format("%.1f%%", stats.getRefundRate()));
        System.out.println("   Dispute rate: " + String.format("%.1f%%", stats.getDisputeRate()));
        System.out.println("   Total volume: $" + stats.getTotalVolume());
        System.out.println("   Total fees: $" + stats.getTotalFees());
        System.out.println("   Net revenue: $" + stats.getNetRevenue());
        System.out.println("   Average transaction: $" + stats.getAverageTransactionAmount());
        System.out.println("   Total payment methods: " + stats.getTotalPaymentMethods());
        System.out.println("   Active payment methods: " + stats.getActivePaymentMethods());
        
        System.out.println("\n3. Merchant statistics:");
        
        MerchantStats merchantStats = paymentService.getMerchantStats("MERCHANT001");
        if (merchantStats != null) {
            System.out.println("   " + merchantStats);
            System.out.println("   Success rate: " + String.format("%.1f%%", merchantStats.getSuccessRate()));
            System.out.println("   Net revenue: $" + merchantStats.getNetRevenue());
            System.out.println("   Is active merchant: " + merchantStats.isActiveMerchant());
        }
        
        System.out.println("\n4. Payment method statistics:");
        
        List<PaymentMethod> customer1Methods = paymentService.getPaymentMethodsByCustomer("CUST001");
        if (!customer1Methods.isEmpty()) {
            String paymentMethodId = customer1Methods.get(0).getPaymentMethodId();
            PaymentMethodStats methodStats = paymentService.getPaymentMethodStats(paymentMethodId);
            if (methodStats != null) {
                System.out.println("   " + methodStats);
                System.out.println("   Success rate: " + String.format("%.1f%%", methodStats.getSuccessRate()));
                System.out.println("   Average transaction: $" + methodStats.getAverageTransactionAmount());
                System.out.println("   Is frequently used: " + methodStats.isFrequentlyUsed());
                System.out.println("   Is recently used: " + methodStats.isRecentlyUsed());
            }
        }
    }
    
    // Helper methods
    private static void setupPaymentMethods(PaymentService service) {
        // Add payment methods for testing
        service.addPaymentMethod("CUST001", PaymentMethodType.CREDIT_CARD, "4111111111111111",
                               "12", "2025", "John Doe", "123 Main St, New York, NY", true);
        service.addPaymentMethod("CUST001", PaymentMethodType.DEBIT_CARD, "5555555555554444",
                               "06", "2024", "John Doe", "123 Main St, New York, NY", false);
        service.addPaymentMethod("CUST002", PaymentMethodType.DIGITAL_WALLET, "6011111111111117",
                               "03", "2026", "Jane Smith", "456 Oak Ave, Boston, MA", true);
    }
    
    private static void createSampleTransactions(PaymentService service) {
        List<PaymentMethod> customer1Methods = service.getPaymentMethodsByCustomer("CUST001");
        List<PaymentMethod> customer2Methods = service.getPaymentMethodsByCustomer("CUST002");
        
        if (!customer1Methods.isEmpty()) {
            String paymentMethodId1 = customer1Methods.get(0).getPaymentMethodId();
            
            // Create various transactions
            service.processPayment(paymentMethodId1, "MERCHANT001", "ORDER001",
                                 new BigDecimal("99.99"), "USD", "Product A");
            service.processPayment(paymentMethodId1, "MERCHANT001", "ORDER002",
                                 new BigDecimal("149.50"), "USD", "Product B");
            service.processPayment(paymentMethodId1, "MERCHANT002", "ORDER003",
                                 new BigDecimal("299.99"), "USD", "Service subscription");
            
            // Create a transaction and refund it
            Transaction refundableTransaction = service.processPayment(paymentMethodId1, "MERCHANT001", "ORDER004",
                                                                     new BigDecimal("75.00"), "USD", "Returnable item");
            service.refundPayment(refundableTransaction.getTransactionId(), new BigDecimal("75.00"), "Customer return");
        }
        
        if (!customer2Methods.isEmpty()) {
            String paymentMethodId2 = customer2Methods.get(0).getPaymentMethodId();
            
            service.processPayment(paymentMethodId2, "MERCHANT002", "ORDER005",
                                 new BigDecimal("199.99"), "USD", "Digital service");
            service.processPayment(paymentMethodId2, "MERCHANT003", "ORDER006",
                                 new BigDecimal("49.99"), "USD", "Monthly subscription");
        }
    }
}