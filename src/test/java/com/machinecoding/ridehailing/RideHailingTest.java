package com.machinecoding.ridehailing;

import com.machinecoding.ridehailing.model.*;
import com.machinecoding.ridehailing.service.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Comprehensive unit tests for the Ride Hailing System.
 * Tests driver-rider matching, trip management, and fare calculation.
 */
public class RideHailingTest {
    
    public static void main(String[] args) {
        System.out.println("=== Ride Hailing System Unit Tests ===\n");
        
        runAllTests();
        
        System.out.println("\n=== All Tests Complete ===");
    }
    
    private static void runAllTests() {
        testDriverManagement();
        testRiderManagement();
        testLocationManagement();
        testDriverMatching();
        testTripCreation();
        testTripLifecycle();
        testFareCalculation();
        testStatistics();
        testEdgeCases();
        testConcurrency();
    }
    
    private static void testDriverManagement() {
        System.out.println("Test 1: Driver Management");
        
        try {
            RideHailingService service = new InMemoryRideHailingService();
            
            // Test driver registration
            Driver driver = createTestDriver("D001", "John Smith", VehicleType.ECONOMY);
            service.registerDriver(driver);
            
            // Test driver retrieval
            Optional<Driver> foundDriver = service.getDriver("D001");
            assert foundDriver.isPresent() : "Driver should be found";
            assert foundDriver.get().getName().equals("John Smith") : "Driver name should match";
            
            // Test driver status update
            boolean statusUpdated = service.updateDriverStatus("D001", DriverStatus.AVAILABLE);
            assert statusUpdated : "Driver status should be updated";
            
            Optional<Driver> updatedDriver = service.getDriver("D001");
            assert updatedDriver.get().getStatus() == DriverStatus.AVAILABLE : "Driver status should be AVAILABLE";
            
            System.out.println("   ✓ Driver management tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Driver management test failed: " + e.getMessage());
        }
    }
    
    private static void testRiderManagement() {
        System.out.println("\nTest 2: Rider Management");
        
        try {
            RideHailingService service = new InMemoryRideHailingService();
            
            // Test rider registration
            Rider rider = createTestRider("R001", "Alice Williams");
            service.registerRider(rider);
            
            // Test rider retrieval
            Optional<Rider> foundRider = service.getRider("R001");
            assert foundRider.isPresent() : "Rider should be found";
            assert foundRider.get().getName().equals("Alice Williams") : "Rider name should match";
            
            // Test payment method update
            boolean paymentUpdated = service.updateRiderPaymentMethod("R001", "PAYMENT_METHOD_1");
            assert paymentUpdated : "Payment method should be updated";
            
            Optional<Rider> updatedRider = service.getRider("R001");
            assert updatedRider.get().hasPaymentMethod() : "Rider should have payment method";
            
            System.out.println("   ✓ Rider management tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Rider management test failed: " + e.getMessage());
        }
    }
    
    private static void testLocationManagement() {
        System.out.println("\nTest 3: Location Management");
        
        try {
            RideHailingService service = new InMemoryRideHailingService();
            
            // Setup driver
            Driver driver = createTestDriver("D001", "John Smith", VehicleType.ECONOMY);
            service.registerDriver(driver);
            
            // Test location update
            Location location = new Location(40.7128, -74.0060, "Downtown");
            boolean locationUpdated = service.updateDriverLocation("D001", location);
            assert locationUpdated : "Driver location should be updated";
            
            // Test driver status update
            service.updateDriverStatus("D001", DriverStatus.AVAILABLE);
            
            // Test finding available drivers
            List<Driver> availableDrivers = service.getAvailableDrivers(location, 5.0);
            assert availableDrivers.size() == 1 : "Should find 1 available driver";
            assert availableDrivers.get(0).getDriverId().equals("D001") : "Should find driver D001";
            
            // Test finding drivers by vehicle type
            List<Driver> economyDrivers = service.getAvailableDriversByType(location, 5.0, VehicleType.ECONOMY);
            assert economyDrivers.size() == 1 : "Should find 1 economy driver";
            
            List<Driver> premiumDrivers = service.getAvailableDriversByType(location, 5.0, VehicleType.PREMIUM);
            assert premiumDrivers.isEmpty() : "Should find 0 premium drivers";
            
            System.out.println("   ✓ Location management tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Location management test failed: " + e.getMessage());
        }
    }
    
    private static void testDriverMatching() {
        System.out.println("\nTest 4: Driver Matching");
        
        try {
            RideHailingService service = new InMemoryRideHailingService();
            
            // Setup multiple drivers at different locations
            setupTestDrivers(service);
            
            // Test finding best driver
            Location pickupLocation = new Location(40.7128, -74.0060, "Downtown");
            Optional<Driver> bestDriver = service.findBestDriver(pickupLocation, VehicleType.ECONOMY);
            
            assert bestDriver.isPresent() : "Should find a best driver";
            assert bestDriver.get().getVehicle().getType() == VehicleType.ECONOMY : "Best driver should have economy vehicle";
            
            // Test driver matching with different vehicle types
            Optional<Driver> premiumDriver = service.findBestDriver(pickupLocation, VehicleType.PREMIUM);
            assert premiumDriver.isPresent() : "Should find a premium driver";
            assert premiumDriver.get().getVehicle().getType() == VehicleType.PREMIUM : "Should match with premium vehicle";
            
            Optional<Driver> suvDriver = service.findBestDriver(pickupLocation, VehicleType.SUV);
            assert suvDriver.isPresent() : "Should find an SUV driver";
            assert suvDriver.get().getVehicle().getType() == VehicleType.SUV : "Should match with SUV vehicle";
            
            System.out.println("   ✓ Driver matching tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Driver matching test failed: " + e.getMessage());
        }
    }
    
    private static void testTripCreation() {
        System.out.println("\nTest 5: Trip Creation");
        
        try {
            RideHailingService service = new InMemoryRideHailingService();
            
            // Setup drivers and riders
            setupTestDrivers(service);
            setupTestRiders(service);
            
            // Test trip creation
            Location pickupLocation = new Location(40.7128, -74.0060, "Downtown");
            Location dropoffLocation = new Location(40.7549, -73.9840, "Midtown");
            
            Trip trip = service.requestRide("R001", pickupLocation, dropoffLocation, 
                                          VehicleType.ECONOMY, "No special instructions");
            
            assert trip != null : "Trip should be created";
            assert trip.getRiderId().equals("R001") : "Trip should be for rider R001";
            assert trip.getStatus() == TripStatus.DRIVER_ASSIGNED : "Trip status should be DRIVER_ASSIGNED";
            assert trip.getFare() != null : "Trip fare should be calculated";
            
            // Test trip retrieval
            Optional<Trip> foundTrip = service.getTrip(trip.getTripId());
            assert foundTrip.isPresent() : "Trip should be found";
            
            // Test trips by rider
            List<Trip> riderTrips = service.getTripsByRider("R001");
            assert riderTrips.size() == 1 : "Should find 1 trip for rider";
            
            // Test trips by driver
            String driverId = trip.getDriverId();
            List<Trip> driverTrips = service.getTripsByDriver(driverId);
            assert driverTrips.size() == 1 : "Should find 1 trip for driver";
            
            // Test active trips
            List<Trip> activeTrips = service.getActiveTrips();
            assert activeTrips.size() == 1 : "Should have 1 active trip";
            
            System.out.println("   ✓ Trip creation tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Trip creation test failed: " + e.getMessage());
        }
    }
    
    private static void testTripLifecycle() {
        System.out.println("\nTest 6: Trip Lifecycle");
        
        try {
            RideHailingService service = new InMemoryRideHailingService();
            
            // Setup drivers and riders
            setupTestDrivers(service);
            setupTestRiders(service);
            
            // Create a trip
            Location pickupLocation = new Location(40.7128, -74.0060, "Downtown");
            Location dropoffLocation = new Location(40.7549, -73.9840, "Midtown");
            
            Trip trip = service.requestRide("R001", pickupLocation, dropoffLocation, 
                                          VehicleType.ECONOMY, null);
            String tripId = trip.getTripId();
            String driverId = trip.getDriverId();
            
            // Test driver status after assignment
            Optional<Driver> driver = service.getDriver(driverId);
            assert driver.get().getStatus() == DriverStatus.BUSY : "Driver status should be BUSY after assignment";
            
            // Test trip status progression
            boolean updated1 = service.updateTripStatus(tripId, TripStatus.DRIVER_EN_ROUTE);
            assert updated1 : "Trip status should be updated to DRIVER_EN_ROUTE";
            assert service.getTrip(tripId).get().getStatus() == TripStatus.DRIVER_EN_ROUTE : "Trip status should be DRIVER_EN_ROUTE";
            
            boolean updated2 = service.updateTripStatus(tripId, TripStatus.DRIVER_ARRIVED);
            assert updated2 : "Trip status should be updated to DRIVER_ARRIVED";
            
            boolean updated3 = service.updateTripStatus(tripId, TripStatus.PICKED_UP);
            assert updated3 : "Trip status should be updated to PICKED_UP";
            assert service.getTrip(tripId).get().getPickupTime() != null : "Pickup time should be set";
            
            boolean updated4 = service.updateTripStatus(tripId, TripStatus.IN_PROGRESS);
            assert updated4 : "Trip status should be updated to IN_PROGRESS";
            
            // Test trip completion
            boolean completed = service.completeTrip(tripId, 4.5, 5.0);
            assert completed : "Trip should be completed";
            
            Trip completedTrip = service.getTrip(tripId).get();
            assert completedTrip.getStatus() == TripStatus.COMPLETED : "Trip status should be COMPLETED";
            assert completedTrip.getDropoffTime() != null : "Dropoff time should be set";
            assert completedTrip.getRiderRating() == 4.5 : "Driver should be rated 4.5";
            assert completedTrip.getDriverRating() == 5.0 : "Rider should be rated 5.0";
            
            // Test driver status after completion
            driver = service.getDriver(driverId);
            assert driver.get().getStatus() == DriverStatus.AVAILABLE : "Driver status should be AVAILABLE after completion";
            
            // Test trip cancellation
            Trip anotherTrip = service.requestRide("R002", pickupLocation, dropoffLocation, 
                                                VehicleType.COMFORT, null);
            
            boolean cancelled = service.cancelTrip(anotherTrip.getTripId());
            assert cancelled : "Trip should be cancelled";
            assert service.getTrip(anotherTrip.getTripId()).get().getStatus() == TripStatus.CANCELLED : "Trip status should be CANCELLED";
            
            System.out.println("   ✓ Trip lifecycle tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Trip lifecycle test failed: " + e.getMessage());
        }
    }
    
    private static void testFareCalculation() {
        System.out.println("\nTest 7: Fare Calculation");
        
        try {
            RideHailingService service = new InMemoryRideHailingService();
            
            // Test locations
            Location location1 = new Location(40.7128, -74.0060, "Downtown");
            Location location2 = new Location(40.7549, -73.9840, "Midtown"); // ~5km from downtown
            
            // Test basic fare calculation
            BigDecimal economyFare = service.calculateFare(location1, location2, VehicleType.ECONOMY);
            assert economyFare.compareTo(BigDecimal.ZERO) > 0 : "Fare should be positive";
            
            // Test vehicle type pricing
            BigDecimal comfortFare = service.calculateFare(location1, location2, VehicleType.COMFORT);
            BigDecimal premiumFare = service.calculateFare(location1, location2, VehicleType.PREMIUM);
            BigDecimal luxuryFare = service.calculateFare(location1, location2, VehicleType.LUXURY);
            
            assert comfortFare.compareTo(economyFare) > 0 : "Comfort fare should be higher than economy";
            assert premiumFare.compareTo(comfortFare) > 0 : "Premium fare should be higher than comfort";
            assert luxuryFare.compareTo(premiumFare) > 0 : "Luxury fare should be higher than premium";
            
            // Test surge pricing
            BigDecimal surgeFare = service.calculateFareWithSurge(location1, location2, VehicleType.ECONOMY, 2.0);
            assert surgeFare.compareTo(economyFare.multiply(BigDecimal.valueOf(2.0))) == 0 : "Surge fare should be double";
            
            // Test surge multiplier calculation
            double surgeMultiplier = service.getSurgeMultiplier(location1);
            assert surgeMultiplier >= 1.0 : "Surge multiplier should be at least 1.0";
            
            System.out.println("   ✓ Fare calculation tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Fare calculation test failed: " + e.getMessage());
        }
    }
    
    private static void testStatistics() {
        System.out.println("\nTest 8: Statistics");
        
        try {
            RideHailingService service = new InMemoryRideHailingService();
            
            // Setup drivers, riders, and trips
            setupTestDrivers(service);
            setupTestRiders(service);
            createTestTrips(service);
            
            // Test overall statistics
            RideHailingStats stats = service.getStats();
            assert stats.getTotalDrivers() > 0 : "Should have drivers";
            assert stats.getTotalRiders() > 0 : "Should have riders";
            assert stats.getTotalTrips() > 0 : "Should have trips";
            assert stats.getCompletedTrips() > 0 : "Should have completed trips";
            assert stats.getTotalRevenue().compareTo(BigDecimal.ZERO) > 0 : "Should have revenue";
            
            // Test derived statistics
            assert stats.getCompletionRate() >= 0.0 && stats.getCompletionRate() <= 100.0 : "Completion rate should be between 0 and 100";
            assert stats.getCancellationRate() >= 0.0 && stats.getCancellationRate() <= 100.0 : "Cancellation rate should be between 0 and 100";
            assert stats.getDriverUtilizationRate() >= 0.0 && stats.getDriverUtilizationRate() <= 100.0 : "Driver utilization rate should be between 0 and 100";
            
            System.out.println("   ✓ Statistics tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Statistics test failed: " + e.getMessage());
        }
    }
    
    private static void testEdgeCases() {
        System.out.println("\nTest 9: Edge Cases");
        
        try {
            RideHailingService service = new InMemoryRideHailingService();
            
            // Setup drivers and riders
            setupTestDrivers(service);
            setupTestRiders(service);
            
            // Test requesting ride with non-existent rider
            try {
                Location loc1 = new Location(40.7128, -74.0060);
                Location loc2 = new Location(40.7549, -73.9840);
                service.requestRide("NONEXISTENT", loc1, loc2, VehicleType.ECONOMY, null);
                assert false : "Should throw exception for non-existent rider";
            } catch (IllegalArgumentException e) {
                // Expected
            }
            
            // Test requesting ride without payment method
            try {
                // Register rider without payment method
                Rider noPaymentRider = createTestRider("R999", "No Payment");
                service.registerRider(noPaymentRider);
                
                Location loc1 = new Location(40.7128, -74.0060);
                Location loc2 = new Location(40.7549, -73.9840);
                service.requestRide("R999", loc1, loc2, VehicleType.ECONOMY, null);
                assert false : "Should throw exception for rider without payment method";
            } catch (IllegalArgumentException e) {
                // Expected
            }
            
            // Test updating non-existent trip
            boolean updated = service.updateTripStatus("NONEXISTENT", TripStatus.DRIVER_EN_ROUTE);
            assert !updated : "Should return false for non-existent trip";
            
            // Test cancelling non-existent trip
            boolean cancelled = service.cancelTrip("NONEXISTENT");
            assert !cancelled : "Should return false for non-existent trip";
            
            System.out.println("   ✓ Edge cases tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Edge cases test failed: " + e.getMessage());
        }
    }
    
    private static void testConcurrency() {
        System.out.println("\nTest 10: Concurrency");
        
        try {
            RideHailingService service = new InMemoryRideHailingService();
            
            // Setup drivers and riders
            setupTestDrivers(service);
            setupTestRiders(service);
            
            // Test concurrent driver location updates
            Thread[] threads = new Thread[5];
            for (int i = 0; i < 5; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    String driverId = "D00" + (index + 1);
                    Location location = new Location(40.7128 + (index * 0.01), -74.0060 + (index * 0.01));
                    service.updateDriverLocation(driverId, location);
                });
            }
            
            for (Thread thread : threads) {
                thread.start();
            }
            
            for (Thread thread : threads) {
                thread.join();
            }
            
            // Verify all locations were updated
            for (int i = 1; i <= 5; i++) {
                String driverId = "D00" + i;
                Optional<Driver> driver = service.getDriver(driverId);
                assert driver.isPresent() && driver.get().getCurrentLocation() != null : 
                       "Driver " + driverId + " location should be updated";
            }
            
            System.out.println("   ✓ Concurrency tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Concurrency test failed: " + e.getMessage());
        }
    }
    
    // Helper methods
    private static Driver createTestDriver(String id, String name, VehicleType vehicleType) {
        Vehicle vehicle = new Vehicle(
            "V" + id.substring(1),
            "Toyota",
            "Camry",
            2020,
            "Black",
            "ABC" + id.substring(1),
            vehicleType,
            vehicleType.getStandardCapacity()
        );
        
        return new Driver(
            id,
            name,
            "+1-555-" + id.substring(1),
            "LICENSE_" + id,
            vehicle
        );
    }
    
    private static Rider createTestRider(String id, String name) {
        return new Rider(
            id,
            name,
            "+1-555-" + id.substring(1),
            name.toLowerCase().replace(" ", ".") + "@example.com"
        );
    }
    
    private static void setupTestDrivers(RideHailingService service) {
        // Register drivers with different vehicle types
        Driver driver1 = createTestDriver("D001", "John Smith", VehicleType.ECONOMY);
        Driver driver2 = createTestDriver("D002", "Maria Garcia", VehicleType.COMFORT);
        Driver driver3 = createTestDriver("D003", "David Lee", VehicleType.PREMIUM);
        Driver driver4 = createTestDriver("D004", "Sarah Johnson", VehicleType.SUV);
        Driver driver5 = createTestDriver("D005", "Michael Brown", VehicleType.LUXURY);
        
        service.registerDriver(driver1);
        service.registerDriver(driver2);
        service.registerDriver(driver3);
        service.registerDriver(driver4);
        service.registerDriver(driver5);
        
        // Set locations and make available
        Location downtown = new Location(40.7128, -74.0060, "Downtown");
        Location midtown = new Location(40.7549, -73.9840, "Midtown");
        Location uptown = new Location(40.8075, -73.9626, "Uptown");
        
        service.updateDriverLocation("D001", downtown);
        service.updateDriverStatus("D001", DriverStatus.AVAILABLE);
        
        service.updateDriverLocation("D002", midtown);
        service.updateDriverStatus("D002", DriverStatus.AVAILABLE);
        
        service.updateDriverLocation("D003", uptown);
        service.updateDriverStatus("D003", DriverStatus.AVAILABLE);
        
        service.updateDriverLocation("D004", downtown);
        service.updateDriverStatus("D004", DriverStatus.AVAILABLE);
        
        service.updateDriverLocation("D005", midtown);
        service.updateDriverStatus("D005", DriverStatus.AVAILABLE);
    }
    
    private static void setupTestRiders(RideHailingService service) {
        Rider rider1 = createTestRider("R001", "Alice Williams");
        Rider rider2 = createTestRider("R002", "Bob Miller");
        Rider rider3 = createTestRider("R003", "Carol Davis");
        
        service.registerRider(rider1);
        service.registerRider(rider2);
        service.registerRider(rider3);
        
        // Add payment methods
        service.updateRiderPaymentMethod("R001", "PAYMENT_R001");
        service.updateRiderPaymentMethod("R002", "PAYMENT_R002");
        service.updateRiderPaymentMethod("R003", "PAYMENT_R003");
    }
    
    private static void createTestTrips(RideHailingService service) {
        Location downtown = new Location(40.7128, -74.0060, "Downtown");
        Location midtown = new Location(40.7549, -73.9840, "Midtown");
        Location uptown = new Location(40.8075, -73.9626, "Uptown");
        
        // Create and complete some trips
        Trip trip1 = service.requestRide("R001", downtown, midtown, VehicleType.ECONOMY, null);
        service.updateTripStatus(trip1.getTripId(), TripStatus.DRIVER_EN_ROUTE);
        service.updateTripStatus(trip1.getTripId(), TripStatus.DRIVER_ARRIVED);
        service.updateTripStatus(trip1.getTripId(), TripStatus.PICKED_UP);
        service.updateTripStatus(trip1.getTripId(), TripStatus.IN_PROGRESS);
        service.completeTrip(trip1.getTripId(), 4.5, 5.0);
        
        Trip trip2 = service.requestRide("R002", midtown, uptown, VehicleType.COMFORT, null);
        service.updateTripStatus(trip2.getTripId(), TripStatus.DRIVER_EN_ROUTE);
        service.updateTripStatus(trip2.getTripId(), TripStatus.DRIVER_ARRIVED);
        service.updateTripStatus(trip2.getTripId(), TripStatus.PICKED_UP);
        service.updateTripStatus(trip2.getTripId(), TripStatus.IN_PROGRESS);
        service.completeTrip(trip2.getTripId(), 5.0, 4.0);
        
        // Create a cancelled trip
        Trip trip3 = service.requestRide("R003", uptown, downtown, VehicleType.PREMIUM, null);
        service.cancelTrip(trip3.getTripId());
    }
}