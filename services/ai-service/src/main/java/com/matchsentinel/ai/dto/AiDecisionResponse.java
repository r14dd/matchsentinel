package com.matchsentinel.ai.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AiDecisionResponse(
        UUID id,
        UUID transactionId,
        UUID accountId,
        BigDecimal amount,
        String currency,
        String country,
        String merchant,
        Instant occurredAt,
        BigDecimal riskScore,
        List<String> reasons,
        String modelVersion,
        Instant createdAt
) {
}
