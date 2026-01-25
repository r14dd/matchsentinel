package com.matchsentinel.notification.service;

import com.matchsentinel.notification.domain.Notification;
import com.matchsentinel.notification.domain.NotificationChannel;
import com.matchsentinel.notification.domain.NotificationStatus;
import com.matchsentinel.notification.dto.CaseCreatedEvent;
import com.matchsentinel.notification.dto.NotificationResponse;
import com.matchsentinel.notification.repository.NotificationRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    public NotificationResponse createFromCaseCreated(CaseCreatedEvent event) {
        Notification notification = new Notification();
        notification.setCaseId(event.caseId());
        notification.setEventType("CASE_CREATED");
        notification.setChannel(NotificationChannel.EMAIL);
        notification.setRecipient("analyst@matchsentinel.local");
        notification.setPayload("Case created: " + event.caseId());
        notification.setStatus(NotificationStatus.SENT);

        Notification saved = repository.save(notification);
        logger.info("Notification sent for caseId={} via channel={}", event.caseId(), notification.getChannel());
        return toResponse(saved);
    }

    public Page<NotificationResponse> list(Pageable pageable) {
        return repository.findAll(pageable).map(this::toResponse);
    }

    public NotificationResponse get(UUID id) {
        Notification notification = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Notification not found"));
        return toResponse(notification);
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getCaseId(),
                notification.getEventType(),
                notification.getChannel(),
                notification.getStatus(),
                notification.getRecipient(),
                notification.getPayload(),
                notification.getCreatedAt(),
                notification.getUpdatedAt()
        );
    }
}
