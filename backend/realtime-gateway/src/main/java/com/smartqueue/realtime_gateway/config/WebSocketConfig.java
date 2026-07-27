package com.smartqueue.realtime_gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Simple broker for broadcasting messages to clients
        registry.enableSimpleBroker("/queue", "/user", "/topic");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint for WebSocket connection with fallback to SockJS
        registry.addEndpoint("/ws-queue")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // Plain WebSocket endpoint for standard WebSocket clients
        registry.addEndpoint("/ws-queue")
                .setAllowedOriginPatterns("*");
    }
}
