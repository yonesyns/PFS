package com.fleet.vehicle.client;

import com.fleet.commons.exception.ServiceUnavailableException;
import com.fleet.vehicle.dto.CustomerInfoResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
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

    @CircuitBreaker(name = "customerService", fallbackMethod = "getCustomerFallback")
    @Retry(name = "customerService")
    public Mono<CustomerInfoResponse> getCustomer(UUID customerId) {
        return webClientBuilder.build()
                .get()
                .uri(customerServiceUrl + "/api/customers/{id}", customerId)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError(),
                        response -> Mono.error(new ServiceUnavailableException("Customer not found: " + customerId))
                )
                .bodyToMono(CustomerInfoResponse.class)
                .doOnNext(customer -> log.info("Fetched customer: {}", customer.getCompanyName()));
    }

    @CircuitBreaker(name = "customerService", fallbackMethod = "customerExistsFallback")
    @Retry(name = "customerService")
    public Mono<Boolean> customerExists(UUID customerId) {
        return webClientBuilder.build()
                .get()
                .uri(customerServiceUrl + "/api/customers/{id}", customerId)
                .retrieve()
                .toBodilessEntity()
                .map(response -> true)
                .onErrorReturn(false);
    }

    public Mono<CustomerInfoResponse> getCustomerFallback(UUID customerId, Exception ex) {
        log.warn("Customer service fallback triggered for customer: {} - {}", customerId, ex.getMessage());
        return Mono.just(CustomerInfoResponse.builder()
                .id(customerId)
                .companyName("UNKNOWN")
                .email("unknown@fleet.com")
                .status("UNKNOWN")
                .build());
    }

    public Mono<Boolean> customerExistsFallback(UUID customerId, Exception ex) {
        log.warn("Customer exists fallback triggered for customer: {} - {}", customerId, ex.getMessage());
        return Mono.just(false);
    }
}
