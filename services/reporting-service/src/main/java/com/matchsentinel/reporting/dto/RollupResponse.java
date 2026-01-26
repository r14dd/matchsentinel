package com.matchsentinel.reporting.dto;

import java.time.LocalDate;

public record RollupResponse(
        LocalDate startDate,
        LocalDate endDate,
        long totalTransactions,
        long flaggedTransactions,
        long casesCreated,
        long notificationsSent
) {
}
