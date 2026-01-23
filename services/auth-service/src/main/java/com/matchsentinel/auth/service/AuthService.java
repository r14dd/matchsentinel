package com.matchsentinel.auth.service;

import com.matchsentinel.auth.domain.Role;
import com.matchsentinel.auth.domain.User;
import com.matchsentinel.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public User register(String email, String rawPassword) {

        User user = User.builder()
                .email(email)
                .password(rawPassword)
                .role(Role.ANALYST)
                .createdAt(Instant.now())
                .build();

        return userRepository.save(user);
    }
}
