package com.matchsentinel.ai.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionCreatedEvent(
        UUID id,
        UUID accountId,
        BigDecimal amount,
        String currency,
        String country,
        String merchant,
        Instant occurredAt,
        Instant createdAt
) {
}
