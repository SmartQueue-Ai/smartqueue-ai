package com.smartqueue.notification_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_EVENTS = "smartqueue.events";

    public static final String QUEUE_BOOKING_CREATED = "notification.booking-created.q";
    public static final String QUEUE_BOOKING_CANCELLED = "notification.booking-cancelled.q";
    public static final String QUEUE_BOOKING_CONFIRMED = "notification.booking-confirmed.q";

    public static final String ROUTING_KEY_BOOKING_CREATED = "booking.created";
    public static final String ROUTING_KEY_BOOKING_CANCELLED = "booking.cancelled";
    public static final String ROUTING_KEY_BOOKING_CONFIRMED = "booking.confirmed";

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(EXCHANGE_EVENTS, true, false);
    }

    @Bean
    public Queue bookingCreatedQueue() {
        return QueueBuilder.durable(QUEUE_BOOKING_CREATED).build();
    }

    @Bean
    public Queue bookingCancelledQueue() {
        return QueueBuilder.durable(QUEUE_BOOKING_CANCELLED).build();
    }

    @Bean
    public Queue bookingConfirmedQueue() {
        return QueueBuilder.durable(QUEUE_BOOKING_CONFIRMED).build();
    }

    @Bean
    public Binding bookingCreatedBinding(Queue bookingCreatedQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(bookingCreatedQueue).to(eventsExchange).with(ROUTING_KEY_BOOKING_CREATED);
    }

    @Bean
    public Binding bookingCancelledBinding(Queue bookingCancelledQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(bookingCancelledQueue).to(eventsExchange).with(ROUTING_KEY_BOOKING_CANCELLED);
    }

    @Bean
    public Binding bookingConfirmedBinding(Queue bookingConfirmedQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(bookingConfirmedQueue).to(eventsExchange).with(ROUTING_KEY_BOOKING_CONFIRMED);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
