package com.matchsentinel.cases.messaging;

import com.matchsentinel.cases.dto.CaseCreatedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RabbitCaseEventPublisher implements CaseEventPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public RabbitCaseEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${case.rabbit.output.exchange}") String exchange,
            @Value("${case.rabbit.output.routing-key}") String routingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    @Override
    public void publishCaseCreated(CaseCreatedEvent event) {
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}
