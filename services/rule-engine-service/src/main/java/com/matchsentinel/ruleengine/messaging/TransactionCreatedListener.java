package com.matchsentinel.ruleengine.messaging;

import com.matchsentinel.ruleengine.dto.TransactionCreatedEvent;
import com.matchsentinel.ruleengine.service.RuleEngineService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionCreatedListener {

    private final RuleEngineService ruleEngineService;

    public TransactionCreatedListener(RuleEngineService ruleEngineService) {
        this.ruleEngineService = ruleEngineService;
    }

    @RabbitListener(queues = "${ruleengine.rabbit.input.queue}")
    public void onTransactionCreated(TransactionCreatedEvent event) {
        ruleEngineService.evaluate(event);
    }
}
