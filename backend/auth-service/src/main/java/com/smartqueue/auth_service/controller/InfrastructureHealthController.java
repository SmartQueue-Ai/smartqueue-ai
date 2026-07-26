package com.smartqueue.auth_service.controller;

import com.smartqueue.auth_service.dto.ApiResponse;
import com.smartqueue.auth_service.util.CorrelationIdUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth Infrastructure", description = "Public health and infrastructure endpoints")
public class InfrastructureHealthController {

    @GetMapping("/ping")
    @Operation(summary = "Ping Auth Service infrastructure", description = "Verifies Auth Service infrastructure and correlation ID pipeline")
    public ResponseEntity<ApiResponse<Map<String, String>>> ping() {
        String correlationId = CorrelationIdUtil.getCurrentCorrelationId();
        Map<String, String> statusData = Map.of(
                "status", "UP",
                "service", "auth-service",
                "framework", "Spring Boot 3.5"
        );

        return ResponseEntity.ok(ApiResponse.success(statusData, "Auth service infrastructure operational", correlationId));
    }
}
