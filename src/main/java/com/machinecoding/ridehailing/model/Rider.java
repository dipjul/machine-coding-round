package com.machinecoding.ridehailing.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a rider in the ride hailing system.
 */
public class Rider {
    private final String riderId;
    private final String name;
    private final String phoneNumber;
    private final String email;
    private double rating;
    private int totalTrips;
    private LocalDateTime lastActiveTime;
    private String paymentMethodId;
    
    public Rider(String riderId, String name, String phoneNumber, String email) {
        if (riderId == null || riderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Rider ID cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Rider name cannot be null or empty");
        }
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        
        this.riderId = riderId.trim();
        this.name = name.trim();
        this.phoneNumber = phoneNumber.trim();
        this.email = email.trim().toLowerCase();
        this.rating = 5.0; // Default rating
        this.totalTrips = 0;
        this.lastActiveTime = LocalDateTime.now();
    }
    
    // Getters
    public String getRiderId() { return riderId; }
    public String getName() { return name; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
    public double getRating() { return rating; }
    public int getTotalTrips() { return totalTrips; }
    public LocalDateTime getLastActiveTime() { return lastActiveTime; }
    public String getPaymentMethodId() { return paymentMethodId; }
    
    /**
     * Updates the rider's rating after a trip.
     */
    public void updateRating(double newRating) {
        if (newRating < 1.0 || newRating > 5.0) {
            throw new IllegalArgumentException("Rating must be between 1.0 and 5.0");
        }
        
        // Calculate weighted average rating
        double totalRatingPoints = this.rating * this.totalTrips + newRating;
        this.totalTrips++;
        this.rating = totalRatingPoints / this.totalTrips;
        this.lastActiveTime = LocalDateTime.now();
    }
    
    /**
     * Sets the payment method for the rider.
     */
    public void setPaymentMethod(String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
        this.lastActiveTime = LocalDateTime.now();
    }
    
    /**
     * Updates the last active time.
     */
    public void updateLastActiveTime() {
        this.lastActiveTime = LocalDateTime.now();
    }
    
    /**
     * Checks if the rider has a valid payment method.
     */
    public boolean hasPaymentMethod() {
        return paymentMethodId != null && !paymentMethodId.trim().isEmpty();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rider rider = (Rider) o;
        return Objects.equals(riderId, rider.riderId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(riderId);
    }
    
    @Override
    public String toString() {
        return String.format("Rider{id='%s', name='%s', rating=%.1f, trips=%d}", 
                           riderId, name, rating, totalTrips);
    }
}