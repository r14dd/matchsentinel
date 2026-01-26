package com.matchsentinel.ai.repository;

import com.matchsentinel.ai.domain.AiDecision;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiDecisionRepository extends JpaRepository<AiDecision, UUID> {
    Optional<AiDecision> findByTransactionId(UUID transactionId);
}
