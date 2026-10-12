package com.banksphere.service;

import com.banksphere.dto.*;
import com.banksphere.model.AccountStatus;
import com.banksphere.model.RoleName;
import com.banksphere.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final BankAccountRepository accountRepository;
    private final BankTransactionRepository transactionRepository;
    private final AuditLogRepository auditLogRepository;
    private final DtoMapper mapper;

    @Transactional(readOnly = true)
    public AdminStatsDto stats() {
        BigDecimal volume = transactionRepository.totalTransactionVolume();
        return new AdminStatsDto(
                userRepository.countByRoleName(RoleName.ROLE_CUSTOMER),
                accountRepository.count(),
                accountRepository.countByStatus(AccountStatus.ACTIVE),
                accountRepository.countByStatus(AccountStatus.SUSPENDED),
                transactionRepository.count(),
                volume == null ? BigDecimal.ZERO : volume);
    }

    @Transactional(readOnly = true)
    public Page<UserDto> customers(Pageable pageable) {
        return userRepository.findByRoleName(RoleName.ROLE_CUSTOMER, pageable).map(mapper::toUserDto);
    }

    @Transactional(readOnly = true)
    public Page<AccountDto> accounts(Pageable pageable) {
        return accountRepository.findAll(pageable).map(mapper::toAccountDto);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogDto> auditLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable).map(mapper::toAuditLogDto);
    }
}
