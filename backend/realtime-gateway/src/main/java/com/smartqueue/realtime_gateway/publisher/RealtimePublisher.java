package com.smartqueue.realtime_gateway.publisher;

import com.smartqueue.realtime_gateway.dto.QueueUpdatePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RealtimePublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publishQueueUpdateToResource(String resourceId, QueueUpdatePayload payload) {
        if (resourceId == null) {
            log.warn("Cannot publish queue update: resourceId is null");
            return;
        }

        String destination = "/queue/" + resourceId;
        String topicDestination = "/topic/queue/" + resourceId;

        log.info("Broadcasting queue update to destination: {} and {}: position={}, waitTime={}s, status={}",
                destination, topicDestination, payload.getQueuePosition(), payload.getEstimatedWaitTime(), payload.getResourceStatus());

        messagingTemplate.convertAndSend(destination, payload);
        messagingTemplate.convertAndSend(topicDestination, payload);
    }

    public void publishQueueUpdateToUser(String userId, QueueUpdatePayload payload) {
        if (userId == null) {
            log.warn("Cannot publish user update: userId is null");
            return;
        }

        String userDestination = "/user/" + userId;
        String userQueueDestination = "/queue/user/" + userId;

        log.info("Broadcasting personal queue update to user destinations: {} and {}: position={}, waitTime={}s",
                userDestination, userQueueDestination, payload.getQueuePosition(), payload.getEstimatedWaitTime());

        messagingTemplate.convertAndSend(userDestination, payload);
        messagingTemplate.convertAndSend(userQueueDestination, payload);
    }

    public void broadcast(QueueUpdatePayload payload) {
        if (payload.getResourceId() != null) {
            publishQueueUpdateToResource(payload.getResourceId(), payload);
        }
        if (payload.getUserId() != null) {
            publishQueueUpdateToUser(payload.getUserId(), payload);
        }
    }
}
