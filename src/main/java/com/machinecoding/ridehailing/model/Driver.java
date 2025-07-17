package com.machinecoding.ridehailing.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a driver in the ride hailing system.
 */
public class Driver {
    private final String driverId;
    private final String name;
    private final String phoneNumber;
    private final String licenseNumber;
    private final Vehicle vehicle;
    private Location currentLocation;
    private DriverStatus status;
    private double rating;
    private int totalTrips;
    private LocalDateTime lastActiveTime;
    
    public Driver(String driverId, String name, String phoneNumber, String licenseNumber, Vehicle vehicle) {
        if (driverId == null || driverId.trim().isEmpty()) {
            throw new IllegalArgumentException("Driver ID cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Driver name cannot be null or empty");
        }
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }
        if (licenseNumber == null || licenseNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("License number cannot be null or empty");
        }
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null");
        }
        
        this.driverId = driverId.trim();
        this.name = name.trim();
        this.phoneNumber = phoneNumber.trim();
        this.licenseNumber = licenseNumber.trim();
        this.vehicle = vehicle;
        this.status = DriverStatus.OFFLINE;
        this.rating = 5.0; // Default rating
        this.totalTrips = 0;
        this.lastActiveTime = LocalDateTime.now();
    }
    
    // Getters
    public String getDriverId() { return driverId; }
    public String getName() { return name; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getLicenseNumber() { return licenseNumber; }
    public Vehicle getVehicle() { return vehicle; }
    public Location getCurrentLocation() { return currentLocation; }
    public DriverStatus getStatus() { return status; }
    public double getRating() { return rating; }
    public int getTotalTrips() { return totalTrips; }
    public LocalDateTime getLastActiveTime() { return lastActiveTime; }
    
    /**
     * Updates the driver's current location.
     */
    public void updateLocation(Location location) {
        this.currentLocation = location;
        this.lastActiveTime = LocalDateTime.now();
    }
    
    /**
     * Updates the driver's status.
     */
    public void updateStatus(DriverStatus status) {
        this.status = status;
        this.lastActiveTime = LocalDateTime.now();
    }
    
    /**
     * Updates the driver's rating after a trip.
     */
    public void updateRating(double newRating) {
        if (newRating < 1.0 || newRating > 5.0) {
            throw new IllegalArgumentException("Rating must be between 1.0 and 5.0");
        }
        
        // Calculate weighted average rating
        double totalRatingPoints = this.rating * this.totalTrips + newRating;
        this.totalTrips++;
        this.rating = totalRatingPoints / this.totalTrips;
    }
    
    /**
     * Checks if the driver is available for a new trip.
     */
    public boolean isAvailable() {
        return status == DriverStatus.AVAILABLE && currentLocation != null;
    }
    
    /**
     * Checks if the driver is within the specified radius of a location.
     */
    public boolean isWithinRadius(Location location, double radiusKm) {
        return currentLocation != null && currentLocation.isWithinRadius(location, radiusKm);
    }
    
    /**
     * Gets the distance to a specific location.
     */
    public double getDistanceTo(Location location) {
        if (currentLocation == null) {
            return Double.MAX_VALUE;
        }
        return currentLocation.distanceTo(location);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Driver driver = (Driver) o;
        return Objects.equals(driverId, driver.driverId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(driverId);
    }
    
    @Override
    public String toString() {
        return String.format("Driver{id='%s', name='%s', status=%s, rating=%.1f, trips=%d, vehicle=%s}", 
                           driverId, name, status, rating, totalTrips, vehicle.getModel());
    }
}