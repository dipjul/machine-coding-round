package com.machinecoding.booking.service;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Statistics for hotel booking operations.
 */
public class BookingStats {
    private final int totalRooms;
    private final int activeRooms;
    private final int totalBookings;
    private final int activeBookings;
    private final int confirmedBookings;
    private final int cancelledBookings;
    private final int checkedInBookings;
    private final int checkedOutBookings;
    private final BigDecimal totalRevenue;
    private final BigDecimal averageBookingValue;
    private final double occupancyRate;
    private final LocalDate reportDate;
    
    public BookingStats(int totalRooms, int activeRooms, int totalBookings, int activeBookings,
                       int confirmedBookings, int cancelledBookings, int checkedInBookings, 
                       int checkedOutBookings, BigDecimal totalRevenue, BigDecimal averageBookingValue,
                       double occupancyRate, LocalDate reportDate) {
        this.totalRooms = totalRooms;
        this.activeRooms = activeRooms;
        this.totalBookings = totalBookings;
        this.activeBookings = activeBookings;
        this.confirmedBookings = confirmedBookings;
        this.cancelledBookings = cancelledBookings;
        this.checkedInBookings = checkedInBookings;
        this.checkedOutBookings = checkedOutBookings;
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        this.averageBookingValue = averageBookingValue != null ? averageBookingValue : BigDecimal.ZERO;
        this.occupancyRate = occupancyRate;
        this.reportDate = reportDate != null ? reportDate : LocalDate.now();
    }
    
    // Getters
    public int getTotalRooms() { return totalRooms; }
    public int getActiveRooms() { return activeRooms; }
    public int getTotalBookings() { return totalBookings; }
    public int getActiveBookings() { return activeBookings; }
    public int getConfirmedBookings() { return confirmedBookings; }
    public int getCancelledBookings() { return cancelledBookings; }
    public int getCheckedInBookings() { return checkedInBookings; }
    public int getCheckedOutBookings() { return checkedOutBookings; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public BigDecimal getAverageBookingValue() { return averageBookingValue; }
    public double getOccupancyRate() { return occupancyRate; }
    public LocalDate getReportDate() { return reportDate; }
    
    /**
     * Gets the cancellation rate as a percentage.
     */
    public double getCancellationRate() {
        return totalBookings == 0 ? 0.0 : (double) cancelledBookings / totalBookings * 100.0;
    }
    
    /**
     * Gets the confirmation rate as a percentage.
     */
    public double getConfirmationRate() {
        return totalBookings == 0 ? 0.0 : (double) confirmedBookings / totalBookings * 100.0;
    }
    
    /**
     * Gets the room utilization rate as a percentage.
     */
    public double getRoomUtilizationRate() {
        return totalRooms == 0 ? 0.0 : (double) activeRooms / totalRooms * 100.0;
    }
    
    @Override
    public String toString() {
        return String.format(
            "BookingStats{rooms=%d/%d (%.1f%%), bookings=%d (active=%d, confirmed=%d, cancelled=%d), " +
            "revenue=%s, avgValue=%s, occupancy=%.1f%%, date=%s}",
            activeRooms, totalRooms, getRoomUtilizationRate(),
            totalBookings, activeBookings, confirmedBookings, cancelledBookings,
            totalRevenue, averageBookingValue, occupancyRate, reportDate
        );
    }
}