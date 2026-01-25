error id: file://<WORKSPACE>/src/main/java/com/matchsentinel/transaction/service/TransactionService.java:org/springframework/data/jpa/domain/Specification#
file://<WORKSPACE>/src/main/java/com/matchsentinel/transaction/service/TransactionService.java
empty definition using pc, found symbol in pc: org/springframework/data/jpa/domain/Specification#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 768
uri: file://<WORKSPACE>/src/main/java/com/matchsentinel/transaction/service/TransactionService.java
text:
```scala
package com.matchsentinel.transaction.service;

import com.matchsentinel.transaction.domain.Transaction;
import com.matchsentinel.transaction.dto.CreateTransactionRequest;
import com.matchsentinel.transaction.dto.TransactionResponse;
import com.matchsentinel.transaction.exception.NotFoundException;
import com.matchsentinel.transaction.messaging.TransactionCreatedEvent;
import com.matchsentinel.transaction.messaging.TransactionEventPublisher;
import com.matchsentinel.transaction.repository.TransactionRepository;
import com.matchsentinel.transaction.repository.TransactionSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.@@Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionEventPublisher eventPublisher;

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
        eventPublisher.publishTransactionCreated(toEvent(saved));
        return toResponse(saved);
    }

    public TransactionResponse getById(UUID id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found"));
        return toResponse(transaction);
    }

    public Page<TransactionResponse> list(
            UUID accountId,
            String country,
            Instant from,
            Instant to,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Pageable pageable
    ) {
        Specification<com.matchsentinel.transaction.domain.Transaction> spec = Specification.where(null);

        if (accountId != null) {
            spec = spec.and(TransactionSpecifications.hasAccountId(accountId));
        }
        if (country != null && !country.isBlank()) {
            spec = spec.and(TransactionSpecifications.hasCountry(country.toUpperCase()));
        }
        if (from != null) {
            spec = spec.and(TransactionSpecifications.occurredAfter(from));
        }
        if (to != null) {
            spec = spec.and(TransactionSpecifications.occurredBefore(to));
        }
        if (minAmount != null) {
            spec = spec.and(TransactionSpecifications.amountGte(minAmount));
        }
        if (maxAmount != null) {
            spec = spec.and(TransactionSpecifications.amountLte(maxAmount));
        }

        return transactionRepository.findAll(spec, pageable)
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

    private TransactionCreatedEvent toEvent(Transaction transaction) {
        return new TransactionCreatedEvent(
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

```


#### Short summary: 

empty definition using pc, found symbol in pc: org/springframework/data/jpa/domain/Specification#