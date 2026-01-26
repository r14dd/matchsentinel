package com.matchsentinel.ruleengine.repository;

import com.matchsentinel.ruleengine.domain.FlaggedTransaction;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FlaggedTransactionRepository extends JpaRepository<FlaggedTransaction, UUID> {
    Optional<FlaggedTransaction> findByTransactionId(UUID transactionId);
}
