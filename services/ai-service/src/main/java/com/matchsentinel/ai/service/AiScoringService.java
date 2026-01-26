package com.matchsentinel.ai.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class AiScoringService {
    private static final String MODEL_VERSION = "heuristic-v1";
    private static final Set<String> HIGH_RISK_COUNTRIES = new HashSet<>(
            List.of("IR", "KP", "SY", "RU")
    );

    public AiScore score(TransactionInput input) {
        BigDecimal score = BigDecimal.ZERO;
        List<String> reasons = new ArrayList<>();

        if (input.amount().compareTo(new BigDecimal("10000")) >= 0) {
            score = score.add(new BigDecimal("0.50"));
            reasons.add("HIGH_AMOUNT");
        }

        String country = input.country().toUpperCase(Locale.ROOT);
        if (HIGH_RISK_COUNTRIES.contains(country)) {
            score = score.add(new BigDecimal("0.40"));
            reasons.add("HIGH_RISK_COUNTRY");
        }

        String merchant = input.merchant().toLowerCase(Locale.ROOT);
        if (merchant.contains("crypto") || merchant.contains("exchange")) {
            score = score.add(new BigDecimal("0.20"));
            reasons.add("CRYPTO_MERCHANT");
        }

        if (!"USD".equalsIgnoreCase(input.currency())) {
            score = score.add(new BigDecimal("0.10"));
            reasons.add("NON_USD_CURRENCY");
        }

        if (score.compareTo(BigDecimal.ONE) > 0) {
            score = BigDecimal.ONE;
        }

        score = score.setScale(2, RoundingMode.HALF_UP);
        return new AiScore(score, reasons, MODEL_VERSION, Instant.now());
    }

    public record TransactionInput(
            java.util.UUID transactionId,
            java.util.UUID accountId,
            java.math.BigDecimal amount,
            String currency,
            String country,
            String merchant,
            java.time.Instant occurredAt
    ) {
    }

    public record AiScore(
            BigDecimal riskScore,
            List<String> reasons,
            String modelVersion,
            Instant scoredAt
    ) {
    }
}
