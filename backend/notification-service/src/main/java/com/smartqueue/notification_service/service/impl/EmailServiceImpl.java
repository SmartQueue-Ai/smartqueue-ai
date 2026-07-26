package com.smartqueue.notification_service.service.impl;

import com.smartqueue.notification_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendEmail(String to, String subject, String body) {
        log.info("Preparing to send email to [{}] with subject [{}]", to, subject);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("no-reply@smartqueue.ai");

            mailSender.send(message);
            log.info("Email successfully sent to [{}]", to);
        } catch (Exception ex) {
            log.warn("Failed to deliver SMTP mail to [{}], logging email output instead: {}. Body: [{}]", to, ex.getMessage(), body);
        }
    }
}
