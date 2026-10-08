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
public class ComplaintHistoryDto {
    private String action;
    private String performedBy;
    private String role;
    private LocalDateTime timestamp;
    private String remarks;
}
