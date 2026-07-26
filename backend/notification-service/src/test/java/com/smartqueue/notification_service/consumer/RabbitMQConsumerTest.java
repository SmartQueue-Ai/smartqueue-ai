package com.smartqueue.notification_service.consumer;

import com.smartqueue.notification_service.event.BookingCancelledEvent;
import com.smartqueue.notification_service.event.BookingConfirmedEvent;
import com.smartqueue.notification_service.event.BookingCreatedEvent;
import com.smartqueue.notification_service.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitMQConsumerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private RabbitMQConsumer rabbitMQConsumer;

    @Test
    void handleBookingCreatedEvent_ShouldInvokeNotificationService() {
        BookingCreatedEvent event = BookingCreatedEvent.builder()
                .bookingId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .resourceId(UUID.randomUUID())
                .quantity(1)
                .build();

        rabbitMQConsumer.handleBookingCreatedEvent(event);

        verify(notificationService).processBookingCreated(event);
    }

    @Test
    void handleBookingCancelledEvent_ShouldInvokeNotificationService() {
        BookingCancelledEvent event = BookingCancelledEvent.builder()
                .bookingId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .reason("Test cancellation")
                .build();

        rabbitMQConsumer.handleBookingCancelledEvent(event);

        verify(notificationService).processBookingCancelled(event);
    }

    @Test
    void handleBookingConfirmedEvent_ShouldInvokeNotificationService() {
        BookingConfirmedEvent event = BookingConfirmedEvent.builder()
                .bookingId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .confirmationCode("CONF-999")
                .build();

        rabbitMQConsumer.handleBookingConfirmedEvent(event);

        verify(notificationService).processBookingConfirmed(event);
    }
}
