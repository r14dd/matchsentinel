package com.matchsentinel.auth.security;

import com.matchsentinel.auth.domain.RefreshToken;
import com.matchsentinel.auth.domain.User;
import com.matchsentinel.auth.exception.InvalidRefreshTokenException;
import com.matchsentinel.auth.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

class RefreshTokenServiceTest {

    @Test
    void createForUser_savesToken() {
        RefreshTokenRepository repository = mock(RefreshTokenRepository.class);
        RefreshTokenService service = new RefreshTokenService(repository, 7);

        User user = User.builder().id(UUID.randomUUID()).build();
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        when(repository.save(captor.capture())).thenAnswer(invocation -> captor.getValue());

        RefreshToken token = service.createForUser(user);

        assertNotNull(token.getToken());
        assertEquals(user, token.getUser());
        verify(repository).save(token);
    }

    @Test
    void validate_throwsWhenMissing() {
        RefreshTokenRepository repository = mock(RefreshTokenRepository.class);
        RefreshTokenService service = new RefreshTokenService(repository, 7);

        when(repository.findByToken("missing")).thenReturn(Optional.empty());

        assertThrows(InvalidRefreshTokenException.class, () -> service.validate("missing"));
    }

    @Test
    void validate_throwsWhenExpired() {
        RefreshTokenRepository repository = mock(RefreshTokenRepository.class);
        RefreshTokenService service = new RefreshTokenService(repository, 7);

        RefreshToken token = RefreshToken.builder()
                .token("token")
                .expiresAt(Instant.now().minusSeconds(60))
                .revoked(false)
                .build();
        when(repository.findByToken("token")).thenReturn(Optional.of(token));

        assertThrows(InvalidRefreshTokenException.class, () -> service.validate("token"));
    }
}
