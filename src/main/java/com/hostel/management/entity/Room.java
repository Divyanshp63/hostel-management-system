package com.hostel.management.entity;

import com.hostel.management.enums.RoomStatus;
import com.hostel.management.enums.RoomType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_number", unique = true, nullable = false, length = 20)
    private String roomNumber;

    @Column(name = "block_name", length = 50)
    private String blockName;

    @Column(nullable = false)
    private Integer floor;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 20)
    private RoomType roomType;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    @Builder.Default
    private Integer occupied = 0;

    @Column(name = "rent_per_month", nullable = false, precision = 10, scale = 2)
    private BigDecimal rentPerMonth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RoomStatus status = RoomStatus.AVAILABLE;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public int getAvailableBeds() {
        return Math.max(0, this.capacity - this.occupied);
    }

    public void updateStatusBasedOnOccupancy() {
        if (this.status != RoomStatus.UNDER_MAINTENANCE) {
            if (this.occupied >= this.capacity) {
                this.status = RoomStatus.FULL;
            } else {
                this.status = RoomStatus.AVAILABLE;
            }
        }
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        updateStatusBasedOnOccupancy();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        updateStatusBasedOnOccupancy();
    }
}
