package com.machinecoding.ridehailing.model;

import java.math.BigDecimal;

/**
 * Enumeration of vehicle types in the ride hailing system.
 */
public enum VehicleType {
    ECONOMY("Economy", "Standard economy vehicle", 4, new BigDecimal("1.0")),
    COMFORT("Comfort", "Comfortable mid-size vehicle", 4, new BigDecimal("1.2")),
    PREMIUM("Premium", "Premium luxury vehicle", 4, new BigDecimal("1.5")),
    SUV("SUV", "Large SUV for groups", 6, new BigDecimal("1.3")),
    LUXURY("Luxury", "High-end luxury vehicle", 4, new BigDecimal("2.0")),
    POOL("Pool", "Shared ride vehicle", 4, new BigDecimal("0.8"));
    
    private final String displayName;
    private final String description;
    private final int standardCapacity;
    private final BigDecimal priceMultiplier;
    
    VehicleType(String displayName, String description, int standardCapacity, BigDecimal priceMultiplier) {
        this.displayName = displayName;
        this.description = description;
        this.standardCapacity = standardCapacity;
        this.priceMultiplier = priceMultiplier;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getStandardCapacity() {
        return standardCapacity;
    }
    
    public BigDecimal getPriceMultiplier() {
        return priceMultiplier;
    }
    
    /**
     * Checks if this vehicle type is suitable for the given passenger count.
     */
    public boolean canAccommodate(int passengerCount) {
        return standardCapacity >= passengerCount;
    }
    
    /**
     * Gets the recommended vehicle types for the given passenger count.
     */
    public static VehicleType[] getRecommendedTypes(int passengerCount) {
        if (passengerCount <= 4) {
            return new VehicleType[]{ECONOMY, COMFORT, PREMIUM, LUXURY, POOL};
        } else if (passengerCount <= 6) {
            return new VehicleType[]{SUV};
        } else {
            return new VehicleType[]{};
        }
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}