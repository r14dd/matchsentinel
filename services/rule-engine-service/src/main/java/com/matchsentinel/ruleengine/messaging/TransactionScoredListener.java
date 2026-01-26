package com.matchsentinel.ruleengine.messaging;

import com.matchsentinel.ruleengine.dto.TransactionScoredEvent;
import com.matchsentinel.ruleengine.service.RuleEngineService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionScoredListener {
    private final RuleEngineService ruleEngineService;

    public TransactionScoredListener(RuleEngineService ruleEngineService) {
        this.ruleEngineService = ruleEngineService;
    }

    @RabbitListener(queues = "${ruleengine.ai.input.queue}")
    public void onTransactionScored(TransactionScoredEvent event) {
        ruleEngineService.evaluateAi(event);
    }
}
