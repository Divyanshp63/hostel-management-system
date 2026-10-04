package com.hostel.management.dto.response;

import com.hostel.management.enums.Gender;
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
public class StudentResponse {

    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String admissionNumber;
    private String course;
    private String yearOfStudy;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String bloodGroup;
    private String address;
    private String guardianName;
    private String guardianPhone;
    private String emergencyContact;
    private boolean enabled;
    private LocalDateTime createdAt;
}
