package com.smartqueue.auth_service.publisher;

import com.smartqueue.auth_service.config.RabbitMQConfig;
import com.smartqueue.auth_service.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishUserRegisteredEvent(UserRegisteredEvent event) {
        try {
            log.info("Publishing UserRegisteredEvent for userId: {}, username: {}", event.getUserId(), event.getUsername());
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_EVENTS,
                    RabbitMQConfig.ROUTING_KEY_USER_REGISTERED,
                    event
            );
        } catch (Exception ex) {
            log.error("Failed to publish UserRegisteredEvent for userId: {}: {}", event.getUserId(), ex.getMessage(), ex);
        }
    }
}
