package com.matchsentinel.reporting.repository;

import com.matchsentinel.reporting.domain.DailyStat;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DailyStatRepository extends JpaRepository<DailyStat, UUID> {
    Optional<DailyStat> findByStatDate(LocalDate statDate);
    List<DailyStat> findByStatDateBetween(LocalDate startDate, LocalDate endDate);

    @Modifying
    @Query(value = """
            INSERT INTO daily_stats
            (id, stat_date, total_transactions, flagged_transactions, cases_created, notifications_sent, created_at, updated_at)
            VALUES (:id, :date, 1, 0, 0, 0, now(), now())
            ON CONFLICT (stat_date) DO UPDATE
            SET total_transactions = daily_stats.total_transactions + 1,
                updated_at = now()
            """, nativeQuery = true)
    void incrementTotalTransactions(@Param("id") UUID id, @Param("date") LocalDate date);

    @Modifying
    @Query(value = """
            INSERT INTO daily_stats
            (id, stat_date, total_transactions, flagged_transactions, cases_created, notifications_sent, created_at, updated_at)
            VALUES (:id, :date, 0, 1, 0, 0, now(), now())
            ON CONFLICT (stat_date) DO UPDATE
            SET flagged_transactions = daily_stats.flagged_transactions + 1,
                updated_at = now()
            """, nativeQuery = true)
    void incrementFlaggedTransactions(@Param("id") UUID id, @Param("date") LocalDate date);

    @Modifying
    @Query(value = """
            INSERT INTO daily_stats
            (id, stat_date, total_transactions, flagged_transactions, cases_created, notifications_sent, created_at, updated_at)
            VALUES (:id, :date, 0, 0, 1, 0, now(), now())
            ON CONFLICT (stat_date) DO UPDATE
            SET cases_created = daily_stats.cases_created + 1,
                updated_at = now()
            """, nativeQuery = true)
    void incrementCasesCreated(@Param("id") UUID id, @Param("date") LocalDate date);

    @Modifying
    @Query(value = """
            INSERT INTO daily_stats
            (id, stat_date, total_transactions, flagged_transactions, cases_created, notifications_sent, created_at, updated_at)
            VALUES (:id, :date, 0, 0, 0, 1, now(), now())
            ON CONFLICT (stat_date) DO UPDATE
            SET notifications_sent = daily_stats.notifications_sent + 1,
                updated_at = now()
            """, nativeQuery = true)
    void incrementNotificationsSent(@Param("id") UUID id, @Param("date") LocalDate date);
}
