package com.machinecoding.booking.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Represents a hotel booking with guest information, room details, and pricing.
 */
public class Booking {
    private final String bookingId;
    private final String guestId;
    private final String roomId;
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;
    private final int numberOfGuests;
    private final BigDecimal totalAmount;
    private final LocalDateTime bookingTime;
    private BookingStatus status;
    private String specialRequests;
    private LocalDateTime lastModified;
    
    public Booking(String bookingId, String guestId, String roomId, 
                  LocalDate checkInDate, LocalDate checkOutDate, int numberOfGuests, 
                  BigDecimal totalAmount) {
        this(bookingId, guestId, roomId, checkInDate, checkOutDate, numberOfGuests, 
             totalAmount, BookingStatus.PENDING, null);
    }
    
    public Booking(String bookingId, String guestId, String roomId, 
                  LocalDate checkInDate, LocalDate checkOutDate, int numberOfGuests, 
                  BigDecimal totalAmount, BookingStatus status, String specialRequests) {
        if (bookingId == null || bookingId.trim().isEmpty()) {
            throw new IllegalArgumentException("Booking ID cannot be null or empty");
        }
        if (guestId == null || guestId.trim().isEmpty()) {
            throw new IllegalArgumentException("Guest ID cannot be null or empty");
        }
        if (roomId == null || roomId.trim().isEmpty()) {
            throw new IllegalArgumentException("Room ID cannot be null or empty");
        }
        if (checkInDate == null) {
            throw new IllegalArgumentException("Check-in date cannot be null");
        }
        if (checkOutDate == null) {
            throw new IllegalArgumentException("Check-out date cannot be null");
        }
        if (checkOutDate.isBefore(checkInDate) || checkOutDate.equals(checkInDate)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }
        if (numberOfGuests <= 0) {
            throw new IllegalArgumentException("Number of guests must be positive");
        }
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Total amount cannot be null or negative");
        }
        
        this.bookingId = bookingId.trim();
        this.guestId = guestId.trim();
        this.roomId = roomId.trim();
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.numberOfGuests = numberOfGuests;
        this.totalAmount = totalAmount;
        this.status = status != null ? status : BookingStatus.PENDING;
        this.specialRequests = specialRequests != null ? specialRequests.trim() : null;
        this.bookingTime = LocalDateTime.now();
        this.lastModified = this.bookingTime;
    }
    
    // Getters
    public String getBookingId() { return bookingId; }
    public String getGuestId() { return guestId; }
    public String getRoomId() { return roomId; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public int getNumberOfGuests() { return numberOfGuests; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public LocalDateTime getBookingTime() { return bookingTime; }
    public BookingStatus getStatus() { return status; }
    public String getSpecialRequests() { return specialRequests; }
    public LocalDateTime getLastModified() { return lastModified; }
    
    /**
     * Gets the number of nights for this booking.
     */
    public long getNumberOfNights() {
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }
    
    /**
     * Gets the average price per night.
     */
    public BigDecimal getPricePerNight() {
        long nights = getNumberOfNights();
        return nights > 0 ? totalAmount.divide(BigDecimal.valueOf(nights), 2, BigDecimal.ROUND_HALF_UP) : totalAmount;
    }
    
    /**
     * Checks if the booking overlaps with the given date range.
     */
    public boolean overlaps(LocalDate startDate, LocalDate endDate) {
        return !checkOutDate.isBefore(startDate) && !checkInDate.isAfter(endDate);
    }
    
    /**
     * Checks if the booking is for the given date range.
     */
    public boolean isForDateRange(LocalDate startDate, LocalDate endDate) {
        return checkInDate.equals(startDate) && checkOutDate.equals(endDate);
    }
    
    /**
     * Updates the booking status.
     */
    public void updateStatus(BookingStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        this.status = newStatus;
        this.lastModified = LocalDateTime.now();
    }
    
    /**
     * Updates special requests.
     */
    public void updateSpecialRequests(String requests) {
        this.specialRequests = requests != null ? requests.trim() : null;
        this.lastModified = LocalDateTime.now();
    }
    
    /**
     * Checks if this booking can be modified.
     */
    public boolean canModify() {
        return status.canModify();
    }
    
    /**
     * Checks if this booking can be cancelled.
     */
    public boolean canCancel() {
        return status.canCancel();
    }
    
    /**
     * Checks if this booking is active.
     */
    public boolean isActive() {
        return status.isActive();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Booking booking = (Booking) o;
        return Objects.equals(bookingId, booking.bookingId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(bookingId);
    }
    
    @Override
    public String toString() {
        return String.format("Booking{id='%s', guest='%s', room='%s', dates=%s to %s, guests=%d, amount=%s, status=%s}", 
                           bookingId, guestId, roomId, checkInDate, checkOutDate, numberOfGuests, totalAmount, status);
    }
}