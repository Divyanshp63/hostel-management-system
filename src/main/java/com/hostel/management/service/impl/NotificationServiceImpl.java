package com.hostel.management.service.impl;

import com.hostel.management.entity.Notification;
import com.hostel.management.repository.NotificationRepository;
import com.hostel.management.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public void notifyMaintenance(String title, String message, Long complaintId) {
        Notification notification = Notification.builder()
                .recipientRole("COMPLAINT_STAFF")
                .title(title)
                .message(message)
                .complaintId(complaintId)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
        log.info("[NOTIFICATION -> MAINTENANCE] {}: {}", title, message);
    }

    @Override
    @Transactional
    public void notifyWarden(String title, String message, Long complaintId) {
        Notification notification = Notification.builder()
                .recipientRole("WARDEN")
                .title(title)
                .message(message)
                .complaintId(complaintId)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
        log.info("[NOTIFICATION -> WARDEN] {}: {}", title, message);
    }

    @Override
    @Transactional
    public void notifyStudent(Long studentUserId, String title, String message, Long complaintId) {
        Notification notification = Notification.builder()
                .recipientUserId(studentUserId)
                .recipientRole("STUDENT")
                .title(title)
                .message(message)
                .complaintId(complaintId)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
        log.info("[NOTIFICATION -> STUDENT (ID: {})] {}: {}", studentUserId, title, message);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsForUser(Long userId, String role) {
        if (userId != null) {
            return notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(userId);
        } else if (role != null) {
            return notificationRepository.findByRecipientRoleOrderByCreatedAtDesc(role);
        }
        return List.of();
    }
}
