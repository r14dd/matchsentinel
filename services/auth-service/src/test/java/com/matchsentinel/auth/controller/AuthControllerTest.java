package com.matchsentinel.auth.controller;

import com.matchsentinel.auth.dto.AuthResponse;
import com.matchsentinel.auth.security.JwtAuthenticationFilter;
import com.matchsentinel.auth.security.JwtService;
import com.matchsentinel.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("null")
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @org.springframework.beans.factory.annotation.Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void register_returnsCreated() throws Exception {
        AuthResponse response = AuthResponse.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .token("token")
                .refreshToken("refresh")
                .verificationToken("verify")
                .message("Registration successful")
                .build();

        when(authService.register(anyString(), anyString()))
                .thenReturn(response);

        String body = """
                {
                  "email": "test@example.com",
                  "password": "password1"
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
                .refreshToken("refresh")
                .message("Login successful")
                .build();

        when(authService.login(anyString(), anyString()))
                .thenReturn(response);

        String body = """
                {
                  "email": "test@example.com",
                  "password": "password1"
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
                  "password": "password1"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_returnsOk() throws Exception {
        AuthResponse response = AuthResponse.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .token("token")
                .refreshToken("refresh")
                .message("Token refreshed")
                .build();

        when(authService.refresh(anyString()))
                .thenReturn(response);

        String body = """
                {
                  "refreshToken": "refresh"
                }
                """;

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token"));
    }

    @Test
    void verifyEmail_returnsOk() throws Exception {
        when(authService.verifyEmail(anyString()))
                .thenReturn(new com.matchsentinel.auth.dto.SimpleResponse("Email verified successfully"));

        String body = """
                {
                  "token": "verify"
                }
                """;

        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email verified successfully"));
    }
}
