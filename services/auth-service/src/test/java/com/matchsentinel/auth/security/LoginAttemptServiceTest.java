package com.matchsentinel.auth.security;

import com.matchsentinel.auth.exception.TooManyRequestsException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginAttemptServiceTest {

    @Test
    void locksAfterMaxAttempts() {
        LoginAttemptService service = new LoginAttemptService(2, 10, 10);

        service.recordFailure("user@example.com");
        service.recordFailure("user@example.com");

        assertThrows(TooManyRequestsException.class,
                () -> service.checkAllowed("user@example.com"));
    }

    @Test
    void clearsAfterSuccess() {
        LoginAttemptService service = new LoginAttemptService(2, 10, 10);

        service.recordFailure("user@example.com");
        service.recordSuccess("user@example.com");

        assertDoesNotThrow(() -> service.checkAllowed("user@example.com"));
    }
}
