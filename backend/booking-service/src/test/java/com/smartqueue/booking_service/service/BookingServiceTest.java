package com.smartqueue.booking_service.service;

import com.smartqueue.booking_service.client.BookingClient;
import com.smartqueue.booking_service.dto.BookingRequest;
import com.smartqueue.booking_service.dto.BookingResponse;
import com.smartqueue.booking_service.entity.Booking;
import com.smartqueue.booking_service.entity.BookingAudit;
import com.smartqueue.booking_service.entity.BookingStatus;
import com.smartqueue.booking_service.event.BookingCreatedEvent;
import com.smartqueue.booking_service.exception.BookingNotFoundException;
import com.smartqueue.booking_service.exception.InventoryLockException;
import com.smartqueue.booking_service.mapper.BookingMapper;
import com.smartqueue.booking_service.publisher.BookingEventPublisher;
import com.smartqueue.booking_service.repository.BookingAuditRepository;
import com.smartqueue.booking_service.repository.BookingRepository;
import com.smartqueue.booking_service.service.impl.BookingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingAuditRepository bookingAuditRepository;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private BookingClient bookingClient;

    @Mock
    private BookingEventPublisher bookingEventPublisher;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private UUID userId;
    private UUID eventId;
    private UUID bookingId;
    private BookingRequest bookingRequest;
    private Booking bookingEntity;
    private BookingResponse bookingResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        eventId = UUID.randomUUID();
        bookingId = UUID.randomUUID();

        bookingRequest = BookingRequest.builder()
                .userId(userId)
                .eventId(eventId)
                .slotId("SLOT-101")
                .quantity(2)
                .totalAmount(new BigDecimal("199.99"))
                .build();

        bookingEntity = Booking.builder()
                .id(bookingId)
                .bookingReference("BK-TEST9999")
                .userId(userId)
                .eventId(eventId)
                .slotId("SLOT-101")
                .status(BookingStatus.CONFIRMED)
                .quantity(2)
                .totalAmount(new BigDecimal("199.99"))
                .expiresAt(Instant.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        bookingResponse = BookingResponse.builder()
                .id(bookingId)
                .bookingReference("BK-TEST9999")
                .userId(userId)
                .eventId(eventId)
                .slotId("SLOT-101")
                .status(BookingStatus.CONFIRMED)
                .quantity(2)
                .totalAmount(new BigDecimal("199.99"))
                .build();
    }

    @Test
    @DisplayName("createBooking - Should successfully acquire inventory lock, save booking, and publish event")
    void createBooking_Success() {
        when(bookingClient.acquireInventoryLock(eq(eventId.toString()), eq(userId.toString()), anyLong()))
                .thenReturn(true);
        when(bookingMapper.toEntity(any(BookingRequest.class), any(String.class)))
                .thenReturn(bookingEntity);
        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(bookingEntity);
        when(bookingMapper.toResponse(any(Booking.class)))
                .thenReturn(bookingResponse);

        BookingResponse response = bookingService.createBooking(bookingRequest);

        assertThat(response).isNotNull();
        assertThat(response.getBookingReference()).isEqualTo("BK-TEST9999");
        assertThat(response.getStatus()).isEqualTo(BookingStatus.CONFIRMED);

        verify(bookingClient).acquireInventoryLock(eq(eventId.toString()), eq(userId.toString()), eq(300L));
        verify(bookingRepository).save(any(Booking.class));
        verify(bookingAuditRepository).save(any(BookingAudit.class));
        verify(bookingEventPublisher).publishBookingCreatedEvent(any(BookingCreatedEvent.class));
    }

    @Test
    @DisplayName("createBooking - Should throw InventoryLockException when inventory lock fails")
    void createBooking_InventoryLockFailure() {
        when(bookingClient.acquireInventoryLock(eq(eventId.toString()), eq(userId.toString()), anyLong()))
                .thenReturn(false);

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequest))
                .isInstanceOf(InventoryLockException.class)
                .hasMessageContaining("Unable to acquire inventory lock");

        verify(bookingRepository, never()).save(any());
        verify(bookingAuditRepository, never()).save(any());
        verify(bookingEventPublisher, never()).publishBookingCreatedEvent(any());
    }

    @Test
    @DisplayName("getBookingById - Should return booking details when found")
    void getBookingById_Success() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(bookingEntity));
        when(bookingMapper.toResponse(bookingEntity)).thenReturn(bookingResponse);

        BookingResponse response = bookingService.getBookingById(bookingId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(bookingId);
    }

    @Test
    @DisplayName("getBookingById - Should throw BookingNotFoundException when not found")
    void getBookingById_NotFound() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingById(bookingId))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessageContaining("Booking not found with ID");
    }
}
