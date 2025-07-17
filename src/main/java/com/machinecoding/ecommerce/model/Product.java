package com.machinecoding.ecommerce.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a product in the e-commerce system.
 */
public class Product {
    private final String productId;
    private final String name;
    private final String description;
    private final BigDecimal price;
    private final String category;
    private final String brand;
    private final String sku;
    private final double weight;
    private final ProductStatus status;
    private final LocalDateTime createdAt;
    
    public Product(String productId, String name, String description, BigDecimal price,
                  String category, String brand, String sku, double weight, ProductStatus status) {
        this.productId = productId != null ? productId.trim() : "";
        this.name = name != null ? name.trim() : "";
        this.description = description != null ? description.trim() : "";
        this.price = price != null ? price : BigDecimal.ZERO;
        this.category = category != null ? category.trim() : "";
        this.brand = brand != null ? brand.trim() : "";
        this.sku = sku != null ? sku.trim() : "";
        this.weight = weight;
        this.status = status != null ? status : ProductStatus.INACTIVE;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters
    public String getProductId() { return productId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public String getCategory() { return category; }
    public String getBrand() { return brand; }
    public String getSku() { return sku; }
    public double getWeight() { return weight; }
    public ProductStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    public boolean isActive() {
        return status == ProductStatus.ACTIVE;
    }
    
    public Product withStatus(ProductStatus newStatus) {
        return new Product(productId, name, description, price, category, brand, sku, weight, newStatus);
    }
    
    public Product withPrice(BigDecimal newPrice) {
        return new Product(productId, name, description, newPrice, category, brand, sku, weight, status);
    }
    
    @Override
    public String toString() {
        return String.format("Product{id='%s', name='%s', price=%s, status=%s}", 
                           productId, name, price, status);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Product product = (Product) obj;
        return productId.equals(product.productId);
    }
    
    @Override
    public int hashCode() {
        return productId.hashCode();
    }
}