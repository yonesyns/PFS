package com.fleet.payment.client;

import com.fleet.commons.exception.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.customer-service.url:http://customer-service:8081}")
    private String customerServiceUrl;

    public Mono<Boolean> customerExists(UUID customerId) {
        return webClientBuilder.build()
                .get()
                .uri(customerServiceUrl + "/api/customers/{id}", customerId)
                .retrieve()
                .toBodilessEntity()
                .map(response -> true)
                .onErrorResume(e -> {
                    log.warn("Customer service error for customer {}: {}", customerId, e.getMessage());
                    return Mono.just(false);
                });
    }

    public Mono<Boolean> isCustomerActive(UUID customerId) {
        return webClientBuilder.build()
                .get()
                .uri(customerServiceUrl + "/api/customers/{id}", customerId)
                .retrieve()
                .bodyToMono(CustomerInfo.class)
                .map(c -> "ACTIVE".equals(c.getStatus()))
                .onErrorReturn(false);
    }

    @lombok.Data
    public static class CustomerInfo {
        private String status;
    }
}
