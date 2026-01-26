package com.matchsentinel.reporting.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateDailyStatRequest(
        @NotNull LocalDate statDate,
        long totalTransactions,
        long flaggedTransactions,
        long casesCreated,
        long notificationsSent
) {
}
