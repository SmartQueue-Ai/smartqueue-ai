package com.smartqueue.notification_service.service;

import com.smartqueue.notification_service.entity.NotificationHistory;
import com.smartqueue.notification_service.entity.NotificationStatus;
import com.smartqueue.notification_service.entity.NotificationType;
import com.smartqueue.notification_service.event.BookingCancelledEvent;
import com.smartqueue.notification_service.event.BookingConfirmedEvent;
import com.smartqueue.notification_service.event.BookingCreatedEvent;
import com.smartqueue.notification_service.repository.NotificationHistoryRepository;
import com.smartqueue.notification_service.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationHistoryRepository historyRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private SMSService smsService;

    @Mock
    private PushNotificationService pushService;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void processBookingCreated_ShouldStoreHistoryAndInvokeEmailService() {
        UUID bookingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();

        BookingCreatedEvent event = BookingCreatedEvent.builder()
                .bookingId(bookingId)
                .userId(userId)
                .resourceId(resourceId)
                .quantity(2)
                .userEmail("testuser@smartqueue.ai")
                .build();

        NotificationHistory mockSavedHistory = NotificationHistory.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .type(NotificationType.BOOKING_CREATED)
                .message("Booking Created")
                .status(NotificationStatus.SENT)
                .build();

        when(historyRepository.save(any(NotificationHistory.class))).thenReturn(mockSavedHistory);

        NotificationHistory result = notificationService.processBookingCreated(event);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);

        // Verify history stored
        ArgumentCaptor<NotificationHistory> historyCaptor = ArgumentCaptor.forClass(NotificationHistory.class);
        verify(historyRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getType()).isEqualTo(NotificationType.BOOKING_CREATED);
        assertThat(historyCaptor.getValue().getUserId()).isEqualTo(userId);

        // Verify email service invoked
        verify(emailService).sendEmail(eq("testuser@smartqueue.ai"), eq("SmartQueue AI - Booking Created"), any(String.class));
    }

    @Test
    void processBookingCancelled_ShouldStoreHistoryAndInvokeEmailService() {
        UUID bookingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        BookingCancelledEvent event = BookingCancelledEvent.builder()
                .bookingId(bookingId)
                .userId(userId)
                .reason("User requested cancellation")
                .userEmail("testuser@smartqueue.ai")
                .build();

        NotificationHistory mockSaved = NotificationHistory.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .type(NotificationType.BOOKING_CANCELLED)
                .message("Booking Cancelled")
                .status(NotificationStatus.SENT)
                .build();

        when(historyRepository.save(any(NotificationHistory.class))).thenReturn(mockSaved);

        NotificationHistory result = notificationService.processBookingCancelled(event);

        assertThat(result).isNotNull();
        verify(historyRepository).save(any(NotificationHistory.class));
        verify(emailService).sendEmail(eq("testuser@smartqueue.ai"), eq("SmartQueue AI - Booking Cancelled"), any(String.class));
    }

    @Test
    void processBookingConfirmed_ShouldStoreHistoryAndInvokeEmailService() {
        UUID bookingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        BookingConfirmedEvent event = BookingConfirmedEvent.builder()
                .bookingId(bookingId)
                .userId(userId)
                .confirmationCode("CONF-123456")
                .userEmail("testuser@smartqueue.ai")
                .build();

        NotificationHistory mockSaved = NotificationHistory.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .type(NotificationType.BOOKING_CONFIRMED)
                .message("Booking Confirmed")
                .status(NotificationStatus.SENT)
                .build();

        when(historyRepository.save(any(NotificationHistory.class))).thenReturn(mockSaved);

        NotificationHistory result = notificationService.processBookingConfirmed(event);

        assertThat(result).isNotNull();
        verify(historyRepository).save(any(NotificationHistory.class));
        verify(emailService).sendEmail(eq("testuser@smartqueue.ai"), eq("SmartQueue AI - Booking Confirmed"), any(String.class));
    }
}
