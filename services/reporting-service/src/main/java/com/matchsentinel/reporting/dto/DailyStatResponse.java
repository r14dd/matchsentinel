package com.matchsentinel.reporting.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record DailyStatResponse(
        UUID id,
        LocalDate statDate,
        long totalTransactions,
        long flaggedTransactions,
        long casesCreated,
        long notificationsSent,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
