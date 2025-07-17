package com.machinecoding.ecommerce.model;

import java.time.LocalDateTime;

/**
 * Represents a customer in the e-commerce system.
 */
public class Customer {
    private final String customerId;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String phoneNumber;
    private final Address defaultAddress;
    private final CustomerStatus status;
    private final LocalDateTime registeredAt;
    
    public Customer(String customerId, String firstName, String lastName, String email,
                   String phoneNumber, Address defaultAddress, CustomerStatus status) {
        this.customerId = customerId != null ? customerId.trim() : "";
        this.firstName = firstName != null ? firstName.trim() : "";
        this.lastName = lastName != null ? lastName.trim() : "";
        this.email = email != null ? email.trim() : "";
        this.phoneNumber = phoneNumber != null ? phoneNumber.trim() : "";
        this.defaultAddress = defaultAddress;
        this.status = status != null ? status : CustomerStatus.ACTIVE;
        this.registeredAt = LocalDateTime.now();
    }
    
    // Getters
    public String getCustomerId() { return customerId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public Address getDefaultAddress() { return defaultAddress; }
    public CustomerStatus getStatus() { return status; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public boolean isActive() {
        return status == CustomerStatus.ACTIVE;
    }
    
    public Customer withStatus(CustomerStatus newStatus) {
        return new Customer(customerId, firstName, lastName, email, phoneNumber, defaultAddress, newStatus);
    }
    
    public Customer withAddress(Address newAddress) {
        return new Customer(customerId, firstName, lastName, email, phoneNumber, newAddress, status);
    }
    
    @Override
    public String toString() {
        return String.format("Customer{id='%s', name='%s', email='%s', status=%s}", 
                           customerId, getFullName(), email, status);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Customer customer = (Customer) obj;
        return customerId.equals(customer.customerId);
    }
    
    @Override
    public int hashCode() {
        return customerId.hashCode();
    }
}