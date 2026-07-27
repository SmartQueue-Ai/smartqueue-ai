package com.smartqueue.realtime_gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Realtime queue status update payload broadcasted to clients over WebSocket")
public class QueueUpdatePayload {

    @Schema(description = "1-based current position in queue", example = "3")
    private Integer queuePosition;

    @Schema(description = "Estimated wait time in seconds", example = "120")
    private Integer estimatedWaitTime;

    @Schema(description = "Resource status", example = "AVAILABLE")
    private String resourceStatus;

    @Schema(description = "Event or Resource ID", example = "res-12345")
    private String resourceId;

    @Schema(description = "User ID", example = "user-67890")
    private String userId;

    @Schema(description = "Event type triggering the update", example = "QUEUE_MOVED")
    private String eventType;

    @Schema(description = "Update creation timestamp", example = "2026-07-27T23:00:00Z")
    @Builder.Default
    private Instant timestamp = Instant.now();
}
