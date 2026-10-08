package com.hostel.management.dto.response;

import com.hostel.management.enums.ComplaintCategory;
import com.hostel.management.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffResponse {
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private Role role;
    private String employeeId;
    private ComplaintCategory department;
    private String designation;
}
