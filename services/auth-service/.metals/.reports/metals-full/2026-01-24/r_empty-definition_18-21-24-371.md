error id: file://<WORKSPACE>/src/main/java/com/matchsentinel/auth/controller/AuthController.java:com/matchsentinel/auth/domain/User#
file://<WORKSPACE>/src/main/java/com/matchsentinel/auth/controller/AuthController.java
empty definition using pc, found symbol in pc: com/matchsentinel/auth/domain/User#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 281
uri: file://<WORKSPACE>/src/main/java/com/matchsentinel/auth/controller/AuthController.java
text:
```scala
package com.matchsentinel.auth.controller;

import com.matchsentinel.auth.dto.LoginRequest;
import com.matchsentinel.auth.dto.LoginResponse;
import com.matchsentinel.auth.dto.RegisterRequest;
import com.matchsentinel.auth.dto.RegisterResponse;
import com.matchsentinel.auth.domain.@@User;
import com.matchsentinel.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request.getEmail(), request.getPassword());

        RegisterResponse response = RegisterResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .message("User registered successfully")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = authService.login(request.getEmail(), request.getPassword());

        LoginResponse response = LoginResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .message("Login successful")
                .build();

        return ResponseEntity.ok(response);
    }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: com/matchsentinel/auth/domain/User#