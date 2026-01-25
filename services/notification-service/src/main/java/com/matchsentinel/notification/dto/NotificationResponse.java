package com.matchsentinel.notification.dto;

import com.matchsentinel.notification.domain.NotificationChannel;
import com.matchsentinel.notification.domain.NotificationStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID caseId,
        String eventType,
        NotificationChannel channel,
        NotificationStatus status,
        String recipient,
        String payload,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
