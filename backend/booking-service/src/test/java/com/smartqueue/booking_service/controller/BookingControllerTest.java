package com.smartqueue.booking_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartqueue.booking_service.config.SecurityConfig;
import com.smartqueue.booking_service.dto.BookingRequest;
import com.smartqueue.booking_service.dto.BookingResponse;
import com.smartqueue.booking_service.entity.Booking;
import com.smartqueue.booking_service.entity.BookingStatus;
import com.smartqueue.booking_service.exception.GlobalExceptionHandler;
import com.smartqueue.booking_service.filter.CorrelationIdFilter;
import com.smartqueue.booking_service.filter.RequestLoggingFilter;
import com.smartqueue.booking_service.mapper.BookingMapper;
import com.smartqueue.booking_service.publisher.BookingEventPublisher;
import com.smartqueue.booking_service.repository.BookingAuditRepository;
import com.smartqueue.booking_service.repository.BookingRepository;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private BookingRepository bookingRepository;

    @MockBean
    private BookingAuditRepository bookingAuditRepository;

    @MockBean
    private BookingMapper bookingMapper;

    @MockBean
    private BookingEventPublisher bookingEventPublisher;

    @Test
    @DisplayName("POST /api/v1/booking - Should create booking and return 201 Created")
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

        Booking bookingEntity = Booking.builder()
                .id(bookingId)
                .bookingReference("BK-12345678")
                .userId(userId)
                .eventId(eventId)
                .slotId("SLOT-A1")
                .status(BookingStatus.PENDING)
                .quantity(2)
                .totalAmount(new BigDecimal("100.00"))
                .expiresAt(Instant.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        BookingResponse responseDto = BookingResponse.builder()
                .id(bookingId)
                .bookingReference("BK-12345678")
                .userId(userId)
                .eventId(eventId)
                .slotId("SLOT-A1")
                .status(BookingStatus.PENDING)
                .quantity(2)
                .totalAmount(new BigDecimal("100.00"))
                .build();

        when(bookingMapper.toEntity(any(BookingRequest.class), any(String.class))).thenReturn(bookingEntity);
        when(bookingRepository.save(any(Booking.class))).thenReturn(bookingEntity);
        when(bookingMapper.toResponse(any(Booking.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("X-Correlation-ID"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bookingReference").value("BK-12345678"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /api/v1/booking/{id} - Should return booking details")
    void getBookingById_ShouldReturn200OK() throws Exception {
        UUID bookingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Booking bookingEntity = Booking.builder()
                .id(bookingId)
                .bookingReference("BK-88888888")
                .userId(userId)
                .eventId(UUID.randomUUID())
                .status(BookingStatus.CONFIRMED)
                .build();

        BookingResponse responseDto = BookingResponse.builder()
                .id(bookingId)
                .bookingReference("BK-88888888")
                .userId(userId)
                .status(BookingStatus.CONFIRMED)
                .build();

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(bookingEntity));
        when(bookingMapper.toResponse(bookingEntity)).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/booking/" + bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bookingReference").value("BK-88888888"));
    }
}
