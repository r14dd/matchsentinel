package com.matchsentinel.cases.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateCaseRequest(
        @NotNull UUID transactionId,
        @NotNull UUID accountId,
        BigDecimal riskScore,
        List<String> reasons
) {
}
