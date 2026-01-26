package com.matchsentinel.reporting.dto;

import java.time.Instant;
import java.util.UUID;

public record NotificationSentEvent(
        UUID notificationId,
        UUID caseId,
        String channel,
        Instant sentAt
) {
}
