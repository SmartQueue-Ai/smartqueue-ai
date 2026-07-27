package com.smartqueue.realtime_gateway.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String BOOKING_EXCHANGE = "smartqueue.booking.events";
    public static final String QUEUE_EXCHANGE = "smartqueue.queue.events";

    public static final String REALTIME_BOOKING_CREATED_QUEUE = "realtime.booking.created.queue";
    public static final String REALTIME_BOOKING_CANCELLED_QUEUE = "realtime.booking.cancelled.queue";
    public static final String REALTIME_QUEUE_MOVED_QUEUE = "realtime.queue.moved.queue";

    public static final String BOOKING_CREATED_ROUTING_KEY = "booking.created";
    public static final String BOOKING_CANCELLED_ROUTING_KEY = "booking.cancelled";
    public static final String QUEUE_MOVED_ROUTING_KEY = "queue.moved";

    @Bean
    public TopicExchange bookingExchange() {
        return new TopicExchange(BOOKING_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange queueExchange() {
        return new TopicExchange(QUEUE_EXCHANGE, true, false);
    }

    @Bean
    public Queue realtimeBookingCreatedQueue() {
        return QueueBuilder.durable(REALTIME_BOOKING_CREATED_QUEUE).build();
    }

    @Bean
    public Queue realtimeBookingCancelledQueue() {
        return QueueBuilder.durable(REALTIME_BOOKING_CANCELLED_QUEUE).build();
    }

    @Bean
    public Queue realtimeQueueMovedQueue() {
        return QueueBuilder.durable(REALTIME_QUEUE_MOVED_QUEUE).build();
    }

    @Bean
    public Binding realtimeBookingCreatedBinding(Queue realtimeBookingCreatedQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(realtimeBookingCreatedQueue).to(bookingExchange).with(BOOKING_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding realtimeBookingCancelledBinding(Queue realtimeBookingCancelledQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(realtimeBookingCancelledQueue).to(bookingExchange).with(BOOKING_CANCELLED_ROUTING_KEY);
    }

    @Bean
    public Binding realtimeQueueMovedBinding(Queue realtimeQueueMovedQueue, TopicExchange queueExchange) {
        return BindingBuilder.bind(realtimeQueueMovedQueue).to(queueExchange).with(QUEUE_MOVED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
