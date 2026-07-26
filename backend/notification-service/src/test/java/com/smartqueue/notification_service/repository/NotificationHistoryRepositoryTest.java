package com.smartqueue.notification_service.repository;

import com.smartqueue.notification_service.entity.NotificationHistory;
import com.smartqueue.notification_service.entity.NotificationStatus;
import com.smartqueue.notification_service.entity.NotificationType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class NotificationHistoryRepositoryTest {

    @Autowired
    private NotificationHistoryRepository repository;

    @Test
    void saveAndFindByUserId_ShouldReturnHistoryRecords() {
        UUID userId = UUID.randomUUID();

        NotificationHistory history = NotificationHistory.builder()
                .userId(userId)
                .type(NotificationType.BOOKING_CREATED)
                .message("Booking created successfully")
                .status(NotificationStatus.SENT)
                .build();

        NotificationHistory saved = repository.save(history);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();

        List<NotificationHistory> userRecords = repository.findByUserId(userId);
        assertThat(userRecords).hasSize(1);
        assertThat(userRecords.get(0).getMessage()).isEqualTo("Booking created successfully");
    }
}
