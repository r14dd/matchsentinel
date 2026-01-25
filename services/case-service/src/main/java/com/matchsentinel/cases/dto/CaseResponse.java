package com.matchsentinel.cases.dto;

import com.matchsentinel.cases.domain.CaseStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record CaseResponse(
        UUID id,
        UUID transactionId,
        UUID accountId,
        CaseStatus status,
        UUID assignedAnalystId,
        BigDecimal riskScore,
        List<String> reasons,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
