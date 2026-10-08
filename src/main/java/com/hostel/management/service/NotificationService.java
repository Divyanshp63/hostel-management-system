package com.hostel.management.service;

import com.hostel.management.entity.Notification;

import java.util.List;

public interface NotificationService {
    void notifyMaintenance(String title, String message, Long complaintId);
    void notifyWarden(String title, String message, Long complaintId);
    void notifyStudent(Long studentUserId, String title, String message, Long complaintId);
    List<Notification> getNotificationsForUser(Long userId, String role);
}
