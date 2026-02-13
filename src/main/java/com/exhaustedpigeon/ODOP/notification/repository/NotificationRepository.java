package com.exhaustedpigeon.ODOP.notification.repository;

import com.exhaustedpigeon.ODOP.notification.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    
    // Find by user
    Page<Notification> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);
    
    // Find unread notifications
    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(String userId);
    
    Page<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(String userId, Pageable pageable);
    
    // Count unread
    long countByUserIdAndReadFalse(String userId);
    
    // Find by type
    List<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(String userId, Notification.NotificationType type);
    
    // Find by category
    List<Notification> findByUserIdAndCategoryOrderByCreatedAtDesc(String userId, String category);
    
    // Find by reference
    List<Notification> findByReferenceIdAndReferenceType(String referenceId, String referenceType);
    
    // Find by date range
    @Query("{'userId': ?0, 'createdAt': {'$gte': ?1, '$lte': ?2}}")
    List<Notification> findByUserIdAndDateRange(String userId, LocalDateTime start, LocalDateTime end);
    
    // Find expired notifications
    List<Notification> findByExpiresAtBeforeAndReadFalse(LocalDateTime now);
    
    // Delete old notifications
    void deleteByCreatedAtBefore(LocalDateTime before);
    
    // Find by priority
    List<Notification> findByUserIdAndPriorityOrderByCreatedAtDesc(String userId, Notification.NotificationPriority priority);
    
    // Count by type for a user
    long countByUserIdAndType(String userId, Notification.NotificationType type);
    
    // Find recent notifications (last 24 hours)
    @Query("{'userId': ?0, 'createdAt': {'$gte': ?1}}")
    List<Notification> findRecentByUserId(String userId, LocalDateTime since);
}
