package com.fleet.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);

        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }

        final String finalCorrelationId = correlationId;
        MDC.put("correlationId", finalCorrelationId);

        log.info("Request: {} {} - CorrelationId: {}",
                exchange.getRequest().getMethod(),
                exchange.getRequest().getPath(),
                finalCorrelationId);

        long startTime = System.currentTimeMillis();

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(r -> r.header(CORRELATION_ID_HEADER, finalCorrelationId))
                .build();

        return chain.filter(mutatedExchange)
                .doFinally(signalType -> {
                    long duration = System.currentTimeMillis() - startTime;
                    int statusCode = mutatedExchange.getResponse().getStatusCode() != null 
                            ? mutatedExchange.getResponse().getStatusCode().value() : 0;
                    log.info("Response: {} {} - Status: {} - Duration: {}ms - CorrelationId: {}",
                            exchange.getRequest().getMethod(),
                            exchange.getRequest().getPath(),
                            statusCode,
                            duration,
                            finalCorrelationId);
                    MDC.remove("correlationId");
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
