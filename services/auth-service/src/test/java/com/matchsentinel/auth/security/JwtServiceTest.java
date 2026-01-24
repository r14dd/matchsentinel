package com.matchsentinel.auth.security;

import com.matchsentinel.auth.domain.Role;
import com.matchsentinel.auth.domain.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    @Test
    void generateAndParseToken() {
        String secret = "test-secret-test-secret-test-secret-test-secret";
        JwtService jwtService = new JwtService(secret, 60);

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .role(Role.ANALYST)
                .createdAt(Instant.now())
                .build();

        String token = jwtService.generateToken(user);

        assertTrue(jwtService.isTokenValid(token));
        assertEquals(user.getId(), jwtService.extractUserId(token));
        assertEquals(user.getEmail(), jwtService.extractEmail(token));
        assertEquals(user.getRole().name(), jwtService.extractRole(token));
    }
}
