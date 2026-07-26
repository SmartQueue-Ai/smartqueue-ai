package com.smartqueue.booking_service.event;

import com.smartqueue.booking_service.entity.BookingStatus;
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
    private BookingStatus status;
    private Integer quantity;
    private BigDecimal totalAmount;
    private Instant createdAt;
}
