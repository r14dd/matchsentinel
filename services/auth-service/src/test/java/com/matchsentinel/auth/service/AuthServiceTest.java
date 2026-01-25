package com.matchsentinel.auth.service;

import com.matchsentinel.auth.domain.Role;
import com.matchsentinel.auth.domain.User;
import com.matchsentinel.auth.exception.EmailAlreadyInUseException;
import com.matchsentinel.auth.exception.InvalidCredentialsException;
import com.matchsentinel.auth.repository.UserRepository;
import com.matchsentinel.auth.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private com.matchsentinel.auth.security.RefreshTokenService refreshTokenService;

    @Mock
    private com.matchsentinel.auth.security.LoginAttemptService loginAttemptService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_throwsWhenEmailExists() {
        when(userRepository.existsByEmailIgnoreCase("test@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyInUseException.class,
                () -> authService.register("test@example.com", "password1"));
    }

    @Test
    void register_savesUserAndReturnsToken() {
        when(userRepository.existsByEmailIgnoreCase("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password1")).thenReturn("hashed");

        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .password("hashed")
                .role(Role.ANALYST)
                .emailVerified(false)
                .disabled(false)
                .createdAt(Instant.now())
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(savedUser)).thenReturn("token");
        when(refreshTokenService.createForUser(savedUser))
                .thenReturn(com.matchsentinel.auth.domain.RefreshToken.builder().token("refresh").user(savedUser).expiresAt(Instant.now()).build());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        var response = authService.register("TEST@EXAMPLE.COM", "password1");

        verify(userRepository).save(userCaptor.capture());
        assertEquals("test@example.com", userCaptor.getValue().getEmail());
        assertEquals("token", response.getToken());
        assertNotNull(response.getRefreshToken());
        assertNotNull(response.getVerificationToken());
        assertNotNull(response.getId());
    }

    @Test
    void login_throwsWhenEmailNotFound() {
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login("missing@example.com", "password1"));
    }

    @Test
    void login_throwsWhenPasswordInvalid() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .password("hashed")
                .role(Role.ANALYST)
                .emailVerified(true)
                .disabled(false)
                .createdAt(Instant.now())
                .build();
        when(userRepository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad1", "hashed")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login("test@example.com", "bad1"));
    }

    @Test
    void login_returnsTokenOnSuccess() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .password("hashed")
                .role(Role.ANALYST)
                .emailVerified(true)
                .disabled(false)
                .createdAt(Instant.now())
                .build();
        when(userRepository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password1", "hashed")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("token");
        when(refreshTokenService.createForUser(user))
                .thenReturn(com.matchsentinel.auth.domain.RefreshToken.builder().token("refresh").user(user).expiresAt(Instant.now()).build());

        var response = authService.login("test@example.com", "password1");

        assertEquals("token", response.getToken());
    }

    @Test
    void verifyEmail_marksVerified() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .emailVerified(false)
                .emailVerificationToken("verify")
                .emailVerificationExpiresAt(Instant.now().plusSeconds(300))
                .build();
        when(userRepository.findByEmailVerificationToken("verify")).thenReturn(Optional.of(user));

        var response = authService.verifyEmail("verify");

        assertEquals("Email verified successfully", response.message());
        assertEquals(true, user.isEmailVerified());
    }
}
