package com.banksphere.dto;

import com.banksphere.model.AccountStatus;
import com.banksphere.model.AccountType;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountDto(
        Long id,
        String accountNumber,
        AccountType accountType,
        AccountStatus status,
        BigDecimal balance,
        Instant createdAt
) {
}
