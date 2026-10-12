package com.banksphere.dto;

import com.banksphere.model.AccountStatus;
import jakarta.validation.constraints.NotNull;

public record AccountStatusUpdateRequest(@NotNull AccountStatus status) {
}
