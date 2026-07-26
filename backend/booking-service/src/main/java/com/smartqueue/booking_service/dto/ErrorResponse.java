package com.smartqueue.booking_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standardized error response payload")
public class ErrorResponse {

    @Schema(description = "HTTP status code", example = "404")
    private int status;

    @Schema(description = "Error category title", example = "Not Found")
    private String error;

    @Schema(description = "Detailed error message", example = "Booking not found with reference: BK-123456")
    private String message;

    @Schema(description = "Request URI path where error occurred", example = "/api/v1/booking/BK-123456")
    private String path;

    @Schema(description = "Error timestamp", example = "2026-07-27T00:00:00Z")
    @Builder.Default
    private Instant timestamp = Instant.now();

    @Schema(description = "Distributed request correlation ID", example = "sqai-12345-abcde")
    private String correlationId;

    @Schema(description = "Map of field validation errors (field -> message)")
    private Map<String, String> validationErrors;
}
