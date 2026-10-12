package com.banksphere.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TransferResponse(
        String transferReference,
        String fromAccountNumber,
        String toAccountNumber,
        BigDecimal amount,
        String status,
        Instant createdAt
) {
}
