package com.smartqueue.booking_service.controller;

import com.smartqueue.booking_service.dto.*;
import com.smartqueue.booking_service.entity.Booking;
import com.smartqueue.booking_service.entity.BookingAudit;
import com.smartqueue.booking_service.entity.BookingStatus;
import com.smartqueue.booking_service.exception.BookingNotFoundException;
import com.smartqueue.booking_service.mapper.BookingMapper;
import com.smartqueue.booking_service.publisher.BookingEventPublisher;
import com.smartqueue.booking_service.repository.BookingAuditRepository;
import com.smartqueue.booking_service.repository.BookingRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/booking")
@RequiredArgsConstructor
@Tag(name = "Booking Controller", description = "REST APIs for Managing Reservation Bookings")
public class BookingController {

    private final BookingRepository bookingRepository;
    private final BookingAuditRepository bookingAuditRepository;
    private final BookingMapper bookingMapper;
    private final BookingEventPublisher bookingEventPublisher;

    @PostMapping
    @Operation(summary = "Create reservation booking", description = "Bootstrap endpoint to save a new booking reservation entity")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Booking created successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(@Valid @RequestBody BookingRequest request) {
        log.info("REST Request to create booking for user: {}, event: {}", request.getUserId(), request.getEventId());

        String reference = "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Booking booking = bookingMapper.toEntity(request, reference);
        Booking savedBooking = bookingRepository.save(booking);

        BookingAudit audit = BookingAudit.builder()
                .booking(savedBooking)
                .previousStatus(null)
                .newStatus(savedBooking.getStatus())
                .reason("Initial booking reservation creation")
                .changedBy("SYSTEM")
                .build();
        bookingAuditRepository.save(audit);

        BookingResponse response = bookingMapper.toResponse(savedBooking);
        bookingEventPublisher.publishBookingCreated(response);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking by ID", description = "Fetches booking details by UUID primary key")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Booking details retrieved",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(
            @Parameter(description = "Booking UUID", example = "c2ffde77-7a09-2ef6-994b-4aa7ab160a33")
            @PathVariable UUID id) {
        log.info("REST Request to get booking by ID: {}", id);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with ID: " + id));

        return ResponseEntity.ok(ApiResponse.success(bookingMapper.toResponse(booking)));
    }

    @GetMapping("/reference/{bookingReference}")
    @Operation(summary = "Get booking by reference", description = "Fetches booking details by unique reference code")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Booking details retrieved",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking reference not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingByReference(
            @Parameter(description = "Unique booking reference code", example = "BK-893A12BC")
            @PathVariable String bookingReference) {
        log.info("REST Request to get booking by reference: {}", bookingReference);

        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with reference: " + bookingReference));

        return ResponseEntity.ok(ApiResponse.success(bookingMapper.toResponse(booking)));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get bookings by user ID", description = "Fetches all booking reservations associated with a user UUID")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getBookingsByUserId(
            @Parameter(description = "User UUID", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
            @PathVariable UUID userId) {
        log.info("REST Request to get bookings for user ID: {}", userId);

        List<BookingResponse> bookings = bookingRepository.findByUserId(userId).stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "Get booking status", description = "Fetches lightweight booking status details")
    public ResponseEntity<ApiResponse<BookingStatusResponse>> getBookingStatus(
            @Parameter(description = "Booking UUID", example = "c2ffde77-7a09-2ef6-994b-4aa7ab160a33")
            @PathVariable UUID id) {
        log.info("REST Request to get booking status for ID: {}", id);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with ID: " + id));

        return ResponseEntity.ok(ApiResponse.success(bookingMapper.toStatusResponse(booking)));
    }
}
