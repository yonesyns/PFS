package com.fleet.gateway.filter;

import com.fleet.gateway.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final List<String> USER_HEADERS = List.of(
            "X-User-Id",
            "X-User-Email",
            "X-User-Role",
            "X-Customer-Id");

    private final SecretKey signingKey;

    public JwtAuthenticationFilter(JwtProperties properties) {
        this.signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (isPublicRequest(request)) {
            return chain.filter(stripUserHeaders(exchange));
        }

        String authorization = request.getHeaders().getFirst("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing Bearer token");
        }

        try {
            Claims claims = parseClaims(authorization.substring(7));
            if (!"access".equals(claims.get("token_type", String.class))) {
                return unauthorized(exchange, "Invalid token type");
            }

            String role = claims.get("role", String.class);
            if (isAdminOnlyRequest(request) && !isAdmin(role)) {
                return forbidden(exchange, "Admin role required");
            }

            ServerWebExchange authenticatedExchange = exchange.mutate()
                    .request(builder -> {
                        builder.headers(headers -> USER_HEADERS.forEach(headers::remove));
                        builder.header("X-User-Id", claims.getSubject());
                        builder.header("X-User-Email", claims.get("email", String.class));
                        builder.header("X-User-Role", role);
                        String customerId = claims.get("customer_id", String.class);
                        if (customerId != null && !customerId.isBlank()) {
                            builder.header("X-Customer-Id", customerId);
                        }
                    })
                    .build();

            return chain.filter(authenticatedExchange);
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("JWT validation failed for {} {}: {}",
                    request.getMethod(),
                    request.getURI().getPath(),
                    ex.getMessage());
            return unauthorized(exchange, "Invalid or expired token");
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isPublicRequest(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        return method == HttpMethod.OPTIONS
                || path.startsWith("/api/auth/")
                || path.equals("/actuator/health")
                || path.equals("/api/customers") && method == HttpMethod.POST;
    }

    private boolean isAdminOnlyRequest(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        return path.equals("/api/customers") && method == HttpMethod.GET
                || path.startsWith("/api/customers/search")
                || path.startsWith("/api/customers/pending")
                || path.matches("^/api/customers/[^/]+/(validate|status)$")
                || path.matches("^/api/customers/[^/]+$") && method == HttpMethod.DELETE;
    }

    private boolean isAdmin(String role) {
        return "ADMIN".equals(role) || "SUPER_ADMIN".equals(role);
    }

    private ServerWebExchange stripUserHeaders(ServerWebExchange exchange) {
        return exchange.mutate()
                .request(builder -> builder.headers(headers -> USER_HEADERS.forEach(headers::remove)))
                .build();
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        return reject(exchange, HttpStatus.UNAUTHORIZED, message);
    }

    private Mono<Void> forbidden(ServerWebExchange exchange, String message) {
        return reject(exchange, HttpStatus.FORBIDDEN, message);
    }

    private Mono<Void> reject(ServerWebExchange exchange, HttpStatus status, String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");
        String body = "{\"success\":false,\"message\":\"" + message + "\"}";
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                .bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8))));
    }
}
