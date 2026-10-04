package com.hostel.management.dto.response;

import com.hostel.management.enums.FeeStatus;
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
public class FeeResponse {

    private Long id;

    // Student information
    private Long studentId;
    private String studentName;
    private String studentAdmissionNumber;
    private String studentEmail;

    // Fee breakdown
    private String month;
    private BigDecimal roomRent;
    private BigDecimal messFee;
    private BigDecimal electricityFee;
    private BigDecimal maintenanceFee;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal pendingAmount;

    // Status and dates
    private LocalDate dueDate;
    private FeeStatus status;
    private String remarks;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
