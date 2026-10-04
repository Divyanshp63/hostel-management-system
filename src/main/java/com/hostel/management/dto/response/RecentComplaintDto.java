package com.hostel.management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentComplaintDto {
    private Long id;
    private String title;
    private String studentName;
    private String roomNumber;
    private String category;
    private String status;
    private LocalDateTime createdAt;
}
