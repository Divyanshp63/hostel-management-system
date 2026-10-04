package com.hostel.management.dto.request;

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
public class CreateVisitorRequest {

    @NotBlank(message = "Visitor name is required")
    private String visitorName;

    @NotBlank(message = "Visitor phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be a valid 10-digit number")
    private String visitorPhone;

    @NotBlank(message = "Relationship with visitor is required (e.g., Father, Mother, Friend)")
    private String relation;

    @NotNull(message = "Visit date is required")
    @FutureOrPresent(message = "Visit date cannot be in the past")
    private LocalDate visitDate;

    @NotBlank(message = "Purpose of visit is required")
    private String purpose;

    private String idProofType;
    private String idProofNumber;
}
