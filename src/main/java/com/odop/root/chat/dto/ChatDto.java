package com.odop.root.chat.dto;

import lombok.*;
import com.odop.root.chat.model.ChatMessage;
import com.odop.root.chat.model.ChatRoom;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTOs for chat functionality
 */
public class ChatDto {
    
    // ==================== MESSAGE DTOs ====================
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SendMessageRequest {
        private String chatRoomId;
        private String content;
        private ChatMessage.MessageType messageType;
        private List<ChatMessage.Attachment> attachments;
        private Map<String, Object> metadata;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MessageResponse {
        private String id;
        private String chatRoomId;
        private String senderId;
        private String senderName;
        private String senderType;
        private String content;
        private String messageType;
        private String status;
        private LocalDateTime timestamp;
        private LocalDateTime readAt;
        private List<ChatMessage.Attachment> attachments;
        
        public static MessageResponse from(ChatMessage message) {
            return MessageResponse.builder()
                    .id(message.getId())
                    .chatRoomId(message.getChatRoomId())
                    .senderId(message.getSenderId())
                    .senderName(message.getSenderName())
                    .senderType(message.getSenderType().name())
                    .content(message.getContent())
                    .messageType(message.getMessageType().name())
                    .status(message.getStatus().name())
                    .timestamp(message.getTimestamp())
                    .readAt(message.getReadAt())
                    .attachments(message.getAttachments())
                    .build();
        }
    }
    
    // ==================== CHAT ROOM DTOs ====================
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateChatRoomRequest {
        private String roomType;
        private String title;
        private String orderId;
        private String productId;
        private String vendorId;
        private String initialMessage;
        private List<String> tags;
        private int priority;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChatRoomResponse {
        private String id;
        private String roomType;
        private String title;
        private String description;
        private String status;
        private String orderId;
        private String productId;
        private String vendorId;
        private String assignedAgentId;
        private String assignedAgentName;
        private int messageCount;
        private String lastMessagePreview;
        private LocalDateTime lastMessageAt;
        private int unreadCount;
        private int priority;
        private List<String> tags;
        private List<ParticipantInfo> participants;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        public static ChatRoomResponse from(ChatRoom room, String currentUserId) {
            return ChatRoomResponse.builder()
                    .id(room.getId())
                    .roomType(room.getRoomType().name())
                    .title(room.getTitle())
                    .description(room.getDescription())
                    .status(room.getStatus().name())
                    .orderId(room.getOrderId())
                    .productId(room.getProductId())
                    .vendorId(room.getVendorId())
                    .assignedAgentId(room.getAssignedAgentId())
                    .assignedAgentName(room.getAssignedAgentName())
                    .messageCount(room.getMessageCount())
                    .lastMessagePreview(room.getLastMessagePreview())
                    .lastMessageAt(room.getLastMessageAt())
                    .unreadCount(room.getUnreadCount(currentUserId))
                    .priority(room.getPriority())
                    .tags(room.getTags())
                    .participants(room.getParticipants() != null ?
                            room.getParticipants().stream()
                                    .map(ParticipantInfo::from)
                                    .toList() : null)
                    .createdAt(room.getCreatedAt())
                    .updatedAt(room.getUpdatedAt())
                    .build();
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ParticipantInfo {
        private String id;
        private String name;
        private String avatarUrl;
        private String role;
        private boolean isOnline;
        private LocalDateTime lastSeenAt;
        
        public static ParticipantInfo from(ChatRoom.Participant p) {
            return ParticipantInfo.builder()
                    .id(p.getId())
                    .name(p.getName())
                    .avatarUrl(p.getAvatarUrl())
                    .role(p.getRole().name())
                    .isOnline(p.isOnline())
                    .lastSeenAt(p.getLastSeenAt())
                    .build();
        }
    }
    
    // ==================== WEBSOCKET EVENTS ====================
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChatEvent {
        private String eventType;
        private String chatRoomId;
        private String senderId;
        private Object payload;
        private LocalDateTime timestamp;
        
        public static ChatEvent newMessage(ChatMessage message) {
            return ChatEvent.builder()
                    .eventType("NEW_MESSAGE")
                    .chatRoomId(message.getChatRoomId())
                    .senderId(message.getSenderId())
                    .payload(MessageResponse.from(message))
                    .timestamp(LocalDateTime.now())
                    .build();
        }
        
        public static ChatEvent typing(String chatRoomId, String userId, String userName, boolean isTyping) {
            return ChatEvent.builder()
                    .eventType("TYPING")
                    .chatRoomId(chatRoomId)
                    .senderId(userId)
                    .payload(Map.of(
                            "userId", userId,
                            "userName", userName,
                            "isTyping", isTyping
                    ))
                    .timestamp(LocalDateTime.now())
                    .build();
        }
        
        public static ChatEvent userJoined(String chatRoomId, String userId, String userName) {
            return ChatEvent.builder()
                    .eventType("USER_JOINED")
                    .chatRoomId(chatRoomId)
                    .senderId(userId)
                    .payload(Map.of(
                            "userId", userId,
                            "userName", userName
                    ))
                    .timestamp(LocalDateTime.now())
                    .build();
        }
        
        public static ChatEvent userLeft(String chatRoomId, String userId, String userName) {
            return ChatEvent.builder()
                    .eventType("USER_LEFT")
                    .chatRoomId(chatRoomId)
                    .senderId(userId)
                    .payload(Map.of(
                            "userId", userId,
                            "userName", userName
                    ))
                    .timestamp(LocalDateTime.now())
                    .build();
        }
        
        public static ChatEvent roomStatusChanged(String chatRoomId, String newStatus) {
            return ChatEvent.builder()
                    .eventType("ROOM_STATUS_CHANGED")
                    .chatRoomId(chatRoomId)
                    .payload(Map.of("status", newStatus))
                    .timestamp(LocalDateTime.now())
                    .build();
        }
        
        public static ChatEvent messagesRead(String chatRoomId, String userId, List<String> messageIds) {
            return ChatEvent.builder()
                    .eventType("MESSAGES_READ")
                    .chatRoomId(chatRoomId)
                    .senderId(userId)
                    .payload(Map.of("messageIds", messageIds))
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }
    
    // ==================== SUPPORT SPECIFIC ====================
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SupportTicketInfo {
        private String ticketId;
        private String chatRoomId;
        private String subject;
        private String category;
        private String priority;
        private String status;
        private String customerName;
        private String customerEmail;
        private String assignedAgent;
        private LocalDateTime createdAt;
        private LocalDateTime lastUpdated;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AssignAgentRequest {
        private String chatRoomId;
        private String agentId;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CloseTicketRequest {
        private String chatRoomId;
        private String resolution;
        private String notes;
        private int satisfactionRating;
    }
}
