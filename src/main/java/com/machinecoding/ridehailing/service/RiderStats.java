package com.machinecoding.ridehailing.service;

import java.math.BigDecimal;

/**
 * Statistics for individual riders.
 */
public class RiderStats {
    private final String riderId;
    private final int totalTrips;
    private final int completedTrips;
    private final int cancelledTrips;
    private final double averageRating;
    private final BigDecimal totalSpent;
    private final double averageTripDistance;
    
    public RiderStats(String riderId, int totalTrips, int completedTrips, int cancelledTrips,
                     double averageRating, BigDecimal totalSpent, double averageTripDistance) {
        this.riderId = riderId;
        this.totalTrips = totalTrips;
        this.completedTrips = completedTrips;
        this.cancelledTrips = cancelledTrips;
        this.averageRating = averageRating;
        this.totalSpent = totalSpent != null ? totalSpent : BigDecimal.ZERO;
        this.averageTripDistance = averageTripDistance;
    }
    
    // Getters
    public String getRiderId() { return riderId; }
    public int getTotalTrips() { return totalTrips; }
    public int getCompletedTrips() { return completedTrips; }
    public int getCancelledTrips() { return cancelledTrips; }
    public double getAverageRating() { return averageRating; }
    public BigDecimal getTotalSpent() { return totalSpent; }
    public double getAverageTripDistance() { return averageTripDistance; }
    
    public double getCompletionRate() {
        return totalTrips == 0 ? 0.0 : (double) completedTrips / totalTrips * 100.0;
    }
    
    public double getCancellationRate() {
        return totalTrips == 0 ? 0.0 : (double) cancelledTrips / totalTrips * 100.0;
    }
    
    public BigDecimal getAverageSpendPerTrip() {
        return completedTrips == 0 ? BigDecimal.ZERO : 
               totalSpent.divide(BigDecimal.valueOf(completedTrips), 2, BigDecimal.ROUND_HALF_UP);
    }
    
    @Override
    public String toString() {
        return String.format("RiderStats{id='%s', trips=%d, rating=%.1f, spent=%s}", 
                           riderId, totalTrips, averageRating, totalSpent);
    }
}