package com.matchsentinel.reporting.repository;

import com.matchsentinel.reporting.domain.ProcessedEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {
    boolean existsByEventKey(String eventKey);
}
