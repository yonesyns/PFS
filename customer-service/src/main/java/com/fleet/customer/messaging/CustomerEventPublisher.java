package com.fleet.customer.messaging;

import com.fleet.commons.event.customer.*;
import com.fleet.customer.entity.Customer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${fleet.rabbitmq.exchange:fleet.topic}")
    private String exchange;

    public void publishCustomerCreated(Customer customer) {
        CustomerCreatedEvent event = CustomerCreatedEvent.builder()
                .eventType("CustomerCreatedEvent")
                .source("customer-service")
                .payload(CustomerCreatedEvent.CustomerPayload.builder()
                        .customerId(customer.getId())
                        .companyName(customer.getCompanyName())
                        .email(customer.getEmail())
                        .vatNumber(customer.getVatNumber())
                        .build())
                .build();

        rabbitTemplate.convertAndSend(exchange, "customer.created", event);
        log.info("Published CustomerCreatedEvent for customer: {}", customer.getId());
    }

    public void publishCustomerValidated(Customer customer) {
        CustomerValidatedEvent event = CustomerValidatedEvent.builder()
                .eventType("CustomerValidatedEvent")
                .source("customer-service")
                .payload(CustomerValidatedEvent.CustomerValidatedPayload.builder()
                        .customerId(customer.getId())
                        .validatedBy(customer.getValidatedBy())
                        .build())
                .build();

        rabbitTemplate.convertAndSend(exchange, "customer.validated", event);
        log.info("Published CustomerValidatedEvent for customer: {}", customer.getId());
    }

    public void publishCustomerUpdated(Customer customer, List<String> fieldsChanged) {
        CustomerUpdatedEvent event = CustomerUpdatedEvent.builder()
                .eventType("CustomerUpdatedEvent")
                .source("customer-service")
                .payload(CustomerUpdatedEvent.CustomerUpdatedPayload.builder()
                        .customerId(customer.getId())
                        .fieldsChanged(fieldsChanged)
                        .build())
                .build();

        rabbitTemplate.convertAndSend(exchange, "customer.updated", event);
        log.info("Published CustomerUpdatedEvent for customer: {}", customer.getId());
    }

    public void publishCustomerSuspended(Customer customer, String reason, UUID suspendedBy) {
        CustomerSuspendedEvent event = CustomerSuspendedEvent.builder()
                .eventType("CustomerSuspendedEvent")
                .source("customer-service")
                .payload(CustomerSuspendedEvent.CustomerSuspendedPayload.builder()
                        .customerId(customer.getId())
                        .reason(reason)
                        .suspendedBy(suspendedBy)
                        .build())
                .build();

        rabbitTemplate.convertAndSend(exchange, "customer.suspended", event);
        log.info("Published CustomerSuspendedEvent for customer: {}", customer.getId());
    }

    public void publishCustomerDeleted(Customer customer, UUID deletedBy) {
        CustomerDeletedEvent event = CustomerDeletedEvent.builder()
                .eventType("CustomerDeletedEvent")
                .source("customer-service")
                .payload(CustomerDeletedEvent.CustomerDeletedPayload.builder()
                        .customerId(customer.getId())
                        .deletedBy(deletedBy)
                        .build())
                .build();

        rabbitTemplate.convertAndSend(exchange, "customer.deleted", event);
        log.info("Published CustomerDeletedEvent for customer: {}", customer.getId());
    }

    public void publishCustomerReactivated(Customer customer, UUID reactivatedBy) {
        CustomerReactivatedEvent event = CustomerReactivatedEvent.builder()
                .eventType("CustomerReactivatedEvent")
                .source("customer-service")
                .payload(CustomerReactivatedEvent.CustomerReactivatedPayload.builder()
                        .customerId(customer.getId())
                        .reactivatedBy(reactivatedBy)
                        .build())
                .build();

        rabbitTemplate.convertAndSend(exchange, "customer.reactivated", event);
        log.info("Published CustomerReactivatedEvent for customer: {}", customer.getId());
    }
}
