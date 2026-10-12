package com.banksphere.config;

import com.banksphere.model.*;
import com.banksphere.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final BankAccountRepository accountRepository;
    private final BankTransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }

        Role customerRole = roleRepository.findByName(RoleName.ROLE_CUSTOMER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_CUSTOMER).build()));
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_ADMIN).build()));

        userRepository.findByEmail("admin@banksphere.test").orElseGet(() -> userRepository.save(User.builder()
                .firstName("BankSphere")
                .lastName("Admin")
                .email("admin@banksphere.test")
                .password(passwordEncoder.encode("Admin@12345"))
                .phone("+1-555-0100")
                .address("Demo operations center")
                .enabled(true)
                .roles(Set.of(adminRole))
                .build()));

        seedCustomer("demo.customer@banksphere.test", "Avery", "Stone", "781000000001", customerRole,
                new BigDecimal("8450.75"));
        seedCustomer("demo.saver@banksphere.test", "Jordan", "Reed", "781000000002", customerRole,
                new BigDecimal("12890.20"));
    }

    private void seedCustomer(String email, String firstName, String lastName, String accountNumber, Role customerRole,
                              BigDecimal openingBalance) {
        User user = userRepository.findByEmail(email).orElseGet(() -> userRepository.save(User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(passwordEncoder.encode("Customer@12345"))
                .phone("+1-555-0199")
                .address("Demo customer address")
                .enabled(true)
                .roles(Set.of(customerRole))
                .build()));

        if (accountRepository.findByAccountNumber(accountNumber).isEmpty()) {
            BankAccount account = accountRepository.save(BankAccount.builder()
                    .accountNumber(accountNumber)
                    .accountType(AccountType.SAVINGS)
                    .status(AccountStatus.ACTIVE)
                    .balance(openingBalance)
                    .user(user)
                    .build());
            transactionRepository.save(BankTransaction.builder()
                    .transactionReference("SEED-" + UUID.randomUUID())
                    .account(account)
                    .type(TransactionType.DEPOSIT)
                    .amount(openingBalance)
                    .balanceAfter(openingBalance)
                    .description("Demo opening balance")
                    .occurredAt(Instant.now().minusSeconds(86_400))
                    .build());
        }
    }
}
