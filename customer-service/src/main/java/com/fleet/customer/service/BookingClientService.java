package com.fleet.customer.service;

import com.fleet.customer.dto.BookingReference;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingClientService {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.booking-service.url:http://booking-service}")
    private String bookingServiceUrl;

    public List<BookingReference> getCustomerBookings(Long customerId, String status) {
        WebClient client = webClientBuilder.baseUrl(bookingServiceUrl).build();
        String uri = "/api/bookings/customer/" + customerId;
        if (status != null) uri += "?status=" + status;

        return client.get()
                .uri(uri)
                .retrieve()
                .bodyToFlux(BookingReference.class)
                .collectList()
                .block();
    }
}
