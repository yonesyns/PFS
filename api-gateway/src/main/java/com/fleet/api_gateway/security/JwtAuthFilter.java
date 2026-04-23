package com.fleet.api_gateway.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.List;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    private final List<String> PUBLIC_URLS = List.of(
            "/api/auth/login",
            "/api/auth/register"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        // Autoriser OPTIONS (CORS)
        if (exchange.getRequest().getMethod().name().equals("OPTIONS")) {
            return chain.filter(exchange);
        }

        // URLs publiques
        if (PUBLIC_URLS.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Extraction user
        String username = jwtUtil.extractUsername(token);

        // Propagation vers microservices
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(r -> r.header("X-User", username))
                .build();

        return chain.filter(mutatedExchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}