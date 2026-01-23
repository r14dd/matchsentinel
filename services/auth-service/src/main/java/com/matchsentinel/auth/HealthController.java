package com.matchsentinel.auth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // means this class handles http requests
public class HealthController {

    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse("auth-service", "UP");
    }
}
