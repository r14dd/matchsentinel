package com.matchsentinel.reporting.service;

import com.matchsentinel.reporting.domain.ProcessedEvent;
import com.matchsentinel.reporting.repository.DailyStatRepository;
import com.matchsentinel.reporting.repository.ProcessedEventRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportingUpdateService {
    private static final Logger logger = LoggerFactory.getLogger(ReportingUpdateService.class);
    private final DailyStatRepository repository;
    private final ProcessedEventRepository processedEventRepository;

    public ReportingUpdateService(
            DailyStatRepository repository,
            ProcessedEventRepository processedEventRepository
    ) {
        this.repository = repository;
        this.processedEventRepository = processedEventRepository;
    }

    @Transactional
    public void incrementTransactions(Instant occurredAt, String eventKey) {
        runOnce(eventKey, () -> {
            LocalDate date = occurredAt.atZone(ZoneOffset.UTC).toLocalDate();
            repository.incrementTotalTransactions(UUID.randomUUID(), date);
        });
    }

    @Transactional
    public void incrementFlagged(Instant flaggedAt, String eventKey) {
        runOnce(eventKey, () -> {
            LocalDate date = flaggedAt.atZone(ZoneOffset.UTC).toLocalDate();
            repository.incrementFlaggedTransactions(UUID.randomUUID(), date);
        });
    }

    @Transactional
    public void incrementCasesCreated(LocalDate date, String eventKey) {
        runOnce(eventKey, () -> {
            repository.incrementCasesCreated(UUID.randomUUID(), date);
        });
    }

    @Transactional
    public void incrementNotificationsSent(Instant sentAt, String eventKey) {
        runOnce(eventKey, () -> {
            LocalDate date = sentAt.atZone(ZoneOffset.UTC).toLocalDate();
            repository.incrementNotificationsSent(UUID.randomUUID(), date);
        });
    }

    private void runOnce(String eventKey, Runnable update) {
        if (eventKey == null || eventKey.isBlank()) {
            update.run();
            return;
        }
        if (processedEventRepository.existsByEventKey(eventKey)) {
            logger.debug("Skipping duplicate event {}", eventKey);
            return;
        }
        ProcessedEvent processed = new ProcessedEvent();
        processed.setEventKey(eventKey);
        try {
            processedEventRepository.saveAndFlush(processed);
        } catch (DataIntegrityViolationException ex) {
            if (isDuplicateEvent(ex)) {
                logger.debug("Duplicate event detected {}", eventKey);
                return;
            }
            throw ex;
        }
        update.run();
    }

    private boolean isDuplicateEvent(DataIntegrityViolationException ex) {
        Throwable cause = ex.getMostSpecificCause();
        if (cause == null || cause.getMessage() == null) {
            return false;
        }
        return cause.getMessage().contains("uq_processed_events_key");
    }
}
