package com.banksphere.service;

import com.banksphere.model.AuditLog;
import com.banksphere.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    public void record(String actorEmail, String action, String entityType, String entityId, String details) {
        auditLogRepository.save(AuditLog.builder()
                .actorEmail(actorEmail == null ? "system" : actorEmail)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .build());
    }
}
