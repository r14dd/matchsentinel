package com.matchsentinel.reporting.service;

import com.matchsentinel.reporting.domain.DailyStat;
import com.matchsentinel.reporting.repository.DailyStatRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportingUpdateService {
    private final DailyStatRepository repository;

    public ReportingUpdateService(DailyStatRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void incrementTransactions(Instant occurredAt) {
        DailyStat stat = getOrCreate(occurredAt.atZone(ZoneOffset.UTC).toLocalDate());
        stat.setTotalTransactions(stat.getTotalTransactions() + 1);
        repository.save(stat);
    }

    @Transactional
    public void incrementFlagged(Instant flaggedAt) {
        DailyStat stat = getOrCreate(flaggedAt.atZone(ZoneOffset.UTC).toLocalDate());
        stat.setFlaggedTransactions(stat.getFlaggedTransactions() + 1);
        repository.save(stat);
    }

    @Transactional
    public void incrementCasesCreated(LocalDate date) {
        DailyStat stat = getOrCreate(date);
        stat.setCasesCreated(stat.getCasesCreated() + 1);
        repository.save(stat);
    }

    @Transactional
    public void incrementNotificationsSent(Instant sentAt) {
        DailyStat stat = getOrCreate(sentAt.atZone(ZoneOffset.UTC).toLocalDate());
        stat.setNotificationsSent(stat.getNotificationsSent() + 1);
        repository.save(stat);
    }

    private DailyStat getOrCreate(LocalDate date) {
        return repository.findByStatDate(date).orElseGet(() -> {
            DailyStat stat = new DailyStat();
            stat.setStatDate(date);
            stat.setTotalTransactions(0);
            stat.setFlaggedTransactions(0);
            stat.setCasesCreated(0);
            stat.setNotificationsSent(0);
            return repository.save(stat);
        });
    }
}
