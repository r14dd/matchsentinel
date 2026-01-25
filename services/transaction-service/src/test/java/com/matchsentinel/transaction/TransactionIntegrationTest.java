package com.matchsentinel.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matchsentinel.transaction.messaging.TransactionCreatedEvent;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.util.StreamUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("integration")
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransactionIntegrationTest {

    private static final String EXCHANGE = "transaction.events";
    private static final String ROUTING_KEY = "transaction.created";
    private static final String QUEUE = "transaction.created.it";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Container
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.13-management");

    static {
        postgres.start();
        rabbit.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.liquibase.enabled", () -> true);

        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
        registry.add("transaction.rabbit.exchange", () -> EXCHANGE);
        registry.add("transaction.rabbit.routing-key", () -> ROUTING_KEY);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private DirectExchange transactionExchange;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createTransaction_persistsAndPublishesEvent() throws Exception {
        Queue queue = QueueBuilder.durable(QUEUE).build();
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareExchange(transactionExchange);
        Binding binding = BindingBuilder.bind(queue)
                .to(transactionExchange)
                .with(ROUTING_KEY);
        rabbitAdmin.declareBinding(binding);

        Map<String, Object> request = Map.of(
                "accountId", UUID.randomUUID().toString(),
                "amount", 123.45,
                "currency", "USD",
                "country", "US",
                "merchant", "Integration Merchant",
                "occurredAt", Instant.now().toString()
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/transactions", request, String.class
        );

        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());

        Object rawMessage = rabbitTemplate.receiveAndConvert(QUEUE, 5000);
        assertNotNull(rawMessage);

        TransactionCreatedEvent event = objectMapper.convertValue(rawMessage, TransactionCreatedEvent.class);
        assertEquals("USD", event.currency());
        assertEquals("US", event.country());
        assertEquals("Integration Merchant", event.merchant());

        String schemaJson = StreamUtils.copyToString(
                new ClassPathResource("schema/transaction-created-event.schema.json").getInputStream(),
                StandardCharsets.UTF_8
        );
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        JsonSchema schema = factory.getSchema(schemaJson);
        JsonNode eventNode = objectMapper.readTree(objectMapper.writeValueAsString(event));
        Set<ValidationMessage> errors = schema.validate(eventNode);
        assertEquals(0, errors.size());
    }
}
