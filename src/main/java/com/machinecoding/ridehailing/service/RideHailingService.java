package com.machinecoding.ridehailing.service;

import com.machinecoding.ridehailing.model.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Interface for ride hailing operations.
 * Provides driver-rider matching, trip management, and fare calculation.
 */
public interface RideHailingService {
    
    // Driver Management
    /**
     * Registers a new driver.
     */
    void registerDriver(Driver driver);
    
    /**
     * Gets a driver by ID.
     */
    Optional<Driver> getDriver(String driverId);
    
    /**
     * Updates driver location.
     */
    boolean updateDriverLocation(String driverId, Location location);
    
    /**
     * Updates driver status.
     */
    boolean updateDriverStatus(String driverId, DriverStatus status);
    
    /**
     * Gets all available drivers within radius of a location.
     */
    List<Driver> getAvailableDrivers(Location location, double radiusKm);
    
    /**
     * Gets available drivers by vehicle type.
     */
    List<Driver> getAvailableDriversByType(Location location, double radiusKm, VehicleType vehicleType);
    
    // Rider Management
    /**
     * Registers a new rider.
     */
    void registerRider(Rider rider);
    
    /**
     * Gets a rider by ID.
     */
    Optional<Rider> getRider(String riderId);
    
    /**
     * Updates rider payment method.
     */
    boolean updateRiderPaymentMethod(String riderId, String paymentMethodId);
    
    // Trip Management
    /**
     * Requests a ride.
     */
    Trip requestRide(String riderId, Location pickupLocation, Location dropoffLocation, 
                    VehicleType vehicleType, String specialInstructions);
    
    /**
     * Gets a trip by ID.
     */
    Optional<Trip> getTrip(String tripId);
    
    /**
     * Gets all trips for a rider.
     */
    List<Trip> getTripsByRider(String riderId);
    
    /**
     * Gets all trips for a driver.
     */
    List<Trip> getTripsByDriver(String driverId);
    
    /**
     * Gets all active trips.
     */
    List<Trip> getActiveTrips();
    
    /**
     * Cancels a trip.
     */
    boolean cancelTrip(String tripId);
    
    /**
     * Updates trip status.
     */
    boolean updateTripStatus(String tripId, TripStatus status);
    
    /**
     * Completes a trip with ratings.
     */
    boolean completeTrip(String tripId, double riderRating, double driverRating);
    
    // Driver Matching
    /**
     * Finds the best driver for a trip request.
     */
    Optional<Driver> findBestDriver(Location pickupLocation, VehicleType vehicleType);
    
    /**
     * Assigns a driver to a trip.
     */
    boolean assignDriverToTrip(String tripId, String driverId);
    
    // Fare Calculation
    /**
     * Calculates fare for a trip.
     */
    BigDecimal calculateFare(Location pickupLocation, Location dropoffLocation, VehicleType vehicleType);
    
    /**
     * Calculates fare with surge pricing.
     */
    BigDecimal calculateFareWithSurge(Location pickupLocation, Location dropoffLocation, 
                                     VehicleType vehicleType, double surgeMultiplier);
    
    /**
     * Gets current surge multiplier for a location.
     */
    double getSurgeMultiplier(Location location);
    
    // Statistics and Reporting
    /**
     * Gets ride hailing statistics.
     */
    RideHailingStats getStats();
    
    /**
     * Gets driver performance metrics.
     */
    DriverStats getDriverStats(String driverId);
    
    /**
     * Gets rider usage statistics.
     */
    RiderStats getRiderStats(String riderId);
}