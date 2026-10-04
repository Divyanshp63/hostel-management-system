package com.hostel.management.dto.request;

import com.hostel.management.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStudentRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be a valid 10-digit number")
    private String phone;

    @NotBlank(message = "Course is required")
    private String course;

    @NotBlank(message = "Year of study is required")
    private String yearOfStudy;

    @NotNull(message = "Gender is required (MALE, FEMALE, OTHER)")
    private Gender gender;

    private LocalDate dateOfBirth;

    private String bloodGroup;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Guardian name is required")
    private String guardianName;

    @NotBlank(message = "Guardian phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Guardian phone must be a valid 10-digit number")
    private String guardianPhone;

    @NotBlank(message = "Emergency contact is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Emergency contact must be a valid 10-digit number")
    private String emergencyContact;
}
