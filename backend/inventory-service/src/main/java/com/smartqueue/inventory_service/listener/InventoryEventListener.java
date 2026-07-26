package com.smartqueue.inventory_service.listener;

import com.smartqueue.inventory_service.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InventoryEventListener {

    @RabbitListener(queues = RabbitMQConfig.QUEUE_INVENTORY_EVENTS)
    public void handleInventoryEvent(Object message) {
        log.info("Inventory Consumer Placeholder: Received message from queue [{}]: {}", RabbitMQConfig.QUEUE_INVENTORY_EVENTS, message);
    }
}
