package com.smartqueue.notification_service.service;

public interface SMSService {

    void sendSMS(String phoneNumber, String message);
}
