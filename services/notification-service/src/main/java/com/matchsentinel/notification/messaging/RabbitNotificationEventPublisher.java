package com.matchsentinel.notification.messaging;

import com.matchsentinel.notification.dto.NotificationSentEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RabbitNotificationEventPublisher implements NotificationEventPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public RabbitNotificationEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${notification.rabbit.output.exchange}") String exchange,
            @Value("${notification.rabbit.output.routing-key}") String routingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    @Override
    public void publishNotificationSent(NotificationSentEvent event) {
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}
