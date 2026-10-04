package com.hostel.management.dto.request;

import com.hostel.management.enums.ComplaintCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateComplaintRequest {

    @NotBlank(message = "Complaint title is required")
    @Size(min = 5, max = 150, message = "Title must be between 5 and 150 characters")
    private String title;

    @NotBlank(message = "Detailed description is required")
    @Size(min = 10, message = "Description must be at least 10 characters long")
    private String description;

    @NotNull(message = "Complaint category is required (MAINTENANCE, ELECTRICAL, PLUMBING, CLEANLINESS, FOOD_MESS, SECURITY, OTHER)")
    private ComplaintCategory category;
}
