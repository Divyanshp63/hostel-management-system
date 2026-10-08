package com.hostel.management.repository;

import com.hostel.management.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientUserIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findByRecipientRoleOrderByCreatedAtDesc(String role);
}
