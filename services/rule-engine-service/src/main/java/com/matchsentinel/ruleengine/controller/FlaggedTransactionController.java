package com.matchsentinel.ruleengine.controller;

import com.matchsentinel.ruleengine.domain.FlaggedTransaction;
import com.matchsentinel.ruleengine.dto.FlaggedTransactionResponse;
import com.matchsentinel.ruleengine.repository.FlaggedTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/flags")
@RequiredArgsConstructor
public class FlaggedTransactionController {

    private final FlaggedTransactionRepository repository;

    @GetMapping
    public ResponseEntity<Page<FlaggedTransactionResponse>> list(
            @PageableDefault(size = 50, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(repository.findAll(pageable).map(this::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlaggedTransactionResponse> getById(@PathVariable UUID id) {
        return repository.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private FlaggedTransactionResponse toResponse(FlaggedTransaction entity) {
        List<String> reasons = entity.getReasons() == null || entity.getReasons().isBlank()
                ? List.of()
                : Arrays.asList(entity.getReasons().split(","));
        return new FlaggedTransactionResponse(
                entity.getId(),
                entity.getTransactionId(),
                entity.getAccountId(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getCountry(),
                entity.getMerchant(),
                entity.getOccurredAt(),
                entity.getCreatedAt(),
                entity.getRiskScore(),
                reasons
        );
    }
}
