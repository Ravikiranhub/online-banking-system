package com.banksphere.service;

import com.banksphere.dto.AuthResponse;
import com.banksphere.dto.RegisterRequest;
import com.banksphere.model.*;
import com.banksphere.repository.BankAccountRepository;
import com.banksphere.repository.RoleRepository;
import com.banksphere.repository.UserRepository;
import com.banksphere.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private BankAccountRepository accountRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuditService auditService;

    @Test
    void registerCreatesCustomerWithHashedPasswordAndDemoAccount() {
        DtoMapper mapper = new DtoMapper();
        AuthService service = new AuthService(userRepository, roleRepository, accountRepository, passwordEncoder,
                authenticationManager, jwtService, mapper, auditService);
        Role customerRole = Role.builder().id(1L).name(RoleName.ROLE_CUSTOMER).build();
        RegisterRequest request = new RegisterRequest("Maya", "Chen", "MAYA@EXAMPLE.TEST",
                "Password@123", "+1-555-0200", "Demo lane");

        when(userRepository.existsByEmail("maya@example.test")).thenReturn(false);
        when(roleRepository.findByName(RoleName.ROLE_CUSTOMER)).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode("Password@123")).thenReturn("bcrypt-hash");
        when(accountRepository.findByAccountNumber(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(42L);
            return user;
        });
        when(accountRepository.save(any(BankAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthResponse response = service.register(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.user().email()).isEqualTo("maya@example.test");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("bcrypt-hash");
        assertThat(userCaptor.getValue().getPassword()).isNotEqualTo("Password@123");
        verify(accountRepository).save(argThat(account ->
                account.getAccountType() == AccountType.SAVINGS
                        && account.getStatus() == AccountStatus.ACTIVE
                        && account.getBalance().signum() == 0));
    }
}
