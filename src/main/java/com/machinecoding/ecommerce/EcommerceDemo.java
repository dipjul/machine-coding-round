package com.machinecoding.ecommerce;

import com.machinecoding.ecommerce.model.*;
import com.machinecoding.ecommerce.service.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Comprehensive demonstration of the E-commerce Order Management System.
 * Shows product management, inventory, shopping cart, and order processing.
 */
public class EcommerceDemo {
    
    public static void main(String[] args) {
        System.out.println("=== E-commerce Order Management System Demo ===\n");
        
        // Demo 1: Product and Customer Management
        System.out.println("=== Demo 1: Product and Customer Management ===");
        demonstrateProductAndCustomerManagement();
        
        // Demo 2: Inventory Management
        System.out.println("\n=== Demo 2: Inventory Management ===");
        demonstrateInventoryManagement();
        
        // Demo 3: Shopping Cart Operations
        System.out.println("\n=== Demo 3: Shopping Cart Operations ===");
        demonstrateShoppingCart();
        
        // Demo 4: Order Processing
        System.out.println("\n=== Demo 4: Order Processing ===");
        demonstrateOrderProcessing();
        
        // Demo 5: Order Lifecycle Management
        System.out.println("\n=== Demo 5: Order Lifecycle Management ===");
        demonstrateOrderLifecycle();
        
        // Demo 6: Statistics and Reporting
        System.out.println("\n=== Demo 6: Statistics and Reporting ===");
        demonstrateStatistics();
        
        System.out.println("\n=== Demo Complete ===");
    }
    
    private static void demonstrateProductAndCustomerManagement() {
        System.out.println("1. Creating e-commerce service:");
        EcommerceService ecommerceService = new InMemoryEcommerceService();
        
        System.out.println("\n2. Adding products to catalog:");
        
        // Create and add products
        Product[] products = {
            createProduct("P001", "iPhone 14", "Latest Apple smartphone", new BigDecimal("999.99"), "Electronics", "Apple"),
            createProduct("P002", "Samsung Galaxy S23", "Android flagship phone", new BigDecimal("899.99"), "Electronics", "Samsung"),
            createProduct("P003", "MacBook Pro", "Professional laptop", new BigDecimal("1999.99"), "Electronics", "Apple"),
            createProduct("P004", "Nike Air Max", "Running shoes", new BigDecimal("129.99"), "Footwear", "Nike"),
            createProduct("P005", "Levi's Jeans", "Classic denim jeans", new BigDecimal("79.99"), "Clothing", "Levi's")
        };
        
        for (Product product : products) {
            ecommerceService.addProduct(product);
            System.out.println("   Added: " + product);
        }
        
        System.out.println("\n3. Registering customers:");
        
        // Create and register customers
        Customer[] customers = {
            createCustomer("C001", "John", "Doe", "john.doe@email.com", "+1-555-0101"),
            createCustomer("C002", "Jane", "Smith", "jane.smith@email.com", "+1-555-0102"),
            createCustomer("C003", "Bob", "Johnson", "bob.johnson@email.com", "+1-555-0103")
        };
        
        for (Customer customer : customers) {
            ecommerceService.registerCustomer(customer);
            System.out.println("   Registered: " + customer);
        }
        
        System.out.println("\n4. Product search and filtering:");
        
        // Search products
        List<Product> electronicsProducts = ecommerceService.getProductsByCategory("Electronics");
        System.out.println("   Electronics products: " + electronicsProducts.size());
        
        List<Product> appleProducts = ecommerceService.getProductsByBrand("Apple");
        System.out.println("   Apple products: " + appleProducts.size());
        
        List<Product> searchResults = ecommerceService.searchProducts("phone");
        System.out.println("   Search 'phone' results: " + searchResults.size());
        for (Product product : searchResults) {
            System.out.println("     - " + product.getName() + " ($" + product.getPrice() + ")");
        }
    }
    
    private static void demonstrateInventoryManagement() {
        EcommerceService ecommerceService = new InMemoryEcommerceService();
        setupProductsAndCustomers(ecommerceService);
        
        System.out.println("1. Adding inventory for products:");
        
        // Add inventory for products
        String[] productIds = {"P001", "P002", "P003", "P004", "P005"};
        int[] quantities = {50, 30, 20, 100, 75};
        
        for (int i = 0; i < productIds.length; i++) {
            Inventory inventory = new Inventory(productIds[i], quantities[i], 0, 10, 200);
            ecommerceService.addInventory(inventory);
            System.out.println("   Added inventory: " + inventory);
        }
        
        System.out.println("\n2. Checking inventory availability:");
        
        for (String productId : productIds) {
            Optional<Inventory> inventory = ecommerceService.getInventory(productId);
            if (inventory.isPresent()) {
                Inventory inv = inventory.get();
                System.out.println("   " + productId + ": Available=" + inv.getAvailableQuantity() + 
                                 ", Reserved=" + inv.getReservedQuantity() + 
                                 ", Needs Reorder=" + inv.needsReorder());
            }
        }
        
        System.out.println("\n3. Inventory operations:");
        
        // Reserve inventory
        boolean reserved = ecommerceService.reserveInventory("P001", 5);
        System.out.println("   Reserved 5 units of P001: " + reserved);
        
        // Check updated inventory
        Optional<Inventory> updatedInventory = ecommerceService.getInventory("P001");
        if (updatedInventory.isPresent()) {
            Inventory inv = updatedInventory.get();
            System.out.println("   P001 after reservation: Available=" + inv.getAvailableQuantity() + 
                             ", Reserved=" + inv.getReservedQuantity());
        }
        
        // Release inventory
        ecommerceService.releaseInventory("P001", 2);
        System.out.println("   Released 2 units of P001");
        
        // Check final inventory
        updatedInventory = ecommerceService.getInventory("P001");
        if (updatedInventory.isPresent()) {
            Inventory inv = updatedInventory.get();
            System.out.println("   P001 after release: Available=" + inv.getAvailableQuantity() + 
                             ", Reserved=" + inv.getReservedQuantity());
        }
    }
    
    private static void demonstrateShoppingCart() {
        EcommerceService ecommerceService = new InMemoryEcommerceService();
        setupProductsAndCustomers(ecommerceService);
        setupInventory(ecommerceService);
        
        System.out.println("1. Adding items to shopping cart:");
        
        String customerId = "C001";
        
        // Add items to cart
        boolean added1 = ecommerceService.addToCart(customerId, "P001", 2);
        System.out.println("   Added 2x iPhone 14 to cart: " + added1);
        
        boolean added2 = ecommerceService.addToCart(customerId, "P004", 1);
        System.out.println("   Added 1x Nike Air Max to cart: " + added2);
        
        boolean added3 = ecommerceService.addToCart(customerId, "P005", 3);
        System.out.println("   Added 3x Levi's Jeans to cart: " + added3);
        
        System.out.println("\n2. Viewing cart contents:");
        
        List<CartItem> cartItems = ecommerceService.getCartItems(customerId);
        System.out.println("   Cart items for customer " + customerId + ":");
        for (CartItem item : cartItems) {
            System.out.println("     - " + item);
        }
        
        BigDecimal cartTotal = ecommerceService.getCartTotal(customerId);
        System.out.println("   Cart total: $" + cartTotal);
        
        System.out.println("\n3. Updating cart items:");
        
        // Update item quantity
        boolean updated = ecommerceService.updateCartItem(customerId, "P001", 1);
        System.out.println("   Updated iPhone 14 quantity to 1: " + updated);
        
        // Remove item from cart
        boolean removed = ecommerceService.removeFromCart(customerId, "P005");
        System.out.println("   Removed Levi's Jeans from cart: " + removed);
        
        System.out.println("\n4. Updated cart contents:");
        
        cartItems = ecommerceService.getCartItems(customerId);
        for (CartItem item : cartItems) {
            System.out.println("     - " + item);
        }
        
        cartTotal = ecommerceService.getCartTotal(customerId);
        System.out.println("   Updated cart total: $" + cartTotal);
    }
    
    private static void demonstrateOrderProcessing() {
        EcommerceService ecommerceService = new InMemoryEcommerceService();
        setupProductsAndCustomers(ecommerceService);
        setupInventory(ecommerceService);
        setupShoppingCart(ecommerceService);
        
        System.out.println("1. Creating order from shopping cart:");
        
        String customerId = "C001";
        Address shippingAddress = new Address("123 Main St", "New York", "NY", "10001", "USA", AddressType.SHIPPING);
        Address billingAddress = new Address("123 Main St", "New York", "NY", "10001", "USA", AddressType.BILLING);
        String paymentMethodId = "PAYMENT_METHOD_123";
        
        try {
            Order order = ecommerceService.createOrder(customerId, shippingAddress, billingAddress, paymentMethodId);
            System.out.println("   Order created: " + order);
            System.out.println("   Order ID: " + order.getOrderId());
            System.out.println("   Total items: " + order.getTotalItems());
            System.out.println("   Subtotal: $" + order.getSubtotal());
            System.out.println("   Tax: $" + order.getTaxAmount());
            System.out.println("   Shipping: $" + order.getShippingCost());
            System.out.println("   Total: $" + order.getTotalAmount());
            
            System.out.println("\n2. Order items breakdown:");
            for (OrderItem item : order.getItems()) {
                System.out.println("     - " + item);
            }
            
            System.out.println("\n3. Verifying cart is cleared:");
            List<CartItem> cartItems = ecommerceService.getCartItems(customerId);
            System.out.println("   Cart items after order: " + cartItems.size());
            
            System.out.println("\n4. Verifying inventory reservation:");
            for (OrderItem item : order.getItems()) {
                Optional<Inventory> inventory = ecommerceService.getInventory(item.getProductId());
                if (inventory.isPresent()) {
                    Inventory inv = inventory.get();
                    System.out.println("   " + item.getProductId() + ": Available=" + inv.getAvailableQuantity() + 
                                     ", Reserved=" + inv.getReservedQuantity());
                }
            }
            
        } catch (Exception e) {
            System.out.println("   Order creation failed: " + e.getMessage());
        }
    }
    
    private static void demonstrateOrderLifecycle() {
        EcommerceService ecommerceService = new InMemoryEcommerceService();
        setupProductsAndCustomers(ecommerceService);
        setupInventory(ecommerceService);
        setupShoppingCart(ecommerceService);
        
        System.out.println("1. Creating and processing orders:");
        
        // Create order
        String customerId = "C001";
        Address shippingAddress = new Address("123 Main St", "New York", "NY", "10001", "USA", AddressType.SHIPPING);
        Order order = ecommerceService.createOrder(customerId, shippingAddress, null, "PAYMENT_123");
        String orderId = order.getOrderId();
        
        System.out.println("   Order created with status: " + order.getStatus());
        
        System.out.println("\n2. Order status progression:");
        
        // Confirm order
        boolean confirmed = ecommerceService.updateOrderStatus(orderId, OrderStatus.CONFIRMED);
        System.out.println("   Order confirmed: " + confirmed);
        System.out.println("   Status: " + ecommerceService.getOrder(orderId).get().getStatus());
        
        // Process order
        boolean processing = ecommerceService.updateOrderStatus(orderId, OrderStatus.PROCESSING);
        System.out.println("   Order processing: " + processing);
        System.out.println("   Status: " + ecommerceService.getOrder(orderId).get().getStatus());
        
        // Ship order
        boolean shipped = ecommerceService.shipOrder(orderId, "TRACK123456789");
        System.out.println("   Order shipped: " + shipped);
        Order shippedOrder = ecommerceService.getOrder(orderId).get();
        System.out.println("   Status: " + shippedOrder.getStatus());
        System.out.println("   Tracking number: " + shippedOrder.getTrackingNumber());
        
        // Deliver order
        boolean delivered = ecommerceService.updateOrderStatus(orderId, OrderStatus.DELIVERED);
        System.out.println("   Order delivered: " + delivered);
        System.out.println("   Status: " + ecommerceService.getOrder(orderId).get().getStatus());
        
        System.out.println("\n3. Order cancellation example:");
        
        // Create another order for cancellation demo
        setupShoppingCart(ecommerceService); // Add items back to cart
        Order anotherOrder = ecommerceService.createOrder(customerId, shippingAddress, null, "PAYMENT_456");
        System.out.println("   New order created: " + anotherOrder.getOrderId());
        
        // Cancel order
        boolean cancelled = ecommerceService.cancelOrder(anotherOrder.getOrderId());
        System.out.println("   Order cancelled: " + cancelled);
        System.out.println("   Status: " + ecommerceService.getOrder(anotherOrder.getOrderId()).get().getStatus());
        
        System.out.println("\n4. Inventory after cancellation:");
        Optional<Inventory> inventory = ecommerceService.getInventory("P001");
        if (inventory.isPresent()) {
            Inventory inv = inventory.get();
            System.out.println("   P001: Available=" + inv.getAvailableQuantity() + 
                             ", Reserved=" + inv.getReservedQuantity());
        }
    }
    
    private static void demonstrateStatistics() {
        EcommerceService ecommerceService = new InMemoryEcommerceService();
        setupProductsAndCustomers(ecommerceService);
        setupInventory(ecommerceService);
        createSampleOrders(ecommerceService);
        
        System.out.println("1. Overall system statistics:");
        
        EcommerceStats stats = ecommerceService.getStats();
        System.out.println("   " + stats);
        
        System.out.println("\n2. Detailed statistics:");
        System.out.println("   Total products: " + stats.getTotalProducts());
        System.out.println("   Active products: " + stats.getActiveProducts());
        System.out.println("   Product active rate: " + String.format("%.1f%%", stats.getProductActiveRate()));
        System.out.println("   Total customers: " + stats.getTotalCustomers());
        System.out.println("   Active customers: " + stats.getActiveCustomers());
        System.out.println("   Customer activity rate: " + String.format("%.1f%%", stats.getCustomerActivityRate()));
        System.out.println("   Total orders: " + stats.getTotalOrders());
        System.out.println("   Delivered orders: " + stats.getDeliveredOrders());
        System.out.println("   Cancelled orders: " + stats.getCancelledOrders());
        System.out.println("   Order fulfillment rate: " + String.format("%.1f%%", stats.getOrderFulfillmentRate()));
        System.out.println("   Order cancellation rate: " + String.format("%.1f%%", stats.getOrderCancellationRate()));
        System.out.println("   Total revenue: $" + stats.getTotalRevenue());
        System.out.println("   Average order value: $" + stats.getAverageOrderValue());
        System.out.println("   Low stock products: " + stats.getLowStockProducts());
        
        System.out.println("\n3. Customer statistics:");
        
        CustomerStats customerStats = ecommerceService.getCustomerStats("C001");
        if (customerStats != null) {
            System.out.println("   " + customerStats);
            System.out.println("   Order completion rate: " + String.format("%.1f%%", customerStats.getOrderCompletionRate()));
            System.out.println("   Is active customer: " + customerStats.isActiveCustomer());
        }
        
        System.out.println("\n4. Product statistics:");
        
        ProductStats productStats = ecommerceService.getProductStats("P001");
        if (productStats != null) {
            System.out.println("   " + productStats);
            System.out.println("   Is popular product: " + productStats.isPopularProduct());
            System.out.println("   Is low stock: " + productStats.isLowStock());
        }
    }
    
    // Helper methods
    private static Product createProduct(String id, String name, String description, BigDecimal price, String category, String brand) {
        return new Product(id, name, description, price, category, brand, "SKU-" + id, 1.0, ProductStatus.ACTIVE);
    }
    
    private static Customer createCustomer(String id, String firstName, String lastName, String email, String phone) {
        Address address = new Address("123 Default St", "Default City", "DC", "12345", "USA", AddressType.BOTH);
        return new Customer(id, firstName, lastName, email, phone, address, CustomerStatus.ACTIVE);
    }
    
    private static void setupProductsAndCustomers(EcommerceService service) {
        // Add products
        service.addProduct(createProduct("P001", "iPhone 14", "Latest Apple smartphone", new BigDecimal("999.99"), "Electronics", "Apple"));
        service.addProduct(createProduct("P002", "Samsung Galaxy S23", "Android flagship phone", new BigDecimal("899.99"), "Electronics", "Samsung"));
        service.addProduct(createProduct("P003", "MacBook Pro", "Professional laptop", new BigDecimal("1999.99"), "Electronics", "Apple"));
        service.addProduct(createProduct("P004", "Nike Air Max", "Running shoes", new BigDecimal("129.99"), "Footwear", "Nike"));
        service.addProduct(createProduct("P005", "Levi's Jeans", "Classic denim jeans", new BigDecimal("79.99"), "Clothing", "Levi's"));
        
        // Add customers
        service.registerCustomer(createCustomer("C001", "John", "Doe", "john.doe@email.com", "+1-555-0101"));
        service.registerCustomer(createCustomer("C002", "Jane", "Smith", "jane.smith@email.com", "+1-555-0102"));
        service.registerCustomer(createCustomer("C003", "Bob", "Johnson", "bob.johnson@email.com", "+1-555-0103"));
    }
    
    private static void setupInventory(EcommerceService service) {
        String[] productIds = {"P001", "P002", "P003", "P004", "P005"};
        int[] quantities = {50, 30, 20, 100, 75};
        
        for (int i = 0; i < productIds.length; i++) {
            Inventory inventory = new Inventory(productIds[i], quantities[i], 0, 10, 200);
            service.addInventory(inventory);
        }
    }
    
    private static void setupShoppingCart(EcommerceService service) {
        service.addToCart("C001", "P001", 1);
        service.addToCart("C001", "P004", 1);
    }
    
    private static void createSampleOrders(EcommerceService service) {
        Address address = new Address("123 Test St", "Test City", "TC", "12345", "USA", AddressType.BOTH);
        
        // Create and complete some orders
        setupShoppingCart(service);
        Order order1 = service.createOrder("C001", address, null, "PAYMENT_1");
        service.updateOrderStatus(order1.getOrderId(), OrderStatus.CONFIRMED);
        service.updateOrderStatus(order1.getOrderId(), OrderStatus.PROCESSING);
        service.shipOrder(order1.getOrderId(), "TRACK001");
        service.updateOrderStatus(order1.getOrderId(), OrderStatus.DELIVERED);
        
        // Create another order
        service.addToCart("C002", "P002", 1);
        service.addToCart("C002", "P005", 2);
        Order order2 = service.createOrder("C002", address, null, "PAYMENT_2");
        service.updateOrderStatus(order2.getOrderId(), OrderStatus.CONFIRMED);
        service.updateOrderStatus(order2.getOrderId(), OrderStatus.PROCESSING);
        service.shipOrder(order2.getOrderId(), "TRACK002");
        service.updateOrderStatus(order2.getOrderId(), OrderStatus.DELIVERED);
        
        // Create a cancelled order
        service.addToCart("C003", "P003", 1);
        Order order3 = service.createOrder("C003", address, null, "PAYMENT_3");
        service.cancelOrder(order3.getOrderId());
    }
}