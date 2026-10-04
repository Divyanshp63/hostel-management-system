package com.hostel.management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomTypeOccupancyDto {
    private String roomType;
    private long roomCount;
    private long totalBeds;
    private long occupiedBeds;
    private double occupancyRate;
}
