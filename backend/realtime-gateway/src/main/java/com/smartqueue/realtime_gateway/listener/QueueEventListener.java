package com.smartqueue.realtime_gateway.listener;

import com.smartqueue.realtime_gateway.config.RabbitMQConfig;
import com.smartqueue.realtime_gateway.dto.QueueUpdatePayload;
import com.smartqueue.realtime_gateway.event.BookingCancelledEvent;
import com.smartqueue.realtime_gateway.event.BookingCreatedEvent;
import com.smartqueue.realtime_gateway.event.QueueMovedEvent;
import com.smartqueue.realtime_gateway.publisher.RealtimePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueEventListener {

    private final RealtimePublisher realtimePublisher;

    @RabbitListener(queues = RabbitMQConfig.REALTIME_BOOKING_CREATED_QUEUE, autoStartup = "false")
    public void handleBookingCreatedEvent(BookingCreatedEvent event) {
        log.info("Received BookingCreatedEvent for reference: {}, userId: {}, resourceId: {}",
                event.getBookingReference(), event.getUserId(), event.getEventId());

        String resourceId = event.getEventId() != null ? event.getEventId().toString() : "unknown-resource";
        String userId = event.getUserId() != null ? event.getUserId().toString() : "unknown-user";

        QueueUpdatePayload payload = QueueUpdatePayload.builder()
                .queuePosition(1)
                .estimatedWaitTime(0)
                .resourceStatus("CONFIRMED")
                .resourceId(resourceId)
                .userId(userId)
                .eventType("BOOKING_CREATED")
                .timestamp(event.getCreatedAt() != null ? event.getCreatedAt() : Instant.now())
                .build();

        realtimePublisher.broadcast(payload);
    }

    @RabbitListener(queues = RabbitMQConfig.REALTIME_BOOKING_CANCELLED_QUEUE, autoStartup = "false")
    public void handleBookingCancelledEvent(BookingCancelledEvent event) {
        log.info("Received BookingCancelledEvent for reference: {}, userId: {}",
                event.getBookingReference(), event.getUserId());

        String resourceId = event.getEventId() != null ? event.getEventId().toString() : "unknown-resource";
        String userId = event.getUserId() != null ? event.getUserId().toString() : "unknown-user";

        QueueUpdatePayload payload = QueueUpdatePayload.builder()
                .queuePosition(0)
                .estimatedWaitTime(0)
                .resourceStatus("CANCELLED")
                .resourceId(resourceId)
                .userId(userId)
                .eventType("BOOKING_CANCELLED")
                .timestamp(event.getCancelledAt() != null ? event.getCancelledAt() : Instant.now())
                .build();

        realtimePublisher.broadcast(payload);
    }

    @RabbitListener(queues = RabbitMQConfig.REALTIME_QUEUE_MOVED_QUEUE, autoStartup = "false")
    public void handleQueueMovedEvent(QueueMovedEvent event) {
        log.info("Received QueueMovedEvent for resourceId: {}, newPosition: {}",
                event.getResourceId(), event.getNewPosition());

        String resourceId = event.getResourceId() != null ? event.getResourceId() :
                (event.getEventId() != null ? event.getEventId().toString() : "unknown-resource");
        String userId = event.getUserId() != null ? event.getUserId().toString() : null;

        QueueUpdatePayload payload = QueueUpdatePayload.builder()
                .queuePosition(event.getNewPosition() != null ? event.getNewPosition() : 1)
                .estimatedWaitTime(event.getEstimatedWaitTime() != null ? event.getEstimatedWaitTime() : 60)
                .resourceStatus(event.getResourceStatus() != null ? event.getResourceStatus() : "AVAILABLE")
                .resourceId(resourceId)
                .userId(userId)
                .eventType("QUEUE_MOVED")
                .timestamp(event.getTimestamp() != null ? event.getTimestamp() : Instant.now())
                .build();

        realtimePublisher.broadcast(payload);
    }
}
