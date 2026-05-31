package com.fleet.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${fleet.rabbitmq.exchange:fleet.topic}")
    private String exchange;

    @Bean
    public TopicExchange fleetExchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    public Queue vehicleQueue() {
        return QueueBuilder.durable("notification.vehicle.events").build();
    }

    @Bean
    public Queue customerQueue() {
        return QueueBuilder.durable("notification.customer.events").build();
    }

    @Bean
    public Queue paymentQueue() {
        return QueueBuilder.durable("notification.payment.events").build();
    }

    @Bean
    public Binding vehicleBinding(Queue vehicleQueue, TopicExchange fleetExchange) {
        return BindingBuilder.bind(vehicleQueue).to(fleetExchange).with("vehicle.#");
    }

    @Bean
    public Binding customerBinding(Queue customerQueue, TopicExchange fleetExchange) {
        return BindingBuilder.bind(customerQueue).to(fleetExchange).with("customer.#");
    }

    @Bean
    public Binding paymentBinding(Queue paymentQueue, TopicExchange fleetExchange) {
        return BindingBuilder.bind(paymentQueue).to(fleetExchange).with("payment.#");
    }
}