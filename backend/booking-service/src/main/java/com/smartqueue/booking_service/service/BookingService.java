package com.smartqueue.booking_service.service;

import com.smartqueue.booking_service.dto.BookingRequest;
import com.smartqueue.booking_service.dto.BookingResponse;
import com.smartqueue.booking_service.dto.BookingStatusResponse;

import java.util.List;
import java.util.UUID;

public interface BookingService {

    BookingResponse createBooking(BookingRequest request);

    BookingResponse getBookingById(UUID id);

    BookingResponse getBookingByReference(String bookingReference);

    List<BookingResponse> getBookingsByUserId(UUID userId);

    BookingStatusResponse getBookingStatus(UUID id);
}
