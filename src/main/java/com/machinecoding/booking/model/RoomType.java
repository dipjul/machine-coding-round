package com.machinecoding.booking.model;

/**
 * Enumeration of different room types in a hotel.
 */
public enum RoomType {
    SINGLE("Single", 1, "Single occupancy room with one bed"),
    DOUBLE("Double", 2, "Double occupancy room with one double bed"),
    TWIN("Twin", 2, "Twin room with two single beds"),
    TRIPLE("Triple", 3, "Triple room with three beds"),
    QUAD("Quad", 4, "Quad room with four beds"),
    SUITE("Suite", 2, "Luxury suite with separate living area"),
    DELUXE("Deluxe", 2, "Deluxe room with premium amenities"),
    PRESIDENTIAL("Presidential", 4, "Presidential suite with luxury amenities");
    
    private final String displayName;
    private final int standardCapacity;
    private final String description;
    
    RoomType(String displayName, int standardCapacity, String description) {
        this.displayName = displayName;
        this.standardCapacity = standardCapacity;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getStandardCapacity() {
        return standardCapacity;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}