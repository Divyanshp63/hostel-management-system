package com.hostel.management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {

    // Room & Bed Capacity KPI
    private long totalRooms;
    private long totalBeds;
    private long occupiedBeds;
    private long availableBeds;
    private double occupancyRate;

    // Student & Allocation Statistics
    private long totalStudents;
    private long activeAllocations;
    private long pendingAllocations;

    // Financial KPI
    private BigDecimal totalFeeBilled;
    private BigDecimal totalFeeCollected;
    private BigDecimal totalFeePending;
    private long overdueFeeCount;

    // Operations & Welfare
    private long pendingComplaints;
    private long inProgressComplaints;
    private long resolvedComplaints;
    private long pendingLeaves;
    private long activeVisitorsToday;
    private long activeNoticesCount;

    // Charts & Analytics
    private List<MonthlyRevenueDto> monthlyRevenue;
    private Map<String, Long> complaintsByCategory;
    private List<RoomTypeOccupancyDto> roomTypeOccupancy;

    // Recent Activity Feeds
    private List<RecentComplaintDto> recentComplaints;
    private List<RecentAllocationDto> pendingAllocationRequests;
}
