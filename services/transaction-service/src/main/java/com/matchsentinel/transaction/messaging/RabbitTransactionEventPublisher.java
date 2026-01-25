package com.matchsentinel.transaction.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RabbitTransactionEventPublisher implements TransactionEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public RabbitTransactionEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${transaction.rabbit.exchange}") String exchange,
            @Value("${transaction.rabbit.routing-key}") String routingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    @Override
    public void publishTransactionCreated(TransactionCreatedEvent event) {
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}
