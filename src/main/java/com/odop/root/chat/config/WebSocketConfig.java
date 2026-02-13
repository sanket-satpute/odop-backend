package com.odop.root.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

/**
 * WebSocket configuration for real-time chat support
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple in-memory message broker for /topic and /queue prefixes
        config.enableSimpleBroker("/topic", "/queue");
        
        // Application destination prefix for client-to-server messages
        config.setApplicationDestinationPrefixes("/app");
        
        // User destination prefix for direct messages
        config.setUserDestinationPrefix("/user");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket endpoint that clients will connect to
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*")
                .withSockJS();  // Fallback for browsers without WebSocket support
        
        // Plain WebSocket endpoint (without SockJS)
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*");
    }
}
