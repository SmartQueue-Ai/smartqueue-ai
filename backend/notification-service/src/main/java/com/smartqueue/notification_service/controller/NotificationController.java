package com.smartqueue.notification_service.controller;

import com.smartqueue.notification_service.dto.ApiResponse;
import com.smartqueue.notification_service.entity.NotificationHistory;
import com.smartqueue.notification_service.service.NotificationService;
import com.smartqueue.notification_service.util.CorrelationIdUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Infrastructure", description = "Notification history and dispatch status endpoints")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/ping")
    @Operation(summary = "Ping Notification Service", description = "Verifies Notification Service status and correlation ID pipeline")
    public ResponseEntity<ApiResponse<Map<String, String>>> ping() {
        String correlationId = CorrelationIdUtil.getCurrentCorrelationId();
        Map<String, String> statusData = Map.of(
                "status", "UP",
                "service", "notification-service",
                "framework", "Spring Boot 3.5"
        );

        return ResponseEntity.ok(ApiResponse.success(statusData, "Notification service infrastructure operational", correlationId));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get User Notification History", description = "Retrieves all notification records for a specific user ID")
    public ResponseEntity<ApiResponse<List<NotificationHistory>>> getUserNotifications(@PathVariable("userId") UUID userId) {
        String correlationId = CorrelationIdUtil.getCurrentCorrelationId();
        List<NotificationHistory> history = notificationService.getNotificationHistory(userId);
        return ResponseEntity.ok(ApiResponse.success(history, "Notification history fetched", correlationId));
    }
}
