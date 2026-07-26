package com.smartqueue.booking_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartqueue.booking_service.config.SecurityConfig;
import com.smartqueue.booking_service.dto.BookingRequest;
import com.smartqueue.booking_service.dto.BookingResponse;
import com.smartqueue.booking_service.entity.BookingStatus;
import com.smartqueue.booking_service.exception.BookingNotFoundException;
import com.smartqueue.booking_service.exception.GlobalExceptionHandler;
import com.smartqueue.booking_service.exception.InventoryLockException;
import com.smartqueue.booking_service.filter.CorrelationIdFilter;
import com.smartqueue.booking_service.filter.RequestLoggingFilter;
import com.smartqueue.booking_service.service.BookingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
@Import({SecurityConfig.class, CorrelationIdFilter.class, RequestLoggingFilter.class, GlobalExceptionHandler.class})
@ActiveProfiles("test")
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @Test
    @DisplayName("POST /bookings - Should create booking and return 201 Created")
    void createBooking_ShouldReturn201Created() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();

        BookingRequest request = BookingRequest.builder()
                .userId(userId)
                .eventId(eventId)
                .slotId("SLOT-A1")
                .quantity(2)
                .totalAmount(new BigDecimal("100.00"))
                .build();

        BookingResponse responseDto = BookingResponse.builder()
                .id(bookingId)
                .bookingReference("BK-12345678")
                .userId(userId)
                .eventId(eventId)
                .slotId("SLOT-A1")
                .status(BookingStatus.CONFIRMED)
                .quantity(2)
                .totalAmount(new BigDecimal("100.00"))
                .build();

        when(bookingService.createBooking(any(BookingRequest.class))).thenReturn(responseDto);

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("X-Correlation-ID"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bookingReference").value("BK-12345678"))
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("POST /bookings - Should return 409 Conflict on inventory lock failure")
    void createBooking_ShouldReturn409ConflictOnInventoryLockFailure() throws Exception {
        BookingRequest request = BookingRequest.builder()
                .userId(UUID.randomUUID())
                .eventId(UUID.randomUUID())
                .quantity(1)
                .build();

        when(bookingService.createBooking(any(BookingRequest.class)))
                .thenThrow(new InventoryLockException("Unable to acquire inventory lock for event/resource ID"));

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Unable to acquire inventory lock for event/resource ID"));
    }

    @Test
    @DisplayName("GET /bookings/{id} - Should return 200 OK and booking details")
    void getBookingById_ShouldReturn200OK() throws Exception {
        UUID bookingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        BookingResponse responseDto = BookingResponse.builder()
                .id(bookingId)
                .bookingReference("BK-88888888")
                .userId(userId)
                .status(BookingStatus.CONFIRMED)
                .build();

        when(bookingService.getBookingById(bookingId)).thenReturn(responseDto);

        mockMvc.perform(get("/bookings/" + bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bookingReference").value("BK-88888888"));
    }

    @Test
    @DisplayName("GET /bookings/{id} - Should return 404 Not Found when booking does not exist")
    void getBookingById_ShouldReturn404NotFound() throws Exception {
        UUID bookingId = UUID.randomUUID();

        when(bookingService.getBookingById(bookingId))
                .thenThrow(new BookingNotFoundException("Booking not found with ID: " + bookingId));

        mockMvc.perform(get("/bookings/" + bookingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("GET /bookings/user/{userId} - Should return list of user bookings")
    void getBookingsByUserId_ShouldReturn200OK() throws Exception {
        UUID userId = UUID.randomUUID();

        BookingResponse responseDto = BookingResponse.builder()
                .id(UUID.randomUUID())
                .bookingReference("BK-USER123")
                .userId(userId)
                .status(BookingStatus.CONFIRMED)
                .build();

        when(bookingService.getBookingsByUserId(userId)).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/bookings/user/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].bookingReference").value("BK-USER123"));
    }
}
