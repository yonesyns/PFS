package com.fleet.payment.client;

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
public class VehicleServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.vehicle-service.url:http://vehicle-service:8082}")
    private String vehicleServiceUrl;

    public Mono<Boolean> vehicleExists(UUID vehicleId) {
        return webClientBuilder.build()
                .get()
                .uri(vehicleServiceUrl + "/api/vehicles/{id}", vehicleId)
                .retrieve()
                .toBodilessEntity()
                .map(response -> true)
                .onErrorResume(e -> {
                    log.warn("Vehicle service error for vehicle {}: {}", vehicleId, e.getMessage());
                    return Mono.just(false);
                });
    }
}
