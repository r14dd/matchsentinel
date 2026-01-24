package com.matchsentinel.auth.service;

import com.matchsentinel.auth.dto.AuthResponse;

import com.matchsentinel.auth.domain.Role;
import com.matchsentinel.auth.domain.User;
import com.matchsentinel.auth.exception.EmailAlreadyInUseException;
import com.matchsentinel.auth.exception.InvalidCredentialsException;
import com.matchsentinel.auth.repository.UserRepository;
import com.matchsentinel.auth.security.JwtService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(String email, String rawPassword) {
        String normalizedEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new EmailAlreadyInUseException("Email is already in use");
        }

        // hashing the password
        String hashedPassword = passwordEncoder.encode(rawPassword);

        User user = User.builder()
                .email(normalizedEmail)
                .password(hashedPassword)
                .role(Role.ANALYST)
                .createdAt(Instant.now())
                .build();

        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(savedUser);

        return AuthResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .createdAt(savedUser.getCreatedAt())
                .token(token)
                .message("Registration successful")
                .build();
    }

    public AuthResponse login(String email, String rawPassword) {
        User user = userRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .token(token)
                .message("Login successful")
                .build();


    }
}
