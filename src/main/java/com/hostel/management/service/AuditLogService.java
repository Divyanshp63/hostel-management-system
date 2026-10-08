package com.hostel.management.service;

import com.hostel.management.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AuditLogService {

    void log(Long userId, String userName, String userRole, String action, String module, String recordId, String remarks);

    List<AuditLog> getRecentLogs();

    Page<AuditLog> getAllLogs(Pageable pageable);
}
