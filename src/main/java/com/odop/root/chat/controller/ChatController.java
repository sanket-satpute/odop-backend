package com.odop.root.chat.controller;

import com.odop.root.chat.dto.ChatDto.*;
import com.odop.root.chat.model.*;
import com.odop.root.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST Controller for chat operations
 */
@RestController
@RequestMapping("/odop/chat")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ChatController {
    
    private final ChatService chatService;
    
    // ==================== CHAT ROOM ENDPOINTS ====================
    
    /**
     * Create a new chat room
     */
    @PostMapping("/rooms")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createChatRoom(
            @RequestBody CreateChatRoomRequest request,
            Authentication auth) {
        
        try {
            String userId = auth.getName();
            String userName = getUserName(auth);
            String userEmail = getUserEmail(auth);
            String userRole = getUserRole(auth);
            
            ChatRoom room = chatService.createChatRoom(userId, userName, userEmail, userRole, request);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Chat room created",
                    "room", ChatRoomResponse.from(room, userId)
            ));
            
        } catch (Exception e) {
            log.error("Error creating chat room", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Get chat room by ID
     */
    @GetMapping("/rooms/{roomId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getChatRoom(@PathVariable String roomId, Authentication auth) {
        ChatRoom room = chatService.getChatRoom(roomId);
        
        if (room == null) {
            return ResponseEntity.notFound().build();
        }
        
        String userId = auth.getName();
        
        // Verify user is participant
        if (!room.getParticipantIds().contains(userId) && !isAdminOrAgent(auth)) {
            return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "Access denied"
            ));
        }
        
        return ResponseEntity.ok(ChatRoomResponse.from(room, userId));
    }
    
    /**
     * Get user's chat rooms
     */
    @GetMapping("/rooms")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserChatRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        
        String userId = auth.getName();
        List<ChatRoomResponse> rooms = chatService.getUserChatRooms(userId, page, size);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "rooms", rooms,
                "page", page,
                "size", size
        ));
    }
    
    /**
     * Get active chats
     */
    @GetMapping("/rooms/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getActiveChats(Authentication auth) {
        String userId = auth.getName();
        List<ChatRoomResponse> rooms = chatService.getActiveChats(userId);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "rooms", rooms
        ));
    }
    
    // ==================== MESSAGE ENDPOINTS ====================
    
    /**
     * Send a message
     */
    @PostMapping("/rooms/{roomId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> sendMessage(
            @PathVariable String roomId,
            @RequestBody SendMessageRequest request,
            Authentication auth) {
        
        try {
            String userId = auth.getName();
            String userName = getUserName(auth);
            ChatMessage.SenderType senderType = getSenderType(auth);
            ChatMessage.MessageType messageType = request.getMessageType() != null ?
                    request.getMessageType() : ChatMessage.MessageType.TEXT;
            
            ChatMessage message = chatService.sendMessage(
                    roomId, userId, userName, senderType, 
                    request.getContent(), messageType);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", MessageResponse.from(message)
            ));
            
        } catch (Exception e) {
            log.error("Error sending message", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Get messages for a room
     */
    @GetMapping("/rooms/{roomId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMessages(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Authentication auth) {
        
        List<MessageResponse> messages = chatService.getMessages(roomId, page, size);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "messages", messages,
                "page", page,
                "size", size
        ));
    }
    
    /**
     * Get recent messages
     */
    @GetMapping("/rooms/{roomId}/messages/recent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getRecentMessages(@PathVariable String roomId) {
        List<MessageResponse> messages = chatService.getRecentMessages(roomId);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "messages", messages
        ));
    }
    
    /**
     * Mark messages as read
     */
    @PostMapping("/rooms/{roomId}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> markAsRead(@PathVariable String roomId, Authentication auth) {
        String userId = auth.getName();
        chatService.markAsRead(roomId, userId);
        
        return ResponseEntity.ok(Map.of("success", true));
    }
    
    /**
     * Search messages
     */
    @GetMapping("/rooms/{roomId}/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> searchMessages(
            @PathVariable String roomId,
            @RequestParam String q) {
        
        List<MessageResponse> messages = chatService.searchMessages(roomId, q);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "messages", messages
        ));
    }
    
    // ==================== SUPPORT AGENT ENDPOINTS ====================
    
    /**
     * Get waiting tickets (for support)
     */
    @GetMapping("/support/waiting")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<?> getWaitingTickets() {
        List<ChatRoomResponse> tickets = chatService.getWaitingTickets();
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "tickets", tickets
        ));
    }
    
    /**
     * Get agent's assigned tickets
     */
    @GetMapping("/support/my-tickets")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<?> getMyTickets(Authentication auth) {
        String agentId = auth.getName();
        List<ChatRoomResponse> tickets = chatService.getAgentTickets(agentId);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "tickets", tickets
        ));
    }
    
    /**
     * Assign ticket to agent
     */
    @PostMapping("/support/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<?> assignTicket(
            @RequestBody AssignAgentRequest request,
            Authentication auth) {
        
        try {
            String agentId = request.getAgentId() != null ? 
                    request.getAgentId() : auth.getName();
            String agentName = getUserName(auth);
            
            ChatRoom room = chatService.assignAgent(request.getChatRoomId(), agentId, agentName);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Ticket assigned",
                    "room", ChatRoomResponse.from(room, agentId)
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Close/resolve ticket
     */
    @PostMapping("/support/close")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<?> closeTicket(
            @RequestBody CloseTicketRequest request,
            Authentication auth) {
        
        try {
            String userId = auth.getName();
            ChatRoom room = chatService.closeTicket(
                    request.getChatRoomId(), request.getResolution(), userId);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Ticket closed",
                    "room", ChatRoomResponse.from(room, userId)
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Get chat statistics
     */
    @GetMapping("/support/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<?> getChatStats() {
        Map<String, Object> stats = chatService.getChatStats();
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "stats", stats
        ));
    }
    
    // ==================== HEALTH CHECK ====================
    
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Chat"
        ));
    }
    
    // ==================== HELPER METHODS ====================
    
    private String getUserRole(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .findFirst()
                .orElse("USER");
    }
    
    private String getUserName(Authentication auth) {
        // In real implementation, fetch from user service
        return auth.getName();
    }
    
    private String getUserEmail(Authentication auth) {
        // In real implementation, fetch from user service
        return auth.getName() + "@example.com";
    }
    
    private boolean isAdminOrAgent(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN") || a.equals("ROLE_SUPPORT"));
    }
    
    private ChatMessage.SenderType getSenderType(Authentication auth) {
        String role = getUserRole(auth);
        return switch (role) {
            case "ADMIN" -> ChatMessage.SenderType.ADMIN;
            case "VENDOR" -> ChatMessage.SenderType.VENDOR;
            case "SUPPORT" -> ChatMessage.SenderType.SUPPORT_AGENT;
            default -> ChatMessage.SenderType.CUSTOMER;
        };
    }
}
