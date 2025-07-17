package com.machinecoding.games.monopoly.model;

import java.math.BigDecimal;

/**
 * Represents a property in the Monopoly game.
 */
public class Property {
    private final String propertyId;
    private final String name;
    private final PropertyType type;
    private final PropertyGroup group;
    private final BigDecimal purchasePrice;
    private final BigDecimal baseRent;
    private final BigDecimal houseCost;
    private final BigDecimal hotelCost;
    private Player owner;
    private int houses;
    private boolean hasHotel;
    private boolean isMortgaged;
    private final BigDecimal mortgageValue;
    
    public Property(String propertyId, String name, PropertyType type, PropertyGroup group,
                   BigDecimal purchasePrice, BigDecimal baseRent, BigDecimal houseCost, BigDecimal hotelCost) {
        this.propertyId = propertyId != null ? propertyId.trim() : "";
        this.name = name != null ? name.trim() : "";
        this.type = type != null ? type : PropertyType.STREET;
        this.group = group;
        this.purchasePrice = purchasePrice != null ? purchasePrice : BigDecimal.ZERO;
        this.baseRent = baseRent != null ? baseRent : BigDecimal.ZERO;
        this.houseCost = houseCost != null ? houseCost : BigDecimal.ZERO;
        this.hotelCost = hotelCost != null ? hotelCost : BigDecimal.ZERO;
        this.owner = null;
        this.houses = 0;
        this.hasHotel = false;
        this.isMortgaged = false;
        this.mortgageValue = purchasePrice != null ? purchasePrice.divide(new BigDecimal("2")) : BigDecimal.ZERO;
    }
    
    // Getters
    public String getPropertyId() { return propertyId; }
    public String getName() { return name; }
    public PropertyType getType() { return type; }
    public PropertyGroup getGroup() { return group; }
    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public BigDecimal getBaseRent() { return baseRent; }
    public BigDecimal getHouseCost() { return houseCost; }
    public BigDecimal getHotelCost() { return hotelCost; }
    public Player getOwner() { return owner; }
    public int getHouses() { return houses; }
    public boolean hasHotel() { return hasHotel; }
    public boolean isMortgaged() { return isMortgaged; }
    public BigDecimal getMortgageValue() { return mortgageValue; }
    
    // Ownership operations
    public void setOwner(Player owner) {
        this.owner = owner;
    }
    
    public boolean isOwned() {
        return owner != null;
    }
    
    public boolean isOwnedBy(Player player) {
        return owner != null && owner.equals(player);
    }
    
    // Development operations
    public boolean canBuildHouse() {
        if (type != PropertyType.STREET || hasHotel || houses >= 4 || isMortgaged) {
            return false;
        }
        
        // Check if owner has complete color group
        if (owner != null && owner.ownsCompleteGroup(group)) {
            return true;
        }
        
        return false;
    }
    
    public boolean buildHouse() {
        if (!canBuildHouse() || owner == null || !owner.canAfford(houseCost)) {
            return false;
        }
        
        if (owner.subtractMoney(houseCost)) {
            houses++;
            return true;
        }
        return false;
    }
    
    public boolean canBuildHotel() {
        return type == PropertyType.STREET && houses == 4 && !hasHotel && !isMortgaged &&
               owner != null && owner.ownsCompleteGroup(group);
    }
    
    public boolean buildHotel() {
        if (!canBuildHotel() || owner == null || !owner.canAfford(hotelCost)) {
            return false;
        }
        
        if (owner.subtractMoney(hotelCost)) {
            houses = 0; // Remove houses when building hotel
            hasHotel = true;
            return true;
        }
        return false;
    }
    
    public boolean sellHouse() {
        if (houses <= 0 || hasHotel || isMortgaged) {
            return false;
        }
        
        houses--;
        if (owner != null) {
            owner.addMoney(houseCost.divide(new BigDecimal("2"))); // Sell for half price
        }
        return true;
    }
    
    public boolean sellHotel() {
        if (!hasHotel || isMortgaged) {
            return false;
        }
        
        hasHotel = false;
        houses = 4; // Convert back to 4 houses
        if (owner != null) {
            owner.addMoney(hotelCost.divide(new BigDecimal("2"))); // Sell for half price
        }
        return true;
    }
    
    // Mortgage operations
    public boolean mortgage() {
        if (isMortgaged || houses > 0 || hasHotel) {
            return false;
        }
        
        isMortgaged = true;
        if (owner != null) {
            owner.addMoney(mortgageValue);
        }
        return true;
    }
    
    public boolean unmortgage() {
        if (!isMortgaged || owner == null) {
            return false;
        }
        
        BigDecimal unmortgageCost = mortgageValue.multiply(new BigDecimal("1.1")); // 10% interest
        if (!owner.canAfford(unmortgageCost)) {
            return false;
        }
        
        if (owner.subtractMoney(unmortgageCost)) {
            isMortgaged = false;
            return true;
        }
        return false;
    }
    
    // Rent calculation
    public BigDecimal calculateRent(int diceRoll) {
        if (!isOwned() || isMortgaged) {
            return BigDecimal.ZERO;
        }
        
        switch (type) {
            case STREET:
                return calculateStreetRent();
            case RAILROAD:
                return calculateRailroadRent();
            case UTILITY:
                return calculateUtilityRent(diceRoll);
            default:
                return BigDecimal.ZERO;
        }
    }
    
    private BigDecimal calculateStreetRent() {
        if (hasHotel) {
            return baseRent.multiply(new BigDecimal("5")); // Hotel rent is typically 5x base rent
        } else if (houses > 0) {
            // Rent increases with each house
            BigDecimal multiplier = new BigDecimal(String.valueOf(houses));
            return baseRent.multiply(multiplier);
        } else if (owner != null && owner.ownsCompleteGroup(group)) {
            return baseRent.multiply(new BigDecimal("2")); // Double rent for complete color group
        } else {
            return baseRent;
        }
    }
    
    private BigDecimal calculateRailroadRent() {
        if (owner == null) return BigDecimal.ZERO;
        
        long railroadsOwned = owner.getOwnedProperties().stream()
                .filter(p -> p.getType() == PropertyType.RAILROAD)
                .count();
        
        // Railroad rent: $25, $50, $100, $200 for 1, 2, 3, 4 railroads
        BigDecimal baseRailroadRent = new BigDecimal("25");
        BigDecimal multiplier = new BigDecimal(String.valueOf(Math.pow(2, railroadsOwned - 1)));
        return baseRailroadRent.multiply(multiplier);
    }
    
    private BigDecimal calculateUtilityRent(int diceRoll) {
        if (owner == null) return BigDecimal.ZERO;
        
        long utilitiesOwned = owner.getOwnedProperties().stream()
                .filter(p -> p.getType() == PropertyType.UTILITY)
                .count();
        
        // Utility rent: 4x dice roll for 1 utility, 10x dice roll for both utilities
        BigDecimal multiplier = utilitiesOwned == 2 ? new BigDecimal("10") : new BigDecimal("4");
        return multiplier.multiply(new BigDecimal(String.valueOf(diceRoll)));
    }
    
    // Value calculation
    public BigDecimal getCurrentValue() {
        BigDecimal value = purchasePrice;
        
        if (houses > 0) {
            value = value.add(houseCost.multiply(new BigDecimal(String.valueOf(houses))));
        }
        
        if (hasHotel) {
            value = value.add(hotelCost);
        }
        
        if (isMortgaged) {
            value = value.subtract(mortgageValue);
        }
        
        return value;
    }
    
    @Override
    public String toString() {
        return String.format("Property{id='%s', name='%s', type=%s, group=%s, owner=%s, houses=%d, hotel=%s}",
                           propertyId, name, type, group, 
                           owner != null ? owner.getName() : "None", houses, hasHotel);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Property property = (Property) obj;
        return propertyId.equals(property.propertyId);
    }
    
    @Override
    public int hashCode() {
        return propertyId.hashCode();
    }
}