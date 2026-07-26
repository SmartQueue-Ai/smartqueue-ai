package com.smartqueue.inventory_service.controller;

import com.smartqueue.inventory_service.dto.ApiResponse;
import com.smartqueue.inventory_service.dto.LockRequest;
import com.smartqueue.inventory_service.dto.LockResponse;
import com.smartqueue.inventory_service.dto.LockStatusResponse;
import com.smartqueue.inventory_service.service.ResourceLockService;
import com.smartqueue.inventory_service.util.CorrelationIdUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Resource Locking", description = "Redis-based distributed resource locking APIs")
public class ResourceLockController {

    private final ResourceLockService resourceLockService;

    @PostMapping({"/inventory/lock/{resourceId}", "/api/v1/inventory/lock/{resourceId}"})
    @Operation(summary = "Acquire distributed lock on a resource", description = "Uses Redis SETNX semantics to lock a resource for 5 minutes (300s)")
    public ResponseEntity<ApiResponse<LockResponse>> lockResource(
            @PathVariable("resourceId") String resourceId,
            @Valid @RequestBody(required = false) LockRequest request,
            @RequestParam(value = "userId", required = false) String queryUserId) {

        String correlationId = CorrelationIdUtil.getCurrentCorrelationId();
        String effectiveUserId = (request != null && StringUtils.hasText(request.getUserId())) ? request.getUserId() :
                (StringUtils.hasText(queryUserId) ? queryUserId : "anonymous-user");
        Long ttlSeconds = (request != null) ? request.getTtlSeconds() : null;

        LockResponse response = resourceLockService.lockResource(resourceId, effectiveUserId, ttlSeconds);
        return ResponseEntity.ok(ApiResponse.success(response, "Resource lock acquired successfully", correlationId));
    }

    @PostMapping({"/inventory/unlock/{resourceId}", "/api/v1/inventory/unlock/{resourceId}"})
    @Operation(summary = "Release distributed lock on a resource", description = "Deletes Redis lock key for the specified resourceId")
    public ResponseEntity<ApiResponse<String>> unlockResource(@PathVariable("resourceId") String resourceId) {
        String correlationId = CorrelationIdUtil.getCurrentCorrelationId();
        resourceLockService.unlockResource(resourceId);
        return ResponseEntity.ok(ApiResponse.success("Resource unlocked successfully", "Lock released", correlationId));
    }

    @GetMapping({"/inventory/status/{resourceId}", "/api/v1/inventory/status/{resourceId}"})
    @Operation(summary = "Get resource lock status", description = "Checks if resource is locked in Redis or automatically available")
    public ResponseEntity<ApiResponse<LockStatusResponse>> getLockStatus(@PathVariable("resourceId") String resourceId) {
        String correlationId = CorrelationIdUtil.getCurrentCorrelationId();
        LockStatusResponse response = resourceLockService.getLockStatus(resourceId);
        return ResponseEntity.ok(ApiResponse.success(response, "Lock status retrieved", correlationId));
    }
}
