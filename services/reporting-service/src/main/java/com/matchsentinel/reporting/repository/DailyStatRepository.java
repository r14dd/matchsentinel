package com.matchsentinel.reporting.repository;

import com.matchsentinel.reporting.domain.DailyStat;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyStatRepository extends JpaRepository<DailyStat, UUID> {
    Optional<DailyStat> findByStatDate(LocalDate statDate);
}
