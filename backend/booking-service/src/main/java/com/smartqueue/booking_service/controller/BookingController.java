package com.smartqueue.booking_service.controller;

import com.smartqueue.booking_service.dto.*;
import com.smartqueue.booking_service.service.BookingService;
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

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Booking Controller", description = "REST APIs for Managing Reservation Bookings and Workflow")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping({"/bookings", "/api/v1/bookings", "/api/v1/booking"})
    @Operation(summary = "Create reservation booking", description = "Acquires inventory lock and persists booking reservation")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Booking created successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Inventory lock acquisition conflict",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(@Valid @RequestBody BookingRequest request) {
        log.info("REST Request to create booking for user: {}, event: {}", request.getUserId(), request.getEventId());
        BookingResponse response = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping({"/bookings/{id}", "/api/v1/bookings/{id}", "/api/v1/booking/{id}"})
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
        BookingResponse response = bookingService.getBookingById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping({"/bookings/reference/{bookingReference}", "/api/v1/bookings/reference/{bookingReference}", "/api/v1/booking/reference/{bookingReference}"})
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
        BookingResponse response = bookingService.getBookingByReference(bookingReference);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping({"/bookings/user/{userId}", "/api/v1/bookings/user/{userId}", "/api/v1/booking/user/{userId}"})
    @Operation(summary = "Get bookings by user ID", description = "Fetches all booking reservations associated with a user UUID")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getBookingsByUserId(
            @Parameter(description = "User UUID", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
            @PathVariable UUID userId) {
        log.info("REST Request to get bookings for user ID: {}", userId);
        List<BookingResponse> responses = bookingService.getBookingsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping({"/bookings/{id}/status", "/api/v1/bookings/{id}/status", "/api/v1/booking/{id}/status"})
    @Operation(summary = "Get booking status", description = "Fetches lightweight booking status details")
    public ResponseEntity<ApiResponse<BookingStatusResponse>> getBookingStatus(
            @Parameter(description = "Booking UUID", example = "c2ffde77-7a09-2ef6-994b-4aa7ab160a33")
            @PathVariable UUID id) {
        log.info("REST Request to get booking status for ID: {}", id);
        BookingStatusResponse response = bookingService.getBookingStatus(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
