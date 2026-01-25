package com.matchsentinel.ruleengine.service;

import com.matchsentinel.ruleengine.domain.FlaggedTransaction;
import com.matchsentinel.ruleengine.dto.TransactionCreatedEvent;
import com.matchsentinel.ruleengine.messaging.RuleEngineEventPublisher;
import com.matchsentinel.ruleengine.repository.FlaggedTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleEngineServiceTest {

    @Mock
    private FlaggedTransactionRepository repository;

    @Mock
    private RuleEngineEventPublisher eventPublisher;

    @InjectMocks
    private RuleEngineService service;

    @Test
    void flagsWhenRulesMatch() {
        ReflectionTestUtils.setField(service, "amountThreshold", new BigDecimal("10000"));
        ReflectionTestUtils.setField(service, "highRiskCountries", "IR,KP,SY");

        TransactionCreatedEvent event = new TransactionCreatedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("15000"),
                "USD",
                "IR",
                "Test Merchant",
                Instant.now(),
                Instant.now()
        );

        ArgumentCaptor<FlaggedTransaction> captor = ArgumentCaptor.forClass(FlaggedTransaction.class);
        when(repository.save(captor.capture())).thenAnswer(invocation -> captor.getValue());

        service.evaluate(event);

        FlaggedTransaction saved = captor.getValue();
        assertTrue(saved.getReasons().contains("AMOUNT_THRESHOLD"));
        assertTrue(saved.getReasons().contains("HIGH_RISK_COUNTRY"));
        verify(eventPublisher).publishTransactionFlagged(org.mockito.ArgumentMatchers.any());
    }
}
