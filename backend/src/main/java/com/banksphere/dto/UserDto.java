package com.banksphere.dto;

import java.util.Set;

public record UserDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String address,
        boolean enabled,
        Set<String> roles
) {
}
