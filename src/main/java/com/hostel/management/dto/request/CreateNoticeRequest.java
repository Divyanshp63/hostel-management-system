package com.hostel.management.dto.request;

import com.hostel.management.enums.NoticePriority;
import com.hostel.management.enums.NoticeTarget;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNoticeRequest {

    @NotBlank(message = "Notice title is required")
    @Size(min = 5, max = 150, message = "Title must be between 5 and 150 characters")
    private String title;

    @NotBlank(message = "Notice content is required")
    @Size(min = 10, message = "Content must be at least 10 characters long")
    private String content;

    @NotNull(message = "Notice priority is required (LOW, NORMAL, HIGH, URGENT)")
    private NoticePriority priority;

    @NotNull(message = "Target audience is required (ALL, STUDENTS, STAFF)")
    private NoticeTarget targetAudience;

    @FutureOrPresent(message = "Expiry date cannot be in the past")
    private LocalDate expiryDate;
}
