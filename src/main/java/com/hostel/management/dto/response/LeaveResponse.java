package com.hostel.management.dto.response;

import com.hostel.management.enums.LeaveStatus;
import com.hostel.management.enums.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveResponse {

    private Long id;

    // Student information
    private Long studentId;
    private String studentName;
    private String studentAdmissionNumber;
    private String studentEmail;
    private String studentPhone;
    private String roomNumber;

    // Leave information
    private LeaveType leaveType;
    private LocalDate fromDate;
    private LocalDate toDate;
    private long numberOfDays;
    private String reason;
    private String emergencyContactPhone;
    private String destinationAddress;
    private LeaveStatus status;
    private String adminRemarks;
    private LocalDateTime approvedOrRejectedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getAdmissionNumber() {
        return this.studentAdmissionNumber;
    }

    public long getTotalDays() {
        return this.numberOfDays;
    }

    public String getWardenRemarks() {
        return this.adminRemarks;
    }
}
