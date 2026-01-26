package com.matchsentinel.reporting;

import com.matchsentinel.reporting.domain.DailyStat;
import com.matchsentinel.reporting.dto.CaseCreatedEvent;
import com.matchsentinel.reporting.dto.NotificationSentEvent;
import com.matchsentinel.reporting.dto.TransactionCreatedEvent;
import com.matchsentinel.reporting.dto.TransactionFlaggedEvent;
import com.matchsentinel.reporting.repository.DailyStatRepository;
import com.matchsentinel.reporting.repository.ProcessedEventRepository;
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
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
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

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private RabbitListenerEndpointRegistry listenerRegistry;

    @Value("${reporting.rabbit.transaction.exchange}")
    private String transactionExchange;

    @Value("${reporting.rabbit.transaction.queue}")
    private String transactionQueue;

    @Value("${reporting.rabbit.transaction.routing-key}")
    private String transactionRoutingKey;

    @Value("${reporting.rabbit.flagged.exchange}")
    private String flaggedExchange;

    @Value("${reporting.rabbit.flagged.queue}")
    private String flaggedQueue;

    @Value("${reporting.rabbit.flagged.routing-key}")
    private String flaggedRoutingKey;

    @Value("${reporting.rabbit.case.exchange}")
    private String caseExchange;

    @Value("${reporting.rabbit.case.queue}")
    private String caseQueue;

    @Value("${reporting.rabbit.case.routing-key}")
    private String caseRoutingKey;

    @Value("${reporting.rabbit.notification.exchange}")
    private String notificationExchange;

    @Value("${reporting.rabbit.notification.queue}")
    private String notificationQueue;

    @Value("${reporting.rabbit.notification.routing-key}")
    private String notificationRoutingKey;

    @Test
    void updatesDailyStatsFromEvents() throws InterruptedException {
        ensureRabbitTopology();

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

        Thread.sleep(300);

        DailyStat stat = awaitStat(date);
        assertThat(stat).isNotNull();
        assertThat(stat.getTotalTransactions()).isEqualTo(1);
        assertThat(stat.getFlaggedTransactions()).isEqualTo(1);
        assertThat(stat.getCasesCreated()).isEqualTo(1);
        assertThat(stat.getNotificationsSent()).isEqualTo(1);

        rabbitTemplate.convertAndSend("transaction.events", "transaction.created", createdEvent);
        rabbitTemplate.convertAndSend("rule-engine.events", "transaction.flagged", flaggedEvent);
        rabbitTemplate.convertAndSend("case.events", "case.created", caseEvent);
        rabbitTemplate.convertAndSend("notification.events", "notification.sent", notificationEvent);

        DailyStat statAfterDuplicate = awaitStat(date);
        assertThat(statAfterDuplicate.getTotalTransactions()).isEqualTo(1);
        assertThat(statAfterDuplicate.getFlaggedTransactions()).isEqualTo(1);
        assertThat(statAfterDuplicate.getCasesCreated()).isEqualTo(1);
        assertThat(statAfterDuplicate.getNotificationsSent()).isEqualTo(1);
    }

    private void ensureRabbitTopology() {
        listenerRegistry.getListenerContainers().forEach(container -> {
            if (!container.isRunning()) {
                container.start();
            }
        });

        DirectExchange txExchange = new DirectExchange(transactionExchange);
        Queue txQueue = new Queue(transactionQueue, true);
        amqpAdmin.declareExchange(txExchange);
        amqpAdmin.declareQueue(txQueue);
        amqpAdmin.declareBinding(BindingBuilder.bind(txQueue).to(txExchange).with(transactionRoutingKey));

        DirectExchange flagExchange = new DirectExchange(flaggedExchange);
        Queue flagQueue = new Queue(flaggedQueue, true);
        amqpAdmin.declareExchange(flagExchange);
        amqpAdmin.declareQueue(flagQueue);
        amqpAdmin.declareBinding(BindingBuilder.bind(flagQueue).to(flagExchange).with(flaggedRoutingKey));

        DirectExchange caseEx = new DirectExchange(caseExchange);
        Queue caseQ = new Queue(caseQueue, true);
        amqpAdmin.declareExchange(caseEx);
        amqpAdmin.declareQueue(caseQ);
        amqpAdmin.declareBinding(BindingBuilder.bind(caseQ).to(caseEx).with(caseRoutingKey));

        DirectExchange notificationEx = new DirectExchange(notificationExchange);
        Queue notificationQ = new Queue(notificationQueue, true);
        amqpAdmin.declareExchange(notificationEx);
        amqpAdmin.declareQueue(notificationQ);
        amqpAdmin.declareBinding(BindingBuilder.bind(notificationQ).to(notificationEx).with(notificationRoutingKey));
    }

    private int getQueueCount(String queue) {
        var props = amqpAdmin.getQueueProperties(queue);
        if (props == null) {
            return -1;
        }
        Object count = props.get("QUEUE_MESSAGE_COUNT");
        return count instanceof Integer ? (Integer) count : -1;
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
