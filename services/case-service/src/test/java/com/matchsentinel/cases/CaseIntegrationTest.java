package com.matchsentinel.cases;

import com.matchsentinel.cases.domain.Case;
import com.matchsentinel.cases.dto.TransactionFlaggedEvent;
import com.matchsentinel.cases.repository.CaseRepository;
import java.math.BigDecimal;
import java.time.Instant;
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
class CaseIntegrationTest {

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
    private CaseRepository caseRepository;

    @Test
    void createsCaseFromFlaggedEvent() throws InterruptedException {
        UUID transactionId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID accountId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        TransactionFlaggedEvent event = new TransactionFlaggedEvent(
                transactionId,
                accountId,
                new BigDecimal("15000.00"),
                "USD",
                "IR",
                "Test Merchant",
                Instant.parse("2026-01-25T10:15:30Z"),
                Instant.now(),
                new BigDecimal("0.90"),
                List.of("AMOUNT_THRESHOLD", "HIGH_RISK_COUNTRY")
        );

        rabbitTemplate.convertAndSend("rule-engine.events", "transaction.flagged", event);

        Case created = awaitCase(transactionId);
        assertThat(created).isNotNull();
        assertThat(created.getTransactionId()).isEqualTo(transactionId);
        assertThat(created.getAccountId()).isEqualTo(accountId);
    }

    private Case awaitCase(UUID transactionId) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < deadline) {
            Optional<Case> found = caseRepository.findByTransactionId(transactionId);
            if (found.isPresent()) {
                return found.get();
            }
            Thread.sleep(100);
        }
        return null;
    }
}
