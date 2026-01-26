package com.matchsentinel.notification.messaging;

import com.matchsentinel.notification.dto.NotificationSentEvent;

public interface NotificationEventPublisher {
    void publishNotificationSent(NotificationSentEvent event);
}
