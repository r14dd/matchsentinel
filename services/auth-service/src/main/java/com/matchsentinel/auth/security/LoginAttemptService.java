package com.matchsentinel.auth.security;

import com.matchsentinel.auth.exception.TooManyRequestsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final class Attempt {
        private int count;
        private Instant firstAttemptAt;
        private Instant lockedUntil;
    }

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();
    private final int maxAttempts;
    private final long windowMinutes;
    private final long lockMinutes;

    public LoginAttemptService(
            @Value("${auth.login.max-attempts}") int maxAttempts,
            @Value("${auth.login.window-minutes}") long windowMinutes,
            @Value("${auth.login.lock-minutes}") long lockMinutes
    ) {
        this.maxAttempts = maxAttempts;
        this.windowMinutes = windowMinutes;
        this.lockMinutes = lockMinutes;
    }

    public void checkAllowed(String key) {
        Attempt attempt = attempts.get(key);
        if (attempt == null) {
            return;
        }
        if (attempt.lockedUntil != null && Instant.now().isBefore(attempt.lockedUntil)) {
            throw new TooManyRequestsException("Too many login attempts. Try again later.");
        }
    }

    public void recordFailure(String key) {
        Attempt attempt = attempts.computeIfAbsent(key, k -> new Attempt());
        Instant now = Instant.now();

        if (attempt.firstAttemptAt == null || now.isAfter(attempt.firstAttemptAt.plusSeconds(windowMinutes * 60))) {
            attempt.firstAttemptAt = now;
            attempt.count = 1;
            attempt.lockedUntil = null;
            return;
        }

        attempt.count++;
        if (attempt.count >= maxAttempts) {
            attempt.lockedUntil = now.plusSeconds(lockMinutes * 60);
        }
    }

    public void recordSuccess(String key) {
        attempts.remove(key);
    }
}
