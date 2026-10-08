package com.hostel.management.dto.response;

import com.hostel.management.enums.ComplaintCategory;
import com.hostel.management.enums.ComplaintPriority;
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
    private String complaintCode;

    // Student information
    private Long studentId;
    private String studentName;
    private String studentAdmissionNumber;
    private String studentEmail;
    private String studentPhone;
    private String roomNumber;
    private String hostelName;

    // Complaint details
    private String title;
    private String description;
    private ComplaintCategory category;
    private ComplaintPriority priority;
    private ComplaintStatus status;

    // Assignment & Workflow
    private ComplaintCategory assignedDepartment;
    private Long assignedStaffId;
    private String assignedStaffName;
    private LocalDateTime assignedAt;

    // SLA
    private LocalDateTime slaDeadline;
    private boolean slaBreached;

    // Resolution & Notes
    private String resolutionRemarks;
    private String resolutionDetails;
    private LocalDateTime acceptedAt;
    private LocalDateTime startedAt;
    private LocalDateTime resolvedAt;
    private String workNotes;

    // Verification
    private LocalDateTime verifiedAt;
    private Long verifiedById;
    private String verifiedByName;
    private String verificationRemarks;

    // Feedback
    private Integer rating;
    private String feedback;
    private LocalDateTime feedbackAt;

    // Audit History Timeline
    private java.util.List<ComplaintHistoryDto> history;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getFeedbackNotes() {
        return this.feedback;
    }

    public String getEffectiveResolutionDetails() {
        if (this.resolutionDetails != null && !this.resolutionDetails.isBlank()) {
            return this.resolutionDetails;
        }
        return this.resolutionRemarks;
    }

    public String getEffectiveVerificationRemarks() {
        return this.verificationRemarks;
    }
}
