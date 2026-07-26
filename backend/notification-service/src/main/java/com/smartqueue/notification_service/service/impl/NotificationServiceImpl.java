package com.smartqueue.notification_service.service.impl;

import com.smartqueue.notification_service.entity.NotificationHistory;
import com.smartqueue.notification_service.entity.NotificationStatus;
import com.smartqueue.notification_service.entity.NotificationType;
import com.smartqueue.notification_service.event.BookingCancelledEvent;
import com.smartqueue.notification_service.event.BookingConfirmedEvent;
import com.smartqueue.notification_service.event.BookingCreatedEvent;
import com.smartqueue.notification_service.repository.NotificationHistoryRepository;
import com.smartqueue.notification_service.service.EmailService;
import com.smartqueue.notification_service.service.NotificationService;
import com.smartqueue.notification_service.service.PushNotificationService;
import com.smartqueue.notification_service.service.SMSService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationHistoryRepository historyRepository;
    private final EmailService emailService;
    private final SMSService smsService;
    private final PushNotificationService pushService;

    @Override
    @Transactional
    public NotificationHistory processBookingCreated(BookingCreatedEvent event) {
        log.info("Processing BookingCreatedEvent for bookingId [{}] and userId [{}]", event.getBookingId(), event.getUserId());

        String message = String.format("Booking Created: Your booking [%s] for resource [%s] with quantity %d has been initialized.",
                event.getBookingId(), event.getResourceId(), event.getQuantity());

        NotificationHistory history = NotificationHistory.builder()
                .userId(event.getUserId())
                .type(NotificationType.BOOKING_CREATED)
                .message(message)
                .status(NotificationStatus.SENT)
                .build();

        NotificationHistory savedHistory = historyRepository.save(history);

        String recipientEmail = (event.getUserEmail() != null) ? event.getUserEmail() : "user@smartqueue.ai";
        emailService.sendEmail(recipientEmail, "SmartQueue AI - Booking Created", message);

        // Future placeholders
        pushService.sendPushNotification("device-token-placeholder", "Booking Created", message);
        smsService.sendSMS("+10000000000", message);

        return savedHistory;
    }

    @Override
    @Transactional
    public NotificationHistory processBookingCancelled(BookingCancelledEvent event) {
        log.info("Processing BookingCancelledEvent for bookingId [{}] and userId [{}]", event.getBookingId(), event.getUserId());

        String message = String.format("Booking Cancelled: Your booking [%s] has been cancelled. Reason: %s",
                event.getBookingId(), event.getReason() != null ? event.getReason() : "N/A");

        NotificationHistory history = NotificationHistory.builder()
                .userId(event.getUserId())
                .type(NotificationType.BOOKING_CANCELLED)
                .message(message)
                .status(NotificationStatus.SENT)
                .build();

        NotificationHistory savedHistory = historyRepository.save(history);

        String recipientEmail = (event.getUserEmail() != null) ? event.getUserEmail() : "user@smartqueue.ai";
        emailService.sendEmail(recipientEmail, "SmartQueue AI - Booking Cancelled", message);

        return savedHistory;
    }

    @Override
    @Transactional
    public NotificationHistory processBookingConfirmed(BookingConfirmedEvent event) {
        log.info("Processing BookingConfirmedEvent for bookingId [{}] and userId [{}]", event.getBookingId(), event.getUserId());

        String message = String.format("Booking Confirmed: Your booking [%s] is confirmed! Confirmation Code: %s",
                event.getBookingId(), event.getConfirmationCode() != null ? event.getConfirmationCode() : "CONF-OK");

        NotificationHistory history = NotificationHistory.builder()
                .userId(event.getUserId())
                .type(NotificationType.BOOKING_CONFIRMED)
                .message(message)
                .status(NotificationStatus.SENT)
                .build();

        NotificationHistory savedHistory = historyRepository.save(history);

        String recipientEmail = (event.getUserEmail() != null) ? event.getUserEmail() : "user@smartqueue.ai";
        emailService.sendEmail(recipientEmail, "SmartQueue AI - Booking Confirmed", message);

        return savedHistory;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationHistory> getNotificationHistory(UUID userId) {
        return historyRepository.findByUserId(userId);
    }
}
