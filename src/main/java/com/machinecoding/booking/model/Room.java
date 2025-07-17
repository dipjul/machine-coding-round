package com.machinecoding.booking.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a hotel room with its properties and pricing.
 */
public class Room {
    private final String roomId;
    private final String roomNumber;
    private final RoomType roomType;
    private final int capacity;
    private final BigDecimal basePrice;
    private final String description;
    private final boolean isActive;
    
    public Room(String roomId, String roomNumber, RoomType roomType, int capacity, 
               BigDecimal basePrice, String description) {
        this(roomId, roomNumber, roomType, capacity, basePrice, description, true);
    }
    
    public Room(String roomId, String roomNumber, RoomType roomType, int capacity, 
               BigDecimal basePrice, String description, boolean isActive) {
        if (roomId == null || roomId.trim().isEmpty()) {
            throw new IllegalArgumentException("Room ID cannot be null or empty");
        }
        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Room number cannot be null or empty");
        }
        if (roomType == null) {
            throw new IllegalArgumentException("Room type cannot be null");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Room capacity must be positive");
        }
        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Base price cannot be null or negative");
        }
        
        this.roomId = roomId.trim();
        this.roomNumber = roomNumber.trim();
        this.roomType = roomType;
        this.capacity = capacity;
        this.basePrice = basePrice;
        this.description = description != null ? description.trim() : "";
        this.isActive = isActive;
    }
    
    // Getters
    public String getRoomId() { return roomId; }
    public String getRoomNumber() { return roomNumber; }
    public RoomType getRoomType() { return roomType; }
    public int getCapacity() { return capacity; }
    public BigDecimal getBasePrice() { return basePrice; }
    public String getDescription() { return description; }
    public boolean isActive() { return isActive; }
    
    /**
     * Creates a copy of this room with active status changed.
     */
    public Room withActiveStatus(boolean active) {
        return new Room(roomId, roomNumber, roomType, capacity, basePrice, description, active);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return Objects.equals(roomId, room.roomId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(roomId);
    }
    
    @Override
    public String toString() {
        return String.format("Room{id='%s', number='%s', type=%s, capacity=%d, price=%s, active=%s}", 
                           roomId, roomNumber, roomType, capacity, basePrice, isActive);
    }
}