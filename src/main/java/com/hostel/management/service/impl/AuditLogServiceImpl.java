package com.hostel.management.service.impl;

import com.hostel.management.entity.AuditLog;
import com.hostel.management.repository.AuditLogRepository;
import com.hostel.management.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public void log(Long userId, String userName, String userRole, String action, String module, String recordId, String remarks) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .userName(userName)
                    .userRole(userRole)
                    .action(action)
                    .module(module)
                    .recordId(recordId)
                    .remarks(remarks)
                    .build();
            auditLogRepository.save(auditLog);
            log.info("Audit logged: [{}] {} by {} ({})", module, action, userName, userRole);
        } catch (Exception ex) {
            log.error("Failed to persist audit log entry: {}", ex.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> getRecentLogs() {
        return auditLogRepository.findTop20ByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> getAllLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
}
