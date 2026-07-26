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
public class BookingConfirmedEvent {

    private UUID bookingId;
    private UUID userId;
    private String confirmationCode;
    private String userEmail;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
