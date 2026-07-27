package com.smartqueue.realtime_gateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartqueue.realtime_gateway.config.SecurityConfig;
import com.smartqueue.realtime_gateway.dto.QueueUpdatePayload;
import com.smartqueue.realtime_gateway.exception.GlobalExceptionHandler;
import com.smartqueue.realtime_gateway.filter.CorrelationIdFilter;
import com.smartqueue.realtime_gateway.filter.RequestLoggingFilter;
import com.smartqueue.realtime_gateway.publisher.RealtimePublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = QueueUpdateController.class)
@Import({SecurityConfig.class, CorrelationIdFilter.class, RequestLoggingFilter.class, GlobalExceptionHandler.class})
@ActiveProfiles("test")
class QueueUpdateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RealtimePublisher realtimePublisher;

    @Test
    @DisplayName("GET /api/v1/realtime/ping - Should return 200 OK and health status")
    void ping_ShouldReturn200OK() throws Exception {
        mockMvc.perform(get("/api/v1/realtime/ping"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-ID"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.service").value("realtime-gateway"))
                .andExpect(jsonPath("$.data.websocket").value("UP"));
    }

    @Test
    @DisplayName("POST /api/v1/realtime/trigger-update - Should trigger update and return 200 OK")
    void triggerUpdate_ShouldReturn200OK() throws Exception {
        QueueUpdatePayload payload = QueueUpdatePayload.builder()
                .queuePosition(2)
                .estimatedWaitTime(90)
                .resourceStatus("BUSY")
                .resourceId("res-101")
                .userId("user-202")
                .timestamp(Instant.now())
                .build();

        doNothing().when(realtimePublisher).broadcast(any(QueueUpdatePayload.class));

        mockMvc.perform(post("/api/v1/realtime/trigger-update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.queuePosition").value(2))
                .andExpect(jsonPath("$.data.estimatedWaitTime").value(90))
                .andExpect(jsonPath("$.data.resourceStatus").value("BUSY"));
    }
}
