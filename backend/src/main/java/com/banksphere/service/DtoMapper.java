package com.banksphere.service;

import com.banksphere.dto.*;
import com.banksphere.model.*;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DtoMapper {
    public UserDto toUserDto(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
        return new UserDto(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getPhone(),
                user.getAddress(), user.isEnabled(), roles);
    }

    public AccountDto toAccountDto(BankAccount account) {
        return new AccountDto(account.getId(), account.getAccountNumber(), account.getAccountType(),
                account.getStatus(), account.getBalance(), account.getCreatedAt());
    }

    public TransactionDto toTransactionDto(BankTransaction transaction) {
        return new TransactionDto(transaction.getId(), transaction.getTransactionReference(),
                transaction.getAccount().getAccountNumber(), transaction.getType(), transaction.getAmount(),
                transaction.getBalanceAfter(), transaction.getDescription(), transaction.getOccurredAt());
    }

    public AuditLogDto toAuditLogDto(AuditLog log) {
        return new AuditLogDto(log.getId(), log.getActorEmail(), log.getAction(), log.getEntityType(),
                log.getEntityId(), log.getDetails(), log.getIpAddress(), log.getCreatedAt());
    }
}
