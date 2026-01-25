package com.matchsentinel.cases.repository;

import com.matchsentinel.cases.domain.Case;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CaseRepository extends JpaRepository<Case, UUID>, JpaSpecificationExecutor<Case> {
}
