package com.matchsentinel.ai.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ScoreTransactionRequest(
        @NotNull UUID transactionId,
        @NotNull UUID accountId,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotBlank @Size(min = 3, max = 3) String currency,
        @NotBlank @Size(min = 2, max = 2) String country,
        @NotBlank String merchant,
        @NotNull Instant occurredAt
) {
}
