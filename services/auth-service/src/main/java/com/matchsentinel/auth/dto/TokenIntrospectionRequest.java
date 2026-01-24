package com.matchsentinel.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenIntrospectionRequest {

    @NotBlank(message = "Token is required")
    private String token;
}
