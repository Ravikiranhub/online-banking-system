package com.banksphere.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @Pattern(regexp = "^[0-9+\\-()\\s]{7,40}$", message = "Phone number format is invalid")
        String phone,
        @Size(max = 260) String address
) {
}
