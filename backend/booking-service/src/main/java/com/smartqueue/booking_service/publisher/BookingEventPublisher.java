package com.smartqueue.booking_service.publisher;

import com.smartqueue.booking_service.config.RabbitMQConfig;
import com.smartqueue.booking_service.dto.BookingResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BookingEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public BookingEventPublisher(@Autowired(required = false) RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishBookingCreated(BookingResponse booking) {
        log.info("Placeholder: Publishing BookingCreated event for reference: {}", booking.getBookingReference());
        if (rabbitTemplate != null) {
            try {
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.BOOKING_EXCHANGE,
                        RabbitMQConfig.BOOKING_CREATED_ROUTING_KEY,
                        booking
                );
            } catch (Exception e) {
                log.warn("Failed to publish BookingCreated event: {}", e.getMessage());
            }
        }
    }

    public void publishBookingCancelled(BookingResponse booking) {
        log.info("Placeholder: Publishing BookingCancelled event for reference: {}", booking.getBookingReference());
        if (rabbitTemplate != null) {
            try {
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.BOOKING_EXCHANGE,
                        RabbitMQConfig.BOOKING_CANCELLED_ROUTING_KEY,
                        booking
                );
            } catch (Exception e) {
                log.warn("Failed to publish BookingCancelled event: {}", e.getMessage());
            }
        }
    }
}
