package com.smartqueue.inventory_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_EVENTS = "smartqueue.events";
    public static final String QUEUE_INVENTORY_EVENTS = "inventory.events";
    public static final String ROUTING_KEY_INVENTORY_WILDCARD = "inventory.#";

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(EXCHANGE_EVENTS, true, false);
    }

    @Bean
    public Queue inventoryEventsQueue() {
        return QueueBuilder.durable(QUEUE_INVENTORY_EVENTS).build();
    }

    @Bean
    public Binding inventoryEventsBinding(Queue inventoryEventsQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(inventoryEventsQueue)
                .to(eventsExchange)
                .with(ROUTING_KEY_INVENTORY_WILDCARD);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
