package com.matchsentinel.ai.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TransactionScoredEvent(
        UUID transactionId,
        UUID accountId,
        BigDecimal amount,
        String currency,
        String country,
        String merchant,
        Instant occurredAt,
        Instant scoredAt,
        BigDecimal riskScore,
        List<String> reasons,
        String modelVersion
) {
}
