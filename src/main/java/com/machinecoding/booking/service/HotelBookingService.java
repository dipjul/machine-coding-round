package com.machinecoding.booking.service;

import com.machinecoding.booking.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Interface for hotel booking operations.
 * Provides room management, booking operations, and availability checking.
 */
public interface HotelBookingService {
    
    // Room Management
    /**
     * Adds a room to the hotel inventory.
     */
    void addRoom(Room room);
    
    /**
     * Gets a room by ID.
     */
    Optional<Room> getRoom(String roomId);
    
    /**
     * Gets all rooms of a specific type.
     */
    List<Room> getRoomsByType(RoomType roomType);
    
    /**
     * Gets all active rooms.
     */
    List<Room> getAllActiveRooms();
    
    /**
     * Updates room status (active/inactive).
     */
    boolean updateRoomStatus(String roomId, boolean active);
    
    // Guest Management
    /**
     * Registers a new guest.
     */
    void registerGuest(Guest guest);
    
    /**
     * Gets a guest by ID.
     */
    Optional<Guest> getGuest(String guestId);
    
    /**
     * Finds guests by email.
     */
    List<Guest> findGuestsByEmail(String email);
    
    // Availability Checking
    /**
     * Checks if a room is available for the given date range.
     */
    boolean isRoomAvailable(String roomId, LocalDate checkIn, LocalDate checkOut);
    
    /**
     * Gets all available rooms for the given date range and guest count.
     */
    List<Room> getAvailableRooms(LocalDate checkIn, LocalDate checkOut, int guestCount);
    
    /**
     * Gets available rooms of a specific type.
     */
    List<Room> getAvailableRoomsByType(RoomType roomType, LocalDate checkIn, LocalDate checkOut, int guestCount);
    
    // Booking Operations
    /**
     * Creates a new booking.
     */
    Booking createBooking(String guestId, String roomId, LocalDate checkIn, LocalDate checkOut, 
                         int numberOfGuests, String specialRequests);
    
    /**
     * Gets a booking by ID.
     */
    Optional<Booking> getBooking(String bookingId);
    
    /**
     * Gets all bookings for a guest.
     */
    List<Booking> getBookingsByGuest(String guestId);
    
    /**
     * Gets all bookings for a room.
     */
    List<Booking> getBookingsByRoom(String roomId);
    
    /**
     * Gets all active bookings.
     */
    List<Booking> getActiveBookings();
    
    /**
     * Modifies an existing booking.
     */
    boolean modifyBooking(String bookingId, LocalDate newCheckIn, LocalDate newCheckOut, 
                         int newGuestCount, String newSpecialRequests);
    
    /**
     * Cancels a booking.
     */
    boolean cancelBooking(String bookingId);
    
    /**
     * Checks in a guest.
     */
    boolean checkIn(String bookingId);
    
    /**
     * Checks out a guest.
     */
    boolean checkOut(String bookingId);
    
    // Pricing
    /**
     * Calculates the total price for a booking.
     */
    BigDecimal calculatePrice(String roomId, LocalDate checkIn, LocalDate checkOut, int guestCount);
    
    /**
     * Gets the dynamic price for a room on a specific date.
     */
    BigDecimal getRoomPrice(String roomId, LocalDate date);
    
    // Statistics
    /**
     * Gets booking statistics.
     */
    BookingStats getBookingStats();
    
    /**
     * Gets occupancy rate for a date range.
     */
    double getOccupancyRate(LocalDate startDate, LocalDate endDate);
    
    /**
     * Gets revenue for a date range.
     */
    BigDecimal getRevenue(LocalDate startDate, LocalDate endDate);
}