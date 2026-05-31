package com.fleet.customer.messaging;

import com.fleet.commons.event.customer.*;
import com.fleet.customer.entity.Customer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${fleet.rabbitmq.exchange:fleet.topic}")
    private String exchange;

    public void publishCustomerCreated(Customer customer) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "CustomerCreatedEvent");
        event.put("source", "customer-service");

        Map<String, Object> payload = new HashMap<>();
        payload.put("customerId", customer.getId());
        payload.put("companyName", customer.getCompanyName());
        payload.put("email", customer.getEmail());
        payload.put("vatNumber", customer.getVatNumber());
        event.put("payload", payload);

        rabbitTemplate.convertAndSend(exchange, "customer.created", event);
        log.info("Published CustomerCreatedEvent for customer: {}", customer.getId());
    }

    public void publishCustomerValidated(Customer customer) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "CustomerValidatedEvent");
        event.put("source", "customer-service");

        Map<String, Object> payload = new HashMap<>();
        payload.put("customerId", customer.getId());
        payload.put("validatedBy", customer.getValidatedBy());
        payload.put("companyName", customer.getCompanyName());
        payload.put("email", customer.getEmail());
        event.put("payload", payload);

        rabbitTemplate.convertAndSend(exchange, "customer.validated", event);
        log.info("Published CustomerValidatedEvent for customer: {}", customer.getId());
    }

    public void publishCustomerUpdated(Customer customer, List<String> fieldsChanged) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "CustomerUpdatedEvent");
        event.put("source", "customer-service");

        Map<String, Object> payload = new HashMap<>();
        payload.put("customerId", customer.getId());
        payload.put("fieldsChanged", fieldsChanged);
        event.put("payload", payload);

        rabbitTemplate.convertAndSend(exchange, "customer.updated", event);
        log.info("Published CustomerUpdatedEvent for customer: {}", customer.getId());
    }

    public void publishCustomerSuspended(Customer customer, String reason, UUID suspendedBy) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "CustomerSuspendedEvent");
        event.put("source", "customer-service");

        Map<String, Object> payload = new HashMap<>();
        payload.put("customerId", customer.getId());
        payload.put("reason", reason);
        payload.put("suspendedBy", suspendedBy);
        event.put("payload", payload);

        rabbitTemplate.convertAndSend(exchange, "customer.suspended", event);
        log.info("Published CustomerSuspendedEvent for customer: {}", customer.getId());
    }

    public void publishCustomerDeleted(Customer customer, UUID deletedBy) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "CustomerDeletedEvent");
        event.put("source", "customer-service");

        Map<String, Object> payload = new HashMap<>();
        payload.put("customerId", customer.getId());
        payload.put("deletedBy", deletedBy);
        event.put("payload", payload);

        rabbitTemplate.convertAndSend(exchange, "customer.deleted", event);
        log.info("Published CustomerDeletedEvent for customer: {}", customer.getId());
    }

    public void publishCustomerReactivated(Customer customer, UUID reactivatedBy) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "CustomerReactivatedEvent");
        event.put("source", "customer-service");

        Map<String, Object> payload = new HashMap<>();
        payload.put("customerId", customer.getId());
        payload.put("reactivatedBy", reactivatedBy);
        event.put("payload", payload);

        rabbitTemplate.convertAndSend(exchange, "customer.reactivated", event);
        log.info("Published CustomerReactivatedEvent for customer: {}", customer.getId());
    }
}