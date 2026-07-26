package com.smartqueue.booking_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standardized API response wrapper")
public class ApiResponse<T> {

    @Schema(description = "Success flag", example = "true")
    private boolean success;

    @Schema(description = "Response message description", example = "Success")
    private String message;

    @Schema(description = "Response payload object")
    private T data;

    @Schema(description = "Response creation timestamp", example = "2026-07-27T00:00:00Z")
    @Builder.Default
    private Instant timestamp = Instant.now();

    @Schema(description = "Distributed request correlation ID", example = "sqai-12345-abcde")
    private String correlationId;

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Success", null);
    }

    public static <T> ApiResponse<T> success(T data, String correlationId) {
        return success(data, "Success", correlationId);
    }

    public static <T> ApiResponse<T> success(T data, String message, String correlationId) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .correlationId(correlationId)
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }
}
