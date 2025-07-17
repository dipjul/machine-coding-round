package com.machinecoding.ridehailing.model;

import java.util.Objects;

/**
 * Represents a vehicle in the ride hailing system.
 */
public class Vehicle {
    private final String vehicleId;
    private final String make;
    private final String model;
    private final int year;
    private final String color;
    private final String licensePlate;
    private final VehicleType type;
    private final int capacity;
    
    public Vehicle(String vehicleId, String make, String model, int year, String color, 
                  String licensePlate, VehicleType type, int capacity) {
        if (vehicleId == null || vehicleId.trim().isEmpty()) {
            throw new IllegalArgumentException("Vehicle ID cannot be null or empty");
        }
        if (make == null || make.trim().isEmpty()) {
            throw new IllegalArgumentException("Make cannot be null or empty");
        }
        if (model == null || model.trim().isEmpty()) {
            throw new IllegalArgumentException("Model cannot be null or empty");
        }
        if (year < 1900 || year > 2030) {
            throw new IllegalArgumentException("Year must be between 1900 and 2030");
        }
        if (color == null || color.trim().isEmpty()) {
            throw new IllegalArgumentException("Color cannot be null or empty");
        }
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            throw new IllegalArgumentException("License plate cannot be null or empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Vehicle type cannot be null");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        
        this.vehicleId = vehicleId.trim();
        this.make = make.trim();
        this.model = model.trim();
        this.year = year;
        this.color = color.trim();
        this.licensePlate = licensePlate.trim().toUpperCase();
        this.type = type;
        this.capacity = capacity;
    }
    
    // Getters
    public String getVehicleId() { return vehicleId; }
    public String getMake() { return make; }
    public String getModel() { return model; }
    public int getYear() { return year; }
    public String getColor() { return color; }
    public String getLicensePlate() { return licensePlate; }
    public VehicleType getType() { return type; }
    public int getCapacity() { return capacity; }
    
    /**
     * Gets the full vehicle description.
     */
    public String getFullDescription() {
        return String.format("%d %s %s %s (%s)", year, color, make, model, licensePlate);
    }
    
    /**
     * Checks if this vehicle can accommodate the specified number of passengers.
     */
    public boolean canAccommodate(int passengerCount) {
        return capacity >= passengerCount;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vehicle vehicle = (Vehicle) o;
        return Objects.equals(vehicleId, vehicle.vehicleId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(vehicleId);
    }
    
    @Override
    public String toString() {
        return String.format("Vehicle{id='%s', %s, type=%s, capacity=%d}", 
                           vehicleId, getFullDescription(), type, capacity);
    }
}