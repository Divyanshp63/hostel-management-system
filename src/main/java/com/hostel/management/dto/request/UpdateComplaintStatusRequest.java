package com.hostel.management.dto.request;

import com.hostel.management.enums.ComplaintStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateComplaintStatusRequest {

    @NotNull(message = "New complaint status is required (IN_PROGRESS, RESOLVED)")
    private ComplaintStatus status;

    private String resolutionRemarks;
}
