package com.fleet.payment.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${fleet.rabbitmq.exchange:fleet.topic}")
    private String exchangeName;

    @Bean
    public TopicExchange fleetExchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Queue paymentEventsQueue() {
        return new Queue("payment.events", true);
    }

    @Bean
    public Queue customerEventsQueue() {
        return new Queue("customer.events", true);
    }

    @Bean
    public Queue vehicleEventsQueue() {
        return new Queue("vehicle.events", true);
    }

    @Bean
    public Binding paymentEventsBinding() {
        return BindingBuilder.bind(paymentEventsQueue())
                .to(fleetExchange())
                .with("payment.*");
    }

    @Bean
    public Binding customerEventsBinding() {
        return BindingBuilder.bind(customerEventsQueue())
                .to(fleetExchange())
                .with("customer.*");
    }

    @Bean
    public Binding vehicleEventsBinding() {
        return BindingBuilder.bind(vehicleEventsQueue())
                .to(fleetExchange())
                .with("vehicle.*");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
