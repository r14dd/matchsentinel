package com.matchsentinel.cases.messaging;

import com.matchsentinel.cases.dto.CaseCreatedEvent;
import com.matchsentinel.cases.dto.CaseResponse;
import com.matchsentinel.cases.dto.TransactionFlaggedEvent;
import com.matchsentinel.cases.service.CaseService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionFlaggedListener {

    private final CaseService caseService;
    private final CaseEventPublisher eventPublisher;

    public TransactionFlaggedListener(CaseService caseService, CaseEventPublisher eventPublisher) {
        this.caseService = caseService;
        this.eventPublisher = eventPublisher;
    }

    @RabbitListener(queues = "${case.rabbit.input.queue}")
    public void onTransactionFlagged(TransactionFlaggedEvent event) {
        CaseResponse created = caseService.createFromFlagged(event);
        eventPublisher.publishCaseCreated(new CaseCreatedEvent(
                created.id(),
                created.transactionId(),
                created.accountId(),
                created.status(),
                created.assignedAnalystId(),
                created.riskScore(),
                created.reasons(),
                created.createdAt()
        ));
    }
}
