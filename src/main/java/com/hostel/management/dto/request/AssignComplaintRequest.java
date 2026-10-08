package com.hostel.management.dto.request;

import com.hostel.management.enums.ComplaintCategory;
import com.hostel.management.enums.ComplaintPriority;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignComplaintRequest {

    @NotNull(message = "Department must be selected")
    private ComplaintCategory department;

    private Long staffId;

    @NotNull(message = "Priority must be set")
    private ComplaintPriority priority;

    private String remarks;
}
