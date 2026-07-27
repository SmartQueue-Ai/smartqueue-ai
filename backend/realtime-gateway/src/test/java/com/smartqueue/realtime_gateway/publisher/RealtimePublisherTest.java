package com.smartqueue.realtime_gateway.publisher;

import com.smartqueue.realtime_gateway.dto.QueueUpdatePayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RealtimePublisherTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private RealtimePublisher realtimePublisher;

    @BeforeEach
    void setUp() {
        realtimePublisher = new RealtimePublisher(messagingTemplate);
    }

    @Test
    @DisplayName("publishQueueUpdateToResource - Should send STOMP message to /queue/{resourceId} and /topic/queue/{resourceId}")
    void publishQueueUpdateToResource() {
        QueueUpdatePayload payload = QueueUpdatePayload.builder()
                .queuePosition(2)
                .estimatedWaitTime(60)
                .resourceStatus("AVAILABLE")
                .resourceId("res-1")
                .timestamp(Instant.now())
                .build();

        realtimePublisher.publishQueueUpdateToResource("res-1", payload);

        verify(messagingTemplate).convertAndSend(eq("/queue/res-1"), eq(payload));
        verify(messagingTemplate).convertAndSend(eq("/topic/queue/res-1"), eq(payload));
    }

    @Test
    @DisplayName("publishQueueUpdateToUser - Should send STOMP message to user destinations")
    void publishQueueUpdateToUser() {
        QueueUpdatePayload payload = QueueUpdatePayload.builder()
                .queuePosition(1)
                .estimatedWaitTime(30)
                .resourceStatus("CONFIRMED")
                .userId("user-1")
                .timestamp(Instant.now())
                .build();

        realtimePublisher.publishQueueUpdateToUser("user-1", payload);

        verify(messagingTemplate).convertAndSend(eq("/user/user-1"), eq(payload));
        verify(messagingTemplate).convertAndSend(eq("/queue/user/user-1"), eq(payload));
    }
}
