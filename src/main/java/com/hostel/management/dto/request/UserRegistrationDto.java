package com.hostel.management.dto.request;

import com.hostel.management.enums.ComplaintCategory;
import com.hostel.management.enums.Gender;
import com.hostel.management.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationDto {

    // Common Account Details
    @NotBlank(message = "Full Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Valid email is required")
    private String email;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be a valid 10-digit number")
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    @NotNull(message = "Please select a role")
    private Role role;

    // Staff common field (Warden, Accountant, Complaint Staff)
    private String employeeId;

    // Warden specific
    private String hostelAssignment;

    // Complaint Department Staff specific
    private ComplaintCategory department;

    // Student specific fields
    private Gender gender;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth;

    private String college;
    private String course;
    private String semester;
    private String admissionNumber;

    // Student Hostel Details
    private String hostelName;
    private String hostelBlock;
    private String roomNumber;
    private String bedNumber;

    // Student Guardian Details
    private String guardianName;
    private String guardianPhone;
    private String emergencyContact;
    private String permanentAddress;
}
