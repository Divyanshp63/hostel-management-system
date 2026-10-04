package com.hostel.management.dto.response;

import com.hostel.management.enums.FeeStatus;
import com.hostel.management.enums.PaymentMethod;
import com.hostel.management.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long id;
    private String transactionId;

    // Fee information
    private Long feeId;
    private String feeMonth;
    private BigDecimal feeTotalAmount;
    private BigDecimal feeRemainingPending;
    private FeeStatus updatedFeeStatus;

    // Student information
    private Long studentId;
    private String studentName;
    private String studentAdmissionNumber;

    // Payment details
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private LocalDateTime paymentDate;
    private String remarks;

    private LocalDateTime createdAt;
}
