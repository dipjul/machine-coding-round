# Chapter 7: Booking and Ordering Systems

Booking and ordering systems are critical components in many industries, from hospitality and travel to e-commerce and transportation. These systems handle complex business logic including inventory management, availability checking, pricing calculations, and transaction processing. This chapter covers the design and implementation of booking systems with a focus on hotel reservations, ride-hailing platforms, and e-commerce order management.

## Problem 1: Build a Hotel Booking System

### Problem Statement

Design and implement a comprehensive hotel booking system that manages room inventory, guest registrations, booking operations, and dynamic pricing. The system should handle concurrent bookings, availability checking, and provide comprehensive reporting capabilities.

**Functional Requirements:**
- Room inventory management with different room types and capacities
- Guest registration and profile management
- Real-time room availability checking
- Booking creation, modification, and cancellation
- Check-in and check-out operations
- Dynamic pricing based on dates, room types, and occupancy
- Booking statistics and revenue reporting
- Support for special requests and guest preferences

**Non-functional Requirements:**
- Thread-safe operations for concurrent bookings
- Efficient availability checking for large inventories
- Scalable architecture for multiple hotels
- Comprehensive validation and error handling
- Real-time pricing calculations
- Detailed audit trails and reporting

### Approach Analysis

#### Approach 1: Simple In-Memory System
**Pros:**
- Fast operations with no database overhead
- Simple implementation and testing
- Good for single-instance applications
- Easy to understand and debug

**Cons:**
- Data lost on restart
- Limited scalability
- No persistence for audit trails
- Single point of failure

#### Approach 2: Database-Backed System
**Pros:**
- Data persistence and durability
- ACID compliance for transactions
- Scalable with proper indexing
- Support for complex queries and reporting

**Cons:**
- Higher latency for operations
- Database becomes bottleneck
- Complex transaction management
- Requires database infrastructure

#### Approach 3: Event-Driven Architecture
**Pros:**
- Excellent scalability and decoupling
- Support for complex business workflows
- Audit trail through event sourcing
- Resilient to failures

**Cons:**
- Higher complexity
- Eventual consistency challenges
- Requires message queue infrastructure
- Complex debugging and monitoring

**Our Implementation**: We use an in-memory approach with thread-safe collections and comprehensive business logic to demonstrate core booking concepts while maintaining simplicity and performance.

### Implementation

#### Core Data Models

**Room Model:**
```java
public class Room {
    private final String roomId;
    private final String roomNumber;
    private final RoomType roomType;
    private final int capacity;
    private final BigDecimal basePrice;
    private final String description;
    private final boolean isActive;
    
    public Room(String roomId, String roomNumber, RoomType roomType, int capacity, 
               BigDecimal basePrice, String description, boolean isActive) {
        // Validation and initialization
        this.roomId = roomId.trim();
        this.roomNumber = roomNumber.trim();
        this.roomType = roomType;
        this.capacity = capacity;
        this.basePrice = basePrice;
        this.description = description != null ? description.trim() : "";
        this.isActive = isActive;
    }
    
    public Room withActiveStatus(boolean active) {
        return new Room(roomId, roomNumber, roomType, capacity, basePrice, description, active);
    }
}
```

**Guest Model:**
```java
public class Guest {
    private final String guestId;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String phoneNumber;
    private final LocalDate dateOfBirth;
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public boolean isAdult() {
        if (dateOfBirth == null) return true;
        return LocalDate.now().minusYears(18).isAfter(dateOfBirth) || 
               LocalDate.now().minusYears(18).equals(dateOfBirth);
    }
}
```

**Booking Model:**
```java
public class Booking {
    private final String bookingId;
    private final String guestId;
    private final String roomId;
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;
    private final int numberOfGuests;
    private final BigDecimal totalAmount;
    private BookingStatus status;
    
    public long getNumberOfNights() {
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }
    
    public boolean overlaps(LocalDate startDate, LocalDate endDate) {
        return !checkOutDate.isBefore(startDate) && !checkInDate.isAfter(endDate);
    }
    
    public boolean canModify() {
        return status.canModify();
    }
}
```

#### Service Layer Architecture

**Core Service Interface:**
```java
public interface HotelBookingService {
    // Room Management
    void addRoom(Room room);
    Optional<Room> getRoom(String roomId);
    List<Room> getRoomsByType(RoomType roomType);
    boolean updateRoomStatus(String roomId, boolean active);
    
    // Guest Management
    void registerGuest(Guest guest);
    Optional<Guest> getGuest(String guestId);
    List<Guest> findGuestsByEmail(String email);
    
    // Availability Checking
    boolean isRoomAvailable(String roomId, LocalDate checkIn, LocalDate checkOut);
    List<Room> getAvailableRooms(LocalDate checkIn, LocalDate checkOut, int guestCount);
    
    // Booking Operations
    Booking createBooking(String guestId, String roomId, LocalDate checkIn, LocalDate checkOut, 
                         int numberOfGuests, String specialRequests);
    boolean modifyBooking(String bookingId, LocalDate newCheckIn, LocalDate newCheckOut, 
                         int newGuestCount, String newSpecialRequests);
    boolean cancelBooking(String bookingId);
    boolean checkIn(String bookingId);
    boolean checkOut(String bookingId);
    
    // Pricing
    BigDecimal calculatePrice(String roomId, LocalDate checkIn, LocalDate checkOut, int guestCount);
    BigDecimal getRoomPrice(String roomId, LocalDate date);
}
```

#### Thread-Safe Implementation

**Concurrent Collections:**
```java
public class InMemoryHotelBookingService implements HotelBookingService {
    private final Map<String, Room> rooms;
    private final Map<String, Guest> guests;
    private final Map<String, Booking> bookings;
    private final AtomicInteger bookingIdCounter;
    private final PricingEngine pricingEngine;
    
    public InMemoryHotelBookingService() {
        this.rooms = new ConcurrentHashMap<>();
        this.guests = new ConcurrentHashMap<>();
        this.bookings = new ConcurrentHashMap<>();
        this.bookingIdCounter = new AtomicInteger(1);
        this.pricingEngine = new PricingEngine();
    }
}
```

**Availability Checking:**
```java
@Override
public boolean isRoomAvailable(String roomId, LocalDate checkIn, LocalDate checkOut) {
    Room room = rooms.get(roomId);
    if (room == null || !room.isActive()) {
        return false;
    }
    
    return bookings.values().stream()
            .filter(booking -> booking.getRoomId().equals(roomId))
            .filter(Booking::isActive)
            .noneMatch(booking -> booking.overlaps(checkIn, checkOut));
}
```

#### Dynamic Pricing Engine

**Pricing Strategy:**
```java
private static class PricingEngine {
    
    public BigDecimal calculatePrice(Room room, LocalDate checkIn, LocalDate checkOut, int guestCount) {
        long nights = checkOut.toEpochDay() - checkIn.toEpochDay();
        BigDecimal totalPrice = BigDecimal.ZERO;
        
        for (long i = 0; i < nights; i++) {
            LocalDate date = checkIn.plusDays(i);
            BigDecimal dailyPrice = getDynamicPrice(room, date);
            totalPrice = totalPrice.add(dailyPrice);
        }
        
        // Apply guest count multiplier for extra capacity
        if (guestCount > room.getRoomType().getStandardCapacity()) {
            BigDecimal extraGuestFee = BigDecimal.valueOf(25.0); // $25 per extra guest per night
            BigDecimal extraFee = extraGuestFee.multiply(BigDecimal.valueOf(guestCount - room.getRoomType().getStandardCapacity()))
                                              .multiply(BigDecimal.valueOf(nights));
            totalPrice = totalPrice.add(extraFee);
        }
        
        return totalPrice;
    }
    
    public BigDecimal getDynamicPrice(Room room, LocalDate date) {
        BigDecimal basePrice = room.getBasePrice();
        
        // Weekend pricing (Friday, Saturday)
        if (date.getDayOfWeek().getValue() >= 5) {
            basePrice = basePrice.multiply(BigDecimal.valueOf(1.2)); // 20% markup
        }
        
        // Holiday/peak season pricing
        int month = date.getMonthValue();
        if (month == 12 || month == 7 || month == 8) { // December, July, August
            basePrice = basePrice.multiply(BigDecimal.valueOf(1.3)); // 30% markup
        }
        
        // Room type premium
        switch (room.getRoomType()) {
            case SUITE:
                basePrice = basePrice.multiply(BigDecimal.valueOf(1.5));
                break;
            case DELUXE:
                basePrice = basePrice.multiply(BigDecimal.valueOf(1.25));
                break;
            case PRESIDENTIAL:
                basePrice = basePrice.multiply(BigDecimal.valueOf(2.0));
                break;
        }
        
        return basePrice.setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
```

### Key Features Demonstrated

#### 1. Room Inventory Management
```java
// Create hotel booking service
HotelBookingService hotelService = new InMemoryHotelBookingService();

// Add rooms to inventory
Room singleRoom = new Room("R001", "101", RoomType.SINGLE, 1, new BigDecimal("80.00"), "Cozy single room");
Room suiteRoom = new Room("R004", "201", RoomType.SUITE, 4, new BigDecimal("250.00"), "Luxury suite");

hotelService.addRoom(singleRoom);
hotelService.addRoom(suiteRoom);

// Get rooms by type
List<Room> suites = hotelService.getRoomsByType(RoomType.SUITE);
```

#### 2. Guest Registration and Management
```java
// Register guests
Guest guest = new Guest("G001", "John", "Doe", "john.doe@email.com", "+1-555-0101", 
                       LocalDate.of(1985, 5, 15), "ID123456", "123 Main St");
hotelService.registerGuest(guest);

// Find guests by email
List<Guest> foundGuests = hotelService.findGuestsByEmail("john.doe@email.com");
```

#### 3. Availability Checking and Booking
```java
// Check room availability
LocalDate checkIn = LocalDate.now().plusDays(7);
LocalDate checkOut = LocalDate.now().plusDays(10);

boolean available = hotelService.isRoomAvailable("R001", checkIn, checkOut);

// Find available rooms
List<Room> availableRooms = hotelService.getAvailableRooms(checkIn, checkOut, 2);

// Create booking
Booking booking = hotelService.createBooking("G001", "R001", checkIn, checkOut, 2, "Late check-in requested");
```

#### 4. Dynamic Pricing
```java
// Calculate total price for booking
BigDecimal totalPrice = hotelService.calculatePrice("R004", checkIn, checkOut, 3);

// Get room price for specific date
BigDecimal roomPrice = hotelService.getRoomPrice("R004", LocalDate.now().plusDays(7));

// Pricing varies by:
// - Weekend vs weekday rates
// - Seasonal pricing (holidays, peak seasons)
// - Room type premiums
// - Extra guest fees
```

#### 5. Booking Lifecycle Management
```java
// Modify booking
boolean modified = hotelService.modifyBooking(booking.getBookingId(),
                                            checkIn.plusDays(1),
                                            checkOut.plusDays(1),
                                            2, "Updated dates");

// Check-in guest
boolean checkedIn = hotelService.checkIn(booking.getBookingId());

// Check-out guest
boolean checkedOut = hotelService.checkOut(booking.getBookingId());

// Cancel booking
boolean cancelled = hotelService.cancelBooking(booking.getBookingId());
```

#### 6. Statistics and Reporting
```java
// Get comprehensive statistics
BookingStats stats = hotelService.getBookingStats();
System.out.println("Total bookings: " + stats.getTotalBookings());
System.out.println("Occupancy rate: " + stats.getOccupancyRate() + "%");
System.out.println("Total revenue: $" + stats.getTotalRevenue());

// Get occupancy rate for date range
double occupancyRate = hotelService.getOccupancyRate(startDate, endDate);

// Get revenue for date range
BigDecimal revenue = hotelService.getRevenue(startDate, endDate);
```

### Performance Characteristics

**Benchmark Results** (from demo):
- **Room Management**: 8 rooms with different types and capacities
- **Guest Registration**: Instant registration and lookup by ID/email
- **Availability Checking**: Real-time availability across all rooms
- **Booking Operations**: 3 concurrent bookings with different date ranges
- **Dynamic Pricing**: Weekend, holiday, and room-type based pricing
- **Statistics**: Real-time occupancy and revenue calculations

### Concurrency Design

#### Thread Safety Mechanisms
- **ConcurrentHashMap**: Thread-safe storage for rooms, guests, and bookings
- **AtomicInteger**: Lock-free booking ID generation
- **Immutable Models**: Room, Guest, and Booking objects are immutable where possible
- **Stream Processing**: Thread-safe filtering and aggregation operations

#### Performance Optimizations
- **Efficient Availability Checking**: Stream-based filtering with early termination
- **Lazy Pricing Calculation**: Calculate prices only when needed
- **Memory Efficient**: Minimal object creation and reuse of collections
- **Concurrent Operations**: Support for multiple simultaneous bookings

### Testing Strategy

The comprehensive test suite covers:

1. **Room Management**: Room creation, retrieval, and status updates
2. **Guest Management**: Registration, lookup, and validation
3. **Availability Checking**: Room availability with overlapping bookings
4. **Booking Operations**: Creation, modification, and lifecycle management
5. **Pricing Engine**: Dynamic pricing with various factors
6. **Statistics**: Occupancy rates, revenue calculations, and reporting
7. **Edge Cases**: Invalid inputs, double bookings, and error handling
8. **Concurrency**: Thread-safe operations under concurrent load
9. **Validation**: Input validation and business rule enforcement

### Common Interview Questions

1. **"How do you handle double bookings?"**
   - Use atomic operations for booking creation
   - Check availability within the same transaction
   - Implement optimistic locking for concurrent updates
   - Use database constraints for data integrity

2. **"How would you scale this to multiple hotels?"**
   - Partition data by hotel ID
   - Use distributed caching for availability
   - Implement hotel-specific pricing rules
   - Consider microservices architecture

3. **"How do you handle pricing complexity?"**
   - Strategy pattern for different pricing algorithms
   - Rule engine for complex pricing rules
   - Cache frequently accessed pricing data
   - Support for promotional codes and discounts

4. **"How do you ensure data consistency?"**
   - Use transactions for multi-step operations
   - Implement saga pattern for distributed transactions
   - Use event sourcing for audit trails
   - Implement compensating actions for failures

### Extensions and Improvements

1. **Payment Integration**: Credit card processing and payment gateways
2. **Inventory Management**: Room maintenance schedules and availability
3. **Loyalty Programs**: Points, rewards, and member pricing
4. **Multi-Hotel Support**: Chain management and cross-hotel bookings
5. **Mobile API**: REST APIs for mobile applications
6. **Real-Time Notifications**: Email and SMS confirmations
7. **Advanced Reporting**: Business intelligence and analytics
8. **Integration APIs**: Third-party booking platforms and OTAs

### Real-World Applications

1. **Hotel Chains**: Marriott, Hilton reservation systems
2. **Online Travel Agencies**: Booking.com, Expedia platform
3. **Vacation Rentals**: Airbnb, VRBO booking management
4. **Corporate Travel**: Business travel booking platforms
5. **Event Venues**: Conference and meeting room bookings
6. **Restaurant Reservations**: OpenTable, Resy systems
7. **Healthcare**: Appointment and facility booking

This Hotel Booking System implementation demonstrates essential concepts in:
- **Business Logic**: Complex booking rules and validation
- **Concurrency**: Thread-safe operations for high-traffic scenarios
- **Data Modeling**: Rich domain models with business behavior
- **System Design**: Scalable architecture with clear separation of concerns

The system showcases advanced programming concepts crucial for machine coding interviews focused on booking and reservation systems.

## Problem 2: Build a Ride Hailing System

### Problem Statement

Design and implement a comprehensive ride hailing system similar to Uber or Lyft that manages drivers, riders, trip matching, and fare calculations. The system should handle real-time driver-rider matching, trip lifecycle management, dynamic pricing, and provide comprehensive analytics.

**Functional Requirements:**
- Driver registration and profile management with vehicle information
- Rider registration and payment method management
- Real-time driver location tracking and availability management
- Driver-rider matching algorithm based on proximity and vehicle type
- Trip creation, status tracking, and completion workflow
- Dynamic fare calculation with surge pricing
- Rating system for both drivers and riders
- Trip history and analytics for all participants
- Support for different vehicle types (Economy, Premium, SUV, etc.)

**Non-functional Requirements:**
- Real-time location updates and driver matching
- Thread-safe operations for concurrent trip requests
- Scalable architecture for thousands of concurrent users
- Efficient spatial queries for driver-rider matching
- Low-latency trip matching and status updates
- Comprehensive audit trails and reporting
- Support for high-frequency location updates

### Approach Analysis

#### Approach 1: Simple In-Memory System
**Pros:**
- Fast operations with minimal latency
- Simple implementation and testing
- Good for demonstrating core concepts
- Easy debugging and monitoring

**Cons:**
- Data lost on restart
- Limited scalability for production
- No persistence for trip history
- Single point of failure

#### Approach 2: Database-Centric Architecture
**Pros:**
- Data persistence and durability
- ACID compliance for financial transactions
- Support for complex queries and reporting
- Scalable with proper indexing

**Cons:**
- Higher latency for real-time operations
- Database becomes bottleneck for location updates
- Complex spatial indexing requirements
- Requires sophisticated caching layer

#### Approach 3: Event-Driven Microservices
**Pros:**
- Excellent scalability and fault tolerance
- Real-time event processing capabilities
- Independent scaling of different services
- Support for complex business workflows

**Cons:**
- High architectural complexity
- Eventual consistency challenges
- Requires message queue infrastructure
- Complex debugging and monitoring

**Our Implementation**: We use an in-memory approach with thread-safe collections and efficient spatial algorithms to demonstrate core ride hailing concepts while maintaining simplicity and performance.

### Implementation

#### Core Data Models

**Driver Model:**
```java
public class Driver {
    private final String driverId;
    private final String name;
    private final String phoneNumber;
    private final String licenseNumber;
    private final Vehicle vehicle;
    private DriverStatus status;
    private Location currentLocation;
    private double rating;
    private int totalTrips;
    
    public Driver(String driverId, String name, String phoneNumber, 
                 String licenseNumber, Vehicle vehicle) {
        this.driverId = driverId.trim();
        this.name = name.trim();
        this.phoneNumber = phoneNumber.trim();
        this.licenseNumber = licenseNumber.trim();
        this.vehicle = vehicle;
        this.status = DriverStatus.OFFLINE;
        this.rating = 5.0; // Start with perfect rating
        this.totalTrips = 0;
    }
    
    public boolean isAvailable() {
        return status == DriverStatus.AVAILABLE;
    }
    
    public void updateRating(double newRating) {
        this.rating = ((rating * totalTrips) + newRating) / (totalTrips + 1);
    }
}
```

**Rider Model:**
```java
public class Rider {
    private final String riderId;
    private final String name;
    private final String phoneNumber;
    private final String email;
    private String paymentMethodId;
    private double rating;
    private int totalTrips;
    
    public Rider(String riderId, String name, String phoneNumber, String email) {
        this.riderId = riderId.trim();
        this.name = name.trim();
        this.phoneNumber = phoneNumber.trim();
        this.email = email.trim();
        this.rating = 5.0; // Start with perfect rating
        this.totalTrips = 0;
    }
    
    public boolean hasPaymentMethod() {
        return paymentMethodId != null && !paymentMethodId.trim().isEmpty();
    }
    
    public void updateRating(double newRating) {
        this.rating = ((rating * totalTrips) + newRating) / (totalTrips + 1);
    }
}
```

**Trip Model:**
```java
public class Trip {
    private final String tripId;
    private final String riderId;
    private final String driverId;
    private final Location pickupLocation;
    private final Location dropoffLocation;
    private final VehicleType requestedVehicleType;
    private final String specialInstructions;
    private final BigDecimal fare;
    private final double distance;
    private TripStatus status;
    private LocalDateTime requestTime;
    private LocalDateTime pickupTime;
    private LocalDateTime dropoffTime;
    private Double driverRating;
    private Double riderRating;
    
    public Trip(String tripId, String riderId, String driverId, Location pickupLocation,
               Location dropoffLocation, VehicleType requestedVehicleType, 
               String specialInstructions, BigDecimal fare, double distance) {
        this.tripId = tripId;
        this.riderId = riderId;
        this.driverId = driverId;
        this.pickupLocation = pickupLocation;
        this.dropoffLocation = dropoffLocation;
        this.requestedVehicleType = requestedVehicleType;
        this.specialInstructions = specialInstructions;
        this.fare = fare;
        this.distance = distance;
        this.status = TripStatus.DRIVER_ASSIGNED;
        this.requestTime = LocalDateTime.now();
    }
    
    public boolean isActive() {
        return status != TripStatus.COMPLETED && status != TripStatus.CANCELLED;
    }
    
    public long getDurationMinutes() {
        if (pickupTime == null || dropoffTime == null) return 0;
        return ChronoUnit.MINUTES.between(pickupTime, dropoffTime);
    }
}
```

**Location Model with Spatial Operations:**
```java
public class Location {
    private final double latitude;
    private final double longitude;
    private final String address;
    
    public Location(double latitude, double longitude, String address) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address != null ? address.trim() : "";
    }
    
    public Location(double latitude, double longitude) {
        this(latitude, longitude, "");
    }
    
    public double distanceTo(Location other) {
        // Haversine formula for calculating distance between two points on Earth
        final double R = 6371; // Earth's radius in kilometers
        
        double lat1Rad = Math.toRadians(this.latitude);
        double lat2Rad = Math.toRadians(other.latitude);
        double deltaLatRad = Math.toRadians(other.latitude - this.latitude);
        double deltaLonRad = Math.toRadians(other.longitude - this.longitude);
        
        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                  Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                  Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
}
```

#### Service Layer Architecture

**Core Service Interface:**
```java
public interface RideHailingService {
    // Driver Management
    void registerDriver(Driver driver);
    Optional<Driver> getDriver(String driverId);
    boolean updateDriverLocation(String driverId, Location location);
    boolean updateDriverStatus(String driverId, DriverStatus status);
    List<Driver> getAvailableDrivers(Location location, double radiusKm);
    List<Driver> getAvailableDriversByType(Location location, double radiusKm, VehicleType vehicleType);
    
    // Rider Management
    void registerRider(Rider rider);
    Optional<Rider> getRider(String riderId);
    boolean updateRiderPaymentMethod(String riderId, String paymentMethodId);
    
    // Trip Management
    Trip requestRide(String riderId, Location pickup, Location dropoff, 
                    VehicleType vehicleType, String specialInstructions);
    boolean updateTripStatus(String tripId, TripStatus status);
    boolean completeTrip(String tripId, double driverRating, double riderRating);
    boolean cancelTrip(String tripId);
    Optional<Trip> getTrip(String tripId);
    List<Trip> getTripsByRider(String riderId);
    List<Trip> getTripsByDriver(String driverId);
    List<Trip> getActiveTrips();
    
    // Driver Matching
    Optional<Driver> findBestDriver(Location pickupLocation, VehicleType vehicleType);
    
    // Fare Calculation
    BigDecimal calculateFare(Location pickup, Location dropoff, VehicleType vehicleType);
    BigDecimal calculateFareWithSurge(Location pickup, Location dropoff, VehicleType vehicleType, double surgeMultiplier);
    double getSurgeMultiplier(Location location);
    
    // Statistics and Analytics
    RideHailingStats getStats();
    DriverStats getDriverStats(String driverId);
    RiderStats getRiderStats(String riderId);
}
```

#### Driver-Rider Matching Algorithm

**Spatial Matching Implementation:**
```java
@Override
public Optional<Driver> findBestDriver(Location pickupLocation, VehicleType vehicleType) {
    return drivers.values().stream()
            .filter(Driver::isAvailable)
            .filter(driver -> driver.getCurrentLocation() != null)
            .filter(driver -> driver.getVehicle().getType() == vehicleType)
            .min(Comparator.comparing(driver -> {
                double distance = driver.getCurrentLocation().distanceTo(pickupLocation);
                double rating = driver.getRating();
                // Weighted score: 70% distance, 30% rating
                return (distance * 0.7) + ((5.0 - rating) * 0.3);
            }));
}

@Override
public List<Driver> getAvailableDrivers(Location location, double radiusKm) {
    return drivers.values().stream()
            .filter(Driver::isAvailable)
            .filter(driver -> driver.getCurrentLocation() != null)
            .filter(driver -> driver.getCurrentLocation().distanceTo(location) <= radiusKm)
            .sorted(Comparator.comparing(driver -> driver.getCurrentLocation().distanceTo(location)))
            .collect(Collectors.toList());
}
```

#### Dynamic Fare Calculation

**Fare Calculation Engine:**
```java
private static class FareCalculator {
    private static final BigDecimal BASE_FARE = new BigDecimal("2.50");
    private static final BigDecimal PER_KM_RATE = new BigDecimal("1.20");
    private static final BigDecimal PER_MINUTE_RATE = new BigDecimal("0.25");
    
    public BigDecimal calculateFare(Location pickup, Location dropoff, VehicleType vehicleType) {
        double distance = pickup.distanceTo(dropoff);
        double estimatedTime = distance * 2.5; // Rough estimate: 2.5 minutes per km in city
        
        BigDecimal distanceFare = PER_KM_RATE.multiply(BigDecimal.valueOf(distance));
        BigDecimal timeFare = PER_MINUTE_RATE.multiply(BigDecimal.valueOf(estimatedTime));
        BigDecimal totalFare = BASE_FARE.add(distanceFare).add(timeFare);
        
        // Apply vehicle type multiplier
        BigDecimal multiplier = getVehicleTypeMultiplier(vehicleType);
        totalFare = totalFare.multiply(multiplier);
        
        return totalFare.setScale(2, BigDecimal.ROUND_HALF_UP);
    }
    
    private BigDecimal getVehicleTypeMultiplier(VehicleType vehicleType) {
        switch (vehicleType) {
            case ECONOMY: return BigDecimal.valueOf(1.0);
            case COMFORT: return BigDecimal.valueOf(1.2);
            case PREMIUM: return BigDecimal.valueOf(1.5);
            case SUV: return BigDecimal.valueOf(1.3);
            case LUXURY: return BigDecimal.valueOf(2.0);
            case POOL: return BigDecimal.valueOf(0.8);
            default: return BigDecimal.valueOf(1.0);
        }
    }
}
```

#### Trip Lifecycle Management

**Trip Status Progression:**
```java
@Override
public boolean updateTripStatus(String tripId, TripStatus newStatus) {
    Trip trip = trips.get(tripId);
    if (trip == null) return false;
    
    // Validate status transition
    if (!isValidStatusTransition(trip.getStatus(), newStatus)) {
        return false;
    }
    
    // Update trip status and timestamps
    trip.setStatus(newStatus);
    
    switch (newStatus) {
        case PICKED_UP:
            trip.setPickupTime(LocalDateTime.now());
            break;
        case COMPLETED:
            trip.setDropoffTime(LocalDateTime.now());
            // Make driver available again
            updateDriverStatus(trip.getDriverId(), DriverStatus.AVAILABLE);
            break;
        case CANCELLED:
            // Make driver available again
            updateDriverStatus(trip.getDriverId(), DriverStatus.AVAILABLE);
            break;
    }
    
    return true;
}

private boolean isValidStatusTransition(TripStatus current, TripStatus next) {
    switch (current) {
        case DRIVER_ASSIGNED:
            return next == TripStatus.DRIVER_EN_ROUTE || next == TripStatus.CANCELLED;
        case DRIVER_EN_ROUTE:
            return next == TripStatus.DRIVER_ARRIVED || next == TripStatus.CANCELLED;
        case DRIVER_ARRIVED:
            return next == TripStatus.PICKED_UP || next == TripStatus.CANCELLED;
        case PICKED_UP:
            return next == TripStatus.IN_PROGRESS;
        case IN_PROGRESS:
            return next == TripStatus.COMPLETED;
        default:
            return false;
    }
}
```

### Key Features Demonstrated

#### 1. Driver and Rider Registration
```java
// Create ride hailing service
RideHailingService rideService = new InMemoryRideHailingService();

// Register drivers with different vehicle types
Vehicle economyVehicle = new Vehicle("V001", "Toyota", "Camry", 2020, "Black", "ABC123", VehicleType.ECONOMY, 4);
Driver driver = new Driver("D001", "John Smith", "+1-555-0101", "LICENSE123", economyVehicle);
rideService.registerDriver(driver);

// Register riders
Rider rider = new Rider("R001", "Alice Williams", "+1-555-0201", "alice@example.com");
rideService.registerRider(rider);
rideService.updateRiderPaymentMethod("R001", "PAYMENT_METHOD_123");
```

#### 2. Real-Time Location and Availability Management
```java
// Update driver location and status
Location downtownLocation = new Location(40.7128, -74.0060, "Downtown NYC");
rideService.updateDriverLocation("D001", downtownLocation);
rideService.updateDriverStatus("D001", DriverStatus.AVAILABLE);

// Find available drivers near location
List<Driver> nearbyDrivers = rideService.getAvailableDrivers(downtownLocation, 5.0); // Within 5km

// Find drivers by vehicle type
List<Driver> premiumDrivers = rideService.getAvailableDriversByType(downtownLocation, 10.0, VehicleType.PREMIUM);
```

#### 3. Trip Request and Driver Matching
```java
// Request a ride
Location pickupLocation = new Location(40.7128, -74.0060, "Downtown");
Location dropoffLocation = new Location(40.7549, -73.9840, "Midtown");

Trip trip = rideService.requestRide("R001", pickupLocation, dropoffLocation, 
                                  VehicleType.ECONOMY, "Please call upon arrival");

System.out.println("Trip created: " + trip);
System.out.println("Assigned driver: " + trip.getDriverId());
System.out.println("Estimated fare: $" + trip.getFare());
```

#### 4. Trip Lifecycle Management
```java
String tripId = trip.getTripId();

// Driver en route to pickup
rideService.updateTripStatus(tripId, TripStatus.DRIVER_EN_ROUTE);

// Driver arrived at pickup location
rideService.updateTripStatus(tripId, TripStatus.DRIVER_ARRIVED);

// Rider picked up
rideService.updateTripStatus(tripId, TripStatus.PICKED_UP);

// Trip in progress
rideService.updateTripStatus(tripId, TripStatus.IN_PROGRESS);

// Complete trip with ratings
rideService.completeTrip(tripId, 4.5, 5.0); // Driver rated 4.5, Rider rated 5.0
```

#### 5. Dynamic Fare Calculation
```java
// Calculate fare for different vehicle types
BigDecimal economyFare = rideService.calculateFare(pickupLocation, dropoffLocation, VehicleType.ECONOMY);
BigDecimal premiumFare = rideService.calculateFare(pickupLocation, dropoffLocation, VehicleType.PREMIUM);
BigDecimal luxuryFare = rideService.calculateFare(pickupLocation, dropoffLocation, VehicleType.LUXURY);

// Calculate fare with surge pricing
double surgeMultiplier = rideService.getSurgeMultiplier(pickupLocation);
BigDecimal surgeFare = rideService.calculateFareWithSurge(pickupLocation, dropoffLocation, 
                                                        VehicleType.ECONOMY, surgeMultiplier);

System.out.println("Economy fare: $" + economyFare);
System.out.println("Premium fare: $" + premiumFare);
System.out.println("Surge fare (2x): $" + surgeFare);
```

#### 6. Analytics and Statistics
```java
// Get overall service statistics
RideHailingStats stats = rideService.getStats();
System.out.println("Total drivers: " + stats.getTotalDrivers());
System.out.println("Active drivers: " + stats.getActiveDrivers());
System.out.println("Total trips: " + stats.getTotalTrips());
System.out.println("Completion rate: " + stats.getCompletionRate() + "%");
System.out.println("Total revenue: $" + stats.getTotalRevenue());

// Get driver-specific statistics
DriverStats driverStats = rideService.getDriverStats("D001");
System.out.println("Driver trips: " + driverStats.getTotalTrips());
System.out.println("Driver rating: " + driverStats.getAverageRating());
System.out.println("Driver earnings: $" + driverStats.getTotalEarnings());

// Get rider-specific statistics
RiderStats riderStats = rideService.getRiderStats("R001");
System.out.println("Rider trips: " + riderStats.getTotalTrips());
System.out.println("Total spent: $" + riderStats.getTotalSpent());
```

### Performance Characteristics

**Benchmark Results** (from demo):
- **Driver Management**: 5 drivers with different vehicle types
- **Real-time Matching**: Sub-second driver matching within 10km radius
- **Trip Processing**: 4 concurrent trips with different statuses
- **Fare Calculation**: Dynamic pricing with surge multipliers
- **Location Updates**: Efficient spatial queries using Haversine formula
- **Statistics**: Real-time analytics across all trips and users

### Concurrency Design

#### Thread Safety Mechanisms
- **ConcurrentHashMap**: Thread-safe storage for drivers, riders, and trips
- **AtomicInteger**: Lock-free trip ID generation
- **Synchronized Methods**: Critical sections for trip status updates
- **Immutable Location**: Thread-safe spatial calculations

#### Spatial Efficiency
- **Haversine Formula**: Accurate distance calculations on Earth's surface
- **Stream Processing**: Efficient filtering and sorting of nearby drivers
- **Lazy Evaluation**: Calculate distances only when needed
- **Indexed Lookups**: Fast retrieval by driver/rider/trip IDs

### Testing Strategy

The comprehensive test suite covers:

1. **Driver Management**: Registration, location updates, status changes
2. **Rider Management**: Registration, payment methods, profile updates
3. **Location Management**: Spatial queries and distance calculations
4. **Driver Matching**: Best driver selection algorithm
5. **Trip Creation**: End-to-end trip request workflow
6. **Trip Lifecycle**: Status transitions and validation
7. **Fare Calculation**: Dynamic pricing with various factors
8. **Statistics**: Real-time analytics and reporting
9. **Edge Cases**: Invalid inputs, no available drivers, payment issues
10. **Concurrency**: Thread-safe operations under load

### Common Interview Questions

1. **"How do you handle driver-rider matching at scale?"**
   - Use spatial indexing (R-tree, Geohash) for efficient location queries
   - Implement caching for frequently accessed areas
   - Use machine learning for demand prediction
   - Consider driver preferences and rider history

2. **"How do you calculate surge pricing?"**
   - Monitor supply-demand ratio in real-time
   - Use historical data for demand prediction
   - Implement gradual price adjustments
   - Consider external factors (weather, events)

3. **"How do you ensure trip consistency?"**
   - Use state machines for trip status transitions
   - Implement idempotent operations
   - Use event sourcing for audit trails
   - Handle network failures gracefully

4. **"How would you scale this globally?"**
   - Partition by geographic regions
   - Use CDN for static content
   - Implement multi-region deployment
   - Consider local regulations and pricing

### Extensions and Improvements

1. **Real-Time Tracking**: WebSocket connections for live location updates
2. **Route Optimization**: Integration with mapping services for optimal routes
3. **Payment Processing**: Credit card processing and split payments
4. **Scheduled Rides**: Advance booking and recurring trips
5. **Pool Rides**: Shared rides with multiple passengers
6. **Driver Incentives**: Bonus systems and performance rewards
7. **Safety Features**: Emergency buttons and trip sharing
8. **Multi-Modal Transport**: Integration with public transit

### Real-World Applications

1. **Ride Sharing**: Uber, Lyft, Didi ride hailing platforms
2. **Food Delivery**: DoorDash, Uber Eats delivery matching
3. **Package Delivery**: Amazon Flex, courier services
4. **Taxi Services**: Traditional taxi dispatch systems
5. **Bike Sharing**: Lime, Bird scooter rental systems
6. **Freight Logistics**: Truck-load matching platforms
7. **Emergency Services**: Ambulance and emergency response dispatch

This Ride Hailing System implementation demonstrates essential concepts in:
- **Spatial Algorithms**: Efficient location-based matching and queries
- **Real-Time Systems**: Low-latency updates and notifications
- **Business Logic**: Complex pricing and matching algorithms
- **State Management**: Trip lifecycle and status transitions
- **Analytics**: Real-time statistics and performance monitoring

The system showcases advanced programming concepts crucial for machine coding interviews focused on location-based and real-time systems.

## Problem 3: Build an E-commerce Order Management System

### Problem Statement

Design and implement a comprehensive e-commerce order management system that handles product catalog, inventory management, shopping cart operations, and order processing. The system should support the complete customer journey from product browsing to order fulfillment.

**Functional Requirements:**
- Product catalog management with categories, brands, and search functionality
- Customer registration and profile management
- Real-time inventory tracking with reservation and confirmation
- Shopping cart operations (add, update, remove items)
- Order creation with pricing calculations (tax, shipping)
- Order lifecycle management (pending → confirmed → processing → shipped → delivered)
- Order cancellation and refund processing
- Comprehensive statistics and reporting

**Non-functional Requirements:**
- Thread-safe operations for concurrent users
- Inventory consistency under high load
- Scalable architecture for large product catalogs
- Efficient search and filtering capabilities
- Real-time pricing calculations
- Comprehensive audit trails and reporting

### Approach Analysis

#### Approach 1: Simple In-Memory System
**Pros:**
- Fast operations with minimal latency
- Simple implementation and testing
- Good for demonstrating core concepts
- Easy debugging and monitoring

**Cons:**
- Data lost on restart
- Limited scalability for production
- No persistence for order history
- Single point of failure

#### Approach 2: Database-Centric Architecture
**Pros:**
- Data persistence and durability
- ACID compliance for financial transactions
- Support for complex queries and reporting
- Scalable with proper indexing

**Cons:**
- Higher latency for operations
- Database becomes bottleneck
- Complex transaction management
- Requires sophisticated caching

#### Approach 3: Microservices Architecture
**Pros:**
- Excellent scalability and fault tolerance
- Independent scaling of different services
- Technology diversity for different domains
- Support for complex business workflows

**Cons:**
- High architectural complexity
- Distributed transaction challenges
- Requires sophisticated infrastructure
- Complex debugging and monitoring

**Our Implementation**: We use an in-memory approach with thread-safe collections and comprehensive business logic to demonstrate core e-commerce concepts while maintaining simplicity and performance.

### Implementation

#### Core Data Models

**Product Model:**
```java
public class Product {
    private final String productId;
    private final String name;
    private final String description;
    private final BigDecimal price;
    private final String category;
    private final String brand;
    private final String sku;
    private final double weight;
    private final ProductStatus status;
    private final LocalDateTime createdAt;
    
    public Product(String productId, String name, String description, BigDecimal price,
                  String category, String brand, String sku, double weight, ProductStatus status) {
        this.productId = productId != null ? productId.trim() : "";
        this.name = name != null ? name.trim() : "";
        this.description = description != null ? description.trim() : "";
        this.price = price != null ? price : BigDecimal.ZERO;
        this.category = category != null ? category.trim() : "";
        this.brand = brand != null ? brand.trim() : "";
        this.sku = sku != null ? sku.trim() : "";
        this.weight = weight;
        this.status = status != null ? status : ProductStatus.INACTIVE;
        this.createdAt = LocalDateTime.now();
    }
    
    public boolean isActive() {
        return status == ProductStatus.ACTIVE;
    }
    
    public Product withPrice(BigDecimal newPrice) {
        return new Product(productId, name, description, newPrice, category, brand, sku, weight, status);
    }
}
```

**Customer Model:**
```java
public class Customer {
    private final String customerId;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String phoneNumber;
    private final Address defaultAddress;
    private final CustomerStatus status;
    private final LocalDateTime registeredAt;
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public boolean isActive() {
        return status == CustomerStatus.ACTIVE;
    }
}
```

**Inventory Model with Thread-Safe Operations:**
```java
public class Inventory {
    private final String productId;
    private int availableQuantity;
    private int reservedQuantity;
    private final int reorderLevel;
    private final int maxStockLevel;
    private final LocalDateTime lastUpdated;
    
    public synchronized boolean reserveQuantity(int quantity) {
        if (availableQuantity >= quantity) {
            availableQuantity -= quantity;
            reservedQuantity += quantity;
            return true;
        }
        return false;
    }
    
    public synchronized void releaseReservedQuantity(int quantity) {
        int releaseAmount = Math.min(quantity, reservedQuantity);
        reservedQuantity -= releaseAmount;
        availableQuantity += releaseAmount;
    }
    
    public synchronized void confirmReservedQuantity(int quantity) {
        int confirmAmount = Math.min(quantity, reservedQuantity);
        reservedQuantity -= confirmAmount;
    }
    
    public boolean needsReorder() {
        return getTotalQuantity() <= reorderLevel;
    }
}
```

**Order Model:**
```java
public class Order {
    private final String orderId;
    private final String customerId;
    private final List<OrderItem> items;
    private final Address shippingAddress;
    private final Address billingAddress;
    private final BigDecimal subtotal;
    private final BigDecimal taxAmount;
    private final BigDecimal shippingCost;
    private final BigDecimal totalAmount;
    private OrderStatus status;
    private final String paymentMethodId;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String trackingNumber;
    
    public boolean canCancel() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }
    
    public boolean canShip() {
        return status == OrderStatus.CONFIRMED || status == OrderStatus.PROCESSING;
    }
    
    public void updateStatus(OrderStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
        
        if (newStatus == OrderStatus.SHIPPED && shippedAt == null) {
            this.shippedAt = LocalDateTime.now();
        } else if (newStatus == OrderStatus.DELIVERED && deliveredAt == null) {
            this.deliveredAt = LocalDateTime.now();
        }
    }
}
```

#### Service Layer Architecture

**Core Service Interface:**
```java
public interface EcommerceService {
    // Product Management
    void addProduct(Product product);
    Optional<Product> getProduct(String productId);
    List<Product> getProductsByCategory(String category);
    List<Product> getProductsByBrand(String brand);
    List<Product> searchProducts(String query);
    boolean updateProductPrice(String productId, BigDecimal newPrice);
    boolean updateProductStatus(String productId, ProductStatus status);
    
    // Customer Management
    void registerCustomer(Customer customer);
    Optional<Customer> getCustomer(String customerId);
    List<Customer> findCustomersByEmail(String email);
    boolean updateCustomerStatus(String customerId, CustomerStatus status);
    
    // Inventory Management
    void addInventory(Inventory inventory);
    Optional<Inventory> getInventory(String productId);
    boolean updateInventory(String productId, int quantity);
    boolean reserveInventory(String productId, int quantity);
    boolean releaseInventory(String productId, int quantity);
    boolean confirmInventory(String productId, int quantity);
    List<Inventory> getLowStockProducts();
    
    // Shopping Cart Management
    boolean addToCart(String customerId, String productId, int quantity);
    boolean updateCartItem(String customerId, String productId, int quantity);
    boolean removeFromCart(String customerId, String productId);
    List<CartItem> getCartItems(String customerId);
    BigDecimal getCartTotal(String customerId);
    boolean clearCart(String customerId);
    
    // Order Management
    Order createOrder(String customerId, Address shippingAddress, Address billingAddress, String paymentMethodId);
    Optional<Order> getOrder(String orderId);
    List<Order> getOrdersByCustomer(String customerId);
    List<Order> getOrdersByStatus(OrderStatus status);
    boolean updateOrderStatus(String orderId, OrderStatus status);
    boolean cancelOrder(String orderId);
    boolean shipOrder(String orderId, String trackingNumber);
    
    // Statistics and Reporting
    EcommerceStats getStats();
    CustomerStats getCustomerStats(String customerId);
    ProductStats getProductStats(String productId);
}
```

#### Shopping Cart Management

**Thread-Safe Cart Operations:**
```java
@Override
public boolean addToCart(String customerId, String productId, int quantity) {
    if (customerId == null || productId == null || quantity <= 0) {
        return false;
    }
    
    Customer customer = customers.get(customerId.trim());
    Product product = products.get(productId.trim());
    Inventory inventory = inventories.get(productId.trim());
    
    if (customer == null || product == null || !product.isActive() || 
        inventory == null || !inventory.isAvailable(quantity)) {
        return false;
    }
    
    List<CartItem> cart = shoppingCarts.computeIfAbsent(customerId.trim(), k -> new ArrayList<>());
    
    synchronized (cart) {
        // Check if item already exists in cart
        Optional<CartItem> existingItem = cart.stream()
                .filter(item -> item.getProductId().equals(productId.trim()))
                .findFirst();
        
        if (existingItem.isPresent()) {
            // Update existing item quantity
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;
            if (inventory.isAvailable(newQuantity)) {
                item.updateQuantity(newQuantity);
                return true;
            }
            return false;
        } else {
            // Add new item to cart
            CartItem newItem = new CartItem(productId.trim(), quantity, product.getPrice());
            cart.add(newItem);
            return true;
        }
    }
}
```

#### Order Processing with Inventory Management

**Order Creation with Inventory Reservation:**
```java
@Override
public Order createOrder(String customerId, Address shippingAddress, Address billingAddress, String paymentMethodId) {
    if (customerId == null || shippingAddress == null || paymentMethodId == null) {
        throw new IllegalArgumentException("Customer ID, shipping address, and payment method are required");
    }
    
    Customer customer = customers.get(customerId.trim());
    if (customer == null || !customer.isActive()) {
        throw new IllegalArgumentException("Customer not found or inactive");
    }
    
    List<CartItem> cartItems = getCartItems(customerId);
    if (cartItems.isEmpty()) {
        throw new IllegalArgumentException("Cart is empty");
    }
    
    // Reserve inventory for all items
    List<String> reservedProducts = new ArrayList<>();
    try {
        for (CartItem cartItem : cartItems) {
            if (!reserveInventory(cartItem.getProductId(), cartItem.getQuantity())) {
                // Release already reserved items
                for (String productId : reservedProducts) {
                    CartItem item = cartItems.stream()
                            .filter(ci -> ci.getProductId().equals(productId))
                            .findFirst().orElse(null);
                    if (item != null) {
                        releaseInventory(productId, item.getQuantity());
                    }
                }
                throw new IllegalArgumentException("Insufficient inventory for product: " + cartItem.getProductId());
            }
            reservedProducts.add(cartItem.getProductId());
        }
        
        // Create order items
        List<OrderItem> orderItems = cartItems.stream()
                .map(cartItem -> {
                    Product product = products.get(cartItem.getProductId());
                    return new OrderItem(cartItem.getProductId(), 
                                       product != null ? product.getName() : "Unknown Product",
                                       cartItem.getQuantity(), 
                                       cartItem.getUnitPrice());
                })
                .collect(Collectors.toList());
        
        // Calculate pricing
        BigDecimal subtotal = cartItems.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal taxAmount = pricingEngine.calculateTax(subtotal);
        BigDecimal shippingCost = pricingEngine.calculateShipping(orderItems, shippingAddress);
        
        // Create order
        String orderId = "ORDER" + String.format("%06d", orderIdCounter.getAndIncrement());
        Order order = new Order(orderId, customerId.trim(), orderItems, shippingAddress, 
                              billingAddress != null ? billingAddress : shippingAddress,
                              subtotal, taxAmount, shippingCost, paymentMethodId.trim());
        
        orders.put(orderId, order);
        
        // Clear cart after successful order creation
        clearCart(customerId);
        
        return order;
        
    } catch (Exception e) {
        // Release reserved inventory on failure
        for (String productId : reservedProducts) {
            CartItem item = cartItems.stream()
                    .filter(ci -> ci.getProductId().equals(productId))
                    .findFirst().orElse(null);
            if (item != null) {
                releaseInventory(productId, item.getQuantity());
            }
        }
        throw e;
    }
}
```

#### Dynamic Pricing Engine

**Pricing Calculations:**
```java
private static class PricingEngine {
    private static final BigDecimal TAX_RATE = new BigDecimal("0.08"); // 8% tax
    private static final BigDecimal BASE_SHIPPING = new BigDecimal("5.99");
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("50.00");
    
    public BigDecimal calculateTax(BigDecimal subtotal) {
        return subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
    }
    
    public BigDecimal calculateShipping(List<OrderItem> items, Address shippingAddress) {
        BigDecimal subtotal = items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Free shipping for orders over threshold
        if (subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0) {
            return BigDecimal.ZERO;
        }
        
        // Calculate weight-based shipping
        int totalItems = items.stream().mapToInt(OrderItem::getQuantity).sum();
        BigDecimal weightMultiplier = BigDecimal.valueOf(Math.max(1, totalItems / 5)); // $1 per 5 items
        
        return BASE_SHIPPING.add(weightMultiplier).setScale(2, RoundingMode.HALF_UP);
    }
}
```

### Key Features Demonstrated

#### 1. Product Catalog Management
```java
// Create e-commerce service
EcommerceService ecommerceService = new InMemoryEcommerceService();

// Add products to catalog
Product iPhone = new Product("P001", "iPhone 14", "Latest Apple smartphone", 
                           new BigDecimal("999.99"), "Electronics", "Apple", "SKU-P001", 0.5, ProductStatus.ACTIVE);
ecommerceService.addProduct(iPhone);

// Search and filter products
List<Product> electronicsProducts = ecommerceService.getProductsByCategory("Electronics");
List<Product> appleProducts = ecommerceService.getProductsByBrand("Apple");
List<Product> searchResults = ecommerceService.searchProducts("phone");

// Update product pricing
ecommerceService.updateProductPrice("P001", new BigDecimal("899.99"));
```

#### 2. Inventory Management with Reservations
```java
// Add inventory for products
Inventory inventory = new Inventory("P001", 100, 0, 10, 500);
ecommerceService.addInventory(inventory);

// Reserve inventory for pending orders
boolean reserved = ecommerceService.reserveInventory("P001", 5);

// Release inventory if order is cancelled
ecommerceService.releaseInventory("P001", 2);

// Confirm inventory when order is delivered
ecommerceService.confirmInventory("P001", 3);

// Check low stock products
List<Inventory> lowStockProducts = ecommerceService.getLowStockProducts();
```

#### 3. Shopping Cart Operations
```java
// Add items to shopping cart
String customerId = "C001";
ecommerceService.addToCart(customerId, "P001", 2);
ecommerceService.addToCart(customerId, "P002", 1);

// View cart contents
List<CartItem> cartItems = ecommerceService.getCartItems(customerId);
BigDecimal cartTotal = ecommerceService.getCartTotal(customerId);

// Update item quantities
ecommerceService.updateCartItem(customerId, "P001", 3);

// Remove items from cart
ecommerceService.removeFromCart(customerId, "P002");

// Clear entire cart
ecommerceService.clearCart(customerId);
```

#### 4. Order Processing and Lifecycle
```java
// Create order from cart
Address shippingAddress = new Address("123 Main St", "New York", "NY", "10001", "USA", AddressType.SHIPPING);
Order order = ecommerceService.createOrder(customerId, shippingAddress, null, "PAYMENT_METHOD_123");

System.out.println("Order created: " + order.getOrderId());
System.out.println("Total: $" + order.getTotalAmount());

// Order status progression
ecommerceService.updateOrderStatus(order.getOrderId(), OrderStatus.CONFIRMED);
ecommerceService.updateOrderStatus(order.getOrderId(), OrderStatus.PROCESSING);
ecommerceService.shipOrder(order.getOrderId(), "TRACK123456789");
ecommerceService.updateOrderStatus(order.getOrderId(), OrderStatus.DELIVERED);

// Order cancellation
ecommerceService.cancelOrder(order.getOrderId());
```

#### 5. Customer Management
```java
// Register customers
Address customerAddress = new Address("456 Oak St", "Boston", "MA", "02101", "USA", AddressType.BOTH);
Customer customer = new Customer("C001", "John", "Doe", "john.doe@email.com", 
                               "+1-555-0101", customerAddress, CustomerStatus.ACTIVE);
ecommerceService.registerCustomer(customer);

// Find customers by email
List<Customer> customers = ecommerceService.findCustomersByEmail("john.doe@email.com");

// Update customer status
ecommerceService.updateCustomerStatus("C001", CustomerStatus.INACTIVE);
```

#### 6. Statistics and Analytics
```java
// Get overall system statistics
EcommerceStats stats = ecommerceService.getStats();
System.out.println("Total products: " + stats.getTotalProducts());
System.out.println("Total orders: " + stats.getTotalOrders());
System.out.println("Total revenue: $" + stats.getTotalRevenue());
System.out.println("Order fulfillment rate: " + stats.getOrderFulfillmentRate() + "%");

// Get customer-specific statistics
CustomerStats customerStats = ecommerceService.getCustomerStats("C001");
System.out.println("Customer orders: " + customerStats.getTotalOrders());
System.out.println("Customer spent: $" + customerStats.getTotalSpent());
System.out.println("Order completion rate: " + customerStats.getOrderCompletionRate() + "%");

// Get product-specific statistics
ProductStats productStats = ecommerceService.getProductStats("P001");
System.out.println("Product sold: " + productStats.getTotalSold());
System.out.println("Product revenue: $" + productStats.getTotalRevenue());
System.out.println("Current stock: " + productStats.getCurrentStock());
```

### Performance Characteristics

**Benchmark Results** (from demo):
- **Product Catalog**: 5 products with search and filtering capabilities
- **Customer Management**: 3 registered customers with profile management
- **Inventory Operations**: Real-time reservation and confirmation
- **Shopping Cart**: Multi-item cart operations with pricing calculations
- **Order Processing**: Complete order lifecycle with status transitions
- **Statistics**: Real-time analytics across all system components

### Concurrency Design

#### Thread Safety Mechanisms
- **ConcurrentHashMap**: Thread-safe storage for all entities
- **Synchronized Methods**: Critical sections for inventory operations
- **AtomicInteger**: Lock-free order ID generation
- **Immutable Models**: Thread-safe data structures where possible

#### Inventory Consistency
- **Atomic Reservations**: Ensure inventory consistency under concurrent access
- **Rollback Mechanisms**: Release reserved inventory on order failures
- **Confirmation Process**: Two-phase commit for inventory updates
- **Low Stock Monitoring**: Real-time tracking of inventory levels

### Testing Strategy

The comprehensive test suite covers:

1. **Product Management**: CRUD operations, search, and filtering
2. **Customer Management**: Registration, lookup, and status updates
3. **Inventory Management**: Reservations, releases, and confirmations
4. **Shopping Cart**: Add, update, remove, and clear operations
5. **Order Processing**: Creation, lifecycle, and cancellation
6. **Statistics**: Real-time analytics and reporting
7. **Edge Cases**: Invalid inputs, insufficient inventory, empty carts
8. **Concurrency**: Thread-safe operations under load
9. **Business Logic**: Pricing calculations, tax, and shipping
10. **Data Consistency**: Inventory and order state management

### Common Interview Questions

1. **"How do you handle inventory consistency?"**
   - Use atomic operations for inventory reservations
   - Implement two-phase commit for order processing
   - Use optimistic locking for concurrent updates
   - Implement compensation patterns for failures

2. **"How would you scale this to millions of products?"**
   - Implement search indexing (Elasticsearch)
   - Use database sharding by product category
   - Implement caching layers for frequently accessed data
   - Consider microservices architecture

3. **"How do you handle cart abandonment?"**
   - Implement cart expiration policies
   - Use background jobs to clean up old carts
   - Send reminder notifications to customers
   - Release reserved inventory after timeout

4. **"How do you ensure order consistency?"**
   - Use database transactions for order creation
   - Implement saga pattern for distributed operations
   - Use event sourcing for audit trails
   - Implement idempotent operations

### Extensions and Improvements

1. **Payment Integration**: Credit card processing and payment gateways
2. **Recommendation Engine**: Product recommendations based on user behavior
3. **Inventory Forecasting**: Predictive analytics for stock management
4. **Multi-Vendor Support**: Marketplace functionality with multiple sellers
5. **Mobile API**: REST APIs for mobile applications
6. **Real-Time Notifications**: Order status updates and promotions
7. **Advanced Search**: Faceted search with filters and sorting
8. **Loyalty Programs**: Points, rewards, and member pricing

### Real-World Applications

1. **E-commerce Platforms**: Amazon, eBay marketplace systems
2. **Retail Chains**: Walmart, Target inventory management
3. **Fashion Retailers**: Zara, H&M online stores
4. **Electronics Stores**: Best Buy, Newegg product catalogs
5. **Grocery Delivery**: Instacart, Amazon Fresh order systems
6. **B2B Marketplaces**: Alibaba, ThomasNet business platforms
7. **Subscription Services**: Subscription box order management

This E-commerce Order Management System implementation demonstrates essential concepts in:
- **Inventory Management**: Real-time stock tracking and reservations
- **Order Processing**: Complete order lifecycle with state management
- **Business Logic**: Complex pricing calculations and business rules
- **Data Consistency**: Thread-safe operations and transaction management
- **System Design**: Scalable architecture with clear separation of concerns

The system showcases advanced programming concepts crucial for machine coding interviews focused on e-commerce and order management systems.

## Problems Covered
1. ✅ Hotel Booking System (Room inventory, guest management, dynamic pricing, booking lifecycle)
2. ✅ Ride Hailing System (Driver-rider matching, trip management, fare calculation, real-time location tracking)
3. ✅ E-commerce Order Management System (Product catalog, inventory management, shopping cart, order processing, customer management)

This chapter demonstrates comprehensive booking and ordering system design patterns, covering hospitality, transportation, and e-commerce domains. Each implementation showcases different aspects of complex business logic, real-time operations, and scalable system architecture.