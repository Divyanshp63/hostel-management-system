package com.hostel.management.dto.response;

import com.hostel.management.enums.NoticePriority;
import com.hostel.management.enums.NoticeTarget;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeResponse {

    private Long id;
    private String title;
    private String content;
    private String postedByName;
    private NoticePriority priority;
    private NoticeTarget targetAudience;
    private LocalDate expiryDate;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
