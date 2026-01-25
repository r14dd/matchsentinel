package com.matchsentinel.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.Objects;

@SuppressWarnings("null")
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @SuppressWarnings("null")
    private final String[] allowedOrigins;

    public WebConfig(@Value("${auth.cors.allowed-origins}") String allowedOrigins) {
        this.allowedOrigins = Arrays.stream(Objects.requireNonNull(allowedOrigins, "auth.cors.allowed-origins").split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toArray(String[]::new);
    }

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
