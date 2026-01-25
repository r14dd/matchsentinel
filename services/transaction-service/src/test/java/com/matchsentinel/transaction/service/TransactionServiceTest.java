package com.matchsentinel.transaction.service;

import com.matchsentinel.transaction.domain.Transaction;
import com.matchsentinel.transaction.dto.CreateTransactionRequest;
import com.matchsentinel.transaction.exception.NotFoundException;
import com.matchsentinel.transaction.messaging.TransactionEventPublisher;
import com.matchsentinel.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionEventPublisher eventPublisher;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void create_savesTransaction() {
        UUID accountId = UUID.randomUUID();
        CreateTransactionRequest request = new CreateTransactionRequest(
                accountId,
                new BigDecimal("123.45"),
                "usd",
                "us",
                "Test Merchant",
                Instant.now()
        );

        Transaction saved = Transaction.builder()
                .id(UUID.randomUUID())
                .accountId(accountId)
                .amount(request.getAmount())
                .currency("USD")
                .country("US")
                .merchant(request.getMerchant())
                .occurredAt(request.getOccurredAt())
                .createdAt(Instant.now())
                .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);

        var response = transactionService.create(request);

        assertNotNull(response.id());
        assertEquals("USD", response.currency());
        assertEquals("US", response.country());
        verify(eventPublisher).publishTransactionCreated(any());
    }

    @Test
    void getById_throwsWhenMissing() {
        when(transactionRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> transactionService.getById(UUID.randomUUID()));
    }
}
