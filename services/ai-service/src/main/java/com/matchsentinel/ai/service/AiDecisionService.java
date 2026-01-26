package com.matchsentinel.ai.service;

import com.matchsentinel.ai.domain.AiDecision;
import com.matchsentinel.ai.dto.AiDecisionResponse;
import com.matchsentinel.ai.dto.ScoreTransactionRequest;
import com.matchsentinel.ai.dto.TransactionCreatedEvent;
import com.matchsentinel.ai.dto.TransactionScoredEvent;
import com.matchsentinel.ai.messaging.AiEventPublisher;
import com.matchsentinel.ai.repository.AiDecisionRepository;
import com.matchsentinel.ai.service.AiScoringService.AiScore;
import com.matchsentinel.ai.service.AiScoringService.TransactionInput;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiDecisionService {
    private final AiDecisionRepository repository;
    private final AiScoringService scoringService;
    private final AiEventPublisher eventPublisher;

    public AiDecisionService(
            AiDecisionRepository repository,
            AiScoringService scoringService,
            AiEventPublisher eventPublisher
    ) {
        this.repository = repository;
        this.scoringService = scoringService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public AiDecisionResponse handleTransactionCreated(TransactionCreatedEvent event) {
        Optional<AiDecision> existing = repository.findByTransactionId(event.id());
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }

        TransactionInput input = new TransactionInput(
                event.id(),
                event.accountId(),
                event.amount(),
                event.currency(),
                event.country(),
                event.merchant(),
                event.occurredAt()
        );
        return createAndPublish(input);
    }

    @Transactional
    public AiDecisionResponse score(ScoreTransactionRequest request) {
        Optional<AiDecision> existing = repository.findByTransactionId(request.transactionId());
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }

        TransactionInput input = new TransactionInput(
                request.transactionId(),
                request.accountId(),
                request.amount(),
                request.currency(),
                request.country(),
                request.merchant(),
                request.occurredAt()
        );
        return createAndPublish(input);
    }

    @Transactional(readOnly = true)
    public Optional<AiDecisionResponse> findById(UUID id) {
        return repository.findById(id).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Optional<AiDecisionResponse> findByTransactionId(UUID transactionId) {
        return repository.findByTransactionId(transactionId).map(this::toResponse);
    }

    private AiDecisionResponse createAndPublish(TransactionInput input) {
        AiScore score = scoringService.score(input);
        AiDecision saved;
        try {
            saved = repository.save(AiDecision.builder()
                    .transactionId(input.transactionId())
                    .accountId(input.accountId())
                    .amount(input.amount())
                    .currency(input.currency())
                    .country(input.country())
                    .merchant(input.merchant())
                    .occurredAt(input.occurredAt())
                    .riskScore(score.riskScore())
                    .reasons(String.join(",", score.reasons()))
                    .modelVersion(score.modelVersion())
                    .createdAt(Instant.now())
                    .build());
        } catch (DataIntegrityViolationException ex) {
            return repository.findByTransactionId(input.transactionId())
                    .map(this::toResponse)
                    .orElseThrow(() -> ex);
        }

        eventPublisher.publishTransactionScored(new TransactionScoredEvent(
                saved.getTransactionId(),
                saved.getAccountId(),
                saved.getAmount(),
                saved.getCurrency(),
                saved.getCountry(),
                saved.getMerchant(),
                saved.getOccurredAt(),
                score.scoredAt(),
                saved.getRiskScore(),
                splitReasons(saved.getReasons()),
                saved.getModelVersion()
        ));

        return toResponse(saved);
    }

    private AiDecisionResponse toResponse(AiDecision decision) {
        return new AiDecisionResponse(
                decision.getId(),
                decision.getTransactionId(),
                decision.getAccountId(),
                decision.getAmount(),
                decision.getCurrency(),
                decision.getCountry(),
                decision.getMerchant(),
                decision.getOccurredAt(),
                decision.getRiskScore(),
                splitReasons(decision.getReasons()),
                decision.getModelVersion(),
                decision.getCreatedAt()
        );
    }

    private List<String> splitReasons(String reasons) {
        if (reasons == null || reasons.isBlank()) {
            return List.of();
        }
        return Arrays.stream(reasons.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }
}
