package com.banksphere.controller;

import com.banksphere.dto.*;
import com.banksphere.model.TransactionType;
import com.banksphere.service.AccountService;
import com.banksphere.service.AdminService;
import com.banksphere.service.BankingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final AccountService accountService;
    private final BankingService bankingService;

    @GetMapping("/stats")
    public AdminStatsDto stats() {
        return adminService.stats();
    }

    @GetMapping("/customers")
    public Page<UserDto> customers(Pageable pageable) {
        return adminService.customers(sorted(pageable, "createdAt"));
    }

    @GetMapping("/accounts")
    public Page<AccountDto> accounts(Pageable pageable) {
        return adminService.accounts(sorted(pageable, "createdAt"));
    }

    @PostMapping("/accounts")
    public AccountDto createDemoAccount(@Valid @RequestBody CreateDemoAccountRequest request, Principal principal) {
        return accountService.createDemoAccount(request, principal.getName());
    }

    @PatchMapping("/accounts/{accountId}/status")
    public AccountDto updateAccountStatus(@PathVariable Long accountId,
                                          @Valid @RequestBody AccountStatusUpdateRequest request,
                                          Principal principal) {
        return accountService.updateAccountStatus(accountId, request.status(), principal.getName());
    }

    @GetMapping("/transactions")
    public Page<TransactionDto> transactions(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Pageable pageable) {
        return bankingService.allTransactions(query, type, from, to, sorted(pageable, "occurredAt"));
    }

    @GetMapping("/audit-logs")
    public Page<AuditLogDto> auditLogs(Pageable pageable) {
        return adminService.auditLogs(sorted(pageable, "createdAt"));
    }

    private Pageable sorted(Pageable pageable, String defaultField) {
        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : Sort.by(Sort.Direction.DESC, defaultField);
        int size = Math.min(pageable.getPageSize(), 50);
        return PageRequest.of(pageable.getPageNumber(), size, sort);
    }
}
