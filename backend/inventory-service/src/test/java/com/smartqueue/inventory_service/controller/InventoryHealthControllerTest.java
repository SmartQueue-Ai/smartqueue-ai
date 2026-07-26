package com.smartqueue.inventory_service.controller;

import com.smartqueue.inventory_service.filter.CorrelationIdFilter;
import com.smartqueue.inventory_service.filter.RequestLoggingFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = InventoryHealthController.class)
@Import({CorrelationIdFilter.class, RequestLoggingFilter.class})
@ActiveProfiles("test")
class InventoryHealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void ping_ShouldReturn200AndCorrelationId() throws Exception {
        mockMvc.perform(get("/api/v1/inventory/ping"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-ID"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.service").value("inventory-service"));
    }
}
