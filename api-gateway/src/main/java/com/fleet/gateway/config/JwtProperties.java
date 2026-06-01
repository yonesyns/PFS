package com.fleet.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fleet.security.jwt")
public record JwtProperties(String secret) {
}
