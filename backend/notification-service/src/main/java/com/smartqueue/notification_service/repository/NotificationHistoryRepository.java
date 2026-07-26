package com.smartqueue.notification_service.repository;

import com.smartqueue.notification_service.entity.NotificationHistory;
import com.smartqueue.notification_service.entity.NotificationStatus;
import com.smartqueue.notification_service.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, UUID> {

    List<NotificationHistory> findByUserId(UUID userId);

    List<NotificationHistory> findByType(NotificationType type);

    List<NotificationHistory> findByStatus(NotificationStatus status);
}
