package com.smartqueue.notification_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreatedEvent {

    private UUID bookingId;
    private UUID userId;
    private UUID resourceId;
    private Integer quantity;
    private String userEmail;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
