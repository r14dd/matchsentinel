package com.matchsentinel.ruleengine.service;

import com.matchsentinel.ruleengine.domain.FlaggedTransaction;
import com.matchsentinel.ruleengine.dto.TransactionCreatedEvent;
import com.matchsentinel.ruleengine.dto.TransactionFlaggedEvent;
import com.matchsentinel.ruleengine.messaging.RuleEngineEventPublisher;
import com.matchsentinel.ruleengine.repository.FlaggedTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RuleEngineService {

    private final FlaggedTransactionRepository repository;
    private final RuleEngineEventPublisher eventPublisher;

    @Value("${ruleengine.rules.amount-threshold}")
    private BigDecimal amountThreshold;

    @Value("${ruleengine.rules.high-risk-countries}")
    private String highRiskCountries;

    public void evaluate(TransactionCreatedEvent event) {
        List<String> reasons = new ArrayList<>();

        if (event.amount().compareTo(amountThreshold) >= 0) {
            reasons.add("AMOUNT_THRESHOLD");
        }

        Set<String> risky = parseCountries(highRiskCountries);
        if (risky.contains(event.country().toUpperCase(Locale.ROOT))) {
            reasons.add("HIGH_RISK_COUNTRY");
        }

        if (reasons.isEmpty()) {
            return;
        }

        BigDecimal riskScore = computeRiskScore(reasons.size());
        FlaggedTransaction saved = repository.save(FlaggedTransaction.builder()
                .transactionId(event.id())
                .accountId(event.accountId())
                .amount(event.amount())
                .currency(event.currency())
                .country(event.country())
                .merchant(event.merchant())
                .occurredAt(event.occurredAt())
                .createdAt(Instant.now())
                .reasons(String.join(",", reasons))
                .riskScore(riskScore)
                .build());

        eventPublisher.publishTransactionFlagged(new TransactionFlaggedEvent(
                saved.getTransactionId(),
                saved.getAccountId(),
                saved.getAmount(),
                saved.getCurrency(),
                saved.getCountry(),
                saved.getMerchant(),
                saved.getOccurredAt(),
                saved.getCreatedAt(),
                saved.getRiskScore(),
                reasons
        ));
    }

    private Set<String> parseCountries(String value) {
        return List.of(value.split(","))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    private BigDecimal computeRiskScore(int reasonCount) {
        if (reasonCount >= 2) {
            return new BigDecimal("0.90");
        }
        return new BigDecimal("0.70");
    }
}
