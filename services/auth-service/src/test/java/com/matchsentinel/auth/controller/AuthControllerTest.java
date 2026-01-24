package com.matchsentinel.auth.controller;

import com.matchsentinel.auth.dto.AuthResponse;
import com.matchsentinel.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    private final MockMvc mockMvc;
    private final AuthService authService;

    AuthControllerTest(MockMvc mockMvc, AuthService authService) {
        this.mockMvc = mockMvc;
        this.authService = authService;
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        AuthService authService() {
            return Mockito.mock(AuthService.class);
        }
    }

    @Test
    void register_returnsCreated() throws Exception {
        AuthResponse response = AuthResponse.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .token("token")
                .message("Registration successful")
                .build();

        when(authService.register(anyString(), anyString()))
                .thenReturn(response);

        String body = """
                {
                  "email": "test@example.com",
                  "password": "password"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("token"));
    }

    @Test
    void login_returnsOk() throws Exception {
        AuthResponse response = AuthResponse.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .token("token")
                .message("Login successful")
                .build();

        when(authService.login(anyString(), anyString()))
                .thenReturn(response);

        String body = """
                {
                  "email": "test@example.com",
                  "password": "password"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token"));
    }

    @Test
    void register_returnsBadRequestOnInvalidEmail() throws Exception {
        String body = """
                {
                  "email": "not-an-email",
                  "password": "password"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
