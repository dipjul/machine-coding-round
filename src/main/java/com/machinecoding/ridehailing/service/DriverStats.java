package com.machinecoding.ridehailing.service;

import java.math.BigDecimal;

/**
 * Statistics for individual drivers.
 */
public class DriverStats {
    private final String driverId;
    private final int totalTrips;
    private final int completedTrips;
    private final double averageRating;
    private final BigDecimal totalEarnings;
    private final double averageTripDistance;
    private final long totalOnlineTimeHours;
    
    public DriverStats(String driverId, int totalTrips, int completedTrips, double averageRating,
                      BigDecimal totalEarnings, double averageTripDistance, long totalOnlineTimeHours) {
        this.driverId = driverId;
        this.totalTrips = totalTrips;
        this.completedTrips = completedTrips;
        this.averageRating = averageRating;
        this.totalEarnings = totalEarnings != null ? totalEarnings : BigDecimal.ZERO;
        this.averageTripDistance = averageTripDistance;
        this.totalOnlineTimeHours = totalOnlineTimeHours;
    }
    
    // Getters
    public String getDriverId() { return driverId; }
    public int getTotalTrips() { return totalTrips; }
    public int getCompletedTrips() { return completedTrips; }
    public double getAverageRating() { return averageRating; }
    public BigDecimal getTotalEarnings() { return totalEarnings; }
    public double getAverageTripDistance() { return averageTripDistance; }
    public long getTotalOnlineTimeHours() { return totalOnlineTimeHours; }
    
    public double getCompletionRate() {
        return totalTrips == 0 ? 0.0 : (double) completedTrips / totalTrips * 100.0;
    }
    
    @Override
    public String toString() {
        return String.format("DriverStats{id='%s', trips=%d, rating=%.1f, earnings=%s}", 
                           driverId, totalTrips, averageRating, totalEarnings);
    }
}