package com.matchsentinel.reporting.controller;

import com.matchsentinel.reporting.dto.CreateDailyStatRequest;
import com.matchsentinel.reporting.dto.DailyStatResponse;
import com.matchsentinel.reporting.dto.RollupResponse;
import com.matchsentinel.reporting.service.ReportingService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports/daily")
public class ReportingController {
    private final ReportingService reportingService;

    public ReportingController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @PostMapping
    public DailyStatResponse create(@Valid @RequestBody CreateDailyStatRequest request) {
        return reportingService.create(request);
    }

    @GetMapping("/{id}")
    public DailyStatResponse get(@PathVariable UUID id) {
        return reportingService.get(id);
    }

    @GetMapping
    public Page<DailyStatResponse> list(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @PageableDefault(size = 50) Pageable pageable
    ) {
        if (date != null) {
            DailyStatResponse stat = reportingService.getByDate(date);
            return new PageImpl<>(List.of(stat), pageable, 1);
        }
        return reportingService.list(pageable);
    }

    @GetMapping("/rollups/weekly")
    public RollupResponse weeklyRollup(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return reportingService.weeklyRollup(date);
    }

    @GetMapping("/rollups/monthly")
    public RollupResponse monthlyRollup(
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM")
            YearMonth month
    ) {
        return reportingService.monthlyRollup(month);
    }
}
