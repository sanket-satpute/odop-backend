package com.odop.root.chat.service;

import com.odop.root.chat.dto.ChatDto.*;
import com.odop.root.chat.model.*;
import com.odop.root.chat.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for chat operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {
    
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    
    // ==================== CHAT ROOM OPERATIONS ====================
    
    /**
     * Create a new chat room
     */
    @Transactional
    public ChatRoom createChatRoom(String userId, String userName, String userEmail, 
                                    String userRole, CreateChatRoomRequest request) {
        
        ChatRoom room;
        
        switch (request.getRoomType()) {
            case "CUSTOMER_SUPPORT":
                room = ChatRoom.createCustomerSupport(userId, userName, userEmail);
                if (request.getTitle() != null) {
                    room.setTitle(request.getTitle());
                }
                break;
                
            case "ORDER_INQUIRY":
                room = ChatRoom.createOrderInquiry(userId, userName, 
                        request.getOrderId(), request.getTitle());
                break;
                
            case "PRODUCT_INQUIRY":
            case "DIRECT_MESSAGE":
                // For vendor chat, check if room already exists
                if (request.getVendorId() != null) {
                    Optional<ChatRoom> existing = chatRoomRepository.findByProductAndParticipants(
                            request.getProductId(), userId, request.getVendorId(),
                            ChatRoom.ChatRoomType.PRODUCT_INQUIRY);
                    
                    if (existing.isPresent()) {
                        return existing.get();
                    }
                    
                    room = createVendorChatRoom(userId, userName, request);
                } else {
                    room = ChatRoom.createCustomerSupport(userId, userName, userEmail);
                    room.setRoomType(ChatRoom.ChatRoomType.valueOf(request.getRoomType()));
                }
                break;
                
            case "VENDOR_SUPPORT":
                room = ChatRoom.createCustomerSupport(userId, userName, userEmail);
                room.setRoomType(ChatRoom.ChatRoomType.VENDOR_SUPPORT);
                break;
                
            default:
                room = ChatRoom.createCustomerSupport(userId, userName, userEmail);
        }
        
        // Set additional properties
        if (request.getTags() != null) {
            room.setTags(request.getTags());
        }
        if (request.getPriority() > 0) {
            room.setPriority(request.getPriority());
        }
        
        room = chatRoomRepository.save(room);
        
        // Send initial message if provided
        if (request.getInitialMessage() != null && !request.getInitialMessage().isEmpty()) {
            sendMessage(room.getId(), userId, userName, 
                    "CUSTOMER".equals(userRole) ? ChatMessage.SenderType.CUSTOMER : ChatMessage.SenderType.VENDOR,
                    request.getInitialMessage(), ChatMessage.MessageType.TEXT);
        }
        
        // Send auto-response
        sendAutoResponse(room);
        
        log.info("Created chat room: {} for user: {}", room.getId(), userId);
        return room;
    }
    
    private ChatRoom createVendorChatRoom(String customerId, String customerName, CreateChatRoomRequest request) {
        ChatRoom.Participant customer = ChatRoom.Participant.builder()
                .id(customerId)
                .name(customerName)
                .role(ChatRoom.ParticipantRole.CUSTOMER)
                .joinedAt(LocalDateTime.now())
                .isOnline(true)
                .build();
        
        ChatRoom.Participant vendor = ChatRoom.Participant.builder()
                .id(request.getVendorId())
                .name("Vendor") // Would be fetched from vendor service
                .role(ChatRoom.ParticipantRole.VENDOR)
                .joinedAt(LocalDateTime.now())
                .build();
        
        return ChatRoom.builder()
                .roomType(ChatRoom.ChatRoomType.PRODUCT_INQUIRY)
                .title(request.getTitle() != null ? request.getTitle() : "Product Inquiry")
                .productId(request.getProductId())
                .vendorId(request.getVendorId())
                .participantIds(List.of(customerId, request.getVendorId()))
                .participants(new ArrayList<>(List.of(customer, vendor)))
                .status(ChatRoom.RoomStatus.ACTIVE)
                .unreadCounts(new HashMap<>())
                .messageCount(0)
                .priority(3)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * Get chat room by ID
     */
    public ChatRoom getChatRoom(String roomId) {
        return chatRoomRepository.findById(roomId).orElse(null);
    }
    
    /**
     * Get user's chat rooms
     */
    public List<ChatRoomResponse> getUserChatRooms(String userId, int page, int size) {
        Page<ChatRoom> rooms = chatRoomRepository
                .findByParticipantIdsContainingOrderByLastMessageAtDesc(
                        userId, PageRequest.of(page, size));
        
        return rooms.getContent().stream()
                .map(room -> ChatRoomResponse.from(room, userId))
                .collect(Collectors.toList());
    }
    
    /**
     * Get active chat rooms for user
     */
    public List<ChatRoomResponse> getActiveChats(String userId) {
        List<ChatRoom.RoomStatus> activeStatuses = List.of(
                ChatRoom.RoomStatus.ACTIVE,
                ChatRoom.RoomStatus.WAITING_CUSTOMER,
                ChatRoom.RoomStatus.WAITING_AGENT
        );
        
        return chatRoomRepository
                .findByParticipantIdsContainingAndStatusInOrderByLastMessageAtDesc(userId, activeStatuses)
                .stream()
                .map(room -> ChatRoomResponse.from(room, userId))
                .collect(Collectors.toList());
    }
    
    // ==================== MESSAGE OPERATIONS ====================
    
    /**
     * Send a message
     */
    @Transactional
    public ChatMessage sendMessage(String roomId, String senderId, String senderName,
                                   ChatMessage.SenderType senderType, String content,
                                   ChatMessage.MessageType messageType) {
        
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found"));
        
        // Create message
        ChatMessage message = ChatMessage.builder()
                .chatRoomId(roomId)
                .senderId(senderId)
                .senderName(senderName)
                .senderType(senderType)
                .content(content)
                .messageType(messageType)
                .status(ChatMessage.MessageStatus.SENT)
                .timestamp(LocalDateTime.now())
                .build();
        
        message = chatMessageRepository.save(message);
        
        // Update room
        room.setLastMessagePreview(truncate(content, 100));
        room.setLastMessageAt(message.getTimestamp());
        room.setMessageCount(room.getMessageCount() + 1);
        room.setUpdatedAt(LocalDateTime.now());
        
        // Update status based on sender
        if (senderType == ChatMessage.SenderType.CUSTOMER || senderType == ChatMessage.SenderType.VENDOR) {
            if (room.getStatus() == ChatRoom.RoomStatus.WAITING_CUSTOMER) {
                room.setStatus(ChatRoom.RoomStatus.WAITING_AGENT);
            }
        } else if (senderType == ChatMessage.SenderType.SUPPORT_AGENT || senderType == ChatMessage.SenderType.ADMIN) {
            if (room.getStatus() == ChatRoom.RoomStatus.WAITING_AGENT) {
                room.setStatus(ChatRoom.RoomStatus.WAITING_CUSTOMER);
            }
        }
        
        // Increment unread for other participants
        for (String participantId : room.getParticipantIds()) {
            if (!participantId.equals(senderId)) {
                room.incrementUnread(participantId);
            }
        }
        
        chatRoomRepository.save(room);
        
        // Broadcast message via WebSocket
        broadcastMessage(message);
        
        return message;
    }
    
    /**
     * Get messages for a chat room
     */
    public List<MessageResponse> getMessages(String roomId, int page, int size) {
        Page<ChatMessage> messages = chatMessageRepository
                .findByChatRoomIdOrderByTimestampDesc(roomId, PageRequest.of(page, size));
        
        List<MessageResponse> responses = messages.getContent().stream()
                .map(MessageResponse::from)
                .collect(Collectors.toList());
        
        // Reverse to get chronological order
        Collections.reverse(responses);
        return responses;
    }
    
    /**
     * Get recent messages
     */
    public List<MessageResponse> getRecentMessages(String roomId) {
        return chatMessageRepository.findTop50ByChatRoomIdOrderByTimestampDesc(roomId)
                .stream()
                .sorted(Comparator.comparing(ChatMessage::getTimestamp))
                .map(MessageResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * Mark messages as read
     */
    @Transactional
    public void markAsRead(String roomId, String userId) {
        List<ChatMessage> unread = chatMessageRepository.findUnreadMessages(roomId, userId);
        
        for (ChatMessage message : unread) {
            message.setStatus(ChatMessage.MessageStatus.READ);
            message.setReadAt(LocalDateTime.now());
        }
        
        chatMessageRepository.saveAll(unread);
        
        // Reset unread count
        ChatRoom room = chatRoomRepository.findById(roomId).orElse(null);
        if (room != null) {
            room.resetUnread(userId);
            chatRoomRepository.save(room);
        }
        
        // Broadcast read receipt
        if (!unread.isEmpty()) {
            List<String> messageIds = unread.stream().map(ChatMessage::getId).toList();
            ChatEvent event = ChatEvent.messagesRead(roomId, userId, messageIds);
            messagingTemplate.convertAndSend("/topic/chat/" + roomId, event);
        }
    }
    
    /**
     * Search messages
     */
    public List<MessageResponse> searchMessages(String roomId, String keyword) {
        return chatMessageRepository.searchMessages(roomId, keyword)
                .stream()
                .map(MessageResponse::from)
                .collect(Collectors.toList());
    }
    
    // ==================== SUPPORT OPERATIONS ====================
    
    /**
     * Get waiting tickets (for support agents)
     */
    public List<ChatRoomResponse> getWaitingTickets() {
        List<ChatRoom.ChatRoomType> supportTypes = List.of(
                ChatRoom.ChatRoomType.CUSTOMER_SUPPORT,
                ChatRoom.ChatRoomType.VENDOR_SUPPORT,
                ChatRoom.ChatRoomType.ORDER_INQUIRY
        );
        
        return chatRoomRepository
                .findByStatusAndRoomTypeInOrderByPriorityDescCreatedAtAsc(
                        ChatRoom.RoomStatus.WAITING_AGENT, supportTypes)
                .stream()
                .map(room -> ChatRoomResponse.from(room, null))
                .collect(Collectors.toList());
    }
    
    /**
     * Get agent's assigned tickets
     */
    public List<ChatRoomResponse> getAgentTickets(String agentId) {
        List<ChatRoom.RoomStatus> activeStatuses = List.of(
                ChatRoom.RoomStatus.ACTIVE,
                ChatRoom.RoomStatus.WAITING_CUSTOMER,
                ChatRoom.RoomStatus.ON_HOLD
        );
        
        return chatRoomRepository
                .findByAssignedAgentIdAndStatusInOrderByLastMessageAtDesc(agentId, activeStatuses)
                .stream()
                .map(room -> ChatRoomResponse.from(room, agentId))
                .collect(Collectors.toList());
    }
    
    /**
     * Assign agent to room
     */
    @Transactional
    public ChatRoom assignAgent(String roomId, String agentId, String agentName) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found"));
        
        room.setAssignedAgentId(agentId);
        room.setAssignedAgentName(agentName);
        room.setStatus(ChatRoom.RoomStatus.ACTIVE);
        room.setUpdatedAt(LocalDateTime.now());
        
        // Add agent as participant
        ChatRoom.Participant agent = ChatRoom.Participant.builder()
                .id(agentId)
                .name(agentName)
                .role(ChatRoom.ParticipantRole.SUPPORT_AGENT)
                .joinedAt(LocalDateTime.now())
                .isOnline(true)
                .build();
        room.addParticipant(agent);
        
        room = chatRoomRepository.save(room);
        
        // Send system message
        sendMessage(roomId, "SYSTEM", "System", ChatMessage.SenderType.SYSTEM,
                agentName + " has joined the conversation.", ChatMessage.MessageType.SYSTEM_MESSAGE);
        
        // Broadcast status change
        ChatEvent event = ChatEvent.roomStatusChanged(roomId, room.getStatus().name());
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, event);
        
        return room;
    }
    
    /**
     * Close/resolve ticket
     */
    @Transactional
    public ChatRoom closeTicket(String roomId, String resolution, String userId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found"));
        
        room.setStatus(ChatRoom.RoomStatus.RESOLVED);
        room.setClosedAt(LocalDateTime.now());
        room.setUpdatedAt(LocalDateTime.now());
        
        if (room.getMetadata() == null) {
            room.setMetadata(new HashMap<>());
        }
        room.getMetadata().put("resolution", resolution);
        room.getMetadata().put("resolvedBy", userId);
        
        room = chatRoomRepository.save(room);
        
        // Send system message
        sendMessage(roomId, "SYSTEM", "System", ChatMessage.SenderType.SYSTEM,
                "This conversation has been resolved. Thank you for contacting ODOP Support!",
                ChatMessage.MessageType.SYSTEM_MESSAGE);
        
        return room;
    }
    
    // ==================== WEBSOCKET OPERATIONS ====================
    
    /**
     * Broadcast message to room participants
     */
    private void broadcastMessage(ChatMessage message) {
        ChatEvent event = ChatEvent.newMessage(message);
        messagingTemplate.convertAndSend("/topic/chat/" + message.getChatRoomId(), event);
    }
    
    /**
     * Send typing indicator
     */
    public void sendTypingIndicator(String roomId, String userId, String userName, boolean isTyping) {
        ChatEvent event = ChatEvent.typing(roomId, userId, userName, isTyping);
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, event);
    }
    
    /**
     * Update user online status
     */
    public void updateOnlineStatus(String roomId, String userId, boolean isOnline) {
        ChatRoom room = chatRoomRepository.findById(roomId).orElse(null);
        if (room != null && room.getParticipants() != null) {
            room.getParticipants().stream()
                    .filter(p -> p.getId().equals(userId))
                    .findFirst()
                    .ifPresent(p -> {
                        p.setOnline(isOnline);
                        p.setLastSeenAt(LocalDateTime.now());
                    });
            chatRoomRepository.save(room);
        }
    }
    
    // ==================== AUTO-RESPONSE ====================
    
    /**
     * Send auto-response based on room type
     */
    private void sendAutoResponse(ChatRoom room) {
        String message = switch (room.getRoomType()) {
            case CUSTOMER_SUPPORT -> 
                "Welcome to ODOP Support! A support agent will be with you shortly. " +
                "In the meantime, you can describe your issue in detail.";
            case ORDER_INQUIRY -> 
                "Thank you for your order inquiry. A support agent will review your " +
                "order details and respond shortly.";
            case PRODUCT_INQUIRY -> 
                "Thank you for your interest! The vendor has been notified and " +
                "will respond to your inquiry soon.";
            case VENDOR_SUPPORT -> 
                "Welcome to ODOP Vendor Support! Please describe your issue and " +
                "our team will assist you shortly.";
            default -> 
                "Thank you for contacting ODOP. We'll respond as soon as possible.";
        };
        
        ChatMessage autoMessage = ChatMessage.createAutoResponse(room.getId(), message);
        chatMessageRepository.save(autoMessage);
        
        // Update room
        room.setLastMessagePreview(truncate(message, 100));
        room.setLastMessageAt(autoMessage.getTimestamp());
        room.setMessageCount(room.getMessageCount() + 1);
        chatRoomRepository.save(room);
    }
    
    // ==================== STATISTICS ====================
    
    /**
     * Get chat statistics for dashboard
     */
    public Map<String, Object> getChatStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Waiting tickets count
        long waitingCount = chatRoomRepository.countByStatusAndRoomTypeIn(
                ChatRoom.RoomStatus.WAITING_AGENT,
                List.of(ChatRoom.ChatRoomType.CUSTOMER_SUPPORT, 
                        ChatRoom.ChatRoomType.VENDOR_SUPPORT,
                        ChatRoom.ChatRoomType.ORDER_INQUIRY));
        
        stats.put("waitingTickets", waitingCount);
        stats.put("urgentTickets", chatRoomRepository.countOpenTicketsByPriority(5));
        stats.put("highPriorityTickets", chatRoomRepository.countOpenTicketsByPriority(4));
        
        return stats;
    }
    
    // ==================== HELPERS ====================
    
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 3) + "...";
    }
}
