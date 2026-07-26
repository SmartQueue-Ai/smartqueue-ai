package com.smartqueue.notification_service.service;

import com.smartqueue.notification_service.entity.NotificationHistory;
import com.smartqueue.notification_service.event.BookingCancelledEvent;
import com.smartqueue.notification_service.event.BookingConfirmedEvent;
import com.smartqueue.notification_service.event.BookingCreatedEvent;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    NotificationHistory processBookingCreated(BookingCreatedEvent event);

    NotificationHistory processBookingCancelled(BookingCancelledEvent event);

    NotificationHistory processBookingConfirmed(BookingConfirmedEvent event);

    List<NotificationHistory> getNotificationHistory(UUID userId);
}
