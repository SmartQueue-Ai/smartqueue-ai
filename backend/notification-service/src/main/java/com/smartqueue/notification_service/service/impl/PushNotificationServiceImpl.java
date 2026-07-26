package com.smartqueue.notification_service.service.impl;

import com.smartqueue.notification_service.service.PushNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PushNotificationServiceImpl implements PushNotificationService {

    @Override
    public void sendPushNotification(String deviceToken, String title, String body) {
        log.info("[Firebase Push Stub] Dispatching Push Notification to token [{}]: Title: [{}] - Body: [{}]",
                deviceToken, title, body);
    }
}
