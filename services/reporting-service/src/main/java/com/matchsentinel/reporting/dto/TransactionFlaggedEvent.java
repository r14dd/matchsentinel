package com.matchsentinel.reporting.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TransactionFlaggedEvent(
        UUID transactionId,
        UUID accountId,
        BigDecimal amount,
        String currency,
        String country,
        String merchant,
        Instant occurredAt,
        Instant flaggedAt,
        BigDecimal riskScore,
        List<String> reasons
) {
}
