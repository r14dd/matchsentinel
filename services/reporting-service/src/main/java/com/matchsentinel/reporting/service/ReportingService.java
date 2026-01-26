package com.matchsentinel.reporting.service;

import com.matchsentinel.reporting.domain.DailyStat;
import com.matchsentinel.reporting.dto.CreateDailyStatRequest;
import com.matchsentinel.reporting.dto.DailyStatResponse;
import com.matchsentinel.reporting.repository.DailyStatRepository;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class ReportingService {
    private final DailyStatRepository repository;

    public ReportingService(DailyStatRepository repository) {
        this.repository = repository;
    }

    public DailyStatResponse create(CreateDailyStatRequest request) {
        repository.findByStatDate(request.statDate())
                .ifPresent(existing -> {
                    throw new ResponseStatusException(CONFLICT, "Stats already exist for date");
                });
        DailyStat stat = new DailyStat();
        stat.setStatDate(request.statDate());
        stat.setTotalTransactions(request.totalTransactions());
        stat.setFlaggedTransactions(request.flaggedTransactions());
        stat.setCasesCreated(request.casesCreated());
        stat.setNotificationsSent(request.notificationsSent());
        return toResponse(repository.save(stat));
    }

    public DailyStatResponse get(UUID id) {
        DailyStat stat = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Stats not found"));
        return toResponse(stat);
    }

    public DailyStatResponse getByDate(LocalDate date) {
        DailyStat stat = repository.findByStatDate(date)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Stats not found"));
        return toResponse(stat);
    }

    public Page<DailyStatResponse> list(Pageable pageable) {
        return repository.findAll(pageable).map(this::toResponse);
    }

    private DailyStatResponse toResponse(DailyStat stat) {
        return new DailyStatResponse(
                stat.getId(),
                stat.getStatDate(),
                stat.getTotalTransactions(),
                stat.getFlaggedTransactions(),
                stat.getCasesCreated(),
                stat.getNotificationsSent(),
                stat.getCreatedAt(),
                stat.getUpdatedAt()
        );
    }
}
