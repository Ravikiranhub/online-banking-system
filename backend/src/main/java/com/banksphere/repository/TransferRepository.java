package com.banksphere.repository;

import com.banksphere.model.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
    boolean existsByIdempotencyKey(String idempotencyKey);

    Optional<Transfer> findByIdempotencyKey(String idempotencyKey);
}
