package com.fleet.auth.security;

import com.fleet.auth.entity.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long accessTokenExpirationSeconds;
    private final long refreshTokenExpirationSeconds;

    public JwtService(
            @Value("${fleet.security.jwt.secret}") String secret,
            @Value("${fleet.security.jwt.access-token-expiration-seconds:3600}") long accessTokenExpirationSeconds,
            @Value("${fleet.security.jwt.refresh-token-expiration-seconds:604800}") long refreshTokenExpirationSeconds) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
        this.refreshTokenExpirationSeconds = refreshTokenExpirationSeconds;
    }

    public String generateAccessToken(UserAccount user) {
        return generateToken(user, "access", accessTokenExpirationSeconds);
    }

    public String generateRefreshToken(UserAccount user) {
        return generateToken(user, "refresh", refreshTokenExpirationSeconds);
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID getUserId(String token) {
        return UUID.fromString(parseToken(token).getSubject());
    }

    public boolean isAccessToken(String token) {
        return "access".equals(parseToken(token).get("token_type", String.class));
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(parseToken(token).get("token_type", String.class));
    }

    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpirationSeconds;
    }

    public boolean isValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private String generateToken(UserAccount user, String tokenType, long expirationSeconds) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(expirationSeconds);

        return Jwts.builder()
                .claims(Map.of(
                        "email", user.getEmail(),
                        "role", user.getRole().name(),
                        "first_name", user.getFirstName(),
                        "last_name", user.getLastName(),
                        "customer_id", user.getCustomerId() != null ? user.getCustomerId().toString() : "",
                        "token_type", tokenType
                ))
                .subject(user.getId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }
}
