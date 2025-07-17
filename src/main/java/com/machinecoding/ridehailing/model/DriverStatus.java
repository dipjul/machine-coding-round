package com.machinecoding.ridehailing.model;

/**
 * Enumeration of driver statuses in the ride hailing system.
 */
public enum DriverStatus {
    OFFLINE("Offline", "Driver is not available for rides"),
    AVAILABLE("Available", "Driver is online and available for rides"),
    BUSY("Busy", "Driver is currently on a trip"),
    EN_ROUTE_TO_PICKUP("En Route to Pickup", "Driver is heading to pick up a rider"),
    ARRIVED_AT_PICKUP("Arrived at Pickup", "Driver has arrived at pickup location"),
    ON_TRIP("On Trip", "Driver is currently transporting a rider"),
    BREAK("On Break", "Driver is taking a break");
    
    private final String displayName;
    private final String description;
    
    DriverStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Checks if this status indicates the driver is available for new rides.
     */
    public boolean isAvailableForRides() {
        return this == AVAILABLE;
    }
    
    /**
     * Checks if this status indicates the driver is currently engaged with a trip.
     */
    public boolean isOnTrip() {
        return this == EN_ROUTE_TO_PICKUP || this == ARRIVED_AT_PICKUP || this == ON_TRIP;
    }
    
    /**
     * Checks if this status indicates the driver is online.
     */
    public boolean isOnline() {
        return this != OFFLINE;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}