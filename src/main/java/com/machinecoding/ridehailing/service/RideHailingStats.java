package com.machinecoding.ridehailing.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Statistics for the ride hailing service.
 */
public class RideHailingStats {
    private final int totalDrivers;
    private final int activeDrivers;
    private final int totalRiders;
    private final int totalTrips;
    private final int activeTrips;
    private final int completedTrips;
    private final int cancelledTrips;
    private final BigDecimal totalRevenue;
    private final BigDecimal averageFare;
    private final double averageRating;
    private final LocalDateTime reportTime;
    
    public RideHailingStats(int totalDrivers, int activeDrivers, int totalRiders, int totalTrips,
                           int activeTrips, int completedTrips, int cancelledTrips, 
                           BigDecimal totalRevenue, BigDecimal averageFare, double averageRating) {
        this.totalDrivers = totalDrivers;
        this.activeDrivers = activeDrivers;
        this.totalRiders = totalRiders;
        this.totalTrips = totalTrips;
        this.activeTrips = activeTrips;
        this.completedTrips = completedTrips;
        this.cancelledTrips = cancelledTrips;
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        this.averageFare = averageFare != null ? averageFare : BigDecimal.ZERO;
        this.averageRating = averageRating;
        this.reportTime = LocalDateTime.now();
    }
    
    // Getters
    public int getTotalDrivers() { return totalDrivers; }
    public int getActiveDrivers() { return activeDrivers; }
    public int getTotalRiders() { return totalRiders; }
    public int getTotalTrips() { return totalTrips; }
    public int getActiveTrips() { return activeTrips; }
    public int getCompletedTrips() { return completedTrips; }
    public int getCancelledTrips() { return cancelledTrips; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public BigDecimal getAverageFare() { return averageFare; }
    public double getAverageRating() { return averageRating; }
    public LocalDateTime getReportTime() { return reportTime; }
    
    /**
     * Gets the completion rate as a percentage.
     */
    public double getCompletionRate() {
        return totalTrips == 0 ? 0.0 : (double) completedTrips / totalTrips * 100.0;
    }
    
    /**
     * Gets the cancellation rate as a percentage.
     */
    public double getCancellationRate() {
        return totalTrips == 0 ? 0.0 : (double) cancelledTrips / totalTrips * 100.0;
    }
    
    /**
     * Gets the driver utilization rate as a percentage.
     */
    public double getDriverUtilizationRate() {
        return totalDrivers == 0 ? 0.0 : (double) activeDrivers / totalDrivers * 100.0;
    }
    
    @Override
    public String toString() {
        return String.format(
            "RideHailingStats{drivers=%d/%d (%.1f%%), riders=%d, trips=%d (active=%d, completed=%d, cancelled=%d), " +
            "revenue=%s, avgFare=%s, avgRating=%.1f, completion=%.1f%%, cancellation=%.1f%%}",
            activeDrivers, totalDrivers, getDriverUtilizationRate(),
            totalRiders, totalTrips, activeTrips, completedTrips, cancelledTrips,
            totalRevenue, averageFare, averageRating, getCompletionRate(), getCancellationRate()
        );
    }
}