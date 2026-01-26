package com.matchsentinel.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matchsentinel.ai.domain.AiDecision;
import com.matchsentinel.ai.dto.AiDecisionResponse;
import com.matchsentinel.ai.dto.ScoreTransactionRequest;
import com.matchsentinel.ai.dto.TransactionCreatedEvent;
import com.matchsentinel.ai.dto.TransactionScoredEvent;
import com.matchsentinel.ai.repository.AiDecisionRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AiIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    @Container
    static final RabbitMQContainer RABBIT = new RabbitMQContainer("rabbitmq:3.13-management");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.rabbitmq.host", RABBIT::getHost);
        registry.add("spring.rabbitmq.port", () -> RABBIT.getAmqpPort());
    }

    private static final String OUTPUT_QUEUE = "ai.transaction.scored.test";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AiDecisionRepository decisionRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${ai.rabbit.input.exchange}")
    private String inputExchange;

    @Value("${ai.rabbit.input.routing-key}")
    private String inputRoutingKey;

    @Value("${ai.rabbit.output.exchange}")
    private String outputExchange;

    @Value("${ai.rabbit.output.routing-key}")
    private String outputRoutingKey;

    @BeforeEach
    void setupQueue() {
        Queue queue = QueueBuilder.nonDurable(OUTPUT_QUEUE).autoDelete().build();
        rabbitAdmin.declareQueue(queue);
        Binding binding = BindingBuilder.bind(queue)
                .to(new DirectExchange(outputExchange))
                .with(outputRoutingKey);
        rabbitAdmin.declareBinding(binding);
    }

    @AfterEach
    void cleanupQueue() {
        rabbitAdmin.deleteQueue(OUTPUT_QUEUE);
    }

    @Test
    void scoreEndpointCreatesDecision() {
        UUID transactionId = UUID.randomUUID();
        ScoreTransactionRequest request = new ScoreTransactionRequest(
                transactionId,
                UUID.randomUUID(),
                new BigDecimal("15000.00"),
                "USD",
                "IR",
                "Test Merchant",
                Instant.parse("2026-01-26T10:15:30Z")
        );

        ResponseEntity<AiDecisionResponse> response =
                restTemplate.postForEntity("/api/ai/score", request, AiDecisionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        AiDecisionResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.transactionId()).isEqualTo(transactionId);
        assertThat(body.riskScore()).isEqualByComparingTo("0.90");
        assertThat(body.reasons()).contains("HIGH_AMOUNT", "HIGH_RISK_COUNTRY");
        assertThat(decisionRepository.findByTransactionId(transactionId)).isPresent();
    }

    @Test
    void publishesScoredEventOnTransactionCreated() throws Exception {
        UUID transactionId = UUID.randomUUID();
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                transactionId,
                UUID.randomUUID(),
                new BigDecimal("12000.00"),
                "USD",
                "IR",
                "Test Merchant",
                Instant.parse("2026-01-26T10:15:30Z"),
                Instant.now()
        );

        rabbitTemplate.convertAndSend(inputExchange, inputRoutingKey, event);

        AiDecision saved = waitForDecision(transactionId);
        assertThat(saved).isNotNull();

        Object message = rabbitTemplate.receiveAndConvert(OUTPUT_QUEUE, 5000);
        assertThat(message).isNotNull();
        TransactionScoredEvent scored = toScoredEvent(message);
        assertThat(scored.transactionId()).isEqualTo(transactionId);
        assertThat(scored.riskScore()).isEqualByComparingTo("0.90");
    }

    private AiDecision waitForDecision(UUID transactionId) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < deadline) {
            Optional<AiDecision> decision = decisionRepository.findByTransactionId(transactionId);
            if (decision.isPresent()) {
                return decision.get();
            }
            Thread.sleep(200);
        }
        return null;
    }

    private TransactionScoredEvent toScoredEvent(Object message) {
        if (message instanceof TransactionScoredEvent event) {
            return event;
        }
        if (message instanceof Map<?, ?> map) {
            return objectMapper.convertValue(map, TransactionScoredEvent.class);
        }
        return objectMapper.convertValue(message, TransactionScoredEvent.class);
    }
}
