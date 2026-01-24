package com.matchsentinel.auth.service;

import com.matchsentinel.auth.domain.Role;
import com.matchsentinel.auth.domain.User;
import com.matchsentinel.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;import org.springframework.stereotype.Service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(String email, String rawPassword) {

        // need to check if email already exists

        if (userRepository.findByEmail(email).isPresent())
{
            throw new IllegalArgumentException("Email is already in use");
}

        // hashing the password
        String hashedPassword = passwordEncoder.encode(rawPassword);

        User user = User.builder()
                .email(email)
                .password(hashedPassword)
                .role(Role.ANALYST)
                .createdAt(Instant.now())
                .build();

        return userRepository.save(user);
    }
}
