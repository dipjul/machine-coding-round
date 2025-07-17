package com.machinecoding.ecommerce.service;

import com.machinecoding.ecommerce.model.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Main service interface for e-commerce operations.
 */
public interface EcommerceService {
    
    // Product Management
    void addProduct(Product product);
    Optional<Product> getProduct(String productId);
    List<Product> getProductsByCategory(String category);
    List<Product> getProductsByBrand(String brand);
    List<Product> searchProducts(String query);
    boolean updateProductPrice(String productId, BigDecimal newPrice);
    boolean updateProductStatus(String productId, ProductStatus status);
    
    // Customer Management
    void registerCustomer(Customer customer);
    Optional<Customer> getCustomer(String customerId);
    List<Customer> findCustomersByEmail(String email);
    boolean updateCustomerStatus(String customerId, CustomerStatus status);
    
    // Inventory Management
    void addInventory(Inventory inventory);
    Optional<Inventory> getInventory(String productId);
    boolean updateInventory(String productId, int quantity);
    boolean reserveInventory(String productId, int quantity);
    boolean releaseInventory(String productId, int quantity);
    boolean confirmInventory(String productId, int quantity);
    List<Inventory> getLowStockProducts();
    
    // Shopping Cart Management
    boolean addToCart(String customerId, String productId, int quantity);
    boolean updateCartItem(String customerId, String productId, int quantity);
    boolean removeFromCart(String customerId, String productId);
    List<CartItem> getCartItems(String customerId);
    BigDecimal getCartTotal(String customerId);
    boolean clearCart(String customerId);
    
    // Order Management
    Order createOrder(String customerId, Address shippingAddress, Address billingAddress, String paymentMethodId);
    Optional<Order> getOrder(String orderId);
    List<Order> getOrdersByCustomer(String customerId);
    List<Order> getOrdersByStatus(OrderStatus status);
    boolean updateOrderStatus(String orderId, OrderStatus status);
    boolean cancelOrder(String orderId);
    boolean shipOrder(String orderId, String trackingNumber);
    
    // Statistics and Reporting
    EcommerceStats getStats();
    CustomerStats getCustomerStats(String customerId);
    ProductStats getProductStats(String productId);
}