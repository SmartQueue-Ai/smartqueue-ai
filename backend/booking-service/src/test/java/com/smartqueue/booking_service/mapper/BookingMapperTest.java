package com.smartqueue.booking_service.mapper;

import com.smartqueue.booking_service.dto.BookingRequest;
import com.smartqueue.booking_service.dto.BookingResponse;
import com.smartqueue.booking_service.dto.BookingStatusResponse;
import com.smartqueue.booking_service.entity.Booking;
import com.smartqueue.booking_service.entity.BookingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BookingMapperTest {

    private BookingMapper bookingMapper;

    @BeforeEach
    void setUp() {
        bookingMapper = new BookingMapper();
    }

    @Test
    @DisplayName("Should map BookingRequest to Booking entity")
    void toEntity() {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        BookingRequest request = BookingRequest.builder()
                .userId(userId)
                .eventId(eventId)
                .slotId("SLOT-A")
                .quantity(3)
                .totalAmount(new BigDecimal("299.99"))
                .build();

        Booking entity = bookingMapper.toEntity(request, "BK-REF123");

        assertThat(entity).isNotNull();
        assertThat(entity.getBookingReference()).isEqualTo("BK-REF123");
        assertThat(entity.getUserId()).isEqualTo(userId);
        assertThat(entity.getEventId()).isEqualTo(eventId);
        assertThat(entity.getSlotId()).isEqualTo("SLOT-A");
        assertThat(entity.getStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(entity.getQuantity()).isEqualTo(3);
        assertThat(entity.getTotalAmount()).isEqualTo(new BigDecimal("299.99"));
        assertThat(entity.getExpiresAt()).isNotNull();
    }

    @Test
    @DisplayName("Should map Booking entity to BookingResponse DTO")
    void toResponse() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Booking entity = Booking.builder()
                .id(id)
                .bookingReference("BK-REF456")
                .userId(userId)
                .eventId(eventId)
                .slotId("SLOT-B")
                .status(BookingStatus.CONFIRMED)
                .quantity(1)
                .totalAmount(new BigDecimal("50.00"))
                .expiresAt(Instant.now())
                .createdAt(now)
                .updatedAt(now)
                .build();

        BookingResponse response = bookingMapper.toResponse(entity);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getBookingReference()).isEqualTo("BK-REF456");
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Should map Booking entity to BookingStatusResponse DTO")
    void toStatusResponse() {
        LocalDateTime now = LocalDateTime.now();

        Booking entity = Booking.builder()
                .bookingReference("BK-REF789")
                .status(BookingStatus.CANCELLED)
                .updatedAt(now)
                .build();

        BookingStatusResponse statusResponse = bookingMapper.toStatusResponse(entity);

        assertThat(statusResponse).isNotNull();
        assertThat(statusResponse.getBookingReference()).isEqualTo("BK-REF789");
        assertThat(statusResponse.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(statusResponse.getUpdatedAt()).isEqualTo(now);
    }
}
