package com.machinecoding.ridehailing.service;

import com.machinecoding.ridehailing.model.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * In-memory implementation of ride hailing service.
 * Provides thread-safe operations for driver-rider matching, trip management, and fare calculation.
 */
public class InMemoryRideHailingService implements RideHailingService {
    
    private final Map<String, Driver> drivers;
    private final Map<String, Rider> riders;
    private final Map<String, Trip> trips;
    private final AtomicInteger tripIdCounter;
    private final FareCalculator fareCalculator;
    private final DriverMatcher driverMatcher;
    
    public InMemoryRideHailingService() {
        this.drivers = new ConcurrentHashMap<>();
        this.riders = new ConcurrentHashMap<>();
        this.trips = new ConcurrentHashMap<>();
        this.tripIdCounter = new AtomicInteger(1);
        this.fareCalculator = new FareCalculator();
        this.driverMatcher = new DriverMatcher();
    }
    
    // Driver Management
    @Override
    public void registerDriver(Driver driver) {
        if (driver == null) {
            throw new IllegalArgumentException("Driver cannot be null");
        }
        drivers.put(driver.getDriverId(), driver);
    }
    
    @Override
    public Optional<Driver> getDriver(String driverId) {
        return Optional.ofNullable(drivers.get(driverId));
    }
    
    @Override
    public boolean updateDriverLocation(String driverId, Location location) {
        Driver driver = drivers.get(driverId);
        if (driver == null || location == null) {
            return false;
        }
        
        driver.updateLocation(location);
        return true;
    }
    
    @Override
    public boolean updateDriverStatus(String driverId, DriverStatus status) {
        Driver driver = drivers.get(driverId);
        if (driver == null || status == null) {
            return false;
        }
        
        driver.updateStatus(status);
        return true;
    }
    
    @Override
    public List<Driver> getAvailableDrivers(Location location, double radiusKm) {
        return drivers.values().stream()
                .filter(Driver::isAvailable)
                .filter(driver -> driver.isWithinRadius(location, radiusKm))
                .sorted(Comparator.comparingDouble(driver -> driver.getDistanceTo(location)))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Driver> getAvailableDriversByType(Location location, double radiusKm, VehicleType vehicleType) {
        return getAvailableDrivers(location, radiusKm).stream()
                .filter(driver -> driver.getVehicle().getType() == vehicleType)
                .collect(Collectors.toList());
    }
    
    // Rider Management
    @Override
    public void registerRider(Rider rider) {
        if (rider == null) {
            throw new IllegalArgumentException("Rider cannot be null");
        }
        riders.put(rider.getRiderId(), rider);
    }
    
    @Override
    public Optional<Rider> getRider(String riderId) {
        return Optional.ofNullable(riders.get(riderId));
    }
    
    @Override
    public boolean updateRiderPaymentMethod(String riderId, String paymentMethodId) {
        Rider rider = riders.get(riderId);
        if (rider == null) {
            return false;
        }
        
        rider.setPaymentMethod(paymentMethodId);
        return true;
    }
    
    // Trip Management
    @Override
    public Trip requestRide(String riderId, Location pickupLocation, Location dropoffLocation, 
                           VehicleType vehicleType, String specialInstructions) {
        // Validate rider exists
        Rider rider = riders.get(riderId);
        if (rider == null) {
            throw new IllegalArgumentException("Rider not found: " + riderId);
        }
        
        // Check if rider has payment method
        if (!rider.hasPaymentMethod()) {
            throw new IllegalArgumentException("Rider must have a payment method");
        }
        
        // Find available driver
        Optional<Driver> bestDriver = findBestDriver(pickupLocation, vehicleType);
        if (!bestDriver.isPresent()) {
            // Create trip with no driver available status
            String tripId = "TRIP" + String.format("%06d", tripIdCounter.getAndIncrement());
            Trip trip = new Trip(tripId, riderId, "", pickupLocation, dropoffLocation, vehicleType);
            trip.updateStatus(TripStatus.NO_DRIVER_AVAILABLE);
            trips.put(tripId, trip);
            return trip;
        }
        
        // Create trip
        String tripId = "TRIP" + String.format("%06d", tripIdCounter.getAndIncrement());
        Trip trip = new Trip(tripId, riderId, bestDriver.get().getDriverId(), 
                           pickupLocation, dropoffLocation, vehicleType);
        
        if (specialInstructions != null) {
            trip.setSpecialInstructions(specialInstructions);
        }
        
        // Calculate and set fare
        BigDecimal fare = calculateFare(pickupLocation, dropoffLocation, vehicleType);
        trip.setFare(fare);
        
        // Assign driver to trip
        assignDriverToTrip(tripId, bestDriver.get().getDriverId());
        
        trips.put(tripId, trip);
        return trip;
    }
    
    @Override
    public Optional<Trip> getTrip(String tripId) {
        return Optional.ofNullable(trips.get(tripId));
    }
    
    @Override
    public List<Trip> getTripsByRider(String riderId) {
        return trips.values().stream()
                .filter(trip -> trip.getRiderId().equals(riderId))
                .sorted(Comparator.comparing(Trip::getRequestTime).reversed())
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Trip> getTripsByDriver(String driverId) {
        return trips.values().stream()
                .filter(trip -> trip.getDriverId().equals(driverId))
                .sorted(Comparator.comparing(Trip::getRequestTime).reversed())
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Trip> getActiveTrips() {
        return trips.values().stream()
                .filter(Trip::isActive)
                .sorted(Comparator.comparing(Trip::getRequestTime))
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean cancelTrip(String tripId) {
        Trip trip = trips.get(tripId);
        if (trip == null || !trip.getStatus().canBeCancelled()) {
            return false;
        }
        
        trip.updateStatus(TripStatus.CANCELLED);
        
        // Free up the driver
        Driver driver = drivers.get(trip.getDriverId());
        if (driver != null) {
            driver.updateStatus(DriverStatus.AVAILABLE);
        }
        
        return true;
    }
    
    @Override
    public boolean updateTripStatus(String tripId, TripStatus status) {
        Trip trip = trips.get(tripId);
        if (trip == null) {
            return false;
        }
        
        trip.updateStatus(status);
        
        // Update driver status based on trip status
        Driver driver = drivers.get(trip.getDriverId());
        if (driver != null) {
            switch (status) {
                case DRIVER_EN_ROUTE:
                    driver.updateStatus(DriverStatus.EN_ROUTE_TO_PICKUP);
                    break;
                case DRIVER_ARRIVED:
                    driver.updateStatus(DriverStatus.ARRIVED_AT_PICKUP);
                    break;
                case PICKED_UP:
                case IN_PROGRESS:
                    driver.updateStatus(DriverStatus.ON_TRIP);
                    break;
                case COMPLETED:
                case CANCELLED:
                    driver.updateStatus(DriverStatus.AVAILABLE);
                    break;
            }
        }
        
        return true;
    }
    
    @Override
    public boolean completeTrip(String tripId, double riderRating, double driverRating) {
        Trip trip = trips.get(tripId);
        if (trip == null || trip.getStatus() != TripStatus.IN_PROGRESS) {
            return false;
        }
        
        // Update trip status and ratings
        trip.updateStatus(TripStatus.COMPLETED);
        trip.setRiderRating(riderRating);
        trip.setDriverRating(driverRating);
        
        // Update driver and rider ratings
        Driver driver = drivers.get(trip.getDriverId());
        if (driver != null) {
            driver.updateRating(riderRating);
            driver.updateStatus(DriverStatus.AVAILABLE);
        }
        
        Rider rider = riders.get(trip.getRiderId());
        if (rider != null) {
            rider.updateRating(driverRating);
        }
        
        return true;
    }
    
    // Driver Matching
    @Override
    public Optional<Driver> findBestDriver(Location pickupLocation, VehicleType vehicleType) {
        return driverMatcher.findBestDriver(drivers.values(), pickupLocation, vehicleType);
    }
    
    @Override
    public boolean assignDriverToTrip(String tripId, String driverId) {
        Trip trip = trips.get(tripId);
        Driver driver = drivers.get(driverId);
        
        if (trip == null || driver == null || !driver.isAvailable()) {
            return false;
        }
        
        // Update trip and driver status
        trip.updateStatus(TripStatus.DRIVER_ASSIGNED);
        driver.updateStatus(DriverStatus.BUSY);
        
        return true;
    }
    
    // Fare Calculation
    @Override
    public BigDecimal calculateFare(Location pickupLocation, Location dropoffLocation, VehicleType vehicleType) {
        return fareCalculator.calculateFare(pickupLocation, dropoffLocation, vehicleType, 1.0);
    }
    
    @Override
    public BigDecimal calculateFareWithSurge(Location pickupLocation, Location dropoffLocation, 
                                           VehicleType vehicleType, double surgeMultiplier) {
        return fareCalculator.calculateFare(pickupLocation, dropoffLocation, vehicleType, surgeMultiplier);
    }
    
    @Override
    public double getSurgeMultiplier(Location location) {
        // Simple surge calculation based on demand/supply ratio
        List<Driver> nearbyDrivers = getAvailableDrivers(location, 5.0); // 5km radius
        List<Trip> nearbyActiveTrips = trips.values().stream()
                .filter(Trip::isActive)
                .filter(trip -> trip.getPickupLocation().isWithinRadius(location, 5.0))
                .collect(Collectors.toList());
        
        if (nearbyDrivers.isEmpty()) {
            return 2.0; // High surge when no drivers available
        }
        
        double demandSupplyRatio = (double) nearbyActiveTrips.size() / nearbyDrivers.size();
        
        if (demandSupplyRatio > 2.0) {
            return 2.0;
        } else if (demandSupplyRatio > 1.5) {
            return 1.5;
        } else if (demandSupplyRatio > 1.0) {
            return 1.2;
        } else {
            return 1.0;
        }
    }
    
    // Statistics
    @Override
    public RideHailingStats getStats() {
        int totalDrivers = drivers.size();
        int activeDrivers = (int) drivers.values().stream().filter(Driver::isAvailable).count();
        int totalRiders = riders.size();
        int totalTrips = trips.size();
        int activeTrips = (int) trips.values().stream().filter(Trip::isActive).count();
        int completedTrips = (int) trips.values().stream().filter(Trip::isCompleted).count();
        int cancelledTrips = (int) trips.values().stream().filter(Trip::isCancelled).count();
        
        BigDecimal totalRevenue = trips.values().stream()
                .filter(Trip::isCompleted)
                .map(Trip::getFare)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal averageFare = completedTrips > 0 ? 
                totalRevenue.divide(BigDecimal.valueOf(completedTrips), 2, BigDecimal.ROUND_HALF_UP) : 
                BigDecimal.ZERO;
        
        double averageRating = drivers.values().stream()
                .mapToDouble(Driver::getRating)
                .average()
                .orElse(0.0);
        
        return new RideHailingStats(totalDrivers, activeDrivers, totalRiders, totalTrips,
                                  activeTrips, completedTrips, cancelledTrips, 
                                  totalRevenue, averageFare, averageRating);
    }
    
    @Override
    public DriverStats getDriverStats(String driverId) {
        // Implementation would return driver-specific statistics
        return null; // Simplified for demo
    }
    
    @Override
    public RiderStats getRiderStats(String riderId) {
        // Implementation would return rider-specific statistics
        return null; // Simplified for demo
    }
    
    /**
     * Driver matching algorithm implementation.
     */
    private static class DriverMatcher {
        
        public Optional<Driver> findBestDriver(Collection<Driver> drivers, Location pickupLocation, VehicleType vehicleType) {
            return drivers.stream()
                    .filter(Driver::isAvailable)
                    .filter(driver -> driver.getVehicle().getType() == vehicleType || 
                                    isCompatibleVehicleType(driver.getVehicle().getType(), vehicleType))
                    .filter(driver -> driver.getCurrentLocation() != null)
                    .min(Comparator
                            .comparingDouble((Driver driver) -> driver.getDistanceTo(pickupLocation))
                            .thenComparingDouble((Driver driver) -> -driver.getRating()) // Higher rating is better
                            .thenComparingInt((Driver driver) -> -driver.getTotalTrips()) // More experienced is better
                    );
        }
        
        private boolean isCompatibleVehicleType(VehicleType driverType, VehicleType requestedType) {
            // Allow upgrades but not downgrades
            switch (requestedType) {
                case ECONOMY:
                    return true; // Any vehicle can serve economy request
                case COMFORT:
                    return driverType != VehicleType.ECONOMY && driverType != VehicleType.POOL;
                case PREMIUM:
                    return driverType == VehicleType.PREMIUM || driverType == VehicleType.LUXURY;
                case SUV:
                    return driverType == VehicleType.SUV;
                case LUXURY:
                    return driverType == VehicleType.LUXURY;
                case POOL:
                    return driverType == VehicleType.POOL || driverType == VehicleType.ECONOMY;
                default:
                    return false;
            }
        }
    }
    
    /**
     * Fare calculation engine.
     */
    private static class FareCalculator {
        private static final BigDecimal BASE_FARE = new BigDecimal("2.50");
        private static final BigDecimal PER_KM_RATE = new BigDecimal("1.20");
        private static final BigDecimal PER_MINUTE_RATE = new BigDecimal("0.25");
        
        public BigDecimal calculateFare(Location pickup, Location dropoff, VehicleType vehicleType, double surgeMultiplier) {
            double distance = pickup.distanceTo(dropoff);
            
            // Estimate time based on distance (assuming average speed of 30 km/h in city)
            double estimatedTimeMinutes = (distance / 30.0) * 60.0;
            
            // Calculate base fare
            BigDecimal distanceFare = PER_KM_RATE.multiply(BigDecimal.valueOf(distance));
            BigDecimal timeFare = PER_MINUTE_RATE.multiply(BigDecimal.valueOf(estimatedTimeMinutes));
            BigDecimal totalFare = BASE_FARE.add(distanceFare).add(timeFare);
            
            // Apply vehicle type multiplier
            totalFare = totalFare.multiply(vehicleType.getPriceMultiplier());
            
            // Apply surge multiplier
            totalFare = totalFare.multiply(BigDecimal.valueOf(surgeMultiplier));
            
            return totalFare.setScale(2, BigDecimal.ROUND_HALF_UP);
        }
    }
}