package com.banksphere.controller;

import com.banksphere.dto.*;
import com.banksphere.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerController {
    private final AccountService accountService;

    @GetMapping("/dashboard")
    public DashboardDto dashboard(Principal principal) {
        return accountService.dashboard(principal.getName());
    }

    @GetMapping("/profile")
    public UserDto profile(Principal principal) {
        return accountService.profile(principal.getName());
    }

    @PutMapping("/profile")
    public UserDto updateProfile(@Valid @RequestBody ProfileUpdateRequest request, Principal principal) {
        return accountService.updateProfile(principal.getName(), request);
    }

    @GetMapping("/notifications")
    public List<NotificationDto> notifications(Principal principal) {
        return accountService.notifications(principal.getName());
    }
}
