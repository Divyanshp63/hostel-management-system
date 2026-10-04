package com.hostel.management.dto.request;

import com.hostel.management.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotNull(message = "Fee invoice ID is required")
    private Long feeId;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "1.0", message = "Minimum payment amount is ₹1.00")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required (UPI, CREDIT_CARD, DEBIT_CARD, NET_BANKING, CASH)")
    private PaymentMethod paymentMethod;

    private String remarks;
}
