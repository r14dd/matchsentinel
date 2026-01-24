error id: file://<WORKSPACE>/src/main/java/com/matchsentinel/auth/service/AuthService.java:com/matchsentinel/auth/dto/AuthResponse#
file://<WORKSPACE>/src/main/java/com/matchsentinel/auth/service/AuthService.java
empty definition using pc, found symbol in pc: com/matchsentinel/auth/dto/AuthResponse#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 75
uri: file://<WORKSPACE>/src/main/java/com/matchsentinel/auth/service/AuthService.java
text:
```scala
package com.matchsentinel.auth.service;

import com.matchsentinel.auth.dto.@@AuthResponse;

import com.matchsentinel.auth.domain.Role;
import com.matchsentinel.auth.domain.User;
import com.matchsentinel.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;

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

        return AuthResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .token("dummy-token") // In real scenario, generate JWT or similar token
                .build();
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: com/matchsentinel/auth/dto/AuthResponse#