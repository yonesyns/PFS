package com.fleet.payment.messaging;

import com.fleet.commons.event.payment.*;
import com.fleet.payment.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${fleet.rabbitmq.exchange:fleet.topic}")
    private String exchange;

    public void publishInvoiceCreated(Invoice invoice) {
        InvoiceCreatedEvent event = InvoiceCreatedEvent.builder()
                .eventType("InvoiceCreatedEvent")
                .source("payment-service")
                .payload(InvoiceCreatedEvent.InvoicePayload.builder()
                        .invoiceId(invoice.getId())
                        .invoiceNumber(invoice.getInvoiceNumber())
                        .customerId(invoice.getCustomerId())
                        .amount(invoice.getAmount())
                        .currency(invoice.getCurrency())
                        .build())
                .build();

        rabbitTemplate.convertAndSend(exchange, "payment.invoice.created", event);
        log.info("Published InvoiceCreatedEvent for invoice: {}", invoice.getId());
    }

    public void publishInvoicePaid(Invoice invoice) {
        InvoicePaidEvent event = InvoicePaidEvent.builder()
                .eventType("InvoicePaidEvent")
                .source("payment-service")
                .payload(InvoicePaidEvent.InvoicePaidPayload.builder()
                        .invoiceId(invoice.getId())
                        .customerId(invoice.getCustomerId())
                        .amount(invoice.getAmount())
                        .paymentMethod("CARD")
                        .build())
                .build();

        rabbitTemplate.convertAndSend(exchange, "payment.invoice.paid", event);
        log.info("Published InvoicePaidEvent for invoice: {}", invoice.getId());
    }

    public void publishInvoiceOverdue(Invoice invoice, int daysOverdue) {
        InvoiceOverdueEvent event = InvoiceOverdueEvent.builder()
                .eventType("InvoiceOverdueEvent")
                .source("payment-service")
                .payload(InvoiceOverdueEvent.InvoiceOverduePayload.builder()
                        .invoiceId(invoice.getId())
                        .customerId(invoice.getCustomerId())
                        .amount(invoice.getAmount())
                        .daysOverdue(daysOverdue)
                        .build())
                .build();

        rabbitTemplate.convertAndSend(exchange, "payment.invoice.overdue", event);
        log.info("Published InvoiceOverdueEvent for invoice: {} ({} days)", invoice.getId(), daysOverdue);
    }

    public void publishInvoiceCancelled(Invoice invoice, String reason) {
        InvoiceCancelledEvent event = InvoiceCancelledEvent.builder()
                .eventType("InvoiceCancelledEvent")
                .source("payment-service")
                .payload(InvoiceCancelledEvent.InvoiceCancelledPayload.builder()
                        .invoiceId(invoice.getId())
                        .customerId(invoice.getCustomerId())
                        .reason(reason)
                        .build())
                .build();

        rabbitTemplate.convertAndSend(exchange, "payment.invoice.cancelled", event);
        log.info("Published InvoiceCancelledEvent for invoice: {}", invoice.getId());
    }

    public void publishSubscriptionCreated(Subscription subscription) {
        SubscriptionCreatedEvent event = SubscriptionCreatedEvent.builder()
                .eventType("SubscriptionCreatedEvent")
                .source("payment-service")
                .payload(SubscriptionCreatedEvent.SubscriptionPayload.builder()
                        .subscriptionId(subscription.getId())
                        .customerId(subscription.getCustomerId())
                        .planType(subscription.getPlanType().name())
                        .build())
                .build();

        rabbitTemplate.convertAndSend(exchange, "payment.subscription.created", event);
        log.info("Published SubscriptionCreatedEvent for subscription: {}", subscription.getId());
    }

    public void publishSubscriptionUpgraded(Subscription subscription, PlanType oldPlan, PlanType newPlan) {
        SubscriptionUpgradedEvent event = SubscriptionUpgradedEvent.builder()
                .eventType("SubscriptionUpgradedEvent")
                .source("payment-service")
                .payload(SubscriptionUpgradedEvent.SubscriptionUpgradedPayload.builder()
                        .subscriptionId(subscription.getId())
                        .customerId(subscription.getCustomerId())
                        .oldPlan(oldPlan.name())
                        .newPlan(newPlan.name())
                        .build())
                .build();

        rabbitTemplate.convertAndSend(exchange, "payment.subscription.upgraded", event);
        log.info("Published SubscriptionUpgradedEvent for subscription: {}", subscription.getId());
    }

    public void publishSubscriptionExpired(Subscription subscription) {
        SubscriptionExpiredEvent event = SubscriptionExpiredEvent.builder()
                .eventType("SubscriptionExpiredEvent")
                .source("payment-service")
                .payload(SubscriptionExpiredEvent.SubscriptionExpiredPayload.builder()
                        .subscriptionId(subscription.getId())
                        .customerId(subscription.getCustomerId())
                        .build())
                .build();

        rabbitTemplate.convertAndSend(exchange, "payment.subscription.expired", event);
        log.info("Published SubscriptionExpiredEvent for subscription: {}", subscription.getId());
    }

    public void publishSubscriptionCancelled(Subscription subscription, String reason) {
        SubscriptionCancelledEvent event = SubscriptionCancelledEvent.builder()
                .eventType("SubscriptionCancelledEvent")
                .source("payment-service")
                .payload(SubscriptionCancelledEvent.SubscriptionCancelledPayload.builder()
                        .subscriptionId(subscription.getId())
                        .customerId(subscription.getCustomerId())
                        .reason(reason)
                        .build())
                .build();

        rabbitTemplate.convertAndSend(exchange, "payment.subscription.cancelled", event);
        log.info("Published SubscriptionCancelledEvent for subscription: {}", subscription.getId());
    }
}
