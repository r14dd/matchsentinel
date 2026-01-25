package com.matchsentinel.cases.repository;

import com.matchsentinel.cases.domain.Case;
import com.matchsentinel.cases.domain.CaseStatus;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class CaseSpecifications {
    private CaseSpecifications() {
    }

    public static Specification<Case> hasStatus(CaseStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Case> hasAssignedAnalyst(UUID analystId) {
        return (root, query, cb) -> analystId == null ? null : cb.equal(root.get("assignedAnalystId"), analystId);
    }

    public static Specification<Case> hasTransactionId(UUID transactionId) {
        return (root, query, cb) -> transactionId == null ? null : cb.equal(root.get("transactionId"), transactionId);
    }

    public static Specification<Case> hasAccountId(UUID accountId) {
        return (root, query, cb) -> accountId == null ? null : cb.equal(root.get("accountId"), accountId);
    }
}
