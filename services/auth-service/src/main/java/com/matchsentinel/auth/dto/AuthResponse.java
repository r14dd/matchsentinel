package com.matchsentinel.auth.dto;

import com.matchsentinel.auth.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private UUID id;
    private String email;
    private Role role;
    private Instant createdAt;
    private String token;
    private String refreshToken;
    private String verificationToken;
    private String message;
    
}
