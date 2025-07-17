package com.machinecoding.booking.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a hotel guest with personal information.
 */
public class Guest {
    private final String guestId;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String phoneNumber;
    private final LocalDate dateOfBirth;
    private final String idNumber;
    private final String address;
    
    public Guest(String guestId, String firstName, String lastName, String email, 
                String phoneNumber, LocalDate dateOfBirth, String idNumber, String address) {
        if (guestId == null || guestId.trim().isEmpty()) {
            throw new IllegalArgumentException("Guest ID cannot be null or empty");
        }
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be null or empty");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be null or empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        
        this.guestId = guestId.trim();
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
        this.email = email.trim().toLowerCase();
        this.phoneNumber = phoneNumber != null ? phoneNumber.trim() : null;
        this.dateOfBirth = dateOfBirth;
        this.idNumber = idNumber != null ? idNumber.trim() : null;
        this.address = address != null ? address.trim() : null;
    }
    
    // Getters
    public String getGuestId() { return guestId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getIdNumber() { return idNumber; }
    public String getAddress() { return address; }
    
    /**
     * Gets the full name of the guest.
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    /**
     * Checks if the guest is an adult (18+ years old).
     */
    public boolean isAdult() {
        if (dateOfBirth == null) {
            return true; // Assume adult if no birth date provided
        }
        return LocalDate.now().minusYears(18).isAfter(dateOfBirth) || 
               LocalDate.now().minusYears(18).equals(dateOfBirth);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Guest guest = (Guest) o;
        return Objects.equals(guestId, guest.guestId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(guestId);
    }
    
    @Override
    public String toString() {
        return String.format("Guest{id='%s', name='%s', email='%s'}", 
                           guestId, getFullName(), email);
    }
}