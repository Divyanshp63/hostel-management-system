package com.hostel.management.entity;

import com.hostel.management.enums.FeeStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fees", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"student_id", "month"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false, length = 30)
    private String month; // e.g. "OCTOBER-2026"

    @Column(name = "room_rent", nullable = false, precision = 10, scale = 2)
    private BigDecimal roomRent;

    @Column(name = "mess_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal messFee;

    @Column(name = "electricity_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal electricityFee;

    @Column(name = "maintenance_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal maintenanceFee;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "paid_amount", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "remaining_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal remainingAmount;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private FeeStatus status = FeeStatus.PENDING;

    @Column(length = 255)
    private String remarks;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public void recalculateTotalsAndStatus() {
        if (this.roomRent == null) this.roomRent = BigDecimal.ZERO;
        if (this.messFee == null) this.messFee = BigDecimal.ZERO;
        if (this.electricityFee == null) this.electricityFee = BigDecimal.ZERO;
        if (this.maintenanceFee == null) this.maintenanceFee = BigDecimal.ZERO;
        if (this.paidAmount == null) this.paidAmount = BigDecimal.ZERO;

        this.totalAmount = this.roomRent
                .add(this.messFee)
                .add(this.electricityFee)
                .add(this.maintenanceFee);

        this.remainingAmount = this.totalAmount.subtract(this.paidAmount);
        if (this.remainingAmount.compareTo(BigDecimal.ZERO) < 0) {
            this.remainingAmount = BigDecimal.ZERO;
        }

        // Determine status based on paid amount and due date
        if (this.paidAmount.compareTo(this.totalAmount) >= 0) {
            this.status = FeeStatus.PAID;
        } else if (this.paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            if (this.dueDate != null && LocalDate.now().isAfter(this.dueDate)) {
                this.status = FeeStatus.OVERDUE;
            } else {
                this.status = FeeStatus.PARTIAL;
            }
        } else {
            if (this.dueDate != null && LocalDate.now().isAfter(this.dueDate)) {
                this.status = FeeStatus.OVERDUE;
            } else {
                this.status = FeeStatus.PENDING;
            }
        }
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        recalculateTotalsAndStatus();
    }

    public BigDecimal getPendingAmount() {
        return this.remainingAmount != null ? this.remainingAmount : BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        recalculateTotalsAndStatus();
    }
}
