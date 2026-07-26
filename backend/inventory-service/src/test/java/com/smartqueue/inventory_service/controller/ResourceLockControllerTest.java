package com.smartqueue.inventory_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartqueue.inventory_service.dto.LockRequest;
import com.smartqueue.inventory_service.dto.LockResponse;
import com.smartqueue.inventory_service.dto.LockStatusResponse;
import com.smartqueue.inventory_service.exception.GlobalExceptionHandler;
import com.smartqueue.inventory_service.exception.LockAcquisitionException;
import com.smartqueue.inventory_service.filter.CorrelationIdFilter;
import com.smartqueue.inventory_service.filter.RequestLoggingFilter;
import com.smartqueue.inventory_service.service.ResourceLockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ResourceLockController.class)
@Import({CorrelationIdFilter.class, RequestLoggingFilter.class, GlobalExceptionHandler.class})
@ActiveProfiles("test")
class ResourceLockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ResourceLockService resourceLockService;

    @Test
    void lockResource_WhenAvailable_ShouldReturn200OK() throws Exception {
        String resourceId = "res-200";
        LockRequest lockRequest = LockRequest.builder().userId("user-10").build();

        LockResponse lockResponse = LockResponse.builder()
                .resourceId(resourceId)
                .userId("user-10")
                .lockKey("smartqueue:lock:res-200")
                .isLocked(true)
                .ttlRemainingSeconds(300L)
                .build();

        when(resourceLockService.lockResource(eq(resourceId), eq("user-10"), nullable(Long.class))).thenReturn(lockResponse);

        mockMvc.perform(post("/inventory/lock/{resourceId}", resourceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lockRequest)))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-ID"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.lockKey").value("smartqueue:lock:res-200"))
                .andExpect(jsonPath("$.data.isLocked").value(true));
    }

    @Test
    void lockResource_WhenAlreadyLocked_ShouldReturn409Conflict() throws Exception {
        String resourceId = "res-200";
        LockRequest lockRequest = LockRequest.builder().userId("user-20").build();

        when(resourceLockService.lockResource(eq(resourceId), eq("user-20"), nullable(Long.class)))
                .thenThrow(new LockAcquisitionException("Resource res-200 is already locked by another user"));

        mockMvc.perform(post("/inventory/lock/{resourceId}", resourceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lockRequest)))
                .andExpect(status().isConflict())
                .andExpect(header().exists("X-Correlation-ID"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Resource res-200 is already locked by another user"));
    }

    @Test
    void unlockResource_ShouldReturn200OK() throws Exception {
        String resourceId = "res-200";
        when(resourceLockService.unlockResource(resourceId)).thenReturn(true);

        mockMvc.perform(post("/inventory/unlock/{resourceId}", resourceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getLockStatus_ShouldReturn200OK() throws Exception {
        String resourceId = "res-200";
        LockStatusResponse statusResponse = LockStatusResponse.builder()
                .resourceId(resourceId)
                .isLocked(true)
                .userId("user-10")
                .ttlRemainingSeconds(250L)
                .build();

        when(resourceLockService.getLockStatus(resourceId)).thenReturn(statusResponse);

        mockMvc.perform(get("/inventory/status/{resourceId}", resourceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isLocked").value(true))
                .andExpect(jsonPath("$.data.userId").value("user-10"));
    }
}
