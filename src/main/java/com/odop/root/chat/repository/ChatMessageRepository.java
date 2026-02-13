package com.odop.root.chat.repository;

import com.odop.root.chat.model.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for chat messages
 */
@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    
    /**
     * Find messages by chat room
     */
    List<ChatMessage> findByChatRoomIdOrderByTimestampAsc(String chatRoomId);
    
    /**
     * Find messages by chat room with pagination
     */
    Page<ChatMessage> findByChatRoomIdOrderByTimestampDesc(String chatRoomId, Pageable pageable);
    
    /**
     * Find recent messages
     */
    List<ChatMessage> findTop50ByChatRoomIdOrderByTimestampDesc(String chatRoomId);
    
    /**
     * Find messages after a timestamp
     */
    List<ChatMessage> findByChatRoomIdAndTimestampAfterOrderByTimestampAsc(
            String chatRoomId, LocalDateTime timestamp);
    
    /**
     * Find unread messages for a user in a room
     */
    @Query("{ 'chatRoomId': ?0, 'senderId': { $ne: ?1 }, 'status': { $ne: 'READ' } }")
    List<ChatMessage> findUnreadMessages(String chatRoomId, String userId);
    
    /**
     * Count unread messages
     */
    @Query(value = "{ 'chatRoomId': ?0, 'senderId': { $ne: ?1 }, 'status': { $ne: 'READ' } }", count = true)
    long countUnreadMessages(String chatRoomId, String userId);
    
    /**
     * Find messages by sender
     */
    List<ChatMessage> findBySenderIdOrderByTimestampDesc(String senderId);
    
    /**
     * Find messages containing keyword (search)
     */
    @Query("{ 'chatRoomId': ?0, 'content': { $regex: ?1, $options: 'i' } }")
    List<ChatMessage> searchMessages(String chatRoomId, String keyword);
    
    /**
     * Delete all messages in a chat room
     */
    void deleteByChatRoomId(String chatRoomId);
    
    /**
     * Find last message in room
     */
    ChatMessage findTopByChatRoomIdOrderByTimestampDesc(String chatRoomId);
    
    /**
     * Count messages in room
     */
    long countByChatRoomId(String chatRoomId);
}
