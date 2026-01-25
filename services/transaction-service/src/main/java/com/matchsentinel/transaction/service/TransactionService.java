package com.matchsentinel.transaction.service;

import com.matchsentinel.transaction.domain.Transaction;
import com.matchsentinel.transaction.dto.CreateTransactionRequest;
import com.matchsentinel.transaction.dto.TransactionResponse;
import com.matchsentinel.transaction.exception.NotFoundException;
import com.matchsentinel.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionResponse create(CreateTransactionRequest request) {
        Transaction transaction = Transaction.builder()
                .accountId(request.getAccountId())
                .amount(request.getAmount())
                .currency(request.getCurrency().toUpperCase())
                .country(request.getCountry().toUpperCase())
                .merchant(request.getMerchant())
                .occurredAt(request.getOccurredAt())
                .createdAt(Instant.now())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        return toResponse(saved);
    }

    public TransactionResponse getById(UUID id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found"));
        return toResponse(transaction);
    }

    public Page<TransactionResponse> list(Pageable pageable) {
        return transactionRepository.findAll(pageable)
                .map(this::toResponse);
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAccountId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getCountry(),
                transaction.getMerchant(),
                transaction.getOccurredAt(),
                transaction.getCreatedAt()
        );
    }
}
