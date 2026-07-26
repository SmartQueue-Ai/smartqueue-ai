package com.smartqueue.inventory_service.publisher;

import com.smartqueue.inventory_service.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishEvent(String routingKey, Object eventPayload) {
        try {
            log.info("Inventory Producer Placeholder: Publishing event with routing key [{}]", routingKey);
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_EVENTS, routingKey, eventPayload);
        } catch (Exception ex) {
            log.error("Failed to publish inventory event to RabbitMQ: {}", ex.getMessage(), ex);
        }
    }
}
