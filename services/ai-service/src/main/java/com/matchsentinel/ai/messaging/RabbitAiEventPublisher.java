package com.matchsentinel.ai.messaging;

import com.matchsentinel.ai.dto.TransactionScoredEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RabbitAiEventPublisher implements AiEventPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public RabbitAiEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${ai.rabbit.output.exchange}") String exchange,
            @Value("${ai.rabbit.output.routing-key}") String routingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    @Override
    public void publishTransactionScored(TransactionScoredEvent event) {
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}
