package com.banksphere.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record MoneyOperationRequest(
        @NotNull Long accountId,
        @NotNull @DecimalMin(value = "0.01", message = "Amount must be at least 0.01") BigDecimal amount,
        @Size(max = 260) String description
) {
}
