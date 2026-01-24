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

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request.getEmail(), request.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        AuthResponse response = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<SimpleResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        SimpleResponse response = authService.verifyEmail(request.getToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/introspect")
    public ResponseEntity<TokenIntrospectionResponse> introspect(@Valid @RequestBody TokenIntrospectionRequest request) {
        TokenIntrospectionResponse response = authService.introspect(request.getToken());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<AuthMeResponse> me(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        AuthMeResponse response = authService.me(userId);
        return ResponseEntity.ok(response);
    }
}
