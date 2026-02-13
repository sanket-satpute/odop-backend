package com.odop.root.chat.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Chat message entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "chat_messages")
public class ChatMessage {
    
    @Id
    private String id;
    
    @Indexed
    private String chatRoomId;
    
    @Indexed
    private String senderId;
    
    private String senderName;
    
    private SenderType senderType;
    
    private String content;
    
    private MessageType messageType;
    
    private MessageStatus status;
    
    @Indexed
    private LocalDateTime timestamp;
    
    private LocalDateTime readAt;
    
    // For attachments
    private List<Attachment> attachments;
    
    // Metadata
    private Map<String, Object> metadata;
    
    // ==================== ENUMS ====================
    
    public enum SenderType {
        CUSTOMER,
        VENDOR,
        ADMIN,
        SUPPORT_AGENT,
        SYSTEM
    }
    
    public enum MessageType {
        TEXT,
        IMAGE,
        FILE,
        SYSTEM_MESSAGE,
        TYPING_INDICATOR,
        ORDER_REFERENCE,
        PRODUCT_REFERENCE,
        AUTO_RESPONSE
    }
    
    public enum MessageStatus {
        SENDING,
        SENT,
        DELIVERED,
        READ,
        FAILED
    }
    
    // ==================== INNER CLASSES ====================
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Attachment {
        private String id;
        private String fileName;
        private String fileUrl;
        private String mimeType;
        private long fileSize;
    }
    
    // ==================== CONVENIENCE METHODS ====================
    
    public static ChatMessage createTextMessage(String chatRoomId, String senderId, 
                                                 String senderName, SenderType senderType, 
                                                 String content) {
        return ChatMessage.builder()
                .chatRoomId(chatRoomId)
                .senderId(senderId)
                .senderName(senderName)
                .senderType(senderType)
                .content(content)
                .messageType(MessageType.TEXT)
                .status(MessageStatus.SENT)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ChatMessage createSystemMessage(String chatRoomId, String content) {
        return ChatMessage.builder()
                .chatRoomId(chatRoomId)
                .senderId("SYSTEM")
                .senderName("System")
                .senderType(SenderType.SYSTEM)
                .content(content)
                .messageType(MessageType.SYSTEM_MESSAGE)
                .status(MessageStatus.SENT)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ChatMessage createAutoResponse(String chatRoomId, String content) {
        return ChatMessage.builder()
                .chatRoomId(chatRoomId)
                .senderId("SYSTEM")
                .senderName("ODOP Support")
                .senderType(SenderType.SUPPORT_AGENT)
                .content(content)
                .messageType(MessageType.AUTO_RESPONSE)
                .status(MessageStatus.SENT)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
