package com.hostel.management.dto.response;

import com.hostel.management.enums.VisitorStatus;
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
public class VisitorResponse {

    private Long id;

    // Student information
    private Long studentId;
    private String studentName;
    private String studentAdmissionNumber;
    private String studentPhone;
    private String roomNumber;

    // Visitor details
    private String visitorName;
    private String visitorPhone;
    private String relation;
    private LocalDate visitDate;
    private String visitTime;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private VisitorStatus status;
    private String idProofType;
    private String idProofNumber;
    private String purpose;
    private String adminRemarks;
    private String wardenRemarks;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
