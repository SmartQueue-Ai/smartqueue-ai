package com.smartqueue.booking_service.repository;

import com.smartqueue.booking_service.entity.Booking;
import com.smartqueue.booking_service.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    Optional<Booking> findByBookingReference(String bookingReference);

    List<Booking> findByUserId(UUID userId);

    Page<Booking> findByUserId(UUID userId, Pageable pageable);

    List<Booking> findByStatus(BookingStatus status);

    boolean existsByBookingReference(String bookingReference);
}
