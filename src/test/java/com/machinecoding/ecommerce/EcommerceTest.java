package com.machinecoding.ecommerce;

import com.machinecoding.ecommerce.model.*;
import com.machinecoding.ecommerce.service.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Comprehensive unit tests for the E-commerce Order Management System.
 * Tests product management, inventory, shopping cart, and order processing.
 */
public class EcommerceTest {
    
    public static void main(String[] args) {
        System.out.println("=== E-commerce System Unit Tests ===\n");
        
        runAllTests();
        
        System.out.println("\n=== All Tests Complete ===");
    }
    
    private static void runAllTests() {
        testProductManagement();
        testCustomerManagement();
        testInventoryManagement();
        testShoppingCartOperations();
        testOrderCreation();
        testOrderLifecycle();
        testStatistics();
        testEdgeCases();
        testConcurrency();
        testBusinessLogic();
    }
    
    private static void testProductManagement() {
        System.out.println("Test 1: Product Management");
        
        try {
            EcommerceService service = new InMemoryEcommerceService();
            
            // Test product creation and addition
            Product product = createTestProduct("P001", "Test Product", new BigDecimal("99.99"));
            service.addProduct(product);
            
            // Test product retrieval
            Optional<Product> foundProduct = service.getProduct("P001");
            assert foundProduct.isPresent() : "Product should be found";
            assert foundProduct.get().getName().equals("Test Product") : "Product name should match";
            assert foundProduct.get().getPrice().equals(new BigDecimal("99.99")) : "Product price should match";
            
            // Test product search by category
            service.addProduct(createTestProduct("P002", "Electronics Item", new BigDecimal("199.99")));
            List<Product> electronicsProducts = service.getProductsByCategory("Electronics");
            assert electronicsProducts.size() == 2 : "Should find 2 electronics products";
            
            // Test product search by brand
            List<Product> testBrandProducts = service.getProductsByBrand("TestBrand");
            assert testBrandProducts.size() == 2 : "Should find 2 TestBrand products";
            
            // Test product search
            List<Product> searchResults = service.searchProducts("Test");
            assert searchResults.size() == 2 : "Should find 2 products with 'Test' in name";
            
            // Test price update
            boolean priceUpdated = service.updateProductPrice("P001", new BigDecimal("89.99"));
            assert priceUpdated : "Price should be updated";
            
            Optional<Product> updatedProduct = service.getProduct("P001");
            assert updatedProduct.get().getPrice().equals(new BigDecimal("89.99")) : "Price should be updated";
            
            // Test status update
            boolean statusUpdated = service.updateProductStatus("P001", ProductStatus.INACTIVE);
            assert statusUpdated : "Status should be updated";
            
            System.out.println("   ✓ Product management tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Product management test failed: " + e.getMessage());
        }
    }
    
    private static void testCustomerManagement() {
        System.out.println("\nTest 2: Customer Management");
        
        try {
            EcommerceService service = new InMemoryEcommerceService();
            
            // Test customer registration
            Customer customer = createTestCustomer("C001", "John", "Doe", "john@test.com");
            service.registerCustomer(customer);
            
            // Test customer retrieval
            Optional<Customer> foundCustomer = service.getCustomer("C001");
            assert foundCustomer.isPresent() : "Customer should be found";
            assert foundCustomer.get().getFullName().equals("John Doe") : "Customer name should match";
            assert foundCustomer.get().getEmail().equals("john@test.com") : "Customer email should match";
            
            // Test customer search by email
            List<Customer> customersByEmail = service.findCustomersByEmail("john@test.com");
            assert customersByEmail.size() == 1 : "Should find 1 customer by email";
            
            // Test customer status update
            boolean statusUpdated = service.updateCustomerStatus("C001", CustomerStatus.INACTIVE);
            assert statusUpdated : "Customer status should be updated";
            
            Optional<Customer> updatedCustomer = service.getCustomer("C001");
            assert updatedCustomer.get().getStatus() == CustomerStatus.INACTIVE : "Customer status should be INACTIVE";
            
            System.out.println("   ✓ Customer management tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Customer management test failed: " + e.getMessage());
        }
    }
    
    private static void testInventoryManagement() {
        System.out.println("\nTest 3: Inventory Management");
        
        try {
            EcommerceService service = new InMemoryEcommerceService();
            
            // Add product first
            service.addProduct(createTestProduct("P001", "Test Product", new BigDecimal("99.99")));
            
            // Test inventory addition
            Inventory inventory = new Inventory("P001", 100, 0, 10, 500);
            service.addInventory(inventory);
            
            // Test inventory retrieval
            Optional<Inventory> foundInventory = service.getInventory("P001");
            assert foundInventory.isPresent() : "Inventory should be found";
            assert foundInventory.get().getAvailableQuantity() == 100 : "Available quantity should be 100";
            
            // Test inventory reservation
            boolean reserved = service.reserveInventory("P001", 20);
            assert reserved : "Inventory should be reserved";
            
            Optional<Inventory> updatedInventory = service.getInventory("P001");
            assert updatedInventory.get().getAvailableQuantity() == 80 : "Available quantity should be 80";
            assert updatedInventory.get().getReservedQuantity() == 20 : "Reserved quantity should be 20";
            
            // Test inventory release
            boolean released = service.releaseInventory("P001", 10);
            assert released : "Inventory should be released";
            
            updatedInventory = service.getInventory("P001");
            assert updatedInventory.get().getAvailableQuantity() == 90 : "Available quantity should be 90";
            assert updatedInventory.get().getReservedQuantity() == 10 : "Reserved quantity should be 10";
            
            // Test inventory confirmation
            boolean confirmed = service.confirmInventory("P001", 10);
            assert confirmed : "Inventory should be confirmed";
            
            updatedInventory = service.getInventory("P001");
            assert updatedInventory.get().getReservedQuantity() == 0 : "Reserved quantity should be 0";
            
            // Test low stock detection
            service.addInventory(new Inventory("P002", 5, 0, 10, 100)); // Below reorder level
            List<Inventory> lowStockProducts = service.getLowStockProducts();
            assert lowStockProducts.size() == 1 : "Should find 1 low stock product";
            
            System.out.println("   ✓ Inventory management tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Inventory management test failed: " + e.getMessage());
        }
    }
    
    private static void testShoppingCartOperations() {
        System.out.println("\nTest 4: Shopping Cart Operations");
        
        try {
            EcommerceService service = new InMemoryEcommerceService();
            setupTestData(service);
            
            String customerId = "C001";
            
            // Test adding items to cart
            boolean added1 = service.addToCart(customerId, "P001", 2);
            assert added1 : "Should add item to cart";
            
            boolean added2 = service.addToCart(customerId, "P002", 1);
            assert added2 : "Should add second item to cart";
            
            // Test cart contents
            List<CartItem> cartItems = service.getCartItems(customerId);
            assert cartItems.size() == 2 : "Cart should have 2 items";
            
            // Test cart total
            BigDecimal cartTotal = service.getCartTotal(customerId);
            assert cartTotal.compareTo(BigDecimal.ZERO) > 0 : "Cart total should be positive";
            
            // Test updating cart item
            boolean updated = service.updateCartItem(customerId, "P001", 3);
            assert updated : "Should update cart item quantity";
            
            cartItems = service.getCartItems(customerId);
            Optional<CartItem> updatedItem = cartItems.stream()
                    .filter(item -> item.getProductId().equals("P001"))
                    .findFirst();
            assert updatedItem.isPresent() && updatedItem.get().getQuantity() == 3 : "Item quantity should be updated to 3";
            
            // Test removing item from cart
            boolean removed = service.removeFromCart(customerId, "P002");
            assert removed : "Should remove item from cart";
            
            cartItems = service.getCartItems(customerId);
            assert cartItems.size() == 1 : "Cart should have 1 item after removal";
            
            // Test clearing cart
            boolean cleared = service.clearCart(customerId);
            assert cleared : "Should clear cart";
            
            cartItems = service.getCartItems(customerId);
            assert cartItems.isEmpty() : "Cart should be empty after clearing";
            
            System.out.println("   ✓ Shopping cart operations tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Shopping cart operations test failed: " + e.getMessage());
        }
    }
    
    private static void testOrderCreation() {
        System.out.println("\nTest 5: Order Creation");
        
        try {
            EcommerceService service = new InMemoryEcommerceService();
            setupTestData(service);
            
            String customerId = "C001";
            
            // Add items to cart
            service.addToCart(customerId, "P001", 1);
            service.addToCart(customerId, "P002", 2);
            
            // Create order
            Address shippingAddress = new Address("123 Test St", "Test City", "TC", "12345", "USA", AddressType.SHIPPING);
            Order order = service.createOrder(customerId, shippingAddress, null, "PAYMENT_123");
            
            assert order != null : "Order should be created";
            assert order.getCustomerId().equals(customerId) : "Order customer should match";
            assert order.getItems().size() == 2 : "Order should have 2 items";
            assert order.getStatus() == OrderStatus.PENDING : "Order status should be PENDING";
            assert order.getTotalAmount().compareTo(BigDecimal.ZERO) > 0 : "Order total should be positive";
            
            // Verify cart is cleared
            List<CartItem> cartItems = service.getCartItems(customerId);
            assert cartItems.isEmpty() : "Cart should be empty after order creation";
            
            // Verify inventory is reserved
            Optional<Inventory> inventory1 = service.getInventory("P001");
            Optional<Inventory> inventory2 = service.getInventory("P002");
            assert inventory1.get().getReservedQuantity() == 1 : "P001 should have 1 reserved";
            assert inventory2.get().getReservedQuantity() == 2 : "P002 should have 2 reserved";
            
            System.out.println("   ✓ Order creation tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Order creation test failed: " + e.getMessage());
        }
    }
    
    private static void testOrderLifecycle() {
        System.out.println("\nTest 6: Order Lifecycle");
        
        try {
            EcommerceService service = new InMemoryEcommerceService();
            setupTestData(service);
            
            // Create order
            String customerId = "C001";
            service.addToCart(customerId, "P001", 1);
            Address address = new Address("123 Test St", "Test City", "TC", "12345", "USA", AddressType.SHIPPING);
            Order order = service.createOrder(customerId, address, null, "PAYMENT_123");
            String orderId = order.getOrderId();
            
            // Test status transitions
            boolean confirmed = service.updateOrderStatus(orderId, OrderStatus.CONFIRMED);
            assert confirmed : "Order should be confirmed";
            assert service.getOrder(orderId).get().getStatus() == OrderStatus.CONFIRMED : "Status should be CONFIRMED";
            
            boolean processing = service.updateOrderStatus(orderId, OrderStatus.PROCESSING);
            assert processing : "Order should be processing";
            
            boolean shipped = service.shipOrder(orderId, "TRACK123");
            assert shipped : "Order should be shipped";
            assert service.getOrder(orderId).get().getTrackingNumber().equals("TRACK123") : "Tracking number should be set";
            
            boolean delivered = service.updateOrderStatus(orderId, OrderStatus.DELIVERED);
            assert delivered : "Order should be delivered";
            
            // Test order cancellation
            service.addToCart(customerId, "P002", 1);
            Order cancelOrder = service.createOrder(customerId, address, null, "PAYMENT_456");
            
            boolean cancelled = service.cancelOrder(cancelOrder.getOrderId());
            assert cancelled : "Order should be cancelled";
            assert service.getOrder(cancelOrder.getOrderId()).get().getStatus() == OrderStatus.CANCELLED : "Status should be CANCELLED";
            
            // Verify inventory is released for cancelled order
            Optional<Inventory> inventory = service.getInventory("P002");
            assert inventory.get().getReservedQuantity() == 0 : "Reserved quantity should be 0 after cancellation";
            
            System.out.println("   ✓ Order lifecycle tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Order lifecycle test failed: " + e.getMessage());
        }
    }
    
    private static void testStatistics() {
        System.out.println("\nTest 7: Statistics");
        
        try {
            EcommerceService service = new InMemoryEcommerceService();
            setupTestData(service);
            createTestOrders(service);
            
            // Test overall statistics
            EcommerceStats stats = service.getStats();
            assert stats.getTotalProducts() > 0 : "Should have products";
            assert stats.getTotalCustomers() > 0 : "Should have customers";
            assert stats.getTotalOrders() > 0 : "Should have orders";
            assert stats.getTotalRevenue().compareTo(BigDecimal.ZERO) > 0 : "Should have revenue";
            
            // Test customer statistics
            CustomerStats customerStats = service.getCustomerStats("C001");
            assert customerStats != null : "Customer stats should exist";
            assert customerStats.getTotalOrders() > 0 : "Customer should have orders";
            
            // Test product statistics
            ProductStats productStats = service.getProductStats("P001");
            assert productStats != null : "Product stats should exist";
            assert productStats.getCurrentStock() >= 0 : "Product should have valid stock";
            
            System.out.println("   ✓ Statistics tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Statistics test failed: " + e.getMessage());
        }
    }
    
    private static void testEdgeCases() {
        System.out.println("\nTest 8: Edge Cases");
        
        try {
            EcommerceService service = new InMemoryEcommerceService();
            setupTestData(service);
            
            // Test adding to cart with insufficient inventory
            boolean addedTooMany = service.addToCart("C001", "P001", 1000);
            assert !addedTooMany : "Should not add more than available inventory";
            
            // Test creating order with empty cart
            try {
                Address address = new Address("123 Test St", "Test City", "TC", "12345", "USA", AddressType.SHIPPING);
                service.createOrder("C001", address, null, "PAYMENT_123");
                assert false : "Should throw exception for empty cart";
            } catch (IllegalArgumentException e) {
                // Expected
            }
            
            // Test invalid status transitions
            service.addToCart("C001", "P001", 1);
            Address address = new Address("123 Test St", "Test City", "TC", "12345", "USA", AddressType.SHIPPING);
            Order order = service.createOrder("C001", address, null, "PAYMENT_123");
            
            boolean invalidTransition = service.updateOrderStatus(order.getOrderId(), OrderStatus.DELIVERED);
            assert !invalidTransition : "Should not allow invalid status transition";
            
            // Test operations on non-existent entities
            assert service.getProduct("NONEXISTENT").isEmpty() : "Should return empty for non-existent product";
            assert service.getCustomer("NONEXISTENT").isEmpty() : "Should return empty for non-existent customer";
            assert service.getOrder("NONEXISTENT").isEmpty() : "Should return empty for non-existent order";
            
            System.out.println("   ✓ Edge cases tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Edge cases test failed: " + e.getMessage());
        }
    }
    
    private static void testConcurrency() {
        System.out.println("\nTest 9: Concurrency");
        
        try {
            EcommerceService service = new InMemoryEcommerceService();
            setupTestData(service);
            
            // Test concurrent inventory operations
            Thread[] threads = new Thread[5];
            for (int i = 0; i < 5; i++) {
                final int threadId = i;
                threads[i] = new Thread(() -> {
                    service.reserveInventory("P001", 1);
                    service.addToCart("C00" + (threadId + 1), "P001", 1);
                });
            }
            
            for (Thread thread : threads) {
                thread.start();
            }
            
            for (Thread thread : threads) {
                thread.join();
            }
            
            // Verify inventory consistency
            Optional<Inventory> inventory = service.getInventory("P001");
            assert inventory.isPresent() : "Inventory should exist";
            assert inventory.get().getTotalQuantity() <= 100 : "Total quantity should not exceed initial amount";
            
            System.out.println("   ✓ Concurrency tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Concurrency test failed: " + e.getMessage());
        }
    }
    
    private static void testBusinessLogic() {
        System.out.println("\nTest 10: Business Logic");
        
        try {
            EcommerceService service = new InMemoryEcommerceService();
            setupTestData(service);
            
            // Test free shipping threshold
            service.addToCart("C001", "P001", 1); // $99.99 - should have shipping
            Address address = new Address("123 Test St", "Test City", "TC", "12345", "USA", AddressType.SHIPPING);
            Order order1 = service.createOrder("C001", address, null, "PAYMENT_1");
            assert order1.getShippingCost().compareTo(BigDecimal.ZERO) > 0 : "Should have shipping cost for small order";
            
            // Test tax calculation
            assert order1.getTaxAmount().compareTo(BigDecimal.ZERO) > 0 : "Should have tax amount";
            
            // Test order total calculation
            BigDecimal expectedTotal = order1.getSubtotal().add(order1.getTaxAmount()).add(order1.getShippingCost());
            assert order1.getTotalAmount().equals(expectedTotal) : "Total should equal subtotal + tax + shipping";
            
            System.out.println("   ✓ Business logic tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Business logic test failed: " + e.getMessage());
        }
    }
    
    // Helper methods
    private static Product createTestProduct(String id, String name, BigDecimal price) {
        return new Product(id, name, "Test description", price, "Electronics", "TestBrand", "SKU-" + id, 1.0, ProductStatus.ACTIVE);
    }
    
    private static Customer createTestCustomer(String id, String firstName, String lastName, String email) {
        Address address = new Address("123 Test St", "Test City", "TC", "12345", "USA", AddressType.BOTH);
        return new Customer(id, firstName, lastName, email, "+1-555-0123", address, CustomerStatus.ACTIVE);
    }
    
    private static void setupTestData(EcommerceService service) {
        // Add products
        service.addProduct(createTestProduct("P001", "Product 1", new BigDecimal("99.99")));
        service.addProduct(createTestProduct("P002", "Product 2", new BigDecimal("149.99")));
        service.addProduct(createTestProduct("P003", "Product 3", new BigDecimal("199.99")));
        
        // Add customers
        service.registerCustomer(createTestCustomer("C001", "John", "Doe", "john@test.com"));
        service.registerCustomer(createTestCustomer("C002", "Jane", "Smith", "jane@test.com"));
        service.registerCustomer(createTestCustomer("C003", "Bob", "Johnson", "bob@test.com"));
        
        // Add inventory
        service.addInventory(new Inventory("P001", 100, 0, 10, 500));
        service.addInventory(new Inventory("P002", 50, 0, 5, 200));
        service.addInventory(new Inventory("P003", 25, 0, 5, 100));
    }
    
    private static void createTestOrders(EcommerceService service) {
        Address address = new Address("123 Test St", "Test City", "TC", "12345", "USA", AddressType.BOTH);
        
        // Create and complete order 1
        service.addToCart("C001", "P001", 1);
        Order order1 = service.createOrder("C001", address, null, "PAYMENT_1");
        service.updateOrderStatus(order1.getOrderId(), OrderStatus.CONFIRMED);
        service.updateOrderStatus(order1.getOrderId(), OrderStatus.PROCESSING);
        service.shipOrder(order1.getOrderId(), "TRACK001");
        service.updateOrderStatus(order1.getOrderId(), OrderStatus.DELIVERED);
        
        // Create and complete order 2
        service.addToCart("C002", "P002", 2);
        Order order2 = service.createOrder("C002", address, null, "PAYMENT_2");
        service.updateOrderStatus(order2.getOrderId(), OrderStatus.CONFIRMED);
        service.updateOrderStatus(order2.getOrderId(), OrderStatus.PROCESSING);
        service.shipOrder(order2.getOrderId(), "TRACK002");
        service.updateOrderStatus(order2.getOrderId(), OrderStatus.DELIVERED);
        
        // Create cancelled order
        service.addToCart("C003", "P003", 1);
        Order order3 = service.createOrder("C003", address, null, "PAYMENT_3");
        service.cancelOrder(order3.getOrderId());
    }
}