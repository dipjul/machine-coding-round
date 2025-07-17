package com.machinecoding.booking;

import com.machinecoding.booking.model.*;
import com.machinecoding.booking.service.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Comprehensive demonstration of the Hotel Booking System.
 * Shows room management, guest registration, booking operations, and pricing.
 */
public class HotelBookingDemo {
    
    public static void main(String[] args) {
        System.out.println("=== Hotel Booking System Demo ===\n");
        
        // Demo 1: Hotel Setup and Room Management
        System.out.println("=== Demo 1: Hotel Setup and Room Management ===");
        demonstrateHotelSetup();
        
        // Demo 2: Guest Registration and Management
        System.out.println("\n=== Demo 2: Guest Registration and Management ===");
        demonstrateGuestManagement();
        
        // Demo 3: Room Availability and Search
        System.out.println("\n=== Demo 3: Room Availability and Search ===");
        demonstrateAvailabilitySearch();
        
        // Demo 4: Booking Operations
        System.out.println("\n=== Demo 4: Booking Operations ===");
        demonstrateBookingOperations();
        
        // Demo 5: Dynamic Pricing
        System.out.println("\n=== Demo 5: Dynamic Pricing ===");
        demonstratePricing();
        
        // Demo 6: Statistics and Reporting
        System.out.println("\n=== Demo 6: Statistics and Reporting ===");
        demonstrateStatistics();
        
        System.out.println("\n=== Demo Complete ===");
    }
    
    private static void demonstrateHotelSetup() {
        System.out.println("1. Creating hotel booking service:");
        HotelBookingService hotelService = new InMemoryHotelBookingService();
        
        System.out.println("\n2. Adding rooms to hotel inventory:");
        
        // Add various room types
        Room[] rooms = {
            new Room("R001", "101", RoomType.SINGLE, 1, new BigDecimal("80.00"), "Cozy single room with city view"),
            new Room("R002", "102", RoomType.DOUBLE, 2, new BigDecimal("120.00"), "Comfortable double room"),
            new Room("R003", "103", RoomType.TWIN, 2, new BigDecimal("110.00"), "Twin room with two single beds"),
            new Room("R004", "201", RoomType.SUITE, 4, new BigDecimal("250.00"), "Luxury suite with living area"),
            new Room("R005", "202", RoomType.DELUXE, 2, new BigDecimal("180.00"), "Deluxe room with premium amenities"),
            new Room("R006", "301", RoomType.PRESIDENTIAL, 6, new BigDecimal("500.00"), "Presidential suite with panoramic view"),
            new Room("R007", "104", RoomType.DOUBLE, 2, new BigDecimal("125.00"), "Double room with balcony"),
            new Room("R008", "105", RoomType.TRIPLE, 3, new BigDecimal("150.00"), "Triple room for families")
        };
        
        for (Room room : rooms) {
            hotelService.addRoom(room);
            System.out.println("   Added: " + room);
        }
        
        System.out.println("\n3. Room inventory summary:");
        List<Room> allRooms = hotelService.getAllActiveRooms();
        System.out.println("   Total active rooms: " + allRooms.size());
        
        for (RoomType type : RoomType.values()) {
            List<Room> roomsByType = hotelService.getRoomsByType(type);
            if (!roomsByType.isEmpty()) {
                System.out.println("   " + type + ": " + roomsByType.size() + " rooms");
            }
        }
    }
    
    private static void demonstrateGuestManagement() {
        HotelBookingService hotelService = new InMemoryHotelBookingService();
        setupHotel(hotelService);
        
        System.out.println("1. Registering guests:");
        
        Guest[] guests = {
            new Guest("G001", "John", "Doe", "john.doe@email.com", "+1-555-0101", 
                     LocalDate.of(1985, 5, 15), "ID123456", "123 Main St, City"),
            new Guest("G002", "Jane", "Smith", "jane.smith@email.com", "+1-555-0102", 
                     LocalDate.of(1990, 8, 22), "ID789012", "456 Oak Ave, Town"),
            new Guest("G003", "Bob", "Johnson", "bob.johnson@email.com", "+1-555-0103", 
                     LocalDate.of(1978, 12, 3), "ID345678", "789 Pine Rd, Village"),
            new Guest("G004", "Alice", "Brown", "alice.brown@email.com", "+1-555-0104", 
                     LocalDate.of(1995, 3, 10), "ID901234", "321 Elm St, City")
        };
        
        for (Guest guest : guests) {
            hotelService.registerGuest(guest);
            System.out.println("   Registered: " + guest);
        }
        
        System.out.println("\n2. Guest lookup examples:");
        
        // Find guest by ID
        hotelService.getGuest("G001").ifPresent(guest -> 
            System.out.println("   Found by ID: " + guest.getFullName()));
        
        // Find guests by email
        List<Guest> foundGuests = hotelService.findGuestsByEmail("jane.smith@email.com");
        if (!foundGuests.isEmpty()) {
            System.out.println("   Found by email: " + foundGuests.get(0).getFullName());
        }
        
        System.out.println("\n3. Guest validation:");
        Guest adultGuest = guests[0];
        Guest youngGuest = guests[3];
        System.out.println("   " + adultGuest.getFullName() + " is adult: " + adultGuest.isAdult());
        System.out.println("   " + youngGuest.getFullName() + " is adult: " + youngGuest.isAdult());
    }
    
    private static void demonstrateAvailabilitySearch() {
        HotelBookingService hotelService = new InMemoryHotelBookingService();
        setupHotel(hotelService);
        setupGuests(hotelService);
        
        System.out.println("1. Checking room availability:");
        
        LocalDate checkIn = LocalDate.now().plusDays(7);
        LocalDate checkOut = LocalDate.now().plusDays(10);
        
        System.out.println("   Search dates: " + checkIn + " to " + checkOut);
        
        // Check specific room availability
        boolean roomAvailable = hotelService.isRoomAvailable("R001", checkIn, checkOut);
        System.out.println("   Room R001 available: " + roomAvailable);
        
        System.out.println("\n2. Finding available rooms:");
        
        // Find all available rooms for 2 guests
        List<Room> availableRooms = hotelService.getAvailableRooms(checkIn, checkOut, 2);
        System.out.println("   Available rooms for 2 guests: " + availableRooms.size());
        
        for (Room room : availableRooms) {
            BigDecimal price = hotelService.calculatePrice(room.getRoomId(), checkIn, checkOut, 2);
            System.out.println("     " + room.getRoomNumber() + " (" + room.getRoomType() + 
                             ") - $" + price + " total");
        }
        
        System.out.println("\n3. Search by room type:");
        
        List<Room> suiteRooms = hotelService.getAvailableRoomsByType(RoomType.SUITE, checkIn, checkOut, 2);
        System.out.println("   Available suites: " + suiteRooms.size());
        
        List<Room> doubleRooms = hotelService.getAvailableRoomsByType(RoomType.DOUBLE, checkIn, checkOut, 2);
        System.out.println("   Available double rooms: " + doubleRooms.size());
    }
    
    private static void demonstrateBookingOperations() {
        HotelBookingService hotelService = new InMemoryHotelBookingService();
        setupHotel(hotelService);
        setupGuests(hotelService);
        
        System.out.println("1. Creating bookings:");
        
        LocalDate checkIn1 = LocalDate.now().plusDays(5);
        LocalDate checkOut1 = LocalDate.now().plusDays(8);
        
        try {
            Booking booking1 = hotelService.createBooking("G001", "R002", checkIn1, checkOut1, 2, 
                                                        "Late check-in requested");
            System.out.println("   Created: " + booking1);
            
            Booking booking2 = hotelService.createBooking("G002", "R004", checkIn1, checkOut1, 3, 
                                                        "Honeymoon suite, champagne requested");
            System.out.println("   Created: " + booking2);
            
            Booking booking3 = hotelService.createBooking("G003", "R001", 
                                                        LocalDate.now().plusDays(10), 
                                                        LocalDate.now().plusDays(12), 1, null);
            System.out.println("   Created: " + booking3);
            
        } catch (Exception e) {
            System.out.println("   Booking failed: " + e.getMessage());
        }
        
        System.out.println("\n2. Booking management operations:");
        
        // Get bookings by guest
        List<Booking> guestBookings = hotelService.getBookingsByGuest("G001");
        System.out.println("   Bookings for G001: " + guestBookings.size());
        
        // Get active bookings
        List<Booking> activeBookings = hotelService.getActiveBookings();
        System.out.println("   Total active bookings: " + activeBookings.size());
        
        System.out.println("\n3. Booking lifecycle operations:");
        
        if (!activeBookings.isEmpty()) {
            Booking firstBooking = activeBookings.get(0);
            String bookingId = firstBooking.getBookingId();
            
            System.out.println("   Original booking: " + firstBooking.getStatus());
            
            // Try to check in (might fail if not check-in date)
            boolean checkedIn = hotelService.checkIn(bookingId);
            System.out.println("   Check-in attempt: " + (checkedIn ? "Success" : "Failed (not check-in date)"));
            
            // Modify booking
            boolean modified = hotelService.modifyBooking(bookingId, 
                                                        firstBooking.getCheckInDate().plusDays(1),
                                                        firstBooking.getCheckOutDate().plusDays(1),
                                                        firstBooking.getNumberOfGuests(),
                                                        "Modified dates");
            System.out.println("   Booking modification: " + (modified ? "Success" : "Failed"));
            
            // Cancel booking
            boolean cancelled = hotelService.cancelBooking(bookingId);
            System.out.println("   Booking cancellation: " + (cancelled ? "Success" : "Failed"));
        }
    }
    
    private static void demonstratePricing() {
        HotelBookingService hotelService = new InMemoryHotelBookingService();
        setupHotel(hotelService);
        
        System.out.println("1. Base room pricing:");
        
        List<Room> rooms = hotelService.getAllActiveRooms();
        for (Room room : rooms.subList(0, Math.min(4, rooms.size()))) {
            System.out.println("   " + room.getRoomNumber() + " (" + room.getRoomType() + 
                             "): Base price $" + room.getBasePrice());
        }
        
        System.out.println("\n2. Dynamic pricing examples:");
        
        LocalDate weekday = LocalDate.now().plusDays(7);
        LocalDate weekend = LocalDate.now().plusDays(13); // Assuming this falls on weekend
        LocalDate holiday = LocalDate.of(2024, 12, 25); // Christmas
        
        Room sampleRoom = rooms.get(1); // Double room
        
        System.out.println("   Room " + sampleRoom.getRoomNumber() + " pricing:");
        System.out.println("     Weekday (" + weekday + "): $" + 
                         hotelService.getRoomPrice(sampleRoom.getRoomId(), weekday));
        System.out.println("     Weekend (" + weekend + "): $" + 
                         hotelService.getRoomPrice(sampleRoom.getRoomId(), weekend));
        System.out.println("     Holiday (" + holiday + "): $" + 
                         hotelService.getRoomPrice(sampleRoom.getRoomId(), holiday));
        
        System.out.println("\n3. Multi-night booking pricing:");
        
        LocalDate checkIn = LocalDate.now().plusDays(7);
        LocalDate checkOut = LocalDate.now().plusDays(10);
        
        for (Room room : rooms.subList(0, 3)) {
            BigDecimal totalPrice = hotelService.calculatePrice(room.getRoomId(), checkIn, checkOut, 2);
            System.out.println("   " + room.getRoomNumber() + " (" + room.getRoomType() + 
                             ") for 3 nights: $" + totalPrice);
        }
        
        System.out.println("\n4. Extra guest pricing:");
        
        Room suiteRoom = rooms.stream()
                .filter(r -> r.getRoomType() == RoomType.SUITE)
                .findFirst()
                .orElse(rooms.get(0));
        
        BigDecimal price2Guests = hotelService.calculatePrice(suiteRoom.getRoomId(), checkIn, checkOut, 2);
        BigDecimal price4Guests = hotelService.calculatePrice(suiteRoom.getRoomId(), checkIn, checkOut, 4);
        
        System.out.println("   Suite for 2 guests: $" + price2Guests);
        System.out.println("   Suite for 4 guests: $" + price4Guests);
        System.out.println("   Extra guest fee: $" + price4Guests.subtract(price2Guests));
    }
    
    private static void demonstrateStatistics() {
        HotelBookingService hotelService = new InMemoryHotelBookingService();
        setupHotel(hotelService);
        setupGuests(hotelService);
        
        // Create some sample bookings
        createSampleBookings(hotelService);
        
        System.out.println("1. Hotel statistics:");
        
        BookingStats stats = hotelService.getBookingStats();
        System.out.println("   " + stats);
        
        System.out.println("\n2. Detailed statistics:");
        System.out.println("   Total rooms: " + stats.getTotalRooms());
        System.out.println("   Active rooms: " + stats.getActiveRooms());
        System.out.println("   Room utilization: " + String.format("%.1f%%", stats.getRoomUtilizationRate()));
        System.out.println("   Total bookings: " + stats.getTotalBookings());
        System.out.println("   Confirmed bookings: " + stats.getConfirmedBookings());
        System.out.println("   Cancellation rate: " + String.format("%.1f%%", stats.getCancellationRate()));
        System.out.println("   Total revenue: $" + stats.getTotalRevenue());
        System.out.println("   Average booking value: $" + stats.getAverageBookingValue());
        
        System.out.println("\n3. Occupancy and revenue analysis:");
        
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        
        double occupancyRate = hotelService.getOccupancyRate(startDate, endDate);
        BigDecimal revenue = hotelService.getRevenue(startDate, endDate);
        
        System.out.println("   30-day occupancy rate: " + String.format("%.1f%%", occupancyRate));
        System.out.println("   30-day revenue: $" + revenue);
    }
    
    // Helper methods
    private static void setupHotel(HotelBookingService hotelService) {
        Room[] rooms = {
            new Room("R001", "101", RoomType.SINGLE, 1, new BigDecimal("80.00"), "Single room"),
            new Room("R002", "102", RoomType.DOUBLE, 2, new BigDecimal("120.00"), "Double room"),
            new Room("R003", "103", RoomType.TWIN, 2, new BigDecimal("110.00"), "Twin room"),
            new Room("R004", "201", RoomType.SUITE, 4, new BigDecimal("250.00"), "Suite"),
            new Room("R005", "202", RoomType.DELUXE, 2, new BigDecimal("180.00"), "Deluxe room"),
            new Room("R006", "301", RoomType.PRESIDENTIAL, 6, new BigDecimal("500.00"), "Presidential suite")
        };
        
        for (Room room : rooms) {
            hotelService.addRoom(room);
        }
    }
    
    private static void setupGuests(HotelBookingService hotelService) {
        Guest[] guests = {
            new Guest("G001", "John", "Doe", "john.doe@email.com", "+1-555-0101", 
                     LocalDate.of(1985, 5, 15), "ID123456", "123 Main St"),
            new Guest("G002", "Jane", "Smith", "jane.smith@email.com", "+1-555-0102", 
                     LocalDate.of(1990, 8, 22), "ID789012", "456 Oak Ave"),
            new Guest("G003", "Bob", "Johnson", "bob.johnson@email.com", "+1-555-0103", 
                     LocalDate.of(1978, 12, 3), "ID345678", "789 Pine Rd")
        };
        
        for (Guest guest : guests) {
            hotelService.registerGuest(guest);
        }
    }
    
    private static void createSampleBookings(HotelBookingService hotelService) {
        try {
            hotelService.createBooking("G001", "R002", 
                                     LocalDate.now().plusDays(5), 
                                     LocalDate.now().plusDays(8), 2, null);
            
            hotelService.createBooking("G002", "R004", 
                                     LocalDate.now().plusDays(10), 
                                     LocalDate.now().plusDays(13), 3, null);
            
            Booking booking3 = hotelService.createBooking("G003", "R001", 
                                                        LocalDate.now().minusDays(5), 
                                                        LocalDate.now().minusDays(2), 1, null);
            
            // Simulate completed booking
            hotelService.checkIn(booking3.getBookingId());
            hotelService.checkOut(booking3.getBookingId());
            
        } catch (Exception e) {
            // Ignore booking failures in demo setup
        }
    }
}