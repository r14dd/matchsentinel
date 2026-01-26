package com.matchsentinel.reporting.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "processed_events")
public class ProcessedEvent {
    @Id
    private UUID id;

    @Column(name = "event_key", nullable = false, length = 128, unique = true)
    private String eventKey;

    @Column(name = "processed_at", nullable = false)
    private OffsetDateTime processedAt;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (processedAt == null) {
            processedAt = OffsetDateTime.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEventKey() {
        return eventKey;
    }

    public void setEventKey(String eventKey) {
        this.eventKey = eventKey;
    }

    public OffsetDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(OffsetDateTime processedAt) {
        this.processedAt = processedAt;
    }
}
