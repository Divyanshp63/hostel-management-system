package com.hostel.management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDashboardResponse {

    // Student & Room Profile
    private Long studentId;
    private String studentName;
    private String admissionNumber;
    private String roomNumber;
    private String roomType;
    private String allocationStatus;

    // Financial Overview
    private BigDecimal totalPendingDues;
    private BigDecimal lastPaymentAmount;
    private LocalDateTime lastPaymentDate;
    private String latestFeeStatus;
    private String latestFeeMonth;

    // Daily Food / Mess Menu
    private String todayBreakfast;
    private String todayLunch;
    private String todaySnacks;
    private String todayDinner;

    // Welfare & Requests Overview
    private long myPendingComplaints;
    private String latestComplaintStatus;
    private long myPendingLeaves;
    private long myPendingVisitors;

    // Announcements
    private List<NoticeResponse> latestNotices;
}
