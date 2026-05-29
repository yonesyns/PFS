package com.fleet.auth.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class AuthResponse {

    private String tokenType;
    private String accessToken;
    private long expiresIn;
    private UUID userId;
    private String email;
    private List<String> roles;
}
