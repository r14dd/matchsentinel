package com.matchsentinel.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionRequest {

    @NotNull(message = "accountId is required")
    private UUID accountId;

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be positive")
    private BigDecimal amount;

    @NotBlank(message = "currency is required")
    private String currency;

    @NotBlank(message = "country is required")
    private String country;

    @NotBlank(message = "merchant is required")
    private String merchant;

    @NotNull(message = "occurredAt is required")
    private Instant occurredAt;
}
