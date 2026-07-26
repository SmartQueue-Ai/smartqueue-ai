package com.smartqueue.booking_service.mapper;

import com.smartqueue.booking_service.entity.Booking;
import com.smartqueue.booking_service.entity.BookingStatus;
import com.smartqueue.booking_service.dto.BookingRequest;
import com.smartqueue.booking_service.dto.BookingResponse;
import com.smartqueue.booking_service.dto.BookingStatusResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Component
public class BookingMapper {

    public Booking toEntity(BookingRequest request, String bookingReference) {
        if (request == null) {
            return null;
        }

        return Booking.builder()
                .bookingReference(bookingReference)
                .userId(request.getUserId())
                .eventId(request.getEventId())
                .slotId(request.getSlotId())
                .status(BookingStatus.PENDING)
                .quantity(request.getQuantity() != null ? request.getQuantity() : 1)
                .totalAmount(request.getTotalAmount() != null ? request.getTotalAmount() : BigDecimal.ZERO)
                .expiresAt(Instant.now().plus(15, ChronoUnit.MINUTES))
                .build();
    }

    public BookingResponse toResponse(Booking booking) {
        if (booking == null) {
            return null;
        }

        return BookingResponse.builder()
                .id(booking.getId())
                .bookingReference(booking.getBookingReference())
                .userId(booking.getUserId())
                .eventId(booking.getEventId())
                .slotId(booking.getSlotId())
                .status(booking.getStatus())
                .quantity(booking.getQuantity())
                .totalAmount(booking.getTotalAmount())
                .expiresAt(booking.getExpiresAt())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }

    public BookingStatusResponse toStatusResponse(Booking booking) {
        if (booking == null) {
            return null;
        }

        return BookingStatusResponse.builder()
                .bookingReference(booking.getBookingReference())
                .status(booking.getStatus())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
