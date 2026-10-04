package com.hostel.management.dto.response;

import com.hostel.management.enums.ComplaintCategory;
import com.hostel.management.enums.ComplaintStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintResponse {

    private Long id;

    // Student information
    private Long studentId;
    private String studentName;
    private String studentAdmissionNumber;
    private String studentEmail;
    private String studentPhone;
    private String roomNumber;

    // Complaint details
    private String title;
    private String description;
    private ComplaintCategory category;
    private ComplaintStatus status;
    private String resolutionRemarks;
    private LocalDateTime resolvedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
