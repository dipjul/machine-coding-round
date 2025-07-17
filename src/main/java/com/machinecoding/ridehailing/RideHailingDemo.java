package com.machinecoding.ridehailing;

import com.machinecoding.ridehailing.model.*;
import com.machinecoding.ridehailing.service.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Comprehensive demonstration of the Ride Hailing System.
 * Shows driver-rider matching, trip management, and fare calculation.
 */
public class RideHailingDemo {
    
    public static void main(String[] args) {
        System.out.println("=== Ride Hailing System Demo ===\n");
        
        // Demo 1: Driver and Rider Registration
        System.out.println("=== Demo 1: Driver and Rider Registration ===");
        demonstrateRegistration();
        
        // Demo 2: Location and Availability Management
        System.out.println("\n=== Demo 2: Location and Availability Management ===");
        demonstrateLocationManagement();
        
        // Demo 3: Ride Requests and Driver Matching
        System.out.println("\n=== Demo 3: Ride Requests and Driver Matching ===");
        demonstrateRideRequests();
        
        // Demo 4: Trip Lifecycle Management
        System.out.println("\n=== Demo 4: Trip Lifecycle Management ===");
        demonstrateTripLifecycle();
        
        // Demo 5: Fare Calculation and Surge Pricing
        System.out.println("\n=== Demo 5: Fare Calculation and Surge Pricing ===");
        demonstrateFareCalculation();
        
        // Demo 6: Statistics and Reporting
        System.out.println("\n=== Demo 6: Statistics and Reporting ===");
        demonstrateStatistics();
        
        System.out.println("\n=== Demo Complete ===");
    }
    
    private static void demonstrateRegistration() {
        System.out.println("1. Creating ride hailing service:");
        RideHailingService rideService = new InMemoryRideHailingService();
        
        System.out.println("\n2. Registering drivers:");
        
        // Create and register drivers with different vehicle types
        Driver[] drivers = {
            createDriver("D001", "John Smith", VehicleType.ECONOMY),
            createDriver("D002", "Maria Garcia", VehicleType.COMFORT),
            createDriver("D003", "David Lee", VehicleType.PREMIUM),
            createDriver("D004", "Sarah Johnson", VehicleType.SUV),
            createDriver("D005", "Michael Brown", VehicleType.LUXURY)
        };
        
        for (Driver driver : drivers) {
            rideService.registerDriver(driver);
            System.out.println("   Registered: " + driver);
        }
        
        System.out.println("\n3. Registering riders:");
        
        // Create and register riders
        Rider[] riders = {
            createRider("R001", "Alice Williams"),
            createRider("R002", "Bob Miller"),
            createRider("R003", "Carol Davis")
        };
        
        for (Rider rider : riders) {
            rideService.registerRider(rider);
            System.out.println("   Registered: " + rider);
            
            // Add payment method for each rider
            rideService.updateRiderPaymentMethod(rider.getRiderId(), "PAYMENT_" + rider.getRiderId());
        }
        
        System.out.println("\n4. Lookup examples:");
        
        // Lookup driver and rider
        Optional<Driver> foundDriver = rideService.getDriver("D001");
        if (foundDriver.isPresent()) {
            System.out.println("   Found driver: " + foundDriver.get().getName());
        }
        
        Optional<Rider> foundRider = rideService.getRider("R001");
        if (foundRider.isPresent()) {
            System.out.println("   Found rider: " + foundRider.get().getName());
        }
    }
    
    private static void demonstrateLocationManagement() {
        RideHailingService rideService = new InMemoryRideHailingService();
        setupDriversAndRiders(rideService);
        
        System.out.println("1. Updating driver locations and status:");
        
        // Update driver locations to different areas
        Location downtownLocation = new Location(40.7128, -74.0060, "Downtown");
        Location midtownLocation = new Location(40.7549, -73.9840, "Midtown");
        Location airportLocation = new Location(40.6413, -73.7781, "Airport");
        
        rideService.updateDriverLocation("D001", downtownLocation);
        rideService.updateDriverStatus("D001", DriverStatus.AVAILABLE);
        System.out.println("   Driver D001 now at Downtown and AVAILABLE");
        
        rideService.updateDriverLocation("D002", midtownLocation);
        rideService.updateDriverStatus("D002", DriverStatus.AVAILABLE);
        System.out.println("   Driver D002 now at Midtown and AVAILABLE");
        
        rideService.updateDriverLocation("D003", airportLocation);
        rideService.updateDriverStatus("D003", DriverStatus.AVAILABLE);
        System.out.println("   Driver D003 now at Airport and AVAILABLE");
        
        rideService.updateDriverLocation("D004", downtownLocation);
        rideService.updateDriverStatus("D004", DriverStatus.AVAILABLE);
        System.out.println("   Driver D004 now at Downtown and AVAILABLE");
        
        rideService.updateDriverLocation("D005", midtownLocation);
        rideService.updateDriverStatus("D005", DriverStatus.BREAK);
        System.out.println("   Driver D005 now at Midtown and on BREAK");
        
        System.out.println("\n2. Finding available drivers by location:");
        
        // Find drivers near downtown (within 5km)
        List<Driver> downtownDrivers = rideService.getAvailableDrivers(downtownLocation, 5.0);
        System.out.println("   Available drivers near Downtown: " + downtownDrivers.size());
        for (Driver driver : downtownDrivers) {
            System.out.println("     - " + driver.getName() + " with " + driver.getVehicle().getType());
        }
        
        // Find drivers near midtown (within 5km)
        List<Driver> midtownDrivers = rideService.getAvailableDrivers(midtownLocation, 5.0);
        System.out.println("\n   Available drivers near Midtown: " + midtownDrivers.size());
        for (Driver driver : midtownDrivers) {
            System.out.println("     - " + driver.getName() + " with " + driver.getVehicle().getType());
        }
        
        System.out.println("\n3. Finding drivers by vehicle type:");
        
        // Find premium vehicles near downtown
        List<Driver> premiumDrivers = rideService.getAvailableDriversByType(downtownLocation, 10.0, VehicleType.PREMIUM);
        System.out.println("   Available PREMIUM vehicles near Downtown: " + premiumDrivers.size());
        
        // Find SUVs near downtown
        List<Driver> suvDrivers = rideService.getAvailableDriversByType(downtownLocation, 10.0, VehicleType.SUV);
        System.out.println("   Available SUVs near Downtown: " + suvDrivers.size());
    }
    
    private static void demonstrateRideRequests() {
        RideHailingService rideService = new InMemoryRideHailingService();
        setupDriversAndRiders(rideService);
        setupDriverLocations(rideService);
        
        System.out.println("1. Requesting rides with different vehicle types:");
        
        // Define locations
        Location downtownLocation = new Location(40.7128, -74.0060, "Downtown");
        Location midtownLocation = new Location(40.7549, -73.9840, "Midtown");
        Location brooklynLocation = new Location(40.6782, -73.9442, "Brooklyn");
        
        // Request economy ride
        Trip economyTrip = rideService.requestRide("R001", downtownLocation, midtownLocation, 
                                                VehicleType.ECONOMY, "No special instructions");
        System.out.println("   Economy ride: " + economyTrip);
        
        // Request premium ride
        Trip premiumTrip = rideService.requestRide("R002", midtownLocation, brooklynLocation, 
                                                VehicleType.PREMIUM, "Need extra legroom");
        System.out.println("   Premium ride: " + premiumTrip);
        
        // Request SUV ride
        Trip suvTrip = rideService.requestRide("R003", brooklynLocation, downtownLocation, 
                                            VehicleType.SUV, "Have 5 passengers");
        System.out.println("   SUV ride: " + suvTrip);
        
        System.out.println("\n2. Driver matching algorithm:");
        
        // Explain driver matching for each trip
        Optional<Driver> economyDriver = rideService.getDriver(economyTrip.getDriverId());
        if (economyDriver.isPresent()) {
            System.out.println("   Economy ride matched with: " + economyDriver.get().getName() + 
                             " (" + economyDriver.get().getVehicle().getType() + ")");
        }
        
        Optional<Driver> premiumDriver = rideService.getDriver(premiumTrip.getDriverId());
        if (premiumDriver.isPresent()) {
            System.out.println("   Premium ride matched with: " + premiumDriver.get().getName() + 
                             " (" + premiumDriver.get().getVehicle().getType() + ")");
        }
        
        Optional<Driver> suvDriver = rideService.getDriver(suvTrip.getDriverId());
        if (suvDriver.isPresent()) {
            System.out.println("   SUV ride matched with: " + suvDriver.get().getName() + 
                             " (" + suvDriver.get().getVehicle().getType() + ")");
        }
        
        System.out.println("\n3. Active trips overview:");
        
        List<Trip> activeTrips = rideService.getActiveTrips();
        System.out.println("   Total active trips: " + activeTrips.size());
        for (Trip trip : activeTrips) {
            System.out.println("     - " + trip);
        }
    }
    
    private static void demonstrateTripLifecycle() {
        RideHailingService rideService = new InMemoryRideHailingService();
        setupDriversAndRiders(rideService);
        setupDriverLocations(rideService);
        
        System.out.println("1. Creating a new trip:");
        
        // Define locations
        Location pickupLocation = new Location(40.7128, -74.0060, "Downtown");
        Location dropoffLocation = new Location(40.7549, -73.9840, "Midtown");
        
        // Request a ride
        Trip trip = rideService.requestRide("R001", pickupLocation, dropoffLocation, 
                                         VehicleType.COMFORT, "Please call upon arrival");
        String tripId = trip.getTripId();
        System.out.println("   New trip created: " + trip);
        
        System.out.println("\n2. Trip status progression:");
        
        // Driver en route to pickup
        rideService.updateTripStatus(tripId, TripStatus.DRIVER_EN_ROUTE);
        System.out.println("   Status updated to: " + rideService.getTrip(tripId).get().getStatus());
        
        // Driver arrived at pickup
        rideService.updateTripStatus(tripId, TripStatus.DRIVER_ARRIVED);
        System.out.println("   Status updated to: " + rideService.getTrip(tripId).get().getStatus());
        
        // Rider picked up
        rideService.updateTripStatus(tripId, TripStatus.PICKED_UP);
        System.out.println("   Status updated to: " + rideService.getTrip(tripId).get().getStatus());
        
        // Trip in progress
        rideService.updateTripStatus(tripId, TripStatus.IN_PROGRESS);
        System.out.println("   Status updated to: " + rideService.getTrip(tripId).get().getStatus());
        
        System.out.println("\n3. Trip completion with ratings:");
        
        // Complete trip with ratings
        rideService.completeTrip(tripId, 4.5, 5.0); // Driver rated 4.5, Rider rated 5.0
        Trip completedTrip = rideService.getTrip(tripId).get();
        System.out.println("   Trip completed with status: " + completedTrip.getStatus());
        System.out.println("   Driver rating: " + completedTrip.getRiderRating());
        System.out.println("   Rider rating: " + completedTrip.getDriverRating());
        
        System.out.println("\n4. Trip cancellation example:");
        
        // Create another trip
        Trip anotherTrip = rideService.requestRide("R002", pickupLocation, dropoffLocation, 
                                               VehicleType.ECONOMY, null);
        System.out.println("   New trip created: " + anotherTrip);
        
        // Cancel the trip
        boolean cancelled = rideService.cancelTrip(anotherTrip.getTripId());
        System.out.println("   Trip cancelled: " + cancelled);
        System.out.println("   New status: " + rideService.getTrip(anotherTrip.getTripId()).get().getStatus());
    }
    
    private static void demonstrateFareCalculation() {
        RideHailingService rideService = new InMemoryRideHailingService();
        
        System.out.println("1. Basic fare calculation:");
        
        // Define locations with different distances
        Location downtown = new Location(40.7128, -74.0060, "Downtown");
        Location midtown = new Location(40.7549, -73.9840, "Midtown"); // ~5km from downtown
        Location airport = new Location(40.6413, -73.7781, "Airport"); // ~20km from downtown
        
        // Calculate fares for different distances and vehicle types
        BigDecimal shortEconomyFare = rideService.calculateFare(downtown, midtown, VehicleType.ECONOMY);
        System.out.println("   Downtown to Midtown (5km) - Economy: $" + shortEconomyFare);
        
        BigDecimal shortPremiumFare = rideService.calculateFare(downtown, midtown, VehicleType.PREMIUM);
        System.out.println("   Downtown to Midtown (5km) - Premium: $" + shortPremiumFare);
        
        BigDecimal longEconomyFare = rideService.calculateFare(downtown, airport, VehicleType.ECONOMY);
        System.out.println("   Downtown to Airport (20km) - Economy: $" + longEconomyFare);
        
        BigDecimal longLuxuryFare = rideService.calculateFare(downtown, airport, VehicleType.LUXURY);
        System.out.println("   Downtown to Airport (20km) - Luxury: $" + longLuxuryFare);
        
        System.out.println("\n2. Surge pricing examples:");
        
        // Calculate surge multiplier for different locations
        double downtownSurge = rideService.getSurgeMultiplier(downtown);
        System.out.println("   Downtown surge multiplier: " + downtownSurge + "x");
        
        // Calculate fares with surge pricing
        BigDecimal surgeFare = rideService.calculateFareWithSurge(downtown, midtown, VehicleType.ECONOMY, 1.8);
        System.out.println("   Downtown to Midtown with 1.8x surge: $" + surgeFare);
        
        System.out.println("\n3. Fare breakdown components:");
        System.out.println("   Base fare: $2.50");
        System.out.println("   Per km rate: $1.20/km");
        System.out.println("   Per minute rate: $0.25/min");
        System.out.println("   Vehicle type multipliers:");
        System.out.println("     - Economy: 1.0x");
        System.out.println("     - Comfort: 1.2x");
        System.out.println("     - Premium: 1.5x");
        System.out.println("     - SUV: 1.3x");
        System.out.println("     - Luxury: 2.0x");
        System.out.println("     - Pool: 0.8x");
    }
    
    private static void demonstrateStatistics() {
        RideHailingService rideService = new InMemoryRideHailingService();
        setupDriversAndRiders(rideService);
        setupDriverLocations(rideService);
        createSampleTrips(rideService);
        
        System.out.println("1. Overall service statistics:");
        
        RideHailingStats stats = rideService.getStats();
        System.out.println("   " + stats);
        
        System.out.println("\n2. Detailed statistics:");
        System.out.println("   Total drivers: " + stats.getTotalDrivers());
        System.out.println("   Active drivers: " + stats.getActiveDrivers());
        System.out.println("   Driver utilization: " + String.format("%.1f%%", stats.getDriverUtilizationRate()));
        System.out.println("   Total riders: " + stats.getTotalRiders());
        System.out.println("   Total trips: " + stats.getTotalTrips());
        System.out.println("   Completed trips: " + stats.getCompletedTrips());
        System.out.println("   Cancelled trips: " + stats.getCancelledTrips());
        System.out.println("   Completion rate: " + String.format("%.1f%%", stats.getCompletionRate()));
        System.out.println("   Cancellation rate: " + String.format("%.1f%%", stats.getCancellationRate()));
        System.out.println("   Total revenue: $" + stats.getTotalRevenue());
        System.out.println("   Average fare: $" + stats.getAverageFare());
        System.out.println("   Average rating: " + String.format("%.1f", stats.getAverageRating()));
    }
    
    // Helper methods
    private static Driver createDriver(String id, String name, VehicleType vehicleType) {
        Vehicle vehicle = new Vehicle(
            "V" + id.substring(1), // Vehicle ID based on driver ID
            getRandomMake(),
            getRandomModel(),
            2020,
            getRandomColor(),
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
    
    private static Rider createRider(String id, String name) {
        return new Rider(
            id,
            name,
            "+1-555-" + id.substring(1),
            name.toLowerCase().replace(" ", ".") + "@example.com"
        );
    }
    
    private static void setupDriversAndRiders(RideHailingService service) {
        // Register drivers
        service.registerDriver(createDriver("D001", "John Smith", VehicleType.ECONOMY));
        service.registerDriver(createDriver("D002", "Maria Garcia", VehicleType.COMFORT));
        service.registerDriver(createDriver("D003", "David Lee", VehicleType.PREMIUM));
        service.registerDriver(createDriver("D004", "Sarah Johnson", VehicleType.SUV));
        service.registerDriver(createDriver("D005", "Michael Brown", VehicleType.LUXURY));
        
        // Register riders
        service.registerRider(createRider("R001", "Alice Williams"));
        service.registerRider(createRider("R002", "Bob Miller"));
        service.registerRider(createRider("R003", "Carol Davis"));
        
        // Add payment methods
        service.updateRiderPaymentMethod("R001", "PAYMENT_R001");
        service.updateRiderPaymentMethod("R002", "PAYMENT_R002");
        service.updateRiderPaymentMethod("R003", "PAYMENT_R003");
    }
    
    private static void setupDriverLocations(RideHailingService service) {
        // Set driver locations
        Location downtownLocation = new Location(40.7128, -74.0060, "Downtown");
        Location midtownLocation = new Location(40.7549, -73.9840, "Midtown");
        Location airportLocation = new Location(40.6413, -73.7781, "Airport");
        
        service.updateDriverLocation("D001", downtownLocation);
        service.updateDriverStatus("D001", DriverStatus.AVAILABLE);
        
        service.updateDriverLocation("D002", midtownLocation);
        service.updateDriverStatus("D002", DriverStatus.AVAILABLE);
        
        service.updateDriverLocation("D003", airportLocation);
        service.updateDriverStatus("D003", DriverStatus.AVAILABLE);
        
        service.updateDriverLocation("D004", downtownLocation);
        service.updateDriverStatus("D004", DriverStatus.AVAILABLE);
        
        service.updateDriverLocation("D005", midtownLocation);
        service.updateDriverStatus("D005", DriverStatus.AVAILABLE);
    }
    
    private static void createSampleTrips(RideHailingService service) {
        // Define locations
        Location downtown = new Location(40.7128, -74.0060, "Downtown");
        Location midtown = new Location(40.7549, -73.9840, "Midtown");
        Location brooklyn = new Location(40.6782, -73.9442, "Brooklyn");
        
        // Create and complete some trips
        Trip trip1 = service.requestRide("R001", downtown, midtown, VehicleType.ECONOMY, null);
        service.updateTripStatus(trip1.getTripId(), TripStatus.DRIVER_EN_ROUTE);
        service.updateTripStatus(trip1.getTripId(), TripStatus.DRIVER_ARRIVED);
        service.updateTripStatus(trip1.getTripId(), TripStatus.PICKED_UP);
        service.updateTripStatus(trip1.getTripId(), TripStatus.IN_PROGRESS);
        service.completeTrip(trip1.getTripId(), 5.0, 4.5);
        
        Trip trip2 = service.requestRide("R002", midtown, brooklyn, VehicleType.PREMIUM, null);
        service.updateTripStatus(trip2.getTripId(), TripStatus.DRIVER_EN_ROUTE);
        service.updateTripStatus(trip2.getTripId(), TripStatus.DRIVER_ARRIVED);
        service.updateTripStatus(trip2.getTripId(), TripStatus.PICKED_UP);
        service.updateTripStatus(trip2.getTripId(), TripStatus.IN_PROGRESS);
        service.completeTrip(trip2.getTripId(), 4.0, 5.0);
        
        Trip trip3 = service.requestRide("R003", brooklyn, downtown, VehicleType.SUV, null);
        service.cancelTrip(trip3.getTripId());
        
        // Create an active trip
        Trip trip4 = service.requestRide("R001", downtown, brooklyn, VehicleType.COMFORT, null);
        service.updateTripStatus(trip4.getTripId(), TripStatus.DRIVER_EN_ROUTE);
    }
    
    private static String getRandomMake() {
        String[] makes = {"Toyota", "Honda", "Ford", "Chevrolet", "Nissan", "Hyundai"};
        return makes[(int)(Math.random() * makes.length)];
    }
    
    private static String getRandomModel() {
        String[] models = {"Camry", "Accord", "Focus", "Malibu", "Altima", "Sonata"};
        return models[(int)(Math.random() * models.length)];
    }
    
    private static String getRandomColor() {
        String[] colors = {"Black", "White", "Silver", "Blue", "Red", "Gray"};
        return colors[(int)(Math.random() * colors.length)];
    }
}