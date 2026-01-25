package com.matchsentinel.auth.security;

import com.matchsentinel.auth.domain.RefreshToken;
import com.matchsentinel.auth.domain.User;
import com.matchsentinel.auth.exception.InvalidRefreshTokenException;
import com.matchsentinel.auth.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.Objects;

@Service
@SuppressWarnings("null")
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshTokenDays;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${auth.refresh.expiration-days}") long refreshTokenDays
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenDays = refreshTokenDays;
    }

    public RefreshToken createForUser(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(Objects.requireNonNull(user, "user"))
                .token(UUID.randomUUID().toString())
                .expiresAt(Instant.now().plusSeconds(refreshTokenDays * 24 * 60 * 60))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken validate(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));
        if (refreshToken.isRevoked() || refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidRefreshTokenException("Invalid refresh token");
        }
        return refreshToken;
    }

    public RefreshToken rotate(String token) {
        RefreshToken current = validate(token);
        current.setRevoked(true);
        refreshTokenRepository.save(current);
        return createForUser(Objects.requireNonNull(current.getUser(), "refreshToken.user"));
    }

    public void revokeAll(User user) {
        refreshTokenRepository.deleteAllByUser(Objects.requireNonNull(user, "user"));
    }
}
