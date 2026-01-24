package com.matchsentinel.auth.service;

import com.matchsentinel.auth.dto.AuthMeResponse;
import com.matchsentinel.auth.dto.AuthResponse;
import com.matchsentinel.auth.dto.SimpleResponse;
import com.matchsentinel.auth.dto.TokenIntrospectionResponse;

import com.matchsentinel.auth.domain.Role;
import com.matchsentinel.auth.domain.RefreshToken;
import com.matchsentinel.auth.domain.User;
import com.matchsentinel.auth.exception.AccountDisabledException;
import com.matchsentinel.auth.exception.EmailAlreadyInUseException;
import com.matchsentinel.auth.exception.EmailNotVerifiedException;
import com.matchsentinel.auth.exception.InvalidCredentialsException;
import com.matchsentinel.auth.repository.UserRepository;
import com.matchsentinel.auth.security.JwtService;
import com.matchsentinel.auth.security.LoginAttemptService;
import com.matchsentinel.auth.security.RefreshTokenService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptService loginAttemptService;
    @Value("${auth.email-verification.expiration-minutes}")
    private long emailVerificationMinutes;

    public AuthResponse register(String email, String rawPassword) {
        String normalizedEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new EmailAlreadyInUseException("Email is already in use");
        }
        validatePassword(rawPassword);

        // hashing the password
        String hashedPassword = passwordEncoder.encode(rawPassword);

        String verificationToken = UUID.randomUUID().toString();
        Instant verificationExpiresAt = Instant.now().plusSeconds(emailVerificationMinutes * 60);

        User user = User.builder()
                .email(normalizedEmail)
                .password(hashedPassword)
                .role(Role.ANALYST)
                .createdAt(Instant.now())
                .emailVerified(false)
                .emailVerificationToken(verificationToken)
                .emailVerificationExpiresAt(verificationExpiresAt)
                .disabled(false)
                .build();

        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(savedUser);
        RefreshToken refreshToken = refreshTokenService.createForUser(savedUser);

        return AuthResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .createdAt(savedUser.getCreatedAt())
                .token(token)
                .refreshToken(refreshToken.getToken())
                .verificationToken(verificationToken)
                .message("Registration successful")
                .build();
    }

    public AuthResponse login(String email, String rawPassword) {
        String normalizedEmail = email.trim().toLowerCase();
        loginAttemptService.checkAllowed(normalizedEmail);
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> {
                    loginAttemptService.recordFailure(normalizedEmail);
                    return new InvalidCredentialsException("Invalid email or password");
                });

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            loginAttemptService.recordFailure(normalizedEmail);
            throw new InvalidCredentialsException("Invalid email or password");
        }
        if (user.isDisabled()) {
            throw new AccountDisabledException("Account is disabled");
        }
        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException("Email is not verified");
        }

        String token = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createForUser(user);
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
        loginAttemptService.recordSuccess(normalizedEmail);

        return AuthResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .token(token)
                .refreshToken(refreshToken.getToken())
                .message("Login successful")
                .build();


    }

    public AuthResponse refresh(String refreshToken) {
        RefreshToken rotated = refreshTokenService.rotate(refreshToken);
        User user = rotated.getUser();
        String token = jwtService.generateToken(user);
        return AuthResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .token(token)
                .refreshToken(rotated.getToken())
                .message("Token refreshed")
                .build();
    }

    public SimpleResponse verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid verification token"));

        if (user.getEmailVerificationExpiresAt() != null
                && user.getEmailVerificationExpiresAt().isBefore(Instant.now())) {
            throw new InvalidCredentialsException("Verification token expired");
        }

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationExpiresAt(null);
        userRepository.save(user);
        return new SimpleResponse("Email verified successfully");
    }

    public TokenIntrospectionResponse introspect(String token) {
        if (!jwtService.isTokenValid(token)) {
            return new TokenIntrospectionResponse(false, null, null, null, null);
        }
        return new TokenIntrospectionResponse(
                true,
                jwtService.extractUserId(token),
                jwtService.extractEmail(token),
                jwtService.extractRole(token),
                jwtService.extractExpiration(token)
        );
    }

    public AuthMeResponse me(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));
        return new AuthMeResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt(),
                user.getLastLoginAt(),
                user.isEmailVerified()
        );
    }

    private void validatePassword(String rawPassword) {
        if (!PASSWORD_PATTERN.matcher(rawPassword).matches()) {
            throw new IllegalArgumentException("Password must be at least 8 characters and contain letters and numbers");
        }
    }
}
