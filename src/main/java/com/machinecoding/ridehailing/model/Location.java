package com.machinecoding.ridehailing.model;

import java.util.Objects;

/**
 * Represents a geographical location with coordinates.
 */
public class Location {
    private final double latitude;
    private final double longitude;
    private final String address;
    
    public Location(double latitude, double longitude) {
        this(latitude, longitude, null);
    }
    
    public Location(double latitude, double longitude, String address) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }
        
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address != null ? address.trim() : null;
    }
    
    // Getters
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getAddress() { return address; }
    
    /**
     * Calculates the distance to another location using Haversine formula.
     * Returns distance in kilometers.
     */
    public double distanceTo(Location other) {
        if (other == null) {
            throw new IllegalArgumentException("Other location cannot be null");
        }
        
        final double R = 6371; // Earth's radius in kilometers
        
        double lat1Rad = Math.toRadians(this.latitude);
        double lat2Rad = Math.toRadians(other.latitude);
        double deltaLatRad = Math.toRadians(other.latitude - this.latitude);
        double deltaLonRad = Math.toRadians(other.longitude - this.longitude);
        
        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    /**
     * Checks if this location is within the specified radius of another location.
     */
    public boolean isWithinRadius(Location other, double radiusKm) {
        return distanceTo(other) <= radiusKm;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Double.compare(location.latitude, latitude) == 0 &&
               Double.compare(location.longitude, longitude) == 0;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }
    
    @Override
    public String toString() {
        if (address != null) {
            return String.format("Location{lat=%.6f, lon=%.6f, address='%s'}", latitude, longitude, address);
        }
        return String.format("Location{lat=%.6f, lon=%.6f}", latitude, longitude);
    }
}