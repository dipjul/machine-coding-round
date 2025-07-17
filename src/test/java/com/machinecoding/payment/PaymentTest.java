package com.machinecoding.payment;

import com.machinecoding.payment.model.*;
import com.machinecoding.payment.service.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Comprehensive unit tests for the Payment Processing System.
 * Tests payment methods, transactions, fraud detection, and statistics.
 */
public class PaymentTest {
    
    public static void main(String[] args) {
        System.out.println("=== Payment System Unit Tests ===\n");
        
        runAllTests();
        
        System.out.println("\n=== All Tests Complete ===");
    }
    
    private static void runAllTests() {
        testPaymentMethodManagement();
        testTransactionProcessing();
        testAuthorizationCapture();
        testRefundsAndVoids();
        testFraudDetection();
        testStatistics();
        testEdgeCases();
        testBusinessLogic();
        testConcurrency();
        testValidation();
    }
    
    private static void testPaymentMethodManagement() {
        System.out.println("Test 1: Payment Method Management");
        
        try {
            PaymentService service = new InMemoryPaymentService();
            
            // Test adding payment method
            PaymentMethod creditCard = service.addPaymentMethod(
                "CUST001", PaymentMethodType.CREDIT_CARD, "4111111111111111",
                "12", "2025", "John Doe", "123 Main St", true
            );
            assert creditCard != null : "Payment method should be created";
            assert creditCard.getType() == PaymentMethodType.CREDIT_CARD : "Type should be credit card";
            assert creditCard.getMaskedNumber().endsWith("1111") : "Should mask card number";
            assert creditCard.isDefault() : "Should be set as default";
            
            // Test retrieving payment method
            Optional<PaymentMethod> found = service.getPaymentMethod(creditCard.getPaymentMethodId());
            assert found.isPresent() : "Payment method should be found";
            assert found.get().getCardHolderName().equals("John Doe") : "Card holder name should match";
            
            // Test getting payment methods by customer
            List<PaymentMethod> customerMethods = service.getPaymentMethodsByCustomer("CUST001");
            assert customerMethods.size() == 1 : "Should have 1 payment method";
            assert customerMethods.get(0).isDefault() : "Should be default method";
            
            // Test adding second payment method
            PaymentMethod debitCard = service.addPaymentMethod(
                "CUST001", PaymentMethodType.DEBIT_CARD, "5555555555554444",
                "06", "2024", "John Doe", "123 Main St", false
            );
            
            customerMethods = service.getPaymentMethodsByCustomer("CUST001");
            assert customerMethods.size() == 2 : "Should have 2 payment methods";
            
            // Test setting default payment method
            boolean defaultSet = service.setDefaultPaymentMethod("CUST001", debitCard.getPaymentMethodId());
            assert defaultSet : "Should set default payment method";
            
            // Verify default changed
            customerMethods = service.getPaymentMethodsByCustomer("CUST001");
            long defaultCount = customerMethods.stream().filter(PaymentMethod::isDefault).count();
            assert defaultCount == 1 : "Should have exactly 1 default payment method";
            
            // Test updating payment method status
            boolean statusUpdated = service.updatePaymentMethodStatus(
                creditCard.getPaymentMethodId(), PaymentMethodStatus.INACTIVE
            );
            assert statusUpdated : "Should update payment method status";
            
            // Test deleting payment method
            boolean deleted = service.deletePaymentMethod(debitCard.getPaymentMethodId());
            assert deleted : "Should delete payment method";
            
            customerMethods = service.getPaymentMethodsByCustomer("CUST001");
            assert customerMethods.size() == 0 : "Should have 0 active payment methods after deletion";
            
            System.out.println("   ✓ Payment method management tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Payment method management test failed: " + e.getMessage());
        }
    }
    
    private static void testTransactionProcessing() {
        System.out.println("\nTest 2: Transaction Processing");
        
        try {
            PaymentService service = new InMemoryPaymentService();
            
            // Setup payment method
            PaymentMethod paymentMethod = service.addPaymentMethod(
                "CUST001", PaymentMethodType.CREDIT_CARD, "4111111111111111",
                "12", "2025", "John Doe", "123 Main St", true
            );
            
            // Test processing payment
            Transaction payment = service.processPayment(
                paymentMethod.getPaymentMethodId(), "MERCHANT001", "ORDER001",
                new BigDecimal("99.99"), "USD", "Test purchase"
            );
            
            assert payment != null : "Payment should be processed";
            assert payment.getAmount().equals(new BigDecimal("99.99")) : "Amount should match";
            assert payment.getType() == TransactionType.PAYMENT : "Type should be payment";
            assert payment.getMerchantId().equals("MERCHANT001") : "Merchant should match";
            assert payment.getOrderId().equals("ORDER001") : "Order should match";
            assert payment.getProcessingFee().compareTo(BigDecimal.ZERO) > 0 : "Should have processing fee";
            
            // Test transaction retrieval
            Optional<Transaction> foundTransaction = service.getTransaction(payment.getTransactionId());
            assert foundTransaction.isPresent() : "Transaction should be found";
            assert foundTransaction.get().getDescription().equals("Test purchase") : "Description should match";
            
            // Test getting transactions by payment method
            List<Transaction> methodTransactions = service.getTransactionsByPaymentMethod(
                paymentMethod.getPaymentMethodId()
            );
            assert methodTransactions.size() == 1 : "Should have 1 transaction for payment method";
            
            // Test getting transactions by merchant
            List<Transaction> merchantTransactions = service.getTransactionsByMerchant("MERCHANT001");
            assert merchantTransactions.size() == 1 : "Should have 1 transaction for merchant";
            
            // Test getting transactions by order
            List<Transaction> orderTransactions = service.getTransactionsByOrder("ORDER001");
            assert orderTransactions.size() == 1 : "Should have 1 transaction for order";
            
            // Test getting transactions by status
            List<Transaction> completedTransactions = service.getTransactionsByStatus(TransactionStatus.COMPLETED);
            assert completedTransactions.size() >= 1 : "Should have at least 1 completed transaction";
            
            System.out.println("   ✓ Transaction processing tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Transaction processing test failed: " + e.getMessage());
        }
    }
    
    private static void testAuthorizationCapture() {
        System.out.println("\nTest 3: Authorization and Capture");
        
        try {
            PaymentService service = new InMemoryPaymentService();
            
            // Setup payment method
            PaymentMethod paymentMethod = service.addPaymentMethod(
                "CUST001", PaymentMethodType.CREDIT_CARD, "4111111111111111",
                "12", "2025", "John Doe", "123 Main St", true
            );
            
            // Test authorization
            Transaction authorization = service.authorizePayment(
                paymentMethod.getPaymentMethodId(), "MERCHANT001", "ORDER001",
                new BigDecimal("500.00"), "USD", "Hotel reservation"
            );
            
            assert authorization != null : "Authorization should be created";
            assert authorization.getType() == TransactionType.AUTHORIZATION : "Type should be authorization";
            
            // Only test capture if authorization was successful
            if (authorization.getStatus() == TransactionStatus.AUTHORIZED) {
                assert authorization.getAuthorizationCode() != null : "Should have authorization code";
                
                // Test capture
                Transaction capture = service.capturePayment(
                    authorization.getTransactionId(), new BigDecimal("300.00")
                );
                
                assert capture != null : "Capture should be created";
                assert capture.getType() == TransactionType.CAPTURE : "Type should be capture";
                assert capture.getAmount().equals(new BigDecimal("300.00")) : "Capture amount should match";
                
                if (capture.getStatus() == TransactionStatus.COMPLETED) {
                    assert capture.getProcessingFee().compareTo(BigDecimal.ZERO) > 0 : "Should have processing fee";
                }
            }
            
            System.out.println("   ✓ Authorization and capture tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Authorization and capture test failed: " + e.getMessage());
        }
    }
    
    private static void testRefundsAndVoids() {
        System.out.println("\nTest 4: Refunds and Voids");
        
        try {
            PaymentService service = new InMemoryPaymentService();
            
            // Setup payment method
            PaymentMethod paymentMethod = service.addPaymentMethod(
                "CUST001", PaymentMethodType.CREDIT_CARD, "4111111111111111",
                "12", "2025", "John Doe", "123 Main St", true
            );
            
            // Test refund
            Transaction payment = service.processPayment(
                paymentMethod.getPaymentMethodId(), "MERCHANT001", "ORDER001",
                new BigDecimal("199.99"), "USD", "Refundable purchase"
            );
            
            if (payment.getStatus() == TransactionStatus.COMPLETED) {
                Transaction refund = service.refundPayment(
                    payment.getTransactionId(), new BigDecimal("199.99"), "Customer return"
                );
                
                assert refund != null : "Refund should be created";
                assert refund.getType() == TransactionType.REFUND : "Type should be refund";
                assert refund.getAmount().equals(new BigDecimal("199.99")) : "Refund amount should match";
                
                if (refund.getStatus() == TransactionStatus.COMPLETED) {
                    // Check original transaction status
                    Transaction originalTransaction = service.getTransaction(payment.getTransactionId()).get();
                    assert originalTransaction.getStatus() == TransactionStatus.REFUNDED : "Original should be refunded";
                }
            }
            
            // Test void
            Transaction authorization = service.authorizePayment(
                paymentMethod.getPaymentMethodId(), "MERCHANT001", "ORDER002",
                new BigDecimal("100.00"), "USD", "Voidable auth"
            );
            
            if (authorization.getStatus() == TransactionStatus.AUTHORIZED) {
                Transaction voidTransaction = service.voidTransaction(
                    authorization.getTransactionId(), "Order cancelled"
                );
                
                assert voidTransaction != null : "Void should be created";
                assert voidTransaction.getType() == TransactionType.VOID : "Type should be void";
                
                if (voidTransaction.getStatus() == TransactionStatus.COMPLETED) {
                    // Check original authorization status
                    Transaction originalAuth = service.getTransaction(authorization.getTransactionId()).get();
                    assert originalAuth.getStatus() == TransactionStatus.CANCELLED : "Original should be cancelled";
                }
            }
            
            System.out.println("   ✓ Refunds and voids tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Refunds and voids test failed: " + e.getMessage());
        }
    }
    
    private static void testFraudDetection() {
        System.out.println("\nTest 5: Fraud Detection");
        
        try {
            PaymentService service = new InMemoryPaymentService();
            
            // Setup payment method
            PaymentMethod paymentMethod = service.addPaymentMethod(
                "CUST001", PaymentMethodType.CREDIT_CARD, "4111111111111111",
                "12", "2025", "John Doe", "123 Main St", true
            );
            
            String paymentMethodId = paymentMethod.getPaymentMethodId();
            
            // Test normal transaction (should not be fraudulent)
            boolean isFraud1 = service.isFraudulent(paymentMethodId, new BigDecimal("50.00"), "MERCHANT001");
            assert !isFraud1 : "Normal transaction should not be flagged as fraud";
            
            // Test large amount (should be fraudulent)
            boolean isFraud2 = service.isFraudulent(paymentMethodId, new BigDecimal("15000.00"), "MERCHANT001");
            assert isFraud2 : "Large amount should be flagged as fraud";
            
            // Test fraud reporting
            Transaction transaction = service.processPayment(
                paymentMethodId, "MERCHANT001", "ORDER001",
                new BigDecimal("100.00"), "USD", "Test transaction"
            );
            
            if (transaction.getStatus() == TransactionStatus.COMPLETED) {
                service.reportFraud(transaction.getTransactionId(), "Unauthorized transaction");
                
                Transaction reportedTransaction = service.getTransaction(transaction.getTransactionId()).get();
                assert reportedTransaction.getStatus() == TransactionStatus.DISPUTED : "Should be marked as disputed";
                assert reportedTransaction.getFailureReason().contains("fraud") : "Should have fraud reason";
            }
            
            System.out.println("   ✓ Fraud detection tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Fraud detection test failed: " + e.getMessage());
        }
    }
    
    private static void testStatistics() {
        System.out.println("\nTest 6: Statistics");
        
        try {
            PaymentService service = new InMemoryPaymentService();
            setupTestData(service);
            
            // Test overall statistics
            PaymentStats stats = service.getPaymentStats();
            assert stats.getTotalTransactions() > 0 : "Should have transactions";
            assert stats.getTotalVolume().compareTo(BigDecimal.ZERO) > 0 : "Should have volume";
            assert stats.getSuccessRate() >= 0 && stats.getSuccessRate() <= 100 : "Success rate should be valid";
            assert stats.getAverageTransactionAmount().compareTo(BigDecimal.ZERO) >= 0 : "Average should be non-negative";
            
            // Test merchant statistics
            MerchantStats merchantStats = service.getMerchantStats("MERCHANT001");
            assert merchantStats != null : "Merchant stats should exist";
            assert merchantStats.getTotalTransactions() > 0 : "Merchant should have transactions";
            assert merchantStats.getSuccessRate() >= 0 && merchantStats.getSuccessRate() <= 100 : "Success rate should be valid";
            
            // Test payment method statistics
            List<PaymentMethod> methods = service.getPaymentMethodsByCustomer("CUST001");
            if (!methods.isEmpty()) {
                PaymentMethodStats methodStats = service.getPaymentMethodStats(methods.get(0).getPaymentMethodId());
                assert methodStats != null : "Payment method stats should exist";
                assert methodStats.getTotalTransactions() >= 0 : "Should have valid transaction count";
            }
            
            System.out.println("   ✓ Statistics tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Statistics test failed: " + e.getMessage());
        }
    }
    
    private static void testEdgeCases() {
        System.out.println("\nTest 7: Edge Cases");
        
        try {
            PaymentService service = new InMemoryPaymentService();
            
            // Test invalid card number
            try {
                service.addPaymentMethod("CUST001", PaymentMethodType.CREDIT_CARD, "invalid",
                                       "12", "2025", "John Doe", "123 Main St", true);
                assert false : "Should reject invalid card number";
            } catch (IllegalArgumentException e) {
                // Expected
            }
            
            // Test processing payment with non-existent payment method
            try {
                service.processPayment("NONEXISTENT", "MERCHANT001", "ORDER001",
                                     new BigDecimal("100.00"), "USD", "Test");
                assert false : "Should reject non-existent payment method";
            } catch (IllegalArgumentException e) {
                // Expected
            }
            
            // Test negative amount
            PaymentMethod paymentMethod = service.addPaymentMethod(
                "CUST001", PaymentMethodType.CREDIT_CARD, "4111111111111111",
                "12", "2025", "John Doe", "123 Main St", true
            );
            
            try {
                service.processPayment(paymentMethod.getPaymentMethodId(), "MERCHANT001", "ORDER001",
                                     new BigDecimal("-100.00"), "USD", "Test");
                assert false : "Should reject negative amount";
            } catch (IllegalArgumentException e) {
                // Expected
            }
            
            // Test refunding more than original amount
            Transaction payment = service.processPayment(
                paymentMethod.getPaymentMethodId(), "MERCHANT001", "ORDER001",
                new BigDecimal("50.00"), "USD", "Test payment"
            );
            
            if (payment.getStatus() == TransactionStatus.COMPLETED) {
                try {
                    service.refundPayment(payment.getTransactionId(), new BigDecimal("100.00"), "Test refund");
                    assert false : "Should reject refund amount greater than original";
                } catch (IllegalArgumentException e) {
                    // Expected
                }
            }
            
            // Test operations on non-existent transactions
            assert service.getTransaction("NONEXISTENT").isEmpty() : "Should return empty for non-existent transaction";
            
            System.out.println("   ✓ Edge cases tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Edge cases test failed: " + e.getMessage());
        }
    }
    
    private static void testBusinessLogic() {
        System.out.println("\nTest 8: Business Logic");
        
        try {
            PaymentService service = new InMemoryPaymentService();
            
            // Test processing fee calculation
            PaymentMethod paymentMethod = service.addPaymentMethod(
                "CUST001", PaymentMethodType.CREDIT_CARD, "4111111111111111",
                "12", "2025", "John Doe", "123 Main St", true
            );
            
            Transaction payment = service.processPayment(
                paymentMethod.getPaymentMethodId(), "MERCHANT001", "ORDER001",
                new BigDecimal("100.00"), "USD", "Test payment"
            );
            
            if (payment.getStatus() == TransactionStatus.COMPLETED) {
                // Processing fee should be 2.9% + $0.30
                BigDecimal expectedFee = new BigDecimal("100.00").multiply(new BigDecimal("0.029")).add(new BigDecimal("0.30"));
                assert payment.getProcessingFee().compareTo(expectedFee.setScale(2, BigDecimal.ROUND_HALF_UP)) == 0 : 
                       "Processing fee should be calculated correctly";
                
                BigDecimal expectedNet = payment.getAmount().subtract(payment.getProcessingFee());
                assert payment.getNetAmount().equals(expectedNet) : "Net amount should be calculated correctly";
            }
            
            // Test card expiration
            PaymentMethod expiredCard = service.addPaymentMethod(
                "CUST002", PaymentMethodType.CREDIT_CARD, "4111111111111111",
                "01", "2020", "Jane Doe", "456 Oak St", true
            );
            
            assert expiredCard.isExpired() : "Card should be detected as expired";
            
            // Test transaction status transitions
            Transaction auth = service.authorizePayment(
                paymentMethod.getPaymentMethodId(), "MERCHANT001", "ORDER002",
                new BigDecimal("200.00"), "USD", "Auth test"
            );
            
            if (auth.getStatus() == TransactionStatus.AUTHORIZED) {
                assert auth.canCancel() : "Authorized transaction should be cancellable";
                assert !auth.canRefund() : "Authorized transaction should not be refundable";
            }
            
            System.out.println("   ✓ Business logic tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Business logic test failed: " + e.getMessage());
        }
    }
    
    private static void testConcurrency() {
        System.out.println("\nTest 9: Concurrency");
        
        try {
            PaymentService service = new InMemoryPaymentService();
            
            // Setup payment method
            PaymentMethod paymentMethod = service.addPaymentMethod(
                "CUST001", PaymentMethodType.CREDIT_CARD, "4111111111111111",
                "12", "2025", "John Doe", "123 Main St", true
            );
            
            // Test concurrent payment processing
            Thread[] threads = new Thread[5];
            for (int i = 0; i < 5; i++) {
                final int threadId = i;
                threads[i] = new Thread(() -> {
                    try {
                        service.processPayment(
                            paymentMethod.getPaymentMethodId(), "MERCHANT001", "ORDER" + threadId,
                            new BigDecimal("50.00"), "USD", "Concurrent payment " + threadId
                        );
                    } catch (Exception e) {
                        // Some may fail due to fraud detection, which is expected
                    }
                });
            }
            
            for (Thread thread : threads) {
                thread.start();
            }
            
            for (Thread thread : threads) {
                thread.join();
            }
            
            // Verify some transactions were processed
            List<Transaction> transactions = service.getTransactionsByPaymentMethod(paymentMethod.getPaymentMethodId());
            assert transactions.size() > 0 : "Should have processed some transactions";
            
            System.out.println("   ✓ Concurrency tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Concurrency test failed: " + e.getMessage());
        }
    }
    
    private static void testValidation() {
        System.out.println("\nTest 10: Validation");
        
        try {
            PaymentService service = new InMemoryPaymentService();
            
            // Test null parameter validation
            try {
                service.addPaymentMethod(null, PaymentMethodType.CREDIT_CARD, "4111111111111111",
                                       "12", "2025", "John Doe", "123 Main St", true);
                assert false : "Should reject null customer ID";
            } catch (IllegalArgumentException e) {
                // Expected
            }
            
            try {
                service.processPayment("PM001", null, "ORDER001",
                                     new BigDecimal("100.00"), "USD", "Test");
                assert false : "Should reject null merchant ID";
            } catch (IllegalArgumentException e) {
                // Expected
            }
            
            // Test empty string validation
            try {
                service.addPaymentMethod("", PaymentMethodType.CREDIT_CARD, "4111111111111111",
                                       "12", "2025", "John Doe", "123 Main St", true);
                assert false : "Should reject empty customer ID";
            } catch (IllegalArgumentException e) {
                // Expected
            }
            
            // Test card number validation
            try {
                service.addPaymentMethod("CUST001", PaymentMethodType.CREDIT_CARD, "123",
                                       "12", "2025", "John Doe", "123 Main St", true);
                assert false : "Should reject short card number";
            } catch (IllegalArgumentException e) {
                // Expected
            }
            
            System.out.println("   ✓ Validation tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Validation test failed: " + e.getMessage());
        }
    }
    
    // Helper methods
    private static void setupTestData(PaymentService service) {
        // Add payment methods
        PaymentMethod pm1 = service.addPaymentMethod("CUST001", PaymentMethodType.CREDIT_CARD, "4111111111111111",
                                                    "12", "2025", "John Doe", "123 Main St", true);
        PaymentMethod pm2 = service.addPaymentMethod("CUST002", PaymentMethodType.DEBIT_CARD, "5555555555554444",
                                                    "06", "2024", "Jane Smith", "456 Oak Ave", true);
        
        // Create transactions
        try {
            service.processPayment(pm1.getPaymentMethodId(), "MERCHANT001", "ORDER001",
                                 new BigDecimal("99.99"), "USD", "Product A");
            service.processPayment(pm1.getPaymentMethodId(), "MERCHANT001", "ORDER002",
                                 new BigDecimal("149.50"), "USD", "Product B");
            service.processPayment(pm2.getPaymentMethodId(), "MERCHANT002", "ORDER003",
                                 new BigDecimal("75.00"), "USD", "Service");
        } catch (Exception e) {
            // Some transactions may fail due to simulated gateway, which is fine for testing
        }
    }
}