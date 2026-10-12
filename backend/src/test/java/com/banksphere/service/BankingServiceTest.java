package com.banksphere.service;

import com.banksphere.dto.MoneyOperationRequest;
import com.banksphere.dto.TransactionDto;
import com.banksphere.dto.TransferRequest;
import com.banksphere.dto.TransferResponse;
import com.banksphere.exception.InsufficientFundsException;
import com.banksphere.model.*;
import com.banksphere.repository.BankAccountRepository;
import com.banksphere.repository.BankTransactionRepository;
import com.banksphere.repository.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankingServiceTest {
    private static final String EMAIL = "demo.customer@banksphere.test";

    @Mock
    private BankAccountRepository accountRepository;
    @Mock
    private BankTransactionRepository transactionRepository;
    @Mock
    private TransferRepository transferRepository;
    @Mock
    private AuditService auditService;

    private BankingService service;

    @BeforeEach
    void setUp() {
        service = new BankingService(accountRepository, transactionRepository, transferRepository,
                new DtoMapper(), auditService);
    }

    @Test
    void withdrawRejectsOverdrafts() {
        BankAccount account = account(1L, "781000000001", new BigDecimal("20.00"), EMAIL);
        when(accountRepository.lockById(1L)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> service.withdraw(new MoneyOperationRequest(1L, new BigDecimal("50.00"), "ATM"), EMAIL))
                .isInstanceOf(InsufficientFundsException.class);

        assertThat(account.getBalance()).isEqualByComparingTo("20.00");
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void depositUsesServerSideBalanceCalculation() {
        BankAccount account = account(1L, "781000000001", new BigDecimal("100.00"), EMAIL);
        when(accountRepository.lockById(1L)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(BankTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionDto dto = service.deposit(new MoneyOperationRequest(1L, new BigDecimal("25.155"), "Paycheck"), EMAIL);

        assertThat(account.getBalance()).isEqualByComparingTo("125.16");
        assertThat(dto.balanceAfter()).isEqualByComparingTo("125.16");
        assertThat(dto.type()).isEqualTo(TransactionType.DEPOSIT);
    }

    @Test
    void transferDebitsAndCreditsLockedAccountsAtomically() {
        BankAccount from = account(1L, "781000000001", new BigDecimal("200.00"), EMAIL);
        BankAccount to = account(2L, "781000000002", new BigDecimal("50.00"), "friend@banksphere.test");
        when(accountRepository.findByIdAndUser_Email(1L, EMAIL)).thenReturn(Optional.of(from));
        when(accountRepository.findByAccountNumber("781000000002")).thenReturn(Optional.of(to));
        when(accountRepository.lockById(1L)).thenReturn(Optional.of(from));
        when(accountRepository.lockById(2L)).thenReturn(Optional.of(to));
        when(transferRepository.existsByIdempotencyKey("idem-1")).thenReturn(false);
        when(transactionRepository.save(any(BankTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> {
            Transfer transfer = invocation.getArgument(0);
            transfer.setCreatedAt(Instant.now());
            return transfer;
        });

        TransferResponse response = service.transfer(new TransferRequest(1L, "781000000002",
                new BigDecimal("75.00"), "Rent share", "idem-1"), EMAIL);

        assertThat(response.status()).isEqualTo("COMPLETED");
        assertThat(from.getBalance()).isEqualByComparingTo("125.00");
        assertThat(to.getBalance()).isEqualByComparingTo("125.00");
        verify(transactionRepository, times(2)).save(any(BankTransaction.class));
    }

    private BankAccount account(Long id, String accountNumber, BigDecimal balance, String email) {
        User user = User.builder()
                .id(id + 100)
                .firstName("Demo")
                .lastName("User")
                .email(email)
                .password("hash")
                .enabled(true)
                .build();
        return BankAccount.builder()
                .id(id)
                .accountNumber(accountNumber)
                .accountType(AccountType.SAVINGS)
                .status(AccountStatus.ACTIVE)
                .balance(balance)
                .user(user)
                .build();
    }
}
