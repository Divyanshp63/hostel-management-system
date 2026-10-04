package com.hostel.management.dto.response;

import com.hostel.management.enums.RoomStatus;
import com.hostel.management.enums.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponse {

    private Long id;
    private String roomNumber;
    private Integer floor;
    private RoomType roomType;
    private Integer capacity;
    private Integer occupied;
    private Integer availableBeds;
    private BigDecimal rentPerMonth;
    private RoomStatus status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
