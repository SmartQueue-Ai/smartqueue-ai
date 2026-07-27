package com.smartqueue.realtime_gateway.controller;

import com.smartqueue.realtime_gateway.dto.ApiResponse;
import com.smartqueue.realtime_gateway.dto.QueueUpdatePayload;
import com.smartqueue.realtime_gateway.publisher.RealtimePublisher;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Realtime Queue Update Controller", description = "WebSocket STOMP and REST APIs for Realtime Queue Status Updates")
public class QueueUpdateController {

    private final RealtimePublisher realtimePublisher;

    @MessageMapping("/queue/update")
    @SendTo("/topic/queue/updates")
    public QueueUpdatePayload handleQueueUpdateMessage(QueueUpdatePayload payload) {
        log.info("STOMP inbound message received for resourceId: {}, position: {}",
                payload.getResourceId(), payload.getQueuePosition());
        realtimePublisher.broadcast(payload);
        return payload;
    }

    @MessageMapping("/queue/{resourceId}")
    @SendTo("/queue/{resourceId}")
    public QueueUpdatePayload handleResourceQueueMessage(@DestinationVariable String resourceId, QueueUpdatePayload payload) {
        log.info("STOMP inbound message received for resourceId: {}", resourceId);
        payload.setResourceId(resourceId);
        realtimePublisher.publishQueueUpdateToResource(resourceId, payload);
        return payload;
    }

    @PostMapping({"/api/v1/realtime/trigger-update", "/realtime/trigger-update"})
    @Operation(summary = "Trigger realtime queue update broadcast", description = "REST endpoint to manually trigger a queue update payload broadcast over WebSocket STOMP")
    public ResponseEntity<ApiResponse<QueueUpdatePayload>> triggerQueueUpdate(@Valid @RequestBody QueueUpdatePayload payload) {
        log.info("REST Request to trigger realtime queue update for resourceId: {}, userId: {}",
                payload.getResourceId(), payload.getUserId());
        realtimePublisher.broadcast(payload);
        return ResponseEntity.ok(ApiResponse.success(payload, "Realtime queue update broadcasted successfully"));
    }

    @GetMapping({"/api/v1/realtime/ping", "/realtime/ping"})
    @Operation(summary = "Realtime Gateway health check", description = "Returns WebSocket service connectivity status")
    public ResponseEntity<ApiResponse<Map<String, String>>> ping() {
        Map<String, String> status = Map.of(
                "service", "realtime-gateway",
                "websocket", "UP"
        );
        return ResponseEntity.ok(ApiResponse.success(status));
    }
}
