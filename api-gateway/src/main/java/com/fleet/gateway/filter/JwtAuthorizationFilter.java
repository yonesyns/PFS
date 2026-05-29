package com.fleet.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final Set<String> ADMIN_ROLES = Set.of("SUPER_ADMIN", "ADMIN");
    private static final Set<String> CUSTOMER_WRITE_ROLES = Set.of("SUPER_ADMIN", "ADMIN", "CUSTOMER_ADMIN");
    private static final Set<String> DOCUMENT_WRITE_ROLES = Set.of("SUPER_ADMIN", "ADMIN", "CUSTOMER_ADMIN", "CUSTOMER_USER");

    private final ObjectMapper objectMapper;

    @Value("${fleet.security.jwt.secret}")
    private String jwtSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        if (isPublicEndpoint(path, method)) {
            return chain.filter(exchange);
        }

        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return writeError(exchange, HttpStatus.UNAUTHORIZED, "Missing bearer token");
        }

        Claims claims;
        try {
            claims = parseClaims(authorization.substring(BEARER_PREFIX.length()));
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Invalid JWT: {}", ex.getMessage());
            return writeError(exchange, HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }

        List<String> roles = getRoles(claims);
        if (!isAuthorized(path, method, roles)) {
            return writeError(exchange, HttpStatus.FORBIDDEN, "Access denied");
        }

        ServerHttpRequest authenticatedRequest = request.mutate()
                .headers(headers -> {
                    headers.remove("X-User-Id");
                    headers.remove("X-User-Email");
                    headers.remove("X-User-Roles");
                })
                .header("X-User-Id", claims.getSubject())
                .header("X-User-Email", String.valueOf(claims.get("email")))
                .header("X-User-Roles", String.join(",", roles))
                .build();

        return chain.filter(exchange.mutate().request(authenticatedRequest).build());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    private boolean isPublicEndpoint(String path, HttpMethod method) {
        if (method == HttpMethod.OPTIONS) {
            return true;
        }
        return path.equals("/api/auth/login")
                || path.equals("/api/auth/register")
                || path.equals("/actuator/health")
                || path.startsWith("/api-docs")
                || path.startsWith("/swagger-ui")
                || path.equals("/swagger-ui.html")
                || (path.equals("/api/customers") && method == HttpMethod.POST);
    }

    private boolean isAuthorized(String path, HttpMethod method, List<String> roles) {
        if (roles.stream().anyMatch("SUPER_ADMIN"::equals)) {
            return true;
        }

        if (path.startsWith("/api/auth")) {
            return true;
        }

        if (path.startsWith("/api/customers")) {
            if (method == HttpMethod.GET) {
                return hasAnyRole(roles, ADMIN_ROLES);
            }
            return hasAnyRole(roles, ADMIN_ROLES);
        }

        if (path.startsWith("/api/vehicles")) {
            if (method == HttpMethod.GET) {
                return !roles.isEmpty();
            }
            return hasAnyRole(roles, CUSTOMER_WRITE_ROLES);
        }

        if (path.startsWith("/api/documents")) {
            if (method == HttpMethod.GET) {
                return !roles.isEmpty();
            }
            return hasAnyRole(roles, DOCUMENT_WRITE_ROLES);
        }

        if (path.startsWith("/api/invoices")
                || path.startsWith("/api/subscriptions")
                || path.startsWith("/api/transactions")) {
            if (method == HttpMethod.GET || path.matches("/api/invoices/[^/]+/pay")) {
                return !roles.isEmpty();
            }
            return hasAnyRole(roles, ADMIN_ROLES);
        }

        return !roles.isEmpty();
    }

    private boolean hasAnyRole(List<String> roles, Set<String> requiredRoles) {
        return roles.stream().anyMatch(requiredRoles::contains);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    private List<String> getRoles(Claims claims) {
        Object roles = claims.get("roles");
        if (roles instanceof Collection<?> collection) {
            return collection.stream()
                    .map(String::valueOf)
                    .toList();
        }
        return List.of();
    }

    private Mono<Void> writeError(ServerWebExchange exchange, HttpStatus status, String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message,
                "path", exchange.getRequest().getURI().getPath());

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException ex) {
            bytes = ("{\"message\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
