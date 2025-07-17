package com.machinecoding.ecommerce.service;

import com.machinecoding.ecommerce.model.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * In-memory implementation of the e-commerce service.
 * Thread-safe implementation using concurrent collections.
 */
public class InMemoryEcommerceService implements EcommerceService {
    
    private final Map<String, Product> products;
    private final Map<String, Customer> customers;
    private final Map<String, Inventory> inventories;
    private final Map<String, List<CartItem>> shoppingCarts;
    private final Map<String, Order> orders;
    private final AtomicInteger orderIdCounter;
    private final PricingEngine pricingEngine;
    
    public InMemoryEcommerceService() {
        this.products = new ConcurrentHashMap<>();
        this.customers = new ConcurrentHashMap<>();
        this.inventories = new ConcurrentHashMap<>();
        this.shoppingCarts = new ConcurrentHashMap<>();
        this.orders = new ConcurrentHashMap<>();
        this.orderIdCounter = new AtomicInteger(1);
        this.pricingEngine = new PricingEngine();
    }
    
    // Product Management
    @Override
    public void addProduct(Product product) {
        if (product == null || product.getProductId().isEmpty()) {
            throw new IllegalArgumentException("Product and product ID cannot be null or empty");
        }
        products.put(product.getProductId(), product);
    }
    
    @Override
    public Optional<Product> getProduct(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(products.get(productId.trim()));
    }
    
    @Override
    public List<Product> getProductsByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        return products.values().stream()
                .filter(product -> product.getCategory().equalsIgnoreCase(category.trim()))
                .filter(Product::isActive)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Product> getProductsByBrand(String brand) {
        if (brand == null || brand.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        return products.values().stream()
                .filter(product -> product.getBrand().equalsIgnoreCase(brand.trim()))
                .filter(Product::isActive)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Product> searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String searchQuery = query.trim().toLowerCase();
        return products.values().stream()
                .filter(Product::isActive)
                .filter(product -> 
                    product.getName().toLowerCase().contains(searchQuery) ||
                    product.getDescription().toLowerCase().contains(searchQuery) ||
                    product.getCategory().toLowerCase().contains(searchQuery) ||
                    product.getBrand().toLowerCase().contains(searchQuery))
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean updateProductPrice(String productId, BigDecimal newPrice) {
        if (productId == null || newPrice == null || newPrice.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        
        Product product = products.get(productId.trim());
        if (product != null) {
            products.put(productId.trim(), product.withPrice(newPrice));
            return true;
        }
        return false;
    }
    
    @Override
    public boolean updateProductStatus(String productId, ProductStatus status) {
        if (productId == null || status == null) {
            return false;
        }
        
        Product product = products.get(productId.trim());
        if (product != null) {
            products.put(productId.trim(), product.withStatus(status));
            return true;
        }
        return false;
    }
    
    // Customer Management
    @Override
    public void registerCustomer(Customer customer) {
        if (customer == null || customer.getCustomerId().isEmpty()) {
            throw new IllegalArgumentException("Customer and customer ID cannot be null or empty");
        }
        customers.put(customer.getCustomerId(), customer);
        // Initialize empty shopping cart
        shoppingCarts.put(customer.getCustomerId(), new ArrayList<>());
    }
    
    @Override
    public Optional<Customer> getCustomer(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(customers.get(customerId.trim()));
    }
    
    @Override
    public List<Customer> findCustomersByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        return customers.values().stream()
                .filter(customer -> customer.getEmail().equalsIgnoreCase(email.trim()))
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean updateCustomerStatus(String customerId, CustomerStatus status) {
        if (customerId == null || status == null) {
            return false;
        }
        
        Customer customer = customers.get(customerId.trim());
        if (customer != null) {
            customers.put(customerId.trim(), customer.withStatus(status));
            return true;
        }
        return false;
    }
    
    // Inventory Management
    @Override
    public void addInventory(Inventory inventory) {
        if (inventory == null || inventory.getProductId().isEmpty()) {
            throw new IllegalArgumentException("Inventory and product ID cannot be null or empty");
        }
        inventories.put(inventory.getProductId(), inventory);
    }
    
    @Override
    public Optional<Inventory> getInventory(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(inventories.get(productId.trim()));
    }
    
    @Override
    public boolean updateInventory(String productId, int quantity) {
        if (productId == null || quantity < 0) {
            return false;
        }
        
        Inventory inventory = inventories.get(productId.trim());
        if (inventory != null) {
            inventory.addStock(quantity);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean reserveInventory(String productId, int quantity) {
        if (productId == null || quantity <= 0) {
            return false;
        }
        
        Inventory inventory = inventories.get(productId.trim());
        if (inventory != null) {
            return inventory.reserveQuantity(quantity);
        }
        return false;
    }
    
    @Override
    public boolean releaseInventory(String productId, int quantity) {
        if (productId == null || quantity <= 0) {
            return false;
        }
        
        Inventory inventory = inventories.get(productId.trim());
        if (inventory != null) {
            inventory.releaseReservedQuantity(quantity);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean confirmInventory(String productId, int quantity) {
        if (productId == null || quantity <= 0) {
            return false;
        }
        
        Inventory inventory = inventories.get(productId.trim());
        if (inventory != null) {
            inventory.confirmReservedQuantity(quantity);
            return true;
        }
        return false;
    }
    
    @Override
    public List<Inventory> getLowStockProducts() {
        return inventories.values().stream()
                .filter(Inventory::needsReorder)
                .collect(Collectors.toList());
    }  
  
    // Shopping Cart Management
    @Override
    public boolean addToCart(String customerId, String productId, int quantity) {
        if (customerId == null || productId == null || quantity <= 0) {
            return false;
        }
        
        Customer customer = customers.get(customerId.trim());
        Product product = products.get(productId.trim());
        Inventory inventory = inventories.get(productId.trim());
        
        if (customer == null || product == null || !product.isActive() || 
            inventory == null || !inventory.isAvailable(quantity)) {
            return false;
        }
        
        List<CartItem> cart = shoppingCarts.computeIfAbsent(customerId.trim(), k -> new ArrayList<>());
        
        synchronized (cart) {
            // Check if item already exists in cart
            Optional<CartItem> existingItem = cart.stream()
                    .filter(item -> item.getProductId().equals(productId.trim()))
                    .findFirst();
            
            if (existingItem.isPresent()) {
                // Update existing item quantity
                CartItem item = existingItem.get();
                int newQuantity = item.getQuantity() + quantity;
                if (inventory.isAvailable(newQuantity)) {
                    item.updateQuantity(newQuantity);
                    return true;
                }
                return false;
            } else {
                // Add new item to cart
                CartItem newItem = new CartItem(productId.trim(), quantity, product.getPrice());
                cart.add(newItem);
                return true;
            }
        }
    }
    
    @Override
    public boolean updateCartItem(String customerId, String productId, int quantity) {
        if (customerId == null || productId == null || quantity <= 0) {
            return false;
        }
        
        List<CartItem> cart = shoppingCarts.get(customerId.trim());
        if (cart == null) {
            return false;
        }
        
        Inventory inventory = inventories.get(productId.trim());
        if (inventory == null || !inventory.isAvailable(quantity)) {
            return false;
        }
        
        synchronized (cart) {
            Optional<CartItem> existingItem = cart.stream()
                    .filter(item -> item.getProductId().equals(productId.trim()))
                    .findFirst();
            
            if (existingItem.isPresent()) {
                existingItem.get().updateQuantity(quantity);
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean removeFromCart(String customerId, String productId) {
        if (customerId == null || productId == null) {
            return false;
        }
        
        List<CartItem> cart = shoppingCarts.get(customerId.trim());
        if (cart == null) {
            return false;
        }
        
        synchronized (cart) {
            return cart.removeIf(item -> item.getProductId().equals(productId.trim()));
        }
    }
    
    @Override
    public List<CartItem> getCartItems(String customerId) {
        if (customerId == null) {
            return new ArrayList<>();
        }
        
        List<CartItem> cart = shoppingCarts.get(customerId.trim());
        return cart != null ? new ArrayList<>(cart) : new ArrayList<>();
    }
    
    @Override
    public BigDecimal getCartTotal(String customerId) {
        List<CartItem> cartItems = getCartItems(customerId);
        return cartItems.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    @Override
    public boolean clearCart(String customerId) {
        if (customerId == null) {
            return false;
        }
        
        List<CartItem> cart = shoppingCarts.get(customerId.trim());
        if (cart != null) {
            synchronized (cart) {
                cart.clear();
                return true;
            }
        }
        return false;
    }
    
    // Order Management
    @Override
    public Order createOrder(String customerId, Address shippingAddress, Address billingAddress, String paymentMethodId) {
        if (customerId == null || shippingAddress == null || paymentMethodId == null) {
            throw new IllegalArgumentException("Customer ID, shipping address, and payment method are required");
        }
        
        Customer customer = customers.get(customerId.trim());
        if (customer == null || !customer.isActive()) {
            throw new IllegalArgumentException("Customer not found or inactive");
        }
        
        List<CartItem> cartItems = getCartItems(customerId);
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }
        
        // Reserve inventory for all items
        List<String> reservedProducts = new ArrayList<>();
        try {
            for (CartItem cartItem : cartItems) {
                if (!reserveInventory(cartItem.getProductId(), cartItem.getQuantity())) {
                    // Release already reserved items
                    for (String productId : reservedProducts) {
                        CartItem item = cartItems.stream()
                                .filter(ci -> ci.getProductId().equals(productId))
                                .findFirst().orElse(null);
                        if (item != null) {
                            releaseInventory(productId, item.getQuantity());
                        }
                    }
                    throw new IllegalArgumentException("Insufficient inventory for product: " + cartItem.getProductId());
                }
                reservedProducts.add(cartItem.getProductId());
            }
            
            // Create order items
            List<OrderItem> orderItems = cartItems.stream()
                    .map(cartItem -> {
                        Product product = products.get(cartItem.getProductId());
                        return new OrderItem(cartItem.getProductId(), 
                                           product != null ? product.getName() : "Unknown Product",
                                           cartItem.getQuantity(), 
                                           cartItem.getUnitPrice());
                    })
                    .collect(Collectors.toList());
            
            // Calculate pricing
            BigDecimal subtotal = cartItems.stream()
                    .map(CartItem::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal taxAmount = pricingEngine.calculateTax(subtotal);
            BigDecimal shippingCost = pricingEngine.calculateShipping(orderItems, shippingAddress);
            
            // Create order
            String orderId = "ORDER" + String.format("%06d", orderIdCounter.getAndIncrement());
            Order order = new Order(orderId, customerId.trim(), orderItems, shippingAddress, 
                                  billingAddress != null ? billingAddress : shippingAddress,
                                  subtotal, taxAmount, shippingCost, paymentMethodId.trim());
            
            orders.put(orderId, order);
            
            // Clear cart after successful order creation
            clearCart(customerId);
            
            return order;
            
        } catch (Exception e) {
            // Release reserved inventory on failure
            for (String productId : reservedProducts) {
                CartItem item = cartItems.stream()
                        .filter(ci -> ci.getProductId().equals(productId))
                        .findFirst().orElse(null);
                if (item != null) {
                    releaseInventory(productId, item.getQuantity());
                }
            }
            throw e;
        }
    }
    
    @Override
    public Optional<Order> getOrder(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(orders.get(orderId.trim()));
    }
    
    @Override
    public List<Order> getOrdersByCustomer(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        return orders.values().stream()
                .filter(order -> order.getCustomerId().equals(customerId.trim()))
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Order> getOrdersByStatus(OrderStatus status) {
        if (status == null) {
            return new ArrayList<>();
        }
        
        return orders.values().stream()
                .filter(order -> order.getStatus() == status)
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean updateOrderStatus(String orderId, OrderStatus status) {
        if (orderId == null || status == null) {
            return false;
        }
        
        Order order = orders.get(orderId.trim());
        if (order != null && order.getStatus().canTransitionTo(status)) {
            order.updateStatus(status);
            
            // Handle inventory confirmation for delivered orders
            if (status == OrderStatus.DELIVERED) {
                for (OrderItem item : order.getItems()) {
                    confirmInventory(item.getProductId(), item.getQuantity());
                }
            }
            
            return true;
        }
        return false;
    }
    
    @Override
    public boolean cancelOrder(String orderId) {
        if (orderId == null) {
            return false;
        }
        
        Order order = orders.get(orderId.trim());
        if (order != null && order.canCancel()) {
            order.updateStatus(OrderStatus.CANCELLED);
            
            // Release reserved inventory
            for (OrderItem item : order.getItems()) {
                releaseInventory(item.getProductId(), item.getQuantity());
            }
            
            return true;
        }
        return false;
    }
    
    @Override
    public boolean shipOrder(String orderId, String trackingNumber) {
        if (orderId == null || trackingNumber == null) {
            return false;
        }
        
        Order order = orders.get(orderId.trim());
        if (order != null && order.canShip()) {
            order.updateStatus(OrderStatus.SHIPPED);
            order.setTrackingNumber(trackingNumber.trim());
            return true;
        }
        return false;
    }
    
    // Statistics and Reporting
    @Override
    public EcommerceStats getStats() {
        int totalProducts = products.size();
        int activeProducts = (int) products.values().stream().filter(Product::isActive).count();
        
        int totalCustomers = customers.size();
        int activeCustomers = (int) customers.values().stream().filter(Customer::isActive).count();
        
        int totalOrders = orders.size();
        int pendingOrders = (int) orders.values().stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();
        int shippedOrders = (int) orders.values().stream().filter(o -> o.getStatus() == OrderStatus.SHIPPED).count();
        int deliveredOrders = (int) orders.values().stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();
        int cancelledOrders = (int) orders.values().stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();
        
        BigDecimal totalRevenue = orders.values().stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal averageOrderValue = deliveredOrders > 0 ? 
                totalRevenue.divide(BigDecimal.valueOf(deliveredOrders), 2, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;
        
        int lowStockProducts = getLowStockProducts().size();
        
        return new EcommerceStats(totalProducts, activeProducts, totalCustomers, activeCustomers,
                                totalOrders, pendingOrders, shippedOrders, deliveredOrders, cancelledOrders,
                                totalRevenue, averageOrderValue, lowStockProducts);
    }
    
    @Override
    public CustomerStats getCustomerStats(String customerId) {
        if (customerId == null) {
            return null;
        }
        
        Customer customer = customers.get(customerId.trim());
        if (customer == null) {
            return null;
        }
        
        List<Order> customerOrders = getOrdersByCustomer(customerId);
        
        int totalOrders = customerOrders.size();
        int deliveredOrders = (int) customerOrders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();
        int cancelledOrders = (int) customerOrders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();
        
        BigDecimal totalSpent = customerOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal averageOrderValue = deliveredOrders > 0 ? 
                totalSpent.divide(BigDecimal.valueOf(deliveredOrders), 2, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;
        
        LocalDateTime lastOrderDate = customerOrders.stream()
                .map(Order::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        
        return new CustomerStats(customerId, totalOrders, deliveredOrders, cancelledOrders,
                               totalSpent, averageOrderValue, lastOrderDate, customer.getRegisteredAt());
    }
    
    @Override
    public ProductStats getProductStats(String productId) {
        if (productId == null) {
            return null;
        }
        
        Product product = products.get(productId.trim());
        if (product == null) {
            return null;
        }
        
        // Calculate product statistics from orders
        List<OrderItem> productOrderItems = orders.values().stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .flatMap(order -> order.getItems().stream())
                .filter(item -> item.getProductId().equals(productId.trim()))
                .collect(Collectors.toList());
        
        int totalSold = productOrderItems.stream().mapToInt(OrderItem::getQuantity).sum();
        BigDecimal totalRevenue = productOrderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Inventory inventory = inventories.get(productId.trim());
        int currentStock = inventory != null ? inventory.getAvailableQuantity() : 0;
        
        return new ProductStats(productId, totalSold, totalRevenue, currentStock, product.getCreatedAt());
    }
    
    // Inner class for pricing calculations
    private static class PricingEngine {
        private static final BigDecimal TAX_RATE = new BigDecimal("0.08"); // 8% tax
        private static final BigDecimal BASE_SHIPPING = new BigDecimal("5.99");
        private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("50.00");
        
        public BigDecimal calculateTax(BigDecimal subtotal) {
            return subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        }
        
        public BigDecimal calculateShipping(List<OrderItem> items, Address shippingAddress) {
            BigDecimal subtotal = items.stream()
                    .map(OrderItem::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Free shipping for orders over threshold
            if (subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0) {
                return BigDecimal.ZERO;
            }
            
            // Calculate weight-based shipping
            int totalItems = items.stream().mapToInt(OrderItem::getQuantity).sum();
            BigDecimal weightMultiplier = BigDecimal.valueOf(Math.max(1, totalItems / 5)); // $1 per 5 items
            
            return BASE_SHIPPING.add(weightMultiplier).setScale(2, RoundingMode.HALF_UP);
        }
    }
}