package com.matchsentinel.auth.dto;

import java.time.Instant;
import java.util.UUID;

public record TokenIntrospectionResponse(
        boolean active,
        UUID userId,
        String email,
        String role,
        Instant expiresAt
) {
}
