package com.smartqueue.notification_service.service.impl;

import com.smartqueue.notification_service.service.SMSService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SMSServiceImpl implements SMSService {

    @Override
    public void sendSMS(String phoneNumber, String message) {
        log.info("[Twilio SMS Stub] Dispatching SMS to [{}]: {}", phoneNumber, message);
    }
}
