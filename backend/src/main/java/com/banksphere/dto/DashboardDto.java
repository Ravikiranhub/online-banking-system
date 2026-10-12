package com.banksphere.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardDto(
        BigDecimal totalBalance,
        String primaryAccountNumber,
        BigDecimal income,
        BigDecimal expenses,
        int accountCount,
        List<TransactionDto> recentTransactions
) {
}
