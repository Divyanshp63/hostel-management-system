package com.hostel.management.dto.request;

import com.hostel.management.enums.RoomType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomRequest {

    @NotBlank(message = "Room number is required")
    private String roomNumber;

    @NotNull(message = "Floor number is required")
    @Min(value = 0, message = "Floor cannot be negative")
    private Integer floor;

    @NotNull(message = "Room type is required (SINGLE, DOUBLE, TRIPLE, FOUR_SHARING, DELUXE)")
    private RoomType roomType;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    @NotNull(message = "Monthly rent is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Rent must be greater than zero")
    private BigDecimal rentPerMonth;

    private String description;
}
