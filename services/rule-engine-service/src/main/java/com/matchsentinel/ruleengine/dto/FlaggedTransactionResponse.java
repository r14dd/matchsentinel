package com.matchsentinel.ruleengine.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FlaggedTransactionResponse(
        UUID id,
        UUID transactionId,
        UUID accountId,
        BigDecimal amount,
        String currency,
        String country,
        String merchant,
        Instant occurredAt,
        Instant createdAt,
        BigDecimal riskScore,
        List<String> reasons
) {
}
