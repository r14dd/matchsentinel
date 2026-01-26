package com.matchsentinel.reporting;

import com.matchsentinel.reporting.domain.DailyStat;
import com.matchsentinel.reporting.dto.CaseCreatedEvent;
import com.matchsentinel.reporting.dto.NotificationSentEvent;
import com.matchsentinel.reporting.dto.TransactionCreatedEvent;
import com.matchsentinel.reporting.dto.TransactionFlaggedEvent;
import com.matchsentinel.reporting.repository.DailyStatRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
class ReportingIntegrationTest {

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
    private DailyStatRepository repository;

    @Test
    void updatesDailyStatsFromEvents() throws InterruptedException {
        LocalDate date = LocalDate.of(2026, 1, 26);
        Instant base = date.atStartOfDay().toInstant(ZoneOffset.UTC);

        TransactionCreatedEvent createdEvent = new TransactionCreatedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("150.00"),
                "USD",
                "US",
                "Merchant",
                base,
                base
        );
        TransactionFlaggedEvent flaggedEvent = new TransactionFlaggedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("15000.00"),
                "USD",
                "IR",
                "Merchant",
                base,
                base.plusSeconds(10),
                new BigDecimal("0.90"),
                List.of("AMOUNT_THRESHOLD")
        );
        CaseCreatedEvent caseEvent = new CaseCreatedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "OPEN",
                null,
                new BigDecimal("0.90"),
                List.of("AMOUNT_THRESHOLD"),
                OffsetDateTime.ofInstant(base.plusSeconds(20), ZoneOffset.UTC)
        );
        NotificationSentEvent notificationEvent = new NotificationSentEvent(
                UUID.randomUUID(),
                caseEvent.caseId(),
                "EMAIL",
                base.plusSeconds(30)
        );

        rabbitTemplate.convertAndSend("transaction.events", "transaction.created", createdEvent);
        rabbitTemplate.convertAndSend("rule-engine.events", "transaction.flagged", flaggedEvent);
        rabbitTemplate.convertAndSend("case.events", "case.created", caseEvent);
        rabbitTemplate.convertAndSend("notification.events", "notification.sent", notificationEvent);

        DailyStat stat = awaitStat(date);
        assertThat(stat).isNotNull();
        assertThat(stat.getTotalTransactions()).isEqualTo(1);
        assertThat(stat.getFlaggedTransactions()).isEqualTo(1);
        assertThat(stat.getCasesCreated()).isEqualTo(1);
        assertThat(stat.getNotificationsSent()).isEqualTo(1);
    }

    private DailyStat awaitStat(LocalDate date) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < deadline) {
            Optional<DailyStat> found = repository.findByStatDate(date);
            if (found.isPresent()) {
                return found.get();
            }
            Thread.sleep(100);
        }
        return null;
    }
}
