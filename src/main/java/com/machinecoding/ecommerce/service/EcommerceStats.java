package com.machinecoding.ecommerce.service;

import java.math.BigDecimal;

/**
 * Overall statistics for the e-commerce system.
 */
public class EcommerceStats {
    private final int totalProducts;
    private final int activeProducts;
    private final int totalCustomers;
    private final int activeCustomers;
    private final int totalOrders;
    private final int pendingOrders;
    private final int shippedOrders;
    private final int deliveredOrders;
    private final int cancelledOrders;
    private final BigDecimal totalRevenue;
    private final BigDecimal averageOrderValue;
    private final int lowStockProducts;
    
    public EcommerceStats(int totalProducts, int activeProducts, int totalCustomers, int activeCustomers,
                         int totalOrders, int pendingOrders, int shippedOrders, int deliveredOrders,
                         int cancelledOrders, BigDecimal totalRevenue, BigDecimal averageOrderValue,
                         int lowStockProducts) {
        this.totalProducts = totalProducts;
        this.activeProducts = activeProducts;
        this.totalCustomers = totalCustomers;
        this.activeCustomers = activeCustomers;
        this.totalOrders = totalOrders;
        this.pendingOrders = pendingOrders;
        this.shippedOrders = shippedOrders;
        this.deliveredOrders = deliveredOrders;
        this.cancelledOrders = cancelledOrders;
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        this.averageOrderValue = averageOrderValue != null ? averageOrderValue : BigDecimal.ZERO;
        this.lowStockProducts = lowStockProducts;
    }
    
    // Getters
    public int getTotalProducts() { return totalProducts; }
    public int getActiveProducts() { return activeProducts; }
    public int getTotalCustomers() { return totalCustomers; }
    public int getActiveCustomers() { return activeCustomers; }
    public int getTotalOrders() { return totalOrders; }
    public int getPendingOrders() { return pendingOrders; }
    public int getShippedOrders() { return shippedOrders; }
    public int getDeliveredOrders() { return deliveredOrders; }
    public int getCancelledOrders() { return cancelledOrders; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public BigDecimal getAverageOrderValue() { return averageOrderValue; }
    public int getLowStockProducts() { return lowStockProducts; }
    
    public double getOrderFulfillmentRate() {
        return totalOrders == 0 ? 0.0 : (double) deliveredOrders / totalOrders * 100.0;
    }
    
    public double getOrderCancellationRate() {
        return totalOrders == 0 ? 0.0 : (double) cancelledOrders / totalOrders * 100.0;
    }
    
    public double getCustomerActivityRate() {
        return totalCustomers == 0 ? 0.0 : (double) activeCustomers / totalCustomers * 100.0;
    }
    
    public double getProductActiveRate() {
        return totalProducts == 0 ? 0.0 : (double) activeProducts / totalProducts * 100.0;
    }
    
    @Override
    public String toString() {
        return String.format("EcommerceStats{products=%d/%d (%.1f%%), customers=%d/%d (%.1f%%), " +
                           "orders=%d (delivered=%d, cancelled=%d), revenue=%s, avgOrder=%s, lowStock=%d}",
                           activeProducts, totalProducts, getProductActiveRate(),
                           activeCustomers, totalCustomers, getCustomerActivityRate(),
                           totalOrders, deliveredOrders, cancelledOrders,
                           totalRevenue, averageOrderValue, lowStockProducts);
    }
}