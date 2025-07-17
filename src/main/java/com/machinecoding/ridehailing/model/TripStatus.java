package com.machinecoding.ridehailing.model;

/**
 * Enumeration of trip statuses in the ride hailing system.
 */
public enum TripStatus {
    REQUESTED("Requested", "Trip has been requested by rider"),
    DRIVER_ASSIGNED("Driver Assigned", "Driver has been assigned to the trip"),
    DRIVER_EN_ROUTE("Driver En Route", "Driver is heading to pickup location"),
    DRIVER_ARRIVED("Driver Arrived", "Driver has arrived at pickup location"),
    PICKED_UP("Picked Up", "Rider has been picked up"),
    IN_PROGRESS("In Progress", "Trip is in progress"),
    COMPLETED("Completed", "Trip has been completed successfully"),
    CANCELLED("Cancelled", "Trip has been cancelled"),
    NO_DRIVER_AVAILABLE("No Driver Available", "No driver available for the trip");
    
    private final String displayName;
    private final String description;
    
    TripStatus(String displayName, String description) {
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
     * Checks if this status indicates an active trip.
     */
    public boolean isActive() {
        return this == DRIVER_ASSIGNED || this == DRIVER_EN_ROUTE || 
               this == DRIVER_ARRIVED || this == PICKED_UP || this == IN_PROGRESS;
    }
    
    /**
     * Checks if this status indicates a completed trip (successfully or not).
     */
    public boolean isFinished() {
        return this == COMPLETED || this == CANCELLED || this == NO_DRIVER_AVAILABLE;
    }
    
    /**
     * Checks if this status allows cancellation.
     */
    public boolean canBeCancelled() {
        return this == REQUESTED || this == DRIVER_ASSIGNED || 
               this == DRIVER_EN_ROUTE || this == DRIVER_ARRIVED;
    }
    
    /**
     * Gets the next logical status in the trip flow.
     */
    public TripStatus getNextStatus() {
        switch (this) {
            case REQUESTED:
                return DRIVER_ASSIGNED;
            case DRIVER_ASSIGNED:
                return DRIVER_EN_ROUTE;
            case DRIVER_EN_ROUTE:
                return DRIVER_ARRIVED;
            case DRIVER_ARRIVED:
                return PICKED_UP;
            case PICKED_UP:
                return IN_PROGRESS;
            case IN_PROGRESS:
                return COMPLETED;
            default:
                return this; // No next status for terminal states
        }
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}