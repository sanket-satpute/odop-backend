package com.odop.root.chat.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Chat room entity - represents a conversation between participants
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "chat_rooms")
@CompoundIndex(name = "participant_idx", def = "{'participantIds': 1}")
public class ChatRoom {
    
    @Id
    private String id;
    
    private ChatRoomType roomType;
    
    private String title;
    
    private String description;
    
    // Participants
    @Indexed
    private List<String> participantIds;
    
    private List<Participant> participants;
    
    // Reference IDs (for order/product specific chats)
    private String orderId;
    
    private String productId;
    
    private String vendorId;
    
    // Status
    private RoomStatus status;
    
    private String assignedAgentId;
    
    private String assignedAgentName;
    
    // Stats
    private int messageCount;
    
    private String lastMessagePreview;
    
    private LocalDateTime lastMessageAt;
    
    private Map<String, Integer> unreadCounts;
    
    // Timestamps
    @Indexed
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private LocalDateTime closedAt;
    
    // Metadata
    private Map<String, Object> metadata;
    
    private int priority; // 1 = low, 5 = urgent
    
    private List<String> tags;
    
    // ==================== ENUMS ====================
    
    public enum ChatRoomType {
        CUSTOMER_SUPPORT,       // Customer to support
        VENDOR_SUPPORT,         // Vendor to support
        ORDER_INQUIRY,          // Order-specific chat
        PRODUCT_INQUIRY,        // Product-specific chat
        DIRECT_MESSAGE,         // Direct customer-vendor chat
        GROUP_CHAT              // Multi-party chat
    }
    
    public enum RoomStatus {
        ACTIVE,
        WAITING_CUSTOMER,
        WAITING_AGENT,
        ON_HOLD,
        RESOLVED,
        CLOSED,
        ARCHIVED
    }
    
    // ==================== INNER CLASSES ====================
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Participant {
        private String id;
        private String name;
        private String email;
        private String avatarUrl;
        private ParticipantRole role;
        private LocalDateTime joinedAt;
        private LocalDateTime lastSeenAt;
        private boolean isOnline;
        private boolean isMuted;
    }
    
    public enum ParticipantRole {
        CUSTOMER,
        VENDOR,
        SUPPORT_AGENT,
        ADMIN,
        OBSERVER
    }
    
    // ==================== CONVENIENCE METHODS ====================
    
    public static ChatRoom createCustomerSupport(String customerId, String customerName, String customerEmail) {
        Participant customer = Participant.builder()
                .id(customerId)
                .name(customerName)
                .email(customerEmail)
                .role(ParticipantRole.CUSTOMER)
                .joinedAt(LocalDateTime.now())
                .isOnline(true)
                .build();
        
        return ChatRoom.builder()
                .roomType(ChatRoomType.CUSTOMER_SUPPORT)
                .title("Support Request from " + customerName)
                .participantIds(List.of(customerId))
                .participants(new ArrayList<>(List.of(customer)))
                .status(RoomStatus.WAITING_AGENT)
                .unreadCounts(new HashMap<>())
                .messageCount(0)
                .priority(3)
                .tags(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    public static ChatRoom createOrderInquiry(String customerId, String customerName, 
                                               String orderId, String orderInfo) {
        Participant customer = Participant.builder()
                .id(customerId)
                .name(customerName)
                .role(ParticipantRole.CUSTOMER)
                .joinedAt(LocalDateTime.now())
                .isOnline(true)
                .build();
        
        return ChatRoom.builder()
                .roomType(ChatRoomType.ORDER_INQUIRY)
                .title("Order Inquiry: " + orderId)
                .description(orderInfo)
                .orderId(orderId)
                .participantIds(List.of(customerId))
                .participants(new ArrayList<>(List.of(customer)))
                .status(RoomStatus.WAITING_AGENT)
                .unreadCounts(new HashMap<>())
                .messageCount(0)
                .priority(4)
                .tags(List.of("order", "inquiry"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    public static ChatRoom createVendorChat(String customerId, String customerName,
                                            String vendorId, String vendorName,
                                            String productId) {
        Participant customer = Participant.builder()
                .id(customerId)
                .name(customerName)
                .role(ParticipantRole.CUSTOMER)
                .joinedAt(LocalDateTime.now())
                .isOnline(true)
                .build();
        
        Participant vendor = Participant.builder()
                .id(vendorId)
                .name(vendorName)
                .role(ParticipantRole.VENDOR)
                .joinedAt(LocalDateTime.now())
                .build();
        
        return ChatRoom.builder()
                .roomType(ChatRoomType.PRODUCT_INQUIRY)
                .title("Chat with " + vendorName)
                .productId(productId)
                .vendorId(vendorId)
                .participantIds(List.of(customerId, vendorId))
                .participants(new ArrayList<>(List.of(customer, vendor)))
                .status(RoomStatus.ACTIVE)
                .unreadCounts(new HashMap<>())
                .messageCount(0)
                .priority(3)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    // ==================== HELPER METHODS ====================
    
    public void addParticipant(Participant participant) {
        if (participants == null) {
            participants = new ArrayList<>();
        }
        if (participantIds == null) {
            participantIds = new ArrayList<>();
        }
        
        if (!participantIds.contains(participant.getId())) {
            participants.add(participant);
            participantIds.add(participant.getId());
        }
    }
    
    public void incrementUnread(String participantId) {
        if (unreadCounts == null) {
            unreadCounts = new HashMap<>();
        }
        unreadCounts.merge(participantId, 1, Integer::sum);
    }
    
    public void resetUnread(String participantId) {
        if (unreadCounts != null) {
            unreadCounts.put(participantId, 0);
        }
    }
    
    public int getUnreadCount(String participantId) {
        return unreadCounts != null ? unreadCounts.getOrDefault(participantId, 0) : 0;
    }
}
