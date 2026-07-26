package com.smartqueue.booking_service.controller;

import com.smartqueue.booking_service.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/booking")
@Tag(name = "Booking Health", description = "Booking Service health and diagnostic endpoints")
public class BookingHealthController {

    @GetMapping("/ping")
    @Operation(summary = "Ping health check", description = "Returns service connectivity status")
    public ResponseEntity<ApiResponse<Map<String, String>>> ping() {
        Map<String, String> status = Map.of(
                "service", "booking-service",
                "status", "UP"
        );
        return ResponseEntity.ok(ApiResponse.success(status));
    }
}
