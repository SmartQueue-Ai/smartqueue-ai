package com.smartqueue.booking_service.listener;

import com.smartqueue.booking_service.config.RabbitMQConfig;
import com.smartqueue.booking_service.dto.BookingResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BookingEventListener {

    @RabbitListener(queues = RabbitMQConfig.BOOKING_CREATED_QUEUE, autoStartup = "false")
    public void handleBookingCreated(BookingResponse booking) {
        log.info("Placeholder: Received BookingCreated event for reference: {}", booking.getBookingReference());
    }

    @RabbitListener(queues = RabbitMQConfig.BOOKING_CANCELLED_QUEUE, autoStartup = "false")
    public void handleBookingCancelled(BookingResponse booking) {
        log.info("Placeholder: Received BookingCancelled event for reference: {}", booking.getBookingReference());
    }
}
