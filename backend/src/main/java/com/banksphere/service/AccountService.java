package com.banksphere.service;

import com.banksphere.dto.*;
import com.banksphere.exception.BadRequestException;
import com.banksphere.exception.ResourceNotFoundException;
import com.banksphere.model.*;
import com.banksphere.repository.BankAccountRepository;
import com.banksphere.repository.BankTransactionRepository;
import com.banksphere.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final UserRepository userRepository;
    private final BankAccountRepository accountRepository;
    private final BankTransactionRepository transactionRepository;
    private final DtoMapper mapper;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public DashboardDto dashboard(String email) {
        List<BankAccount> accounts = accountRepository.findByUser_EmailOrderByCreatedAtAsc(email);
        BigDecimal total = accounts.stream()
                .map(BankAccount::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal income = nullToZero(transactionRepository.sumAmountByUserAndTypes(email,
                List.of(TransactionType.DEPOSIT, TransactionType.TRANSFER_IN)));
        BigDecimal expenses = nullToZero(transactionRepository.sumAmountByUserAndTypes(email,
                List.of(TransactionType.WITHDRAWAL, TransactionType.TRANSFER_OUT)));
        List<TransactionDto> recent = transactionRepository.findTop5ByAccount_User_EmailOrderByOccurredAtDesc(email)
                .stream()
                .map(mapper::toTransactionDto)
                .toList();
        String primary = accounts.isEmpty() ? null : accounts.getFirst().getAccountNumber();
        return new DashboardDto(total, primary, income, expenses, accounts.size(), recent);
    }

    @Transactional(readOnly = true)
    public List<AccountDto> customerAccounts(String email) {
        return accountRepository.findByUser_EmailOrderByCreatedAtAsc(email)
                .stream()
                .map(mapper::toAccountDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserDto profile(String email) {
        return mapper.toUserDto(findUser(email));
    }

    @Transactional
    public UserDto updateProfile(String email, ProfileUpdateRequest request) {
        User user = findUser(email);
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setPhone(request.phone());
        user.setAddress(request.address());
        auditService.record(email, "UPDATE_PROFILE", "User", user.getId().toString(), "Profile updated");
        return mapper.toUserDto(user);
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> notifications(String email) {
        List<NotificationDto> accountNotices = accountRepository.findByUser_EmailOrderByCreatedAtAsc(email).stream()
                .filter(account -> account.getStatus() == AccountStatus.SUSPENDED)
                .map(account -> new NotificationDto("account-" + account.getId(), "Account suspended",
                        "Account " + account.getAccountNumber() + " is suspended. Transfers are paused.",
                        "warning", false, Instant.now()))
                .toList();
        if (!accountNotices.isEmpty()) {
            return accountNotices;
        }
        return List.of(new NotificationDto("welcome", "BankSphere is ready",
                "Your demo banking workspace is active. All balances use simulated money only.",
                "info", false, Instant.now()));
    }

    @Transactional
    public AccountDto createDemoAccount(CreateDemoAccountRequest request, String adminEmail) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        if (request.openingBalance().signum() < 0) {
            throw new BadRequestException("Opening balance cannot be negative");
        }
        BankAccount account = accountRepository.save(BankAccount.builder()
                .accountNumber(generateAccountNumber())
                .accountType(request.accountType())
                .status(AccountStatus.ACTIVE)
                .balance(request.openingBalance().setScale(2))
                .user(user)
                .build());
        auditService.record(adminEmail, "CREATE_DEMO_ACCOUNT", "BankAccount", account.getId().toString(),
                "Created demo account for " + user.getEmail());
        return mapper.toAccountDto(account);
    }

    @Transactional
    public AccountDto updateAccountStatus(Long accountId, AccountStatus status, String adminEmail) {
        BankAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        account.setStatus(status);
        auditService.record(adminEmail, "UPDATE_ACCOUNT_STATUS", "BankAccount", account.getId().toString(),
                "Status changed to " + status);
        return mapper.toAccountDto(account);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String generateAccountNumber() {
        String accountNumber;
        do {
            accountNumber = String.format("78%010d", ThreadLocalRandom.current().nextLong(10_000_000_000L));
        } while (accountRepository.findByAccountNumber(accountNumber).isPresent());
        return accountNumber;
    }
}
