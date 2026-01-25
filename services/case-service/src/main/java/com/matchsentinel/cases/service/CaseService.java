package com.matchsentinel.cases.service;

import com.matchsentinel.cases.domain.Case;
import com.matchsentinel.cases.domain.CaseStatus;
import com.matchsentinel.cases.dto.AssignCaseRequest;
import com.matchsentinel.cases.dto.TransactionFlaggedEvent;
import com.matchsentinel.cases.dto.CaseResponse;
import com.matchsentinel.cases.dto.CreateCaseRequest;
import com.matchsentinel.cases.dto.UpdateCaseStatusRequest;
import com.matchsentinel.cases.repository.CaseRepository;
import com.matchsentinel.cases.repository.CaseSpecifications;
import com.matchsentinel.cases.util.ReasonsCodec;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class CaseService {
    private final CaseRepository repository;

    public CaseService(CaseRepository repository) {
        this.repository = repository;
    }

    public CaseResponse create(CreateCaseRequest request) {
        repository.findByTransactionId(request.transactionId())
                .ifPresent(existing -> {
                    throw new ResponseStatusException(CONFLICT, "Case already exists for transaction");
                });
        Case entity = new Case();
        entity.setTransactionId(request.transactionId());
        entity.setAccountId(request.accountId());
        entity.setRiskScore(request.riskScore());
        entity.setReasons(ReasonsCodec.encode(request.reasons()));
        Case saved = repository.save(entity);
        return toResponse(saved);
    }

    public CaseResponse createFromFlagged(TransactionFlaggedEvent event) {
        return repository.findByTransactionId(event.transactionId())
                .map(this::toResponse)
                .orElseGet(() -> {
                    Case entity = new Case();
                    entity.setTransactionId(event.transactionId());
                    entity.setAccountId(event.accountId());
                    entity.setRiskScore(event.riskScore());
                    entity.setReasons(ReasonsCodec.encode(event.reasons()));
                    Case saved = repository.save(entity);
                    return toResponse(saved);
                });
    }

    public CaseResponse get(UUID id) {
        Case entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Case not found"));
        return toResponse(entity);
    }

    public Page<CaseResponse> list(CaseStatus status, UUID analystId, UUID transactionId, UUID accountId, Pageable pageable) {
        Specification<Case> spec = Specification.allOf(
                CaseSpecifications.hasStatus(status),
                CaseSpecifications.hasAssignedAnalyst(analystId),
                CaseSpecifications.hasTransactionId(transactionId),
                CaseSpecifications.hasAccountId(accountId)
        );
        return repository.findAll(spec, pageable).map(this::toResponse);
    }

    public CaseResponse updateStatus(UUID id, UpdateCaseStatusRequest request) {
        Case entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Case not found"));
        entity.setStatus(request.status());
        return toResponse(repository.save(entity));
    }

    public CaseResponse assign(UUID id, AssignCaseRequest request) {
        Case entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Case not found"));
        entity.setAssignedAnalystId(request.analystId());
        return toResponse(repository.save(entity));
    }

    private CaseResponse toResponse(Case entity) {
        return new CaseResponse(
                entity.getId(),
                entity.getTransactionId(),
                entity.getAccountId(),
                entity.getStatus(),
                entity.getAssignedAnalystId(),
                entity.getRiskScore(),
                ReasonsCodec.decode(entity.getReasons()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
