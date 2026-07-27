package com.smartqueue.realtime_gateway.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueMovedEvent {

    private UUID eventId;
    private String resourceId;
    private UUID userId;
    private Integer newPosition;
    private Integer estimatedWaitTime;
    private String resourceStatus;
    private Instant timestamp;
}
