package com.matchsentinel.reporting.messaging;

import com.matchsentinel.reporting.dto.CaseCreatedEvent;
import com.matchsentinel.reporting.dto.NotificationSentEvent;
import com.matchsentinel.reporting.dto.TransactionCreatedEvent;
import com.matchsentinel.reporting.dto.TransactionFlaggedEvent;
import com.matchsentinel.reporting.service.ReportingUpdateService;
import java.time.LocalDate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ReportingEventListener {
    private final ReportingUpdateService updateService;

    public ReportingEventListener(ReportingUpdateService updateService) {
        this.updateService = updateService;
    }

    @RabbitListener(queues = "${reporting.rabbit.transaction.queue}")
    public void onTransactionCreated(TransactionCreatedEvent event) {
        updateService.incrementTransactions(event.occurredAt());
    }

    @RabbitListener(queues = "${reporting.rabbit.flagged.queue}")
    public void onTransactionFlagged(TransactionFlaggedEvent event) {
        updateService.incrementFlagged(event.flaggedAt());
    }

    @RabbitListener(queues = "${reporting.rabbit.case.queue}")
    public void onCaseCreated(CaseCreatedEvent event) {
        LocalDate date = event.createdAt().toLocalDate();
        updateService.incrementCasesCreated(date);
    }

    @RabbitListener(queues = "${reporting.rabbit.notification.queue}")
    public void onNotificationSent(NotificationSentEvent event) {
        updateService.incrementNotificationsSent(event.sentAt());
    }
}
