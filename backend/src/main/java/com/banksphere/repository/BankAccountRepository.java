package com.banksphere.repository;

import com.banksphere.model.AccountStatus;
import com.banksphere.model.BankAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByAccountNumber(String accountNumber);

    List<BankAccount> findByUser_EmailOrderByCreatedAtAsc(String email);

    Optional<BankAccount> findByIdAndUser_Email(Long id, String email);

    long countByStatus(AccountStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from BankAccount a where a.id = :id")
    Optional<BankAccount> lockById(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from BankAccount a where a.accountNumber = :accountNumber")
    Optional<BankAccount> lockByAccountNumber(@Param("accountNumber") String accountNumber);
}
