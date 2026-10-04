package com.hostel.management.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFeeRequest {

    @NotNull(message = "Room rent is required")
    @DecimalMin(value = "0.0", message = "Room rent cannot be negative")
    private BigDecimal roomRent;

    @NotNull(message = "Mess fee is required")
    @DecimalMin(value = "0.0", message = "Mess fee cannot be negative")
    private BigDecimal messFee;

    @NotNull(message = "Electricity fee is required")
    @DecimalMin(value = "0.0", message = "Electricity fee cannot be negative")
    private BigDecimal electricityFee;

    @NotNull(message = "Maintenance fee is required")
    @DecimalMin(value = "0.0", message = "Maintenance fee cannot be negative")
    private BigDecimal maintenanceFee;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    private String remarks;
}
