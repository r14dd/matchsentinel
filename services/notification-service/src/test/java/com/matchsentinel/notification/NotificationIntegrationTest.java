package com.matchsentinel.notification;

import com.matchsentinel.notification.domain.Notification;
import com.matchsentinel.notification.dto.CaseCreatedEvent;
import com.matchsentinel.notification.repository.NotificationRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration")
class NotificationIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Container
    static final RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3.13-management");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", () -> rabbitmq.getMappedPort(5672));
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void createsNotificationFromCaseCreatedEvent() throws InterruptedException {
        UUID caseId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        CaseCreatedEvent event = new CaseCreatedEvent(
                caseId,
                UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
                UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
                "OPEN",
                null,
                new BigDecimal("0.80"),
                List.of("AMOUNT_THRESHOLD"),
                OffsetDateTime.now()
        );

        rabbitTemplate.convertAndSend("case.events", "case.created", event);

        Notification created = awaitNotification(caseId);
        assertThat(created).isNotNull();
        assertThat(created.getCaseId()).isEqualTo(caseId);
        assertThat(created.getEventType()).isEqualTo("CASE_CREATED");
    }

    private Notification awaitNotification(UUID caseId) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < deadline) {
            Optional<Notification> found = notificationRepository.findByCaseId(caseId);
            if (found.isPresent()) {
                return found.get();
            }
            Thread.sleep(100);
        }
        return null;
    }
}
