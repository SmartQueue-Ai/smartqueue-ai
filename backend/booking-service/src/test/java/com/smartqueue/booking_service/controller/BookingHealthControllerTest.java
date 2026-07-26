package com.smartqueue.booking_service.controller;

import com.smartqueue.booking_service.config.SecurityConfig;
import com.smartqueue.booking_service.filter.CorrelationIdFilter;
import com.smartqueue.booking_service.filter.RequestLoggingFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingHealthController.class)
@Import({SecurityConfig.class, CorrelationIdFilter.class, RequestLoggingFilter.class})
@ActiveProfiles("test")
class BookingHealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/v1/booking/ping - Should return 200 OK and health status")
    void ping_ShouldReturn200OK() throws Exception {
        mockMvc.perform(get("/api/v1/booking/ping")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-ID"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.service").value("booking-service"))
                .andExpect(jsonPath("$.data.status").value("UP"));
    }
}
