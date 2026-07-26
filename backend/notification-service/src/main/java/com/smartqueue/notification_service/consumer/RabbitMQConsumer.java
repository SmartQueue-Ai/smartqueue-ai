package com.smartqueue.notification_service.consumer;

import com.smartqueue.notification_service.config.RabbitMQConfig;
import com.smartqueue.notification_service.event.BookingCancelledEvent;
import com.smartqueue.notification_service.event.BookingConfirmedEvent;
import com.smartqueue.notification_service.event.BookingCreatedEvent;
import com.smartqueue.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_BOOKING_CREATED)
    public void handleBookingCreatedEvent(BookingCreatedEvent event) {
        log.info("Received BookingCreatedEvent from RabbitMQ: bookingId [{}]", event.getBookingId());
        notificationService.processBookingCreated(event);
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_BOOKING_CANCELLED)
    public void handleBookingCancelledEvent(BookingCancelledEvent event) {
        log.info("Received BookingCancelledEvent from RabbitMQ: bookingId [{}]", event.getBookingId());
        notificationService.processBookingCancelled(event);
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_BOOKING_CONFIRMED)
    public void handleBookingConfirmedEvent(BookingConfirmedEvent event) {
        log.info("Received BookingConfirmedEvent from RabbitMQ: bookingId [{}]", event.getBookingId());
        notificationService.processBookingConfirmed(event);
    }
}
