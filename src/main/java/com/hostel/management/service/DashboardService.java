package com.hostel.management.service;

import com.hostel.management.dto.response.AdminDashboardResponse;
import com.hostel.management.dto.response.StudentDashboardResponse;

public interface DashboardService {

    AdminDashboardResponse getAdminDashboardStats();

    StudentDashboardResponse getStudentDashboardStats(String userEmail);
}
