package com.smartqueue.booking_service.dto;

import com.smartqueue.booking_service.entity.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Booking reservation response details")
public class BookingResponse {

    @Schema(description = "Unique Booking ID (UUID)", example = "c2ffde77-7a09-2ef6-994b-4aa7ab160a33")
    private UUID id;

    @Schema(description = "Unique human-readable booking reference code", example = "BK-893A12BC")
    private String bookingReference;

    @Schema(description = "User ID who owns this booking", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
    private UUID userId;

    @Schema(description = "Event / Resource ID reserved", example = "b1ffcd88-8b0a-3ef7-aa5c-5aa8ac270a22")
    private UUID eventId;

    @Schema(description = "Slot or seat identifier", example = "SEAT-A12")
    private String slotId;

    @Schema(description = "Current booking status", example = "PENDING")
    private BookingStatus status;

    @Schema(description = "Quantity reserved", example = "2")
    private Integer quantity;

    @Schema(description = "Total booking amount", example = "150.00")
    private BigDecimal totalAmount;

    @Schema(description = "Booking reservation expiration timestamp", example = "2026-07-27T01:00:00Z")
    private Instant expiresAt;

    @Schema(description = "Booking creation timestamp", example = "2026-07-27T00:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Booking last updated timestamp", example = "2026-07-27T00:30:00")
    private LocalDateTime updatedAt;
}
