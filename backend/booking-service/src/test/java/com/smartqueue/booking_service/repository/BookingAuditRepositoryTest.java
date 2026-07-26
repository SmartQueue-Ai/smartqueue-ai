package com.smartqueue.booking_service.repository;

import com.smartqueue.booking_service.config.JpaAuditingConfig;
import com.smartqueue.booking_service.entity.Booking;
import com.smartqueue.booking_service.entity.BookingAudit;
import com.smartqueue.booking_service.entity.BookingStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
class BookingAuditRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingAuditRepository bookingAuditRepository;

    @Test
    @DisplayName("Should save and find booking audit trail by booking ID")
    void saveAndFindByBookingId() {
        Booking booking = Booking.builder()
                .bookingReference("BK-AUDIT0001")
                .userId(UUID.randomUUID())
                .eventId(UUID.randomUUID())
                .status(BookingStatus.PENDING)
                .expiresAt(Instant.now().plus(15, ChronoUnit.MINUTES))
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        BookingAudit audit = BookingAudit.builder()
                .booking(savedBooking)
                .previousStatus(null)
                .newStatus(BookingStatus.PENDING)
                .reason("Reservation initiated")
                .changedBy("SYSTEM")
                .build();

        BookingAudit savedAudit = bookingAuditRepository.save(audit);

        assertThat(savedAudit.getId()).isNotNull();
        assertThat(savedAudit.getCreatedAt()).isNotNull();

        List<BookingAudit> audits = bookingAuditRepository.findByBookingId(savedBooking.getId());
        assertThat(audits).hasSize(1);
        assertThat(audits.get(0).getNewStatus()).isEqualTo(BookingStatus.PENDING);
    }
}
