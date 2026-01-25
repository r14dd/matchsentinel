package com.matchsentinel.notification.messaging;

import com.matchsentinel.notification.dto.CaseCreatedEvent;
import com.matchsentinel.notification.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class CaseCreatedListener {
    private final NotificationService notificationService;

    public CaseCreatedListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "${notification.rabbit.input.queue}")
    public void onCaseCreated(CaseCreatedEvent event) {
        notificationService.createFromCaseCreated(event);
    }
}
