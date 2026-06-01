package com.fleet.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String tokenType;
    private String accessToken;
    private String refreshToken;
    private long expiresInSeconds;
    private UserResponse user;
}
