package com.matchsentinel.transaction.controller;

import com.matchsentinel.transaction.dto.TransactionResponse;
import com.matchsentinel.transaction.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @org.springframework.beans.factory.annotation.Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @Test
    void create_returnsCreated() throws Exception {
        TransactionResponse response = new TransactionResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("123.45"),
                "USD",
                "US",
                "Test Merchant",
                Instant.now(),
                Instant.now()
        );

        when(transactionService.create(any())).thenReturn(response);

        String body = """
                {
                  \"accountId\": \"11111111-1111-1111-1111-111111111111\",
                  \"amount\": 123.45,
                  \"currency\": \"USD\",
                  \"country\": \"US\",
                  \"merchant\": \"Test Merchant\",
                  \"occurredAt\": \"2026-01-25T10:15:30Z\"
                }
                """;

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.currency").value("USD"));
    }
}
