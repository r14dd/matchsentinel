package com.matchsentinel.reporting.service;

import com.matchsentinel.reporting.domain.DailyStat;
import com.matchsentinel.reporting.dto.CreateDailyStatRequest;
import com.matchsentinel.reporting.dto.DailyStatResponse;
import com.matchsentinel.reporting.dto.RollupResponse;
import com.matchsentinel.reporting.repository.DailyStatRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
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

    public RollupResponse weeklyRollup(LocalDate date) {
        LocalDate base = date != null ? date : LocalDate.now();
        LocalDate start = base.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate end = base.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        return rollup(start, end);
    }

    public RollupResponse monthlyRollup(YearMonth month) {
        YearMonth target = month != null ? month : YearMonth.now();
        LocalDate start = target.atDay(1);
        LocalDate end = target.atEndOfMonth();
        return rollup(start, end);
    }

    private RollupResponse rollup(LocalDate start, LocalDate end) {
        List<DailyStat> stats = repository.findByStatDateBetween(start, end);
        long totalTransactions = stats.stream().mapToLong(DailyStat::getTotalTransactions).sum();
        long flaggedTransactions = stats.stream().mapToLong(DailyStat::getFlaggedTransactions).sum();
        long casesCreated = stats.stream().mapToLong(DailyStat::getCasesCreated).sum();
        long notificationsSent = stats.stream().mapToLong(DailyStat::getNotificationsSent).sum();
        return new RollupResponse(start, end, totalTransactions, flaggedTransactions, casesCreated, notificationsSent);
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
