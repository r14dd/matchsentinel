package com.matchsentinel.ruleengine.messaging;

import com.matchsentinel.ruleengine.dto.TransactionFlaggedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RabbitRuleEngineEventPublisher implements RuleEngineEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public RabbitRuleEngineEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${ruleengine.rabbit.output.exchange}") String exchange,
            @Value("${ruleengine.rabbit.output.routing-key}") String routingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    @Override
    public void publishTransactionFlagged(TransactionFlaggedEvent event) {
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}
