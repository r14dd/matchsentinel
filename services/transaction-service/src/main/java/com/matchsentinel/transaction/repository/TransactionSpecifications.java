package com.matchsentinel.transaction.repository;

import com.matchsentinel.transaction.domain.Transaction;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class TransactionSpecifications {

    private TransactionSpecifications() {
    }

    public static Specification<Transaction> hasAccountId(UUID accountId) {
        return (root, query, cb) -> cb.equal(root.get("accountId"), accountId);
    }

    public static Specification<Transaction> hasCountry(String country) {
        return (root, query, cb) -> cb.equal(root.get("country"), country);
    }

    public static Specification<Transaction> occurredAfter(Instant from) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("occurredAt"), from);
    }

    public static Specification<Transaction> occurredBefore(Instant to) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("occurredAt"), to);
    }

    public static Specification<Transaction> amountGte(BigDecimal minAmount) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("amount"), minAmount);
    }

    public static Specification<Transaction> amountLte(BigDecimal maxAmount) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("amount"), maxAmount);
    }
}
