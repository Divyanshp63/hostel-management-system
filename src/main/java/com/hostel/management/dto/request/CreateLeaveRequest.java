package com.hostel.management.dto.request;

import com.hostel.management.enums.LeaveType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeaveRequest {

    @NotNull(message = "Leave type is required (HOME_VISIT, MEDICAL, ACADEMIC, EMERGENCY, OTHER)")
    private LeaveType leaveType;

    @NotNull(message = "From date is required")
    @FutureOrPresent(message = "From date cannot be in the past")
    private LocalDate fromDate;

    @NotNull(message = "To date is required")
    @FutureOrPresent(message = "To date cannot be in the past")
    private LocalDate toDate;

    @NotBlank(message = "Reason for leave is required")
    private String reason;

    @NotBlank(message = "Emergency contact phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Emergency contact must be a valid 10-digit number")
    private String emergencyContactPhone;

    private String destinationAddress;
}
