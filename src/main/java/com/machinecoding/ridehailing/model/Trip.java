package com.machinecoding.ridehailing.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Represents a trip in the ride hailing system.
 */
public class Trip {
    private final String tripId;
    private final String riderId;
    private final String driverId;
    private final Location pickupLocation;
    private final Location dropoffLocation;
    private final VehicleType vehicleType;
    private final LocalDateTime requestTime;
    private TripStatus status;
    private LocalDateTime pickupTime;
    private LocalDateTime dropoffTime;
    private BigDecimal fare;
    private BigDecimal distance;
    private String specialInstructions;
    private Double riderRating;
    private Double driverRating;
    
    public Trip(String tripId, String riderId, String driverId, Location pickupLocation, 
               Location dropoffLocation, VehicleType vehicleType) {
        if (tripId == null || tripId.trim().isEmpty()) {
            throw new IllegalArgumentException("Trip ID cannot be null or empty");
        }
        if (riderId == null || riderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Rider ID cannot be null or empty");
        }
        if (driverId == null || driverId.trim().isEmpty()) {
            throw new IllegalArgumentException("Driver ID cannot be null or empty");
        }
        if (pickupLocation == null) {
            throw new IllegalArgumentException("Pickup location cannot be null");
        }
        if (dropoffLocation == null) {
            throw new IllegalArgumentException("Dropoff location cannot be null");
        }
        if (vehicleType == null) {
            throw new IllegalArgumentException("Vehicle type cannot be null");
        }
        
        this.tripId = tripId.trim();
        this.riderId = riderId.trim();
        this.driverId = driverId.trim();
        this.pickupLocation = pickupLocation;
        this.dropoffLocation = dropoffLocation;
        this.vehicleType = vehicleType;
        this.requestTime = LocalDateTime.now();
        this.status = TripStatus.REQUESTED;
        this.distance = BigDecimal.valueOf(pickupLocation.distanceTo(dropoffLocation));
    }
    
    // Getters
    public String getTripId() { return tripId; }
    public String getRiderId() { return riderId; }
    public String getDriverId() { return driverId; }
    public Location getPickupLocation() { return pickupLocation; }
    public Location getDropoffLocation() { return dropoffLocation; }
    public VehicleType getVehicleType() { return vehicleType; }
    public LocalDateTime getRequestTime() { return requestTime; }
    public TripStatus getStatus() { return status; }
    public LocalDateTime getPickupTime() { return pickupTime; }
    public LocalDateTime getDropoffTime() { return dropoffTime; }
    public BigDecimal getFare() { return fare; }
    public BigDecimal getDistance() { return distance; }
    public String getSpecialInstructions() { return specialInstructions; }
    public Double getRiderRating() { return riderRating; }
    public Double getDriverRating() { return driverRating; }
    
    /**
     * Updates the trip status.
     */
    public void updateStatus(TripStatus newStatus) {
        this.status = newStatus;
        
        // Set timestamps based on status
        switch (newStatus) {
            case PICKED_UP:
                this.pickupTime = LocalDateTime.now();
                break;
            case COMPLETED:
                this.dropoffTime = LocalDateTime.now();
                break;
        }
    }
    
    /**
     * Sets the fare for the trip.
     */
    public void setFare(BigDecimal fare) {
        if (fare == null || fare.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Fare cannot be null or negative");
        }
        this.fare = fare;
    }
    
    /**
     * Sets special instructions for the trip.
     */
    public void setSpecialInstructions(String instructions) {
        this.specialInstructions = instructions != null ? instructions.trim() : null;
    }
    
    /**
     * Sets the rider's rating for the driver.
     */
    public void setRiderRating(double rating) {
        if (rating < 1.0 || rating > 5.0) {
            throw new IllegalArgumentException("Rating must be between 1.0 and 5.0");
        }
        this.riderRating = rating;
    }
    
    /**
     * Sets the driver's rating for the rider.
     */
    public void setDriverRating(double rating) {
        if (rating < 1.0 || rating > 5.0) {
            throw new IllegalArgumentException("Rating must be between 1.0 and 5.0");
        }
        this.driverRating = rating;
    }
    
    /**
     * Gets the duration of the trip in minutes.
     */
    public long getTripDurationMinutes() {
        if (pickupTime == null || dropoffTime == null) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(pickupTime, dropoffTime);
    }
    
    /**
     * Gets the wait time before pickup in minutes.
     */
    public long getWaitTimeMinutes() {
        if (pickupTime == null) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(requestTime, pickupTime);
    }
    
    /**
     * Checks if the trip is active (in progress).
     */
    public boolean isActive() {
        return status.isActive();
    }
    
    /**
     * Checks if the trip is completed.
     */
    public boolean isCompleted() {
        return status == TripStatus.COMPLETED;
    }
    
    /**
     * Checks if the trip is cancelled.
     */
    public boolean isCancelled() {
        return status == TripStatus.CANCELLED;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trip trip = (Trip) o;
        return Objects.equals(tripId, trip.tripId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(tripId);
    }
    
    @Override
    public String toString() {
        return String.format("Trip{id='%s', rider='%s', driver='%s', status=%s, distance=%.2fkm, fare=%s}", 
                           tripId, riderId, driverId, status, 
                           distance != null ? distance.doubleValue() : 0.0, 
                           fare != null ? "$" + fare : "TBD");
    }
}