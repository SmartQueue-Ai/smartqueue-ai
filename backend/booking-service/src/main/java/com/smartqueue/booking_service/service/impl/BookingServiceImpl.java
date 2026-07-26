package com.smartqueue.booking_service.service.impl;

import com.smartqueue.booking_service.client.BookingClient;
import com.smartqueue.booking_service.dto.BookingRequest;
import com.smartqueue.booking_service.dto.BookingResponse;
import com.smartqueue.booking_service.dto.BookingStatusResponse;
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
import com.smartqueue.booking_service.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingAuditRepository bookingAuditRepository;
    private final BookingMapper bookingMapper;
    private final BookingClient bookingClient;
    private final BookingEventPublisher bookingEventPublisher;

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        log.info("Processing booking workflow for user: {}, event: {}", request.getUserId(), request.getEventId());

        String resourceId = request.getEventId().toString();
        String userId = request.getUserId().toString();

        boolean lockAcquired = bookingClient.acquireInventoryLock(resourceId, userId, 300L);
        if (!lockAcquired) {
            log.warn("Inventory lock acquisition failed for event ID: {}", request.getEventId());
            throw new InventoryLockException("Unable to acquire inventory lock for event/resource ID: " + request.getEventId());
        }

        String reference = "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Booking booking = bookingMapper.toEntity(request, reference);
        booking.setStatus(BookingStatus.CONFIRMED);

        Booking savedBooking = bookingRepository.save(booking);

        BookingAudit audit = BookingAudit.builder()
                .booking(savedBooking)
                .previousStatus(null)
                .newStatus(BookingStatus.CONFIRMED)
                .reason("Inventory lock acquired and booking confirmed")
                .changedBy("SYSTEM")
                .build();
        bookingAuditRepository.save(audit);

        BookingResponse response = bookingMapper.toResponse(savedBooking);

        BookingCreatedEvent event = BookingCreatedEvent.builder()
                .bookingId(savedBooking.getId())
                .bookingReference(savedBooking.getBookingReference())
                .userId(savedBooking.getUserId())
                .eventId(savedBooking.getEventId())
                .slotId(savedBooking.getSlotId())
                .status(savedBooking.getStatus())
                .quantity(savedBooking.getQuantity())
                .totalAmount(savedBooking.getTotalAmount())
                .createdAt(savedBooking.getCreatedAt() != null ? savedBooking.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : java.time.Instant.now())
                .build();

        bookingEventPublisher.publishBookingCreatedEvent(event);
        bookingEventPublisher.publishBookingCreated(response);

        log.info("Booking created successfully with reference: {} and ID: {}", reference, savedBooking.getId());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(UUID id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with ID: " + id));
        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingByReference(String bookingReference) {
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with reference: " + bookingReference));
        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByUserId(UUID userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BookingStatusResponse getBookingStatus(UUID id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with ID: " + id));
        return bookingMapper.toStatusResponse(booking);
    }
}
