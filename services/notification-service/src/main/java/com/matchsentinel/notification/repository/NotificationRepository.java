package com.matchsentinel.notification.repository;

import com.matchsentinel.notification.domain.Notification;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Optional<Notification> findByCaseId(UUID caseId);
}
