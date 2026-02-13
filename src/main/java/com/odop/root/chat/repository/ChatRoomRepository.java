package com.odop.root.chat.repository;

import com.odop.root.chat.model.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for chat rooms
 */
@Repository
public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {
    
    /**
     * Find rooms by participant
     */
    List<ChatRoom> findByParticipantIdsContaining(String participantId);
    
    /**
     * Find rooms by participant with pagination
     */
    Page<ChatRoom> findByParticipantIdsContainingOrderByLastMessageAtDesc(
            String participantId, Pageable pageable);
    
    /**
     * Find active rooms for participant
     */
    List<ChatRoom> findByParticipantIdsContainingAndStatusInOrderByLastMessageAtDesc(
            String participantId, List<ChatRoom.RoomStatus> statuses);
    
    /**
     * Find rooms by status
     */
    List<ChatRoom> findByStatusOrderByCreatedAtDesc(ChatRoom.RoomStatus status);
    
    /**
     * Find waiting rooms (for support agents)
     */
    List<ChatRoom> findByStatusAndRoomTypeInOrderByPriorityDescCreatedAtAsc(
            ChatRoom.RoomStatus status, List<ChatRoom.ChatRoomType> types);
    
    /**
     * Find rooms assigned to agent
     */
    List<ChatRoom> findByAssignedAgentIdAndStatusInOrderByLastMessageAtDesc(
            String agentId, List<ChatRoom.RoomStatus> statuses);
    
    /**
     * Find by order ID
     */
    Optional<ChatRoom> findByOrderIdAndRoomType(String orderId, ChatRoom.ChatRoomType roomType);
    
    /**
     * Find by product ID and participants
     */
    @Query("{ 'productId': ?0, 'participantIds': { $all: [?1, ?2] }, 'roomType': ?3 }")
    Optional<ChatRoom> findByProductAndParticipants(
            String productId, String customerId, String vendorId, ChatRoom.ChatRoomType roomType);
    
    /**
     * Find direct chat between two users
     */
    @Query("{ 'participantIds': { $all: [?0, ?1] }, 'roomType': 'DIRECT_MESSAGE' }")
    Optional<ChatRoom> findDirectChat(String userId1, String userId2);
    
    /**
     * Count active chats for user
     */
    long countByParticipantIdsContainingAndStatusIn(
            String participantId, List<ChatRoom.RoomStatus> statuses);
    
    /**
     * Count waiting support tickets
     */
    long countByStatusAndRoomTypeIn(
            ChatRoom.RoomStatus status, List<ChatRoom.ChatRoomType> types);
    
    /**
     * Find rooms updated after timestamp
     */
    List<ChatRoom> findByParticipantIdsContainingAndUpdatedAtAfter(
            String participantId, LocalDateTime timestamp);
    
    /**
     * Find by vendor ID
     */
    List<ChatRoom> findByVendorIdOrderByLastMessageAtDesc(String vendorId);
    
    /**
     * Count open tickets by priority
     */
    @Query(value = "{ 'status': { $in: ['ACTIVE', 'WAITING_CUSTOMER', 'WAITING_AGENT'] }, 'priority': ?0 }", count = true)
    long countOpenTicketsByPriority(int priority);
}
