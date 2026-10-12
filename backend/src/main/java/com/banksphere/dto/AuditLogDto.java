package com.banksphere.dto;

import java.time.Instant;

public record AuditLogDto(
        Long id,
        String actorEmail,
        String action,
        String entityType,
        String entityId,
        String details,
        String ipAddress,
        Instant createdAt
) {
}
