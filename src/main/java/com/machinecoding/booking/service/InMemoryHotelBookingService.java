package com.machinecoding.booking.service;

import com.machinecoding.booking.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * In-memory implementation of hotel booking service.
 * Provides thread-safe operations for room management, booking, and pricing.
 */
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
    
    // Room Management
    @Override
    public void addRoom(Room room) {
        if (room == null) {
            throw new IllegalArgumentException("Room cannot be null");
        }
        rooms.put(room.getRoomId(), room);
    }
    
    @Override
    public Optional<Room> getRoom(String roomId) {
        return Optional.ofNullable(rooms.get(roomId));
    }
    
    @Override
    public List<Room> getRoomsByType(RoomType roomType) {
        return rooms.values().stream()
                .filter(room -> room.getRoomType() == roomType)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Room> getAllActiveRooms() {
        return rooms.values().stream()
                .filter(Room::isActive)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean updateRoomStatus(String roomId, boolean active) {
        Room room = rooms.get(roomId);
        if (room == null) {
            return false;
        }
        
        Room updatedRoom = room.withActiveStatus(active);
        rooms.put(roomId, updatedRoom);
        return true;
    }
    
    // Guest Management
    @Override
    public void registerGuest(Guest guest) {
        if (guest == null) {
            throw new IllegalArgumentException("Guest cannot be null");
        }
        guests.put(guest.getGuestId(), guest);
    }
    
    @Override
    public Optional<Guest> getGuest(String guestId) {
        return Optional.ofNullable(guests.get(guestId));
    }
    
    @Override
    public List<Guest> findGuestsByEmail(String email) {
        if (email == null) {
            return new ArrayList<>();
        }
        
        String normalizedEmail = email.toLowerCase().trim();
        return guests.values().stream()
                .filter(guest -> guest.getEmail().equals(normalizedEmail))
                .collect(Collectors.toList());
    }
    
    // Availability Checking
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
    
    @Override
    public List<Room> getAvailableRooms(LocalDate checkIn, LocalDate checkOut, int guestCount) {
        return rooms.values().stream()
                .filter(Room::isActive)
                .filter(room -> room.getCapacity() >= guestCount)
                .filter(room -> isRoomAvailable(room.getRoomId(), checkIn, checkOut))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Room> getAvailableRoomsByType(RoomType roomType, LocalDate checkIn, LocalDate checkOut, int guestCount) {
        return getAvailableRooms(checkIn, checkOut, guestCount).stream()
                .filter(room -> room.getRoomType() == roomType)
                .collect(Collectors.toList());
    }
    
    // Booking Operations
    @Override
    public Booking createBooking(String guestId, String roomId, LocalDate checkIn, LocalDate checkOut, 
                                int numberOfGuests, String specialRequests) {
        // Validate guest exists
        if (!guests.containsKey(guestId)) {
            throw new IllegalArgumentException("Guest not found: " + guestId);
        }
        
        // Validate room exists and is available
        if (!isRoomAvailable(roomId, checkIn, checkOut)) {
            throw new IllegalArgumentException("Room not available for the specified dates");
        }
        
        Room room = rooms.get(roomId);
        if (room.getCapacity() < numberOfGuests) {
            throw new IllegalArgumentException("Room capacity insufficient for number of guests");
        }
        
        // Calculate total price
        BigDecimal totalAmount = calculatePrice(roomId, checkIn, checkOut, numberOfGuests);
        
        // Create booking
        String bookingId = "BK" + String.format("%06d", bookingIdCounter.getAndIncrement());
        Booking booking = new Booking(bookingId, guestId, roomId, checkIn, checkOut, 
                                    numberOfGuests, totalAmount, BookingStatus.CONFIRMED, specialRequests);
        
        bookings.put(bookingId, booking);
        return booking;
    }
    
    @Override
    public Optional<Booking> getBooking(String bookingId) {
        return Optional.ofNullable(bookings.get(bookingId));
    }
    
    @Override
    public List<Booking> getBookingsByGuest(String guestId) {
        return bookings.values().stream()
                .filter(booking -> booking.getGuestId().equals(guestId))
                .sorted(Comparator.comparing(Booking::getBookingTime).reversed())
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Booking> getBookingsByRoom(String roomId) {
        return bookings.values().stream()
                .filter(booking -> booking.getRoomId().equals(roomId))
                .sorted(Comparator.comparing(Booking::getCheckInDate))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Booking> getActiveBookings() {
        return bookings.values().stream()
                .filter(Booking::isActive)
                .sorted(Comparator.comparing(Booking::getCheckInDate))
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean modifyBooking(String bookingId, LocalDate newCheckIn, LocalDate newCheckOut, 
                                int newGuestCount, String newSpecialRequests) {
        Booking booking = bookings.get(bookingId);
        if (booking == null || !booking.canModify()) {
            return false;
        }
        
        // Check if room is available for new dates (excluding current booking)
        boolean available = bookings.values().stream()
                .filter(b -> !b.getBookingId().equals(bookingId))
                .filter(b -> b.getRoomId().equals(booking.getRoomId()))
                .filter(Booking::isActive)
                .noneMatch(b -> b.overlaps(newCheckIn, newCheckOut));
        
        if (!available) {
            return false;
        }
        
        Room room = rooms.get(booking.getRoomId());
        if (room.getCapacity() < newGuestCount) {
            return false;
        }
        
        // Calculate new price
        BigDecimal newTotalAmount = calculatePrice(booking.getRoomId(), newCheckIn, newCheckOut, newGuestCount);
        
        // Create new booking with updated details
        Booking updatedBooking = new Booking(bookingId, booking.getGuestId(), booking.getRoomId(),
                                           newCheckIn, newCheckOut, newGuestCount, newTotalAmount,
                                           booking.getStatus(), newSpecialRequests);
        
        bookings.put(bookingId, updatedBooking);
        return true;
    }
    
    @Override
    public boolean cancelBooking(String bookingId) {
        Booking booking = bookings.get(bookingId);
        if (booking == null || !booking.canCancel()) {
            return false;
        }
        
        booking.updateStatus(BookingStatus.CANCELLED);
        return true;
    }
    
    @Override
    public boolean checkIn(String bookingId) {
        Booking booking = bookings.get(bookingId);
        if (booking == null || booking.getStatus() != BookingStatus.CONFIRMED) {
            return false;
        }
        
        // Check if check-in date is today or in the past
        if (booking.getCheckInDate().isAfter(LocalDate.now())) {
            return false;
        }
        
        booking.updateStatus(BookingStatus.CHECKED_IN);
        return true;
    }
    
    @Override
    public boolean checkOut(String bookingId) {
        Booking booking = bookings.get(bookingId);
        if (booking == null || booking.getStatus() != BookingStatus.CHECKED_IN) {
            return false;
        }
        
        booking.updateStatus(BookingStatus.CHECKED_OUT);
        return true;
    }
    
    // Pricing
    @Override
    public BigDecimal calculatePrice(String roomId, LocalDate checkIn, LocalDate checkOut, int guestCount) {
        Room room = rooms.get(roomId);
        if (room == null) {
            throw new IllegalArgumentException("Room not found: " + roomId);
        }
        
        return pricingEngine.calculatePrice(room, checkIn, checkOut, guestCount);
    }
    
    @Override
    public BigDecimal getRoomPrice(String roomId, LocalDate date) {
        Room room = rooms.get(roomId);
        if (room == null) {
            throw new IllegalArgumentException("Room not found: " + roomId);
        }
        
        return pricingEngine.getDynamicPrice(room, date);
    }
    
    // Statistics
    @Override
    public BookingStats getBookingStats() {
        int totalRooms = rooms.size();
        int activeRooms = (int) rooms.values().stream().filter(Room::isActive).count();
        int totalBookings = bookings.size();
        int activeBookings = (int) bookings.values().stream().filter(Booking::isActive).count();
        
        Map<BookingStatus, Long> statusCounts = bookings.values().stream()
                .collect(Collectors.groupingBy(Booking::getStatus, Collectors.counting()));
        
        int confirmedBookings = statusCounts.getOrDefault(BookingStatus.CONFIRMED, 0L).intValue();
        int cancelledBookings = statusCounts.getOrDefault(BookingStatus.CANCELLED, 0L).intValue();
        int checkedInBookings = statusCounts.getOrDefault(BookingStatus.CHECKED_IN, 0L).intValue();
        int checkedOutBookings = statusCounts.getOrDefault(BookingStatus.CHECKED_OUT, 0L).intValue();
        
        BigDecimal totalRevenue = bookings.values().stream()
                .filter(booking -> booking.getStatus() == BookingStatus.CHECKED_OUT)
                .map(Booking::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal averageBookingValue = totalBookings > 0 ? 
                totalRevenue.divide(BigDecimal.valueOf(totalBookings), 2, BigDecimal.ROUND_HALF_UP) : 
                BigDecimal.ZERO;
        
        double occupancyRate = getOccupancyRate(LocalDate.now().minusDays(30), LocalDate.now());
        
        return new BookingStats(totalRooms, activeRooms, totalBookings, activeBookings,
                              confirmedBookings, cancelledBookings, checkedInBookings, checkedOutBookings,
                              totalRevenue, averageBookingValue, occupancyRate, LocalDate.now());
    }
    
    @Override
    public double getOccupancyRate(LocalDate startDate, LocalDate endDate) {
        if (rooms.isEmpty()) {
            return 0.0;
        }
        
        long totalRoomNights = rooms.values().stream()
                .filter(Room::isActive)
                .count() * (endDate.toEpochDay() - startDate.toEpochDay());
        
        if (totalRoomNights == 0) {
            return 0.0;
        }
        
        long occupiedRoomNights = bookings.values().stream()
                .filter(booking -> booking.getStatus() == BookingStatus.CHECKED_IN || 
                                 booking.getStatus() == BookingStatus.CHECKED_OUT)
                .filter(booking -> booking.overlaps(startDate, endDate))
                .mapToLong(booking -> {
                    LocalDate overlapStart = booking.getCheckInDate().isBefore(startDate) ? startDate : booking.getCheckInDate();
                    LocalDate overlapEnd = booking.getCheckOutDate().isAfter(endDate) ? endDate : booking.getCheckOutDate();
                    return overlapEnd.toEpochDay() - overlapStart.toEpochDay();
                })
                .sum();
        
        return (double) occupiedRoomNights / totalRoomNights * 100.0;
    }
    
    @Override
    public BigDecimal getRevenue(LocalDate startDate, LocalDate endDate) {
        return bookings.values().stream()
                .filter(booking -> booking.getStatus() == BookingStatus.CHECKED_OUT)
                .filter(booking -> booking.overlaps(startDate, endDate))
                .map(Booking::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Simple pricing engine for dynamic pricing calculations.
     */
    private static class PricingEngine {
        
        public BigDecimal calculatePrice(Room room, LocalDate checkIn, LocalDate checkOut, int guestCount) {
            long nights = checkOut.toEpochDay() - checkIn.toEpochDay();
            BigDecimal totalPrice = BigDecimal.ZERO;
            
            for (long i = 0; i < nights; i++) {
                LocalDate date = checkIn.plusDays(i);
                BigDecimal dailyPrice = getDynamicPrice(room, date);
                totalPrice = totalPrice.add(dailyPrice);
            }
            
            // Apply guest count multiplier for rooms with extra capacity
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
            
            // Holiday/peak season pricing (simplified)
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
                default:
                    // No additional markup for standard rooms
                    break;
            }
            
            return basePrice.setScale(2, BigDecimal.ROUND_HALF_UP);
        }
    }
}