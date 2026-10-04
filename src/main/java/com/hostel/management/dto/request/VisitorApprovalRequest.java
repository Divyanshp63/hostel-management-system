package com.hostel.management.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitorApprovalRequest {

    private String adminRemarks;
    private LocalDateTime checkInTime; // Optional; defaults to now() if omitted
}
