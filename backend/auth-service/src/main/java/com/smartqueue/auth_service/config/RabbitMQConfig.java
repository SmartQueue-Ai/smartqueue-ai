package com.smartqueue.auth_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_EVENTS = "smartqueue.events";
    public static final String QUEUE_USER_REGISTERED = "auth.user-registered.q";
    public static final String ROUTING_KEY_USER_REGISTERED = "user.registered";

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(EXCHANGE_EVENTS, true, false);
    }

    @Bean
    public Queue userRegisteredQueue() {
        return QueueBuilder.durable(QUEUE_USER_REGISTERED).build();
    }

    @Bean
    public Binding userRegisteredBinding(Queue userRegisteredQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(userRegisteredQueue)
                .to(eventsExchange)
                .with(ROUTING_KEY_USER_REGISTERED);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
