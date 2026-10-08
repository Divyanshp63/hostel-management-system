package com.hostel.management.controller;

import com.hostel.management.dto.response.AdminDashboardResponse;
import com.hostel.management.dto.response.ApiResponse;
import com.hostel.management.dto.response.StudentDashboardResponse;
import com.hostel.management.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping({"/warden", "/admin"})
    @PreAuthorize("hasAnyRole('WARDEN', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getWardenDashboard() {
        AdminDashboardResponse response = dashboardService.getAdminDashboardStats();
        return ResponseEntity.ok(
                ApiResponse.success("Warden dashboard statistics retrieved successfully", response)
        );
    }

    @GetMapping("/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentDashboardResponse>> getStudentDashboard(
            Authentication authentication) {
        String userEmail = authentication.getName();
        StudentDashboardResponse response = dashboardService.getStudentDashboardStats(userEmail);
        return ResponseEntity.ok(
                ApiResponse.success("Student dashboard statistics retrieved successfully", response)
        );
    }
}
