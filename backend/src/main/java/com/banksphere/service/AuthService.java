package com.banksphere.service;

import com.banksphere.dto.AuthResponse;
import com.banksphere.dto.LoginRequest;
import com.banksphere.dto.RegisterRequest;
import com.banksphere.dto.UserDto;
import com.banksphere.exception.BadRequestException;
import com.banksphere.exception.ResourceNotFoundException;
import com.banksphere.model.*;
import com.banksphere.repository.BankAccountRepository;
import com.banksphere.repository.RoleRepository;
import com.banksphere.repository.UserRepository;
import com.banksphere.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BankAccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final DtoMapper mapper;
    private final AuditService auditService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().toLowerCase().trim();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email is already registered");
        }
        Role role = roleRepository.findByName(RoleName.ROLE_CUSTOMER)
                .orElseThrow(() -> new ResourceNotFoundException("Customer role is not configured"));
        User user = userRepository.save(User.builder()
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .address(request.address())
                .enabled(true)
                .roles(Set.of(role))
                .build());
        accountRepository.save(BankAccount.builder()
                .accountNumber(generateAccountNumber())
                .accountType(AccountType.SAVINGS)
                .status(AccountStatus.ACTIVE)
                .balance(BigDecimal.ZERO.setScale(2))
                .user(user)
                .build());
        auditService.record(email, "REGISTER", "User", user.getId().toString(), "New demo customer registered");
        return new AuthResponse(jwtService.generateToken(user), mapper.toUserDto(user));
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().toLowerCase().trim();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        auditService.record(email, "LOGIN", "User", user.getId().toString(), "User logged in");
        return new AuthResponse(jwtService.generateToken(user), mapper.toUserDto(user));
    }

    public UserDto currentUser(String email) {
        return mapper.toUserDto(userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found")));
    }

    private String generateAccountNumber() {
        String accountNumber;
        do {
            accountNumber = String.format("78%010d", ThreadLocalRandom.current().nextLong(10_000_000_000L));
        } while (accountRepository.findByAccountNumber(accountNumber).isPresent());
        return accountNumber;
    }
}
