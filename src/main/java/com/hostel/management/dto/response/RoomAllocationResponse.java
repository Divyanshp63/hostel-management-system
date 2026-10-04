package com.hostel.management.dto.response;

import com.hostel.management.enums.AllocationStatus;
import com.hostel.management.enums.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomAllocationResponse {

    private Long id;

    // Student information
    private Long studentId;
    private String studentName;
    private String studentAdmissionNumber;
    private String studentEmail;
    private String studentPhone;

    // Room information
    private Long roomId;
    private String roomNumber;
    private Integer floor;
    private RoomType roomType;
    private BigDecimal rentPerMonth;

    // Allocation details
    private String bedNumber;
    private AllocationStatus status;
    private LocalDate requestDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private String rejectionReason;
    private String remarks;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
