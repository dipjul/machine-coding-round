package com.machinecoding.booking.model;

/**
 * Enumeration of booking statuses in the hotel system.
 */
public enum BookingStatus {
    PENDING("Pending", "Booking is pending confirmation"),
    CONFIRMED("Confirmed", "Booking is confirmed and active"),
    CHECKED_IN("Checked In", "Guest has checked in"),
    CHECKED_OUT("Checked Out", "Guest has checked out"),
    CANCELLED("Cancelled", "Booking has been cancelled"),
    NO_SHOW("No Show", "Guest did not show up for the booking");
    
    private final String displayName;
    private final String description;
    
    BookingStatus(String displayName, String description) {
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
     * Checks if this status represents an active booking.
     */
    public boolean isActive() {
        return this == PENDING || this == CONFIRMED || this == CHECKED_IN;
    }
    
    /**
     * Checks if this status allows modification.
     */
    public boolean canModify() {
        return this == PENDING || this == CONFIRMED;
    }
    
    /**
     * Checks if this status allows cancellation.
     */
    public boolean canCancel() {
        return this == PENDING || this == CONFIRMED;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}