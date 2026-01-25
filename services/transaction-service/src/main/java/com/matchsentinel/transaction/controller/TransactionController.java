package com.matchsentinel.transaction.controller;

import com.matchsentinel.transaction.dto.CreateTransactionRequest;
import com.matchsentinel.transaction.dto.TransactionResponse;
import com.matchsentinel.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody CreateTransactionRequest request) {
        TransactionResponse response = transactionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(transactionService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(transactionService.list(pageable));
    }
}
