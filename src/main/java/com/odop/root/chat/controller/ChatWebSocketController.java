package com.odop.root.chat.controller;

import com.odop.root.chat.dto.ChatDto.*;
import com.odop.root.chat.model.*;
import com.odop.root.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

/**
 * WebSocket Controller for real-time chat messaging
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {
    
    private final ChatService chatService;
    
    /**
     * Handle incoming chat messages via WebSocket
     * Client sends to: /app/chat.send
     * Broadcasts to: /topic/chat/{roomId}
     */
    @MessageMapping("/chat.send")
    public void sendMessage(
            @Payload SendMessageRequest request,
            SimpMessageHeaderAccessor headerAccessor) {
        
        Principal principal = headerAccessor.getUser();
        if (principal == null) {
            log.warn("Unauthorized message attempt");
            return;
        }
        
        String userId = principal.getName();
        String userName = (String) headerAccessor.getSessionAttributes().getOrDefault("userName", userId);
        String userRole = (String) headerAccessor.getSessionAttributes().getOrDefault("userRole", "CUSTOMER");
        
        ChatMessage.SenderType senderType = switch (userRole) {
            case "ADMIN" -> ChatMessage.SenderType.ADMIN;
            case "VENDOR" -> ChatMessage.SenderType.VENDOR;
            case "SUPPORT" -> ChatMessage.SenderType.SUPPORT_AGENT;
            default -> ChatMessage.SenderType.CUSTOMER;
        };
        
        ChatMessage.MessageType messageType = request.getMessageType() != null ?
                request.getMessageType() : ChatMessage.MessageType.TEXT;
        
        chatService.sendMessage(
                request.getChatRoomId(),
                userId,
                userName,
                senderType,
                request.getContent(),
                messageType
        );
        
        log.debug("Message sent to room {} by user {}", request.getChatRoomId(), userId);
    }
    
    /**
     * Handle typing indicator
     * Client sends to: /app/chat.typing
     * Broadcasts to: /topic/chat/{roomId}
     */
    @MessageMapping("/chat.typing")
    public void sendTypingIndicator(
            @Payload Map<String, Object> payload,
            SimpMessageHeaderAccessor headerAccessor) {
        
        Principal principal = headerAccessor.getUser();
        if (principal == null) return;
        
        String userId = principal.getName();
        String userName = (String) headerAccessor.getSessionAttributes().getOrDefault("userName", userId);
        String roomId = (String) payload.get("roomId");
        boolean isTyping = (Boolean) payload.getOrDefault("isTyping", false);
        
        chatService.sendTypingIndicator(roomId, userId, userName, isTyping);
    }
    
    /**
     * Handle mark as read
     * Client sends to: /app/chat.read
     */
    @MessageMapping("/chat.read")
    public void markAsRead(
            @Payload Map<String, String> payload,
            SimpMessageHeaderAccessor headerAccessor) {
        
        Principal principal = headerAccessor.getUser();
        if (principal == null) return;
        
        String roomId = payload.get("roomId");
        chatService.markAsRead(roomId, principal.getName());
    }
    
    /**
     * Handle user joining a room
     * Client sends to: /app/chat.join
     */
    @MessageMapping("/chat.join")
    @SendToUser("/queue/joined")
    public ChatRoomResponse joinRoom(
            @Payload Map<String, String> payload,
            SimpMessageHeaderAccessor headerAccessor) {
        
        Principal principal = headerAccessor.getUser();
        if (principal == null) return null;
        
        String roomId = payload.get("roomId");
        String userId = principal.getName();
        
        // Update online status
        chatService.updateOnlineStatus(roomId, userId, true);
        
        // Return room info
        ChatRoom room = chatService.getChatRoom(roomId);
        if (room != null) {
            return ChatRoomResponse.from(room, userId);
        }
        return null;
    }
    
    /**
     * Handle user leaving a room
     * Client sends to: /app/chat.leave
     */
    @MessageMapping("/chat.leave")
    public void leaveRoom(
            @Payload Map<String, String> payload,
            SimpMessageHeaderAccessor headerAccessor) {
        
        Principal principal = headerAccessor.getUser();
        if (principal == null) return;
        
        String roomId = payload.get("roomId");
        String userId = principal.getName();
        
        chatService.updateOnlineStatus(roomId, userId, false);
    }
}
