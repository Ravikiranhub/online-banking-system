package com.banksphere.dto;

import com.banksphere.model.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateDemoAccountRequest(
        @NotNull Long userId,
        @NotNull AccountType accountType,
        @NotNull @DecimalMin("0.00") BigDecimal openingBalance
) {
}
