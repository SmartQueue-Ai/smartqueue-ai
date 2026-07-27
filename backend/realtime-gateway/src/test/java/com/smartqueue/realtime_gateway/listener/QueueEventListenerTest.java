package com.smartqueue.realtime_gateway.listener;

import com.smartqueue.realtime_gateway.dto.QueueUpdatePayload;
import com.smartqueue.realtime_gateway.event.BookingCancelledEvent;
import com.smartqueue.realtime_gateway.event.BookingCreatedEvent;
import com.smartqueue.realtime_gateway.event.QueueMovedEvent;
import com.smartqueue.realtime_gateway.publisher.RealtimePublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class QueueEventListenerTest {

    @Mock
    private RealtimePublisher realtimePublisher;

    private QueueEventListener queueEventListener;

    @BeforeEach
    void setUp() {
        queueEventListener = new QueueEventListener(realtimePublisher);
    }

    @Test
    @DisplayName("handleBookingCreatedEvent - Should process event and broadcast update payload")
    void handleBookingCreatedEvent() {
        UUID bookingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        BookingCreatedEvent event = BookingCreatedEvent.builder()
                .bookingId(bookingId)
                .bookingReference("BK-1001")
                .userId(userId)
                .eventId(eventId)
                .status("CONFIRMED")
                .createdAt(Instant.now())
                .build();

        queueEventListener.handleBookingCreatedEvent(event);

        ArgumentCaptor<QueueUpdatePayload> captor = ArgumentCaptor.forClass(QueueUpdatePayload.class);
        verify(realtimePublisher).broadcast(captor.capture());

        QueueUpdatePayload payload = captor.getValue();
        assertThat(payload).isNotNull();
        assertThat(payload.getQueuePosition()).isEqualTo(1);
        assertThat(payload.getResourceStatus()).isEqualTo("CONFIRMED");
        assertThat(payload.getResourceId()).isEqualTo(eventId.toString());
        assertThat(payload.getUserId()).isEqualTo(userId.toString());
    }

    @Test
    @DisplayName("handleBookingCancelledEvent - Should process event and broadcast update payload")
    void handleBookingCancelledEvent() {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        BookingCancelledEvent event = BookingCancelledEvent.builder()
                .bookingReference("BK-1002")
                .userId(userId)
                .eventId(eventId)
                .reason("User request")
                .cancelledAt(Instant.now())
                .build();

        queueEventListener.handleBookingCancelledEvent(event);

        ArgumentCaptor<QueueUpdatePayload> captor = ArgumentCaptor.forClass(QueueUpdatePayload.class);
        verify(realtimePublisher).broadcast(captor.capture());

        QueueUpdatePayload payload = captor.getValue();
        assertThat(payload).isNotNull();
        assertThat(payload.getQueuePosition()).isEqualTo(0);
        assertThat(payload.getResourceStatus()).isEqualTo("CANCELLED");
    }

    @Test
    @DisplayName("handleQueueMovedEvent - Should process event and broadcast new position payload")
    void handleQueueMovedEvent() {
        UUID userId = UUID.randomUUID();

        QueueMovedEvent event = QueueMovedEvent.builder()
                .resourceId("res-99")
                .userId(userId)
                .newPosition(5)
                .estimatedWaitTime(180)
                .resourceStatus("AVAILABLE")
                .timestamp(Instant.now())
                .build();

        queueEventListener.handleQueueMovedEvent(event);

        ArgumentCaptor<QueueUpdatePayload> captor = ArgumentCaptor.forClass(QueueUpdatePayload.class);
        verify(realtimePublisher).broadcast(captor.capture());

        QueueUpdatePayload payload = captor.getValue();
        assertThat(payload).isNotNull();
        assertThat(payload.getQueuePosition()).isEqualTo(5);
        assertThat(payload.getEstimatedWaitTime()).isEqualTo(180);
        assertThat(payload.getResourceStatus()).isEqualTo("AVAILABLE");
        assertThat(payload.getResourceId()).isEqualTo("res-99");
    }
}
