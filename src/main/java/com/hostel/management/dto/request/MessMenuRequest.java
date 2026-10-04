package com.hostel.management.dto.request;

import com.hostel.management.enums.MealType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessMenuRequest {

    @NotNull(message = "Day of week is required (e.g., MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "Meal type is required (BREAKFAST, LUNCH, SNACKS, DINNER)")
    private MealType mealType;

    @NotBlank(message = "Menu items cannot be blank")
    @Size(max = 1000, message = "Menu items description cannot exceed 1000 characters")
    private String items;

    @Size(max = 100, message = "Timing description cannot exceed 100 characters")
    private String timing;

    @Size(max = 255, message = "Special diet note cannot exceed 255 characters")
    private String specialDiet;
}
