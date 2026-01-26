package com.matchsentinel.reporting.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "daily_stats")
public class DailyStat {
    @Id
    private UUID id;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "total_transactions", nullable = false)
    private long totalTransactions;

    @Column(name = "flagged_transactions", nullable = false)
    private long flaggedTransactions;

    @Column(name = "cases_created", nullable = false)
    private long casesCreated;

    @Column(name = "notifications_sent", nullable = false)
    private long notificationsSent;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDate getStatDate() {
        return statDate;
    }

    public void setStatDate(LocalDate statDate) {
        this.statDate = statDate;
    }

    public long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public long getFlaggedTransactions() {
        return flaggedTransactions;
    }

    public void setFlaggedTransactions(long flaggedTransactions) {
        this.flaggedTransactions = flaggedTransactions;
    }

    public long getCasesCreated() {
        return casesCreated;
    }

    public void setCasesCreated(long casesCreated) {
        this.casesCreated = casesCreated;
    }

    public long getNotificationsSent() {
        return notificationsSent;
    }

    public void setNotificationsSent(long notificationsSent) {
        this.notificationsSent = notificationsSent;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
