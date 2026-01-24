package com.matchsentinel.auth.dto;

import com.matchsentinel.auth.domain.Role;

import java.time.Instant;
import java.util.UUID;

public record AuthMeResponse(
        UUID id,
        String email,
        Role role,
        Instant createdAt,
        Instant lastLoginAt,
        boolean emailVerified
) {
}
