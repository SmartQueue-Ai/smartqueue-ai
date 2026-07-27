package com.smartqueue.realtime_gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartqueue.realtime_gateway.dto.QueueUpdatePayload;
import com.smartqueue.realtime_gateway.publisher.RealtimePublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class WebSocketQueueUpdateIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private RealtimePublisher realtimePublisher;

    private WebSocketStompClient stompClient;

    @BeforeEach
    void setUp() {
        List<Transport> transports = List.of(new WebSocketTransport(new StandardWebSocketClient()));
        SockJsClient sockJsClient = new SockJsClient(transports);
        stompClient = new WebSocketStompClient(sockJsClient);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();
        messageConverter.setObjectMapper(objectMapper);
        stompClient.setMessageConverter(messageConverter);
    }

    private String getWsUrl() {
        return "ws://localhost:" + port + "/ws-queue";
    }

    @Test
    @DisplayName("WebSocket - Client connects, subscribes, and receives realtime queue updates")
    void clientConnectsAndReceivesUpdates() throws Exception {
        String resourceId = "res-" + UUID.randomUUID().toString().substring(0, 8);
        CompletableFuture<QueueUpdatePayload> messageFuture = new CompletableFuture<>();

        StompSessionHandler sessionHandler = new StompSessionHandlerAdapter() {
            @Override
            public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                messageFuture.completeExceptionally(exception);
            }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                messageFuture.completeExceptionally(exception);
            }

            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                session.subscribe("/queue/" + resourceId, new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return QueueUpdatePayload.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        messageFuture.complete((QueueUpdatePayload) payload);
                    }
                });
            }
        };

        StompSession session = stompClient.connectAsync(getWsUrl(), sessionHandler)
                .get(5, TimeUnit.SECONDS);

        assertThat(session.isConnected()).isTrue();

        // Give Spring STOMP subscription time to register in broker
        Thread.sleep(500);

        QueueUpdatePayload payload = QueueUpdatePayload.builder()
                .queuePosition(3)
                .estimatedWaitTime(120)
                .resourceStatus("AVAILABLE")
                .resourceId(resourceId)
                .userId("user-100")
                .eventType("QUEUE_MOVED")
                .timestamp(Instant.now())
                .build();

        realtimePublisher.publishQueueUpdateToResource(resourceId, payload);

        QueueUpdatePayload received = messageFuture.get(5, TimeUnit.SECONDS);

        assertThat(received).isNotNull();
        assertThat(received.getQueuePosition()).isEqualTo(3);
        assertThat(received.getEstimatedWaitTime()).isEqualTo(120);
        assertThat(received.getResourceStatus()).isEqualTo("AVAILABLE");
        assertThat(received.getResourceId()).isEqualTo(resourceId);

        session.disconnect();
    }

    @Test
    @DisplayName("WebSocket - Reconnect works seamlessly after disconnect")
    void reconnectWorksAfterDisconnect() throws Exception {
        StompSessionHandler sessionHandler = new StompSessionHandlerAdapter() {};

        // Initial Connection
        StompSession session1 = stompClient.connectAsync(getWsUrl(), sessionHandler)
                .get(5, TimeUnit.SECONDS);

        assertThat(session1.isConnected()).isTrue();
        session1.disconnect();

        Thread.sleep(200);

        // Reconnect
        StompSession session2 = stompClient.connectAsync(getWsUrl(), sessionHandler)
                .get(5, TimeUnit.SECONDS);

        assertThat(session2.isConnected()).isTrue();
        session2.disconnect();
    }
}
