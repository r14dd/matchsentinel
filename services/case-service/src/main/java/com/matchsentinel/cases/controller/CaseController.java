package com.matchsentinel.cases.controller;

import com.matchsentinel.cases.domain.CaseStatus;
import com.matchsentinel.cases.dto.AssignCaseRequest;
import com.matchsentinel.cases.dto.CaseResponse;
import com.matchsentinel.cases.dto.CreateCaseRequest;
import com.matchsentinel.cases.dto.UpdateCaseStatusRequest;
import com.matchsentinel.cases.service.CaseService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cases")
public class CaseController {
    private final CaseService caseService;

    public CaseController(CaseService caseService) {
        this.caseService = caseService;
    }

    @PostMapping
    public CaseResponse create(@Valid @RequestBody CreateCaseRequest request) {
        return caseService.create(request);
    }

    @GetMapping("/{id}")
    public CaseResponse get(@PathVariable UUID id) {
        return caseService.get(id);
    }

    @GetMapping
    public Page<CaseResponse> list(
            @RequestParam(required = false) CaseStatus status,
            @RequestParam(required = false) UUID analystId,
            @RequestParam(required = false) UUID transactionId,
            @RequestParam(required = false) UUID accountId,
            @PageableDefault(size = 50) Pageable pageable
    ) {
        return caseService.list(status, analystId, transactionId, accountId, pageable);
    }

    @PatchMapping("/{id}/status")
    public CaseResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateCaseStatusRequest request) {
        return caseService.updateStatus(id, request);
    }

    @PatchMapping("/{id}/assign")
    public CaseResponse assign(@PathVariable UUID id, @Valid @RequestBody AssignCaseRequest request) {
        return caseService.assign(id, request);
    }
}
