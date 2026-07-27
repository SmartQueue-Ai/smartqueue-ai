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
public class BookingCancelledEvent {

    private UUID bookingId;
    private String bookingReference;
    private UUID userId;
    private UUID eventId;
    private String reason;
    private Instant cancelledAt;
}
