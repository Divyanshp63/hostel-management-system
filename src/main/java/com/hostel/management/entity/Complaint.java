package com.hostel.management.entity;

import com.hostel.management.enums.ComplaintCategory;
import com.hostel.management.enums.ComplaintPriority;
import com.hostel.management.enums.ComplaintStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "complaints")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(name = "hostel_name", length = 100)
    private String hostelName;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ComplaintCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ComplaintPriority priority = ComplaintPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ComplaintStatus status = ComplaintStatus.SUBMITTED;

    @Enumerated(EnumType.STRING)
    @Column(name = "assigned_department", length = 30)
    @Builder.Default
    private ComplaintCategory assignedDepartment = ComplaintCategory.MAINTENANCE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_staff_id")
    private User assignedStaff;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "sla_deadline")
    private LocalDateTime slaDeadline;

    @Column(name = "resolution_remarks", columnDefinition = "TEXT")
    private String resolutionRemarks;

    @Column(name = "resolution_details", columnDefinition = "TEXT")
    private String resolutionDetails;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_id")
    private User verifiedBy;

    @Column(name = "verified_by_name", length = 100)
    private String verifiedByName;

    @Column(name = "verification_remarks", columnDefinition = "TEXT")
    private String verificationRemarks;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "feedback_at")
    private LocalDateTime feedbackAt;

    @Column(name = "work_notes", columnDefinition = "TEXT")
    private String workNotes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public String getComplaintCode() {
        return "CMP-" + String.format("%04d", this.id != null ? this.id : 0);
    }

    public String getAssignedStaffName() {
        return this.assignedStaff != null ? this.assignedStaff.getName() : null;
    }

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

    public String getStudentName() {
        return this.student != null && this.student.getUser() != null ? this.student.getUser().getName() : "Unknown";
    }

    public String getStudentAdmissionNumber() {
        return this.student != null ? this.student.getAdmissionNumber() : "N/A";
    }

    public boolean isSlaBreached() {
        if (this.status == ComplaintStatus.RESOLVED_BY_MAINTENANCE ||
                this.status == ComplaintStatus.VERIFIED ||
                this.status == ComplaintStatus.CLOSED ||
                this.status == ComplaintStatus.REJECTED) {
            return false;
        }
        if (this.slaDeadline == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(this.slaDeadline);
    }

    public LocalDateTime computeSlaDeadline() {
        LocalDateTime baseTime = this.createdAt != null ? this.createdAt : LocalDateTime.now();
        int hours = this.priority != null ? this.priority.getSlaHours() : 24;
        return baseTime.plusHours(hours);
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = ComplaintStatus.SUBMITTED;
        }
        if (this.priority == null) {
            this.priority = ComplaintPriority.MEDIUM;
        }
        if (this.slaDeadline == null) {
            this.slaDeadline = computeSlaDeadline();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.slaDeadline == null) {
            this.slaDeadline = computeSlaDeadline();
        }
    }
}
