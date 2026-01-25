package com.matchsentinel.auth.controller;

import com.matchsentinel.auth.dto.AuthMeResponse;
import com.matchsentinel.auth.dto.AuthResponse;
import com.matchsentinel.auth.dto.LoginRequest;
import com.matchsentinel.auth.dto.RefreshRequest;
import com.matchsentinel.auth.dto.RegisterRequest;
import com.matchsentinel.auth.dto.SimpleResponse;
import com.matchsentinel.auth.dto.TokenIntrospectionRequest;
import com.matchsentinel.auth.dto.TokenIntrospectionResponse;
import com.matchsentinel.auth.dto.VerifyEmailRequest;
import com.matchsentinel.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.lang.NonNull;

import java.util.Objects;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody @NonNull RegisterRequest request) {
        AuthResponse response = authService.register(
                Objects.requireNonNull(request.getEmail(), "email"),
                Objects.requireNonNull(request.getPassword(), "password")
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody @NonNull LoginRequest request) {
        AuthResponse response = authService.login(
                Objects.requireNonNull(request.getEmail(), "email"),
                Objects.requireNonNull(request.getPassword(), "password")
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody @NonNull RefreshRequest request) {
        AuthResponse response = authService.refresh(
                Objects.requireNonNull(request.getRefreshToken(), "refreshToken")
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<SimpleResponse> verifyEmail(@Valid @RequestBody @NonNull VerifyEmailRequest request) {
        SimpleResponse response = authService.verifyEmail(
                Objects.requireNonNull(request.getToken(), "token")
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/introspect")
    public ResponseEntity<TokenIntrospectionResponse> introspect(@Valid @RequestBody @NonNull TokenIntrospectionRequest request) {
        TokenIntrospectionResponse response = authService.introspect(
                Objects.requireNonNull(request.getToken(), "token")
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<AuthMeResponse> me(@NonNull Authentication authentication) {
        UUID userId = (UUID) Objects.requireNonNull(authentication.getPrincipal(), "principal");
        AuthMeResponse response = authService.me(userId);
        return ResponseEntity.ok(response);
    }
}
