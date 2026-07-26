package com.smartqueue.booking_service.repository;

import com.smartqueue.booking_service.config.JpaAuditingConfig;
import com.smartqueue.booking_service.entity.Booking;
import com.smartqueue.booking_service.entity.BookingStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    @DisplayName("Should save and find booking by reference")
    void saveAndFindByBookingReference() {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        Booking booking = Booking.builder()
                .bookingReference("BK-TEST1234")
                .userId(userId)
                .eventId(eventId)
                .slotId("SLOT-1")
                .status(BookingStatus.PENDING)
                .quantity(2)
                .totalAmount(new BigDecimal("100.00"))
                .expiresAt(Instant.now().plus(15, ChronoUnit.MINUTES))
                .build();

        Booking saved = bookingRepository.save(booking);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();

        Optional<Booking> found = bookingRepository.findByBookingReference("BK-TEST1234");
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(userId);
        assertThat(found.get().getEventId()).isEqualTo(eventId);
    }

    @Test
    @DisplayName("Should find bookings by user ID")
    void findByUserId() {
        UUID userId = UUID.randomUUID();

        Booking booking1 = Booking.builder()
                .bookingReference("BK-USER1111")
                .userId(userId)
                .eventId(UUID.randomUUID())
                .status(BookingStatus.CONFIRMED)
                .expiresAt(Instant.now().plus(10, ChronoUnit.MINUTES))
                .build();

        Booking booking2 = Booking.builder()
                .bookingReference("BK-USER2222")
                .userId(userId)
                .eventId(UUID.randomUUID())
                .status(BookingStatus.PENDING)
                .expiresAt(Instant.now().plus(10, ChronoUnit.MINUTES))
                .build();

        bookingRepository.save(booking1);
        bookingRepository.save(booking2);

        List<Booking> userBookings = bookingRepository.findByUserId(userId);
        assertThat(userBookings).hasSize(2);
    }
}
