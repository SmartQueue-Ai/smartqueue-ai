package com.smartqueue.booking_service.repository;

import com.smartqueue.booking_service.entity.BookingAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookingAuditRepository extends JpaRepository<BookingAudit, UUID> {

    List<BookingAudit> findByBookingId(UUID bookingId);

    List<BookingAudit> findByBookingIdOrderByCreatedAtDesc(UUID bookingId);
}
