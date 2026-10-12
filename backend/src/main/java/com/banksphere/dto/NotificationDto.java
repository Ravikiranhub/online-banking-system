package com.banksphere.dto;

import java.time.Instant;

public record NotificationDto(
        String id,
        String title,
        String message,
        String level,
        boolean read,
        Instant createdAt
) {
}
