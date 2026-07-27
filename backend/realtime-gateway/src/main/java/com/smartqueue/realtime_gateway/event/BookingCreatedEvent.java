package com.smartqueue.realtime_gateway.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreatedEvent {

    private UUID bookingId;
    private String bookingReference;
    private UUID userId;
    private UUID eventId;
    private String slotId;
    private String status;
    private Integer quantity;
    private BigDecimal totalAmount;
    private Instant createdAt;
}
