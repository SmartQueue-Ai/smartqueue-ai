package com.smartqueue.booking_service.dto;

import com.smartqueue.booking_service.entity.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Lightweight status check response payload")
public class BookingStatusResponse {

    @Schema(description = "Booking reference code", example = "BK-893A12BC")
    private String bookingReference;

    @Schema(description = "Current booking status", example = "CONFIRMED")
    private BookingStatus status;

    @Schema(description = "Status updated timestamp", example = "2026-07-27T00:30:00")
    private LocalDateTime updatedAt;
}
