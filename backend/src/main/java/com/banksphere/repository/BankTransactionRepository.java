package com.banksphere.repository;

import com.banksphere.model.BankTransaction;
import com.banksphere.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long>, JpaSpecificationExecutor<BankTransaction> {
    List<BankTransaction> findTop5ByAccount_User_EmailOrderByOccurredAtDesc(String email);

    Page<BankTransaction> findByAccount_User_Email(String email, Pageable pageable);

    @Query("select sum(t.amount) from BankTransaction t where t.account.user.email = :email and t.type in :types")
    BigDecimal sumAmountByUserAndTypes(@Param("email") String email, @Param("types") Collection<TransactionType> types);

    @Query("select sum(t.amount) from BankTransaction t")
    BigDecimal totalTransactionVolume();
}
