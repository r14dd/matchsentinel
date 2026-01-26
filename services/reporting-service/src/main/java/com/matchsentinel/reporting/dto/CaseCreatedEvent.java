package com.matchsentinel.reporting.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record CaseCreatedEvent(
        UUID caseId,
        UUID transactionId,
        UUID accountId,
        String status,
        UUID assignedAnalystId,
        BigDecimal riskScore,
        List<String> reasons,
        OffsetDateTime createdAt
) {
}
