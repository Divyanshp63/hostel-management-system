package com.hostel.management.entity;

import com.hostel.management.enums.LeaveStatus;
import com.hostel.management.enums.LeaveType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false, length = 30)
    private LeaveType leaveType;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String reason;

    @Column(name = "emergency_contact_phone", nullable = false, length = 20)
    private String emergencyContactPhone;

    @Column(name = "destination_address", length = 255)
    private String destinationAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private LeaveStatus status = LeaveStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private User approvedBy;

    @Column(name = "admin_remarks", length = 255)
    private String adminRemarks;

    @Column(name = "approved_or_rejected_at")
    private LocalDateTime approvedOrRejectedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public String getWardenRemarks() {
        return this.adminRemarks;
    }

    public void setWardenRemarks(String remarks) {
        this.adminRemarks = remarks;
    }

    public String getStudentName() {
        return this.student != null && this.student.getUser() != null ? this.student.getUser().getName() : "Unknown";
    }

    public String getAdmissionNumber() {
        return this.student != null ? this.student.getAdmissionNumber() : "";
    }

    public String getStudentAdmissionNumber() {
        return getAdmissionNumber();
    }

    public String getStudentPhone() {
        return this.student != null && this.student.getUser() != null ? this.student.getUser().getPhone() : "";
    }

    public long getTotalDays() {
        if (this.fromDate != null && this.toDate != null) {
            return java.time.temporal.ChronoUnit.DAYS.between(this.fromDate, this.toDate) + 1;
        }
        return 0;
    }

    public long getNumberOfDays() {
        return getTotalDays();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
