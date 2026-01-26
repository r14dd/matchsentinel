package com.matchsentinel.ai.messaging;

import com.matchsentinel.ai.dto.TransactionCreatedEvent;
import com.matchsentinel.ai.service.AiDecisionService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AiEventListener {
    private final AiDecisionService decisionService;

    public AiEventListener(AiDecisionService decisionService) {
        this.decisionService = decisionService;
    }

    @RabbitListener(queues = "${ai.rabbit.input.queue}")
    public void onTransactionCreated(TransactionCreatedEvent event) {
        decisionService.handleTransactionCreated(event);
    }
}
