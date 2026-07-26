package com.smartqueue.booking_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload to create a new reservation booking")
public class BookingRequest {

    @Schema(description = "User ID making the booking", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "User ID is required")
    private UUID userId;

    @Schema(description = "Event / Resource ID being reserved", example = "b1ffcd88-8b0a-3ef7-aa5c-5aa8ac270a22", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Event ID is required")
    private UUID eventId;

    @Schema(description = "Optional slot or seat identifier", example = "SEAT-A12")
    private String slotId;

    @Schema(description = "Quantity of slots/tickets reserved", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @Schema(description = "Total cost amount for reservation", example = "150.00")
    private BigDecimal totalAmount;
}
