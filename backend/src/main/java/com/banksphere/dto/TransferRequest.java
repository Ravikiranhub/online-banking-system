package com.banksphere.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TransferRequest(
        @NotNull Long fromAccountId,
        @NotBlank @Size(max = 20) String recipientAccountNumber,
        @NotNull @DecimalMin(value = "0.01", message = "Amount must be at least 0.01") BigDecimal amount,
        @Size(max = 260) String description,
        @NotBlank @Size(max = 120) String idempotencyKey
) {
}
