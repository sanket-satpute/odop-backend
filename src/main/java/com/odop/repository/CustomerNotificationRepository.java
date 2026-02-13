package com.odop.repository;

import com.odop.model.CustomerNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CustomerNotificationRepository extends MongoRepository<CustomerNotification, String> {

    /**
     * Find all notifications for a customer (not deleted), ordered by creation time desc
     */
    List<CustomerNotification> findByCustomerIdAndDeletedFalseOrderByCreatedAtDesc(String customerId);

    /**
     * Find notifications for a customer with pagination
     */
    Page<CustomerNotification> findByCustomerIdAndDeletedFalseOrderByCreatedAtDesc(String customerId, Pageable pageable);

    /**
     * Find unread notifications for a customer
     */
    List<CustomerNotification> findByCustomerIdAndReadFalseAndDeletedFalseOrderByCreatedAtDesc(String customerId);

    /**
     * Find notifications by type
     */
    List<CustomerNotification> findByCustomerIdAndTypeAndDeletedFalseOrderByCreatedAtDesc(String customerId, String type);

    /**
     * Find notifications by category
     */
    List<CustomerNotification> findByCustomerIdAndCategoryAndDeletedFalseOrderByCreatedAtDesc(String customerId, String category);

    /**
     * Count unread notifications
     */
    long countByCustomerIdAndReadFalseAndDeletedFalse(String customerId);

    /**
     * Count total notifications (not deleted)
     */
    long countByCustomerIdAndDeletedFalse(String customerId);

    /**
     * Find notifications created after a certain date
     */
    List<CustomerNotification> findByCustomerIdAndCreatedAtAfterAndDeletedFalseOrderByCreatedAtDesc(
        String customerId, LocalDateTime after);

    /**
     * Find notifications related to a specific order
     */
    List<CustomerNotification> findByCustomerIdAndOrderIdAndDeletedFalse(String customerId, String orderId);

    /**
     * Find notifications related to a support ticket
     */
    List<CustomerNotification> findByCustomerIdAndTicketIdAndDeletedFalse(String customerId, String ticketId);

    /**
     * Find expired notifications
     */
    @Query("{ 'customerId': ?0, 'expiresAt': { $lt: ?1 }, 'deleted': false }")
    List<CustomerNotification> findExpiredNotifications(String customerId, LocalDateTime now);

    /**
     * Find high priority unread notifications
     */
    @Query("{ 'customerId': ?0, 'read': false, 'deleted': false, 'priority': { $gte: ?1 } }")
    List<CustomerNotification> findHighPriorityUnread(String customerId, int minPriority);

    /**
     * Find notifications by multiple types
     */
    @Query("{ 'customerId': ?0, 'type': { $in: ?1 }, 'deleted': false }")
    List<CustomerNotification> findByCustomerIdAndTypeIn(String customerId, List<String> types);

    /**
     * Delete old read notifications (cleanup)
     */
    @Query(value = "{ 'customerId': ?0, 'read': true, 'createdAt': { $lt: ?1 } }", delete = true)
    long deleteOldReadNotifications(String customerId, LocalDateTime before);

    /**
     * Soft delete all notifications for a customer
     */
    @Query("{ 'customerId': ?0 }")
    List<CustomerNotification> findAllByCustomerId(String customerId);

    /**
     * Find recent promotion notifications (to avoid duplicates)
     */
    @Query("{ 'customerId': ?0, 'type': 'promotion', 'createdAt': { $gte: ?1 } }")
    List<CustomerNotification> findRecentPromotions(String customerId, LocalDateTime since);

    /**
     * Check if notification exists for order and type
     */
    boolean existsByCustomerIdAndOrderIdAndType(String customerId, String orderId, String type);
}
