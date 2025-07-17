package com.machinecoding.booking;

import com.machinecoding.booking.model.*;
import com.machinecoding.booking.service.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Comprehensive unit tests for the Hotel Booking System.
 * Tests room management, guest registration, booking operations, and pricing.
 */
public class HotelBookingTest {
    
    public static void main(String[] args) {
        System.out.println("=== Hotel Booking System Unit Tests ===\n");
        
        runAllTests();
        
        System.out.println("\n=== All Tests Complete ===");
    }
    
    private static void runAllTests() {
        testRoomManagement();
        testGuestManagement();
        testAvailabilityChecking();
        testBookingOperations();
        testBookingLifecycle();
        testPricingEngine();
        testStatistics();
        testEdgeCases();
        testConcurrency();
        testValidation();
    }
    
    private static void testRoomManagement() {
        System.out.println("Test 1: Room Management");
        
        try {
            HotelBookingService service = new InMemoryHotelBookingService();
            
            // Test adding rooms
            Room room1 = new Room("R001", "101", RoomType.SINGLE, 1, new BigDecimal("80.00"), "Single room");
            Room room2 = new Room("R002", "102", RoomType.DOUBLE, 2, new BigDecimal("120.00"), "Double room");
            
            service.addRoom(room1);
            service.addRoom(room2);
            
            // Test room retrieval
            Optional<Room> foundRoom = service.getRoom("R001");
            assert foundRoom.isPresent() : "Room should be found";
            assert foundRoom.get().getRoomNumber().equals("101") : "Room number should match";
            
            // Test rooms by type
            List<Room> singleRooms = service.getRoomsByType(RoomType.SINGLE);
            assert singleRooms.size() == 1 : "Should have 1 single room";
            
            // Test active rooms
            List<Room> activeRooms = service.getAllActiveRooms();
            assert activeRooms.size() == 2 : "Should have 2 active rooms";
            
            // Test room status update
            boolean updated = service.updateRoomStatus("R001", false);
            assert updated : "Room status should be updated";
            
            List<Room> activeAfterUpdate = service.getAllActiveRooms();
            assert activeAfterUpdate.size() == 1 : "Should have 1 active room after deactivation";
            
            System.out.println("   ✓ Room management tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Room management test failed: " + e.getMessage());
        }
    }
    
    private static void testGuestManagement() {
        System.out.println("\nTest 2: Guest Management");
        
        try {
            HotelBookingService service = new InMemoryHotelBookingService();
            
            // Test guest registration
            Guest guest1 = new Guest("G001", "John", "Doe", "john.doe@email.com", "+1-555-0101", 
                                   LocalDate.of(1985, 5, 15), "ID123456", "123 Main St");
            Guest guest2 = new Guest("G002", "Jane", "Smith", "jane.smith@email.com", "+1-555-0102", 
                                   LocalDate.of(1990, 8, 22), "ID789012", "456 Oak Ave");
            
            service.registerGuest(guest1);
            service.registerGuest(guest2);
            
            // Test guest retrieval
            Optional<Guest> foundGuest = service.getGuest("G001");
            assert foundGuest.isPresent() : "Guest should be found";
            assert foundGuest.get().getFullName().equals("John Doe") : "Guest name should match";
            
            // Test guest search by email
            List<Guest> foundByEmail = service.findGuestsByEmail("jane.smith@email.com");
            assert foundByEmail.size() == 1 : "Should find 1 guest by email";
            assert foundByEmail.get(0).getFirstName().equals("Jane") : "Found guest should be Jane";
            
            // Test case insensitive email search
            List<Guest> foundByEmailCaseInsensitive = service.findGuestsByEmail("JOHN.DOE@EMAIL.COM");
            assert foundByEmailCaseInsensitive.size() == 1 : "Should find guest with case insensitive email";
            
            System.out.println("   ✓ Guest management tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Guest management test failed: " + e.getMessage());
        }
    }
    
    private static void testAvailabilityChecking() {
        System.out.println("\nTest 3: Availability Checking");
        
        try {
            HotelBookingService service = new InMemoryHotelBookingService();
            setupTestData(service);
            
            LocalDate checkIn = LocalDate.now().plusDays(7);
            LocalDate checkOut = LocalDate.now().plusDays(10);
            
            // Test room availability
            boolean available = service.isRoomAvailable("R001", checkIn, checkOut);
            assert available : "Room should be available";
            
            // Create a booking to test unavailability
            service.createBooking("G001", "R001", checkIn, checkOut, 1, null);
            
            boolean availableAfterBooking = service.isRoomAvailable("R001", checkIn, checkOut);
            assert !availableAfterBooking : "Room should not be available after booking";
            
            // Test overlapping dates
            LocalDate overlapCheckIn = checkIn.plusDays(1);
            LocalDate overlapCheckOut = checkOut.plusDays(1);
            boolean overlapAvailable = service.isRoomAvailable("R001", overlapCheckIn, overlapCheckOut);
            assert !overlapAvailable : "Room should not be available for overlapping dates";
            
            // Test available rooms search
            List<Room> availableRooms = service.getAvailableRooms(checkIn, checkOut, 2);
            assert !availableRooms.isEmpty() : "Should have available rooms";
            assert availableRooms.stream().noneMatch(r -> r.getRoomId().equals("R001")) : 
                "Booked room should not be in available list";
            
            // Test available rooms by type
            List<Room> availableDoubles = service.getAvailableRoomsByType(RoomType.DOUBLE, checkIn, checkOut, 2);
            assert !availableDoubles.isEmpty() : "Should have available double rooms";
            
            System.out.println("   ✓ Availability checking tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Availability checking test failed: " + e.getMessage());
        }
    }
    
    private static void testBookingOperations() {
        System.out.println("\nTest 4: Booking Operations");
        
        try {
            HotelBookingService service = new InMemoryHotelBookingService();
            setupTestData(service);
            
            LocalDate checkIn = LocalDate.now().plusDays(5);
            LocalDate checkOut = LocalDate.now().plusDays(8);
            
            // Test booking creation
            Booking booking = service.createBooking("G001", "R002", checkIn, checkOut, 2, "Late check-in");
            assert booking != null : "Booking should be created";
            assert booking.getGuestId().equals("G001") : "Guest ID should match";
            assert booking.getRoomId().equals("R002") : "Room ID should match";
            assert booking.getStatus() == BookingStatus.CONFIRMED : "Booking should be confirmed";
            
            // Test booking retrieval
            Optional<Booking> foundBooking = service.getBooking(booking.getBookingId());
            assert foundBooking.isPresent() : "Booking should be found";
            
            // Test bookings by guest
            List<Booking> guestBookings = service.getBookingsByGuest("G001");
            assert guestBookings.size() == 1 : "Should have 1 booking for guest";
            
            // Test bookings by room
            List<Booking> roomBookings = service.getBookingsByRoom("R002");
            assert roomBookings.size() == 1 : "Should have 1 booking for room";
            
            // Test active bookings
            List<Booking> activeBookings = service.getActiveBookings();
            assert activeBookings.size() == 1 : "Should have 1 active booking";
            
            System.out.println("   ✓ Booking operations tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Booking operations test failed: " + e.getMessage());
        }
    }
    
    private static void testBookingLifecycle() {
        System.out.println("\nTest 5: Booking Lifecycle");
        
        try {
            HotelBookingService service = new InMemoryHotelBookingService();
            setupTestData(service);
            
            LocalDate checkIn = LocalDate.now();
            LocalDate checkOut = LocalDate.now().plusDays(3);
            
            // Create booking
            Booking booking = service.createBooking("G001", "R002", checkIn, checkOut, 2, null);
            String bookingId = booking.getBookingId();
            
            // Test check-in
            boolean checkedIn = service.checkIn(bookingId);
            assert checkedIn : "Check-in should succeed";
            
            Optional<Booking> afterCheckIn = service.getBooking(bookingId);
            assert afterCheckIn.get().getStatus() == BookingStatus.CHECKED_IN : "Status should be checked in";
            
            // Test check-out
            boolean checkedOut = service.checkOut(bookingId);
            assert checkedOut : "Check-out should succeed";
            
            Optional<Booking> afterCheckOut = service.getBooking(bookingId);
            assert afterCheckOut.get().getStatus() == BookingStatus.CHECKED_OUT : "Status should be checked out";
            
            // Test booking modification
            Booking newBooking = service.createBooking("G002", "R003", 
                                                     LocalDate.now().plusDays(10), 
                                                     LocalDate.now().plusDays(13), 2, null);
            
            boolean modified = service.modifyBooking(newBooking.getBookingId(),
                                                   LocalDate.now().plusDays(11),
                                                   LocalDate.now().plusDays(14),
                                                   2, "Modified booking");
            assert modified : "Booking modification should succeed";
            
            // Test booking cancellation
            boolean cancelled = service.cancelBooking(newBooking.getBookingId());
            assert cancelled : "Booking cancellation should succeed";
            
            Optional<Booking> cancelledBooking = service.getBooking(newBooking.getBookingId());
            assert cancelledBooking.get().getStatus() == BookingStatus.CANCELLED : "Status should be cancelled";
            
            System.out.println("   ✓ Booking lifecycle tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Booking lifecycle test failed: " + e.getMessage());
        }
    }
    
    private static void testPricingEngine() {
        System.out.println("\nTest 6: Pricing Engine");
        
        try {
            HotelBookingService service = new InMemoryHotelBookingService();
            setupTestData(service);
            
            LocalDate checkIn = LocalDate.now().plusDays(7);
            LocalDate checkOut = LocalDate.now().plusDays(10);
            
            // Test basic pricing
            BigDecimal price = service.calculatePrice("R002", checkIn, checkOut, 2);
            assert price.compareTo(BigDecimal.ZERO) > 0 : "Price should be positive";
            
            // Test room price for specific date
            BigDecimal roomPrice = service.getRoomPrice("R002", checkIn);
            assert roomPrice.compareTo(BigDecimal.ZERO) > 0 : "Room price should be positive";
            
            // Test pricing with extra guests
            BigDecimal priceStandard = service.calculatePrice("R004", checkIn, checkOut, 2); // Suite standard capacity
            BigDecimal priceExtra = service.calculatePrice("R004", checkIn, checkOut, 4); // Extra guests
            assert priceExtra.compareTo(priceStandard) > 0 : "Price with extra guests should be higher";
            
            // Test different room types have different pricing
            BigDecimal singlePrice = service.calculatePrice("R001", checkIn, checkOut, 1);
            BigDecimal suitePrice = service.calculatePrice("R004", checkIn, checkOut, 2);
            assert suitePrice.compareTo(singlePrice) > 0 : "Suite should be more expensive than single";
            
            System.out.println("   ✓ Pricing engine tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Pricing engine test failed: " + e.getMessage());
        }
    }
    
    private static void testStatistics() {
        System.out.println("\nTest 7: Statistics");
        
        try {
            HotelBookingService service = new InMemoryHotelBookingService();
            setupTestData(service);
            
            // Create some bookings for statistics
            service.createBooking("G001", "R002", LocalDate.now().plusDays(5), LocalDate.now().plusDays(8), 2, null);
            service.createBooking("G002", "R003", LocalDate.now().plusDays(10), LocalDate.now().plusDays(13), 2, null);
            
            // Test booking statistics
            BookingStats stats = service.getBookingStats();
            assert stats.getTotalRooms() > 0 : "Should have rooms";
            assert stats.getTotalBookings() > 0 : "Should have bookings";
            assert stats.getActiveBookings() > 0 : "Should have active bookings";
            assert stats.getTotalRevenue().compareTo(BigDecimal.ZERO) >= 0 : "Revenue should be non-negative";
            
            // Test occupancy rate
            double occupancyRate = service.getOccupancyRate(LocalDate.now().minusDays(30), LocalDate.now().plusDays(30));
            assert occupancyRate >= 0.0 && occupancyRate <= 100.0 : "Occupancy rate should be between 0 and 100";
            
            // Test revenue calculation
            BigDecimal revenue = service.getRevenue(LocalDate.now().minusDays(30), LocalDate.now().plusDays(30));
            assert revenue.compareTo(BigDecimal.ZERO) >= 0 : "Revenue should be non-negative";
            
            System.out.println("   ✓ Statistics tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Statistics test failed: " + e.getMessage());
        }
    }
    
    private static void testEdgeCases() {
        System.out.println("\nTest 8: Edge Cases");
        
        try {
            HotelBookingService service = new InMemoryHotelBookingService();
            setupTestData(service);
            
            // Test booking non-existent guest
            try {
                service.createBooking("NONEXISTENT", "R001", LocalDate.now().plusDays(1), LocalDate.now().plusDays(2), 1, null);
                assert false : "Should throw exception for non-existent guest";
            } catch (IllegalArgumentException e) {
                // Expected
            }
            
            // Test booking non-existent room
            try {
                service.createBooking("G001", "NONEXISTENT", LocalDate.now().plusDays(1), LocalDate.now().plusDays(2), 1, null);
                assert false : "Should throw exception for non-existent room";
            } catch (IllegalArgumentException e) {
                // Expected
            }
            
            // Test booking with too many guests
            try {
                service.createBooking("G001", "R001", LocalDate.now().plusDays(1), LocalDate.now().plusDays(2), 5, null);
                assert false : "Should throw exception for too many guests";
            } catch (IllegalArgumentException e) {
                // Expected
            }
            
            // Test double booking
            LocalDate checkIn = LocalDate.now().plusDays(5);
            LocalDate checkOut = LocalDate.now().plusDays(8);
            
            service.createBooking("G001", "R002", checkIn, checkOut, 2, null);
            
            try {
                service.createBooking("G002", "R002", checkIn, checkOut, 2, null);
                assert false : "Should throw exception for double booking";
            } catch (IllegalArgumentException e) {
                // Expected
            }
            
            // Test operations on non-existent booking
            boolean result = service.cancelBooking("NONEXISTENT");
            assert !result : "Should return false for non-existent booking";
            
            System.out.println("   ✓ Edge cases tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Edge cases test failed: " + e.getMessage());
        }
    }
    
    private static void testConcurrency() {
        System.out.println("\nTest 9: Concurrency");
        
        try {
            HotelBookingService service = new InMemoryHotelBookingService();
            setupTestData(service);
            
            // Test concurrent room additions
            Thread[] threads = new Thread[5];
            for (int i = 0; i < 5; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    Room room = new Room("R" + (100 + index), "Room" + index, RoomType.SINGLE, 1, 
                                        new BigDecimal("80.00"), "Test room " + index);
                    service.addRoom(room);
                });
            }
            
            for (Thread thread : threads) {
                thread.start();
            }
            
            for (Thread thread : threads) {
                thread.join();
            }
            
            List<Room> allRooms = service.getAllActiveRooms();
            assert allRooms.size() >= 9 : "Should have at least 9 rooms after concurrent additions";
            
            System.out.println("   ✓ Concurrency tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Concurrency test failed: " + e.getMessage());
        }
    }
    
    private static void testValidation() {
        System.out.println("\nTest 10: Validation");
        
        try {
            // Test room validation
            try {
                new Room(null, "101", RoomType.SINGLE, 1, new BigDecimal("80.00"), "Test");
                assert false : "Should throw exception for null room ID";
            } catch (IllegalArgumentException e) {
                // Expected
            }
            
            try {
                new Room("R001", "101", RoomType.SINGLE, 0, new BigDecimal("80.00"), "Test");
                assert false : "Should throw exception for zero capacity";
            } catch (IllegalArgumentException e) {
                // Expected
            }
            
            // Test guest validation
            try {
                new Guest(null, "John", "Doe", "john@email.com", "+1-555-0101", 
                         LocalDate.of(1985, 5, 15), "ID123456", "123 Main St");
                assert false : "Should throw exception for null guest ID";
            } catch (IllegalArgumentException e) {
                // Expected
            }
            
            // Test booking validation
            try {
                new Booking("B001", "G001", "R001", LocalDate.now().plusDays(2), LocalDate.now().plusDays(1), 
                           1, new BigDecimal("100.00"));
                assert false : "Should throw exception for check-out before check-in";
            } catch (IllegalArgumentException e) {
                // Expected
            }
            
            System.out.println("   ✓ Validation tests passed");
            
        } catch (Exception e) {
            System.out.println("   ✗ Validation test failed: " + e.getMessage());
        }
    }
    
    // Helper method to set up test data
    private static void setupTestData(HotelBookingService service) {
        // Add rooms
        Room[] rooms = {
            new Room("R001", "101", RoomType.SINGLE, 1, new BigDecimal("80.00"), "Single room"),
            new Room("R002", "102", RoomType.DOUBLE, 2, new BigDecimal("120.00"), "Double room"),
            new Room("R003", "103", RoomType.TWIN, 2, new BigDecimal("110.00"), "Twin room"),
            new Room("R004", "201", RoomType.SUITE, 4, new BigDecimal("250.00"), "Suite")
        };
        
        for (Room room : rooms) {
            service.addRoom(room);
        }
        
        // Add guests
        Guest[] guests = {
            new Guest("G001", "John", "Doe", "john.doe@email.com", "+1-555-0101", 
                     LocalDate.of(1985, 5, 15), "ID123456", "123 Main St"),
            new Guest("G002", "Jane", "Smith", "jane.smith@email.com", "+1-555-0102", 
                     LocalDate.of(1990, 8, 22), "ID789012", "456 Oak Ave")
        };
        
        for (Guest guest : guests) {
            service.registerGuest(guest);
        }
    }
}