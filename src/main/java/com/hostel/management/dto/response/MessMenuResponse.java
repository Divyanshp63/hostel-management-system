package com.hostel.management.dto.response;

import com.hostel.management.enums.MealType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessMenuResponse {

    private Long id;
    private DayOfWeek dayOfWeek;
    private MealType mealType;
    private String items;
    private String timing;
    private String specialDiet;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
