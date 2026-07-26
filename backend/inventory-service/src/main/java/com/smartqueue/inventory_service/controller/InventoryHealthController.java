package com.smartqueue.inventory_service.controller;

import com.smartqueue.inventory_service.dto.ApiResponse;
import com.smartqueue.inventory_service.util.CorrelationIdUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/inventory")
@Tag(name = "Inventory Infrastructure", description = "Public health and infrastructure endpoints")
public class InventoryHealthController {

    @GetMapping("/ping")
    @Operation(summary = "Ping Inventory Service", description = "Verifies Inventory Service infrastructure and correlation ID pipeline")
    public ResponseEntity<ApiResponse<Map<String, String>>> ping() {
        String correlationId = CorrelationIdUtil.getCurrentCorrelationId();
        Map<String, String> statusData = Map.of(
                "status", "UP",
                "service", "inventory-service",
                "framework", "Spring Boot 3.5"
        );

        return ResponseEntity.ok(ApiResponse.success(statusData, "Inventory service infrastructure operational", correlationId));
    }
}
