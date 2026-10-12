package com.banksphere.dto;

import java.math.BigDecimal;

public record AdminStatsDto(
        long totalCustomers,
        long totalAccounts,
        long activeAccounts,
        long suspendedAccounts,
        long totalTransactions,
        BigDecimal simulatedTransactionVolume
) {
}
