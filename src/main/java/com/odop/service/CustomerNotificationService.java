package com.odop.service;

import com.odop.model.CustomerNotification;
import com.odop.repository.CustomerNotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CustomerNotificationService {

    @Autowired
    private CustomerNotificationRepository notificationRepository;

    // ================================
    // Fetch Operations
    // ================================

    /**
     * Get all notifications for a customer
     */
    public List<CustomerNotification> getNotifications(String customerId) {
        return notificationRepository.findByCustomerIdAndDeletedFalseOrderByCreatedAtDesc(customerId);
    }

    /**
     * Get notifications with pagination
     */
    public Page<CustomerNotification> getNotificationsPaginated(String customerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return notificationRepository.findByCustomerIdAndDeletedFalseOrderByCreatedAtDesc(customerId, pageable);
    }

    /**
     * Get unread notifications
     */
    public List<CustomerNotification> getUnreadNotifications(String customerId) {
        return notificationRepository.findByCustomerIdAndReadFalseAndDeletedFalseOrderByCreatedAtDesc(customerId);
    }

    /**
     * Get notifications by type
     */
    public List<CustomerNotification> getNotificationsByType(String customerId, String type) {
        return notificationRepository.findByCustomerIdAndTypeAndDeletedFalseOrderByCreatedAtDesc(customerId, type);
    }

    /**
     * Get notifications by multiple types
     */
    public List<CustomerNotification> getNotificationsByTypes(String customerId, List<String> types) {
        return notificationRepository.findByCustomerIdAndTypeIn(customerId, types);
    }

    /**
     * Get notification by ID
     */
    public Optional<CustomerNotification> getNotificationById(String notificationId) {
        return notificationRepository.findById(notificationId);
    }

    /**
     * Get unread count
     */
    public long getUnreadCount(String customerId) {
        return notificationRepository.countByCustomerIdAndReadFalseAndDeletedFalse(customerId);
    }

    /**
     * Get notification statistics
     */
    public Map<String, Object> getNotificationStats(String customerId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", notificationRepository.countByCustomerIdAndDeletedFalse(customerId));
        stats.put("unread", notificationRepository.countByCustomerIdAndReadFalseAndDeletedFalse(customerId));
        stats.put("highPriority", notificationRepository.findHighPriorityUnread(customerId, 7).size());
        return stats;
    }

    // ================================
    // Create Operations
    // ================================

    /**
     * Create a notification
     */
    @Transactional
    public CustomerNotification createNotification(CustomerNotification notification) {
        notification.setCreatedAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    /**
     * Create order notification
     */
    @Transactional
    public CustomerNotification createOrderNotification(String customerId, String orderId, String title, String message) {
        CustomerNotification notification = CustomerNotification.orderNotification(customerId, orderId, title, message);
        return notificationRepository.save(notification);
    }

    /**
     * Create wallet notification
     */
    @Transactional
    public CustomerNotification createWalletNotification(String customerId, String title, String message) {
        CustomerNotification notification = CustomerNotification.walletNotification(customerId, title, message);
        return notificationRepository.save(notification);
    }

    /**
     * Create promotion notification
     */
    @Transactional
    public CustomerNotification createPromotionNotification(String customerId, String title, String message, String actionLink) {
        CustomerNotification notification = CustomerNotification.promotionNotification(customerId, title, message, actionLink);
        return notificationRepository.save(notification);
    }

    /**
     * Create security notification
     */
    @Transactional
    public CustomerNotification createSecurityNotification(String customerId, String title, String message) {
        CustomerNotification notification = CustomerNotification.securityNotification(customerId, title, message);
        return notificationRepository.save(notification);
    }

    /**
     * Create review request notification
     */
    @Transactional
    public CustomerNotification createReviewNotification(String customerId, String orderId, String productId, String title, String message) {
        CustomerNotification notification = CustomerNotification.reviewNotification(customerId, orderId, productId, title, message);
        return notificationRepository.save(notification);
    }

    /**
     * Create support ticket notification
     */
    @Transactional
    public CustomerNotification createSupportNotification(String customerId, String ticketId, String title, String message) {
        CustomerNotification notification = CustomerNotification.supportNotification(customerId, ticketId, title, message);
        return notificationRepository.save(notification);
    }

    /**
     * Create wishlist notification (price drop, back in stock)
     */
    @Transactional
    public CustomerNotification createWishlistNotification(String customerId, String productId, String title, String message) {
        CustomerNotification notification = CustomerNotification.wishlistNotification(customerId, productId, title, message);
        return notificationRepository.save(notification);
    }

    /**
     * Create system notification
     */
    @Transactional
    public CustomerNotification createSystemNotification(String customerId, String title, String message) {
        CustomerNotification notification = CustomerNotification.systemNotification(customerId, title, message);
        return notificationRepository.save(notification);
    }

    // ================================
    // Update Operations
    // ================================

    /**
     * Mark notification as read
     */
    @Transactional
    public CustomerNotification markAsRead(String notificationId) {
        Optional<CustomerNotification> optional = notificationRepository.findById(notificationId);
        if (optional.isPresent()) {
            CustomerNotification notification = optional.get();
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            return notificationRepository.save(notification);
        }
        return null;
    }

    /**
     * Mark all notifications as read for a customer
     */
    @Transactional
    public int markAllAsRead(String customerId) {
        List<CustomerNotification> unread = notificationRepository.findByCustomerIdAndReadFalseAndDeletedFalseOrderByCreatedAtDesc(customerId);
        LocalDateTime now = LocalDateTime.now();
        int count = 0;
        
        for (CustomerNotification notification : unread) {
            notification.setRead(true);
            notification.setReadAt(now);
            notificationRepository.save(notification);
            count++;
        }
        
        return count;
    }

    /**
     * Mark notifications of a specific type as read
     */
    @Transactional
    public int markTypeAsRead(String customerId, String type) {
        List<CustomerNotification> notifications = notificationRepository.findByCustomerIdAndTypeAndDeletedFalseOrderByCreatedAtDesc(customerId, type);
        LocalDateTime now = LocalDateTime.now();
        int count = 0;
        
        for (CustomerNotification notification : notifications) {
            if (!notification.isRead()) {
                notification.setRead(true);
                notification.setReadAt(now);
                notificationRepository.save(notification);
                count++;
            }
        }
        
        return count;
    }

    // ================================
    // Delete Operations
    // ================================

    /**
     * Soft delete a notification
     */
    @Transactional
    public boolean deleteNotification(String notificationId) {
        Optional<CustomerNotification> optional = notificationRepository.findById(notificationId);
        if (optional.isPresent()) {
            CustomerNotification notification = optional.get();
            notification.setDeleted(true);
            notificationRepository.save(notification);
            return true;
        }
        return false;
    }

    /**
     * Restore a soft-deleted notification
     */
    @Transactional
    public CustomerNotification restoreNotification(String notificationId) {
        Optional<CustomerNotification> optional = notificationRepository.findById(notificationId);
        if (optional.isPresent()) {
            CustomerNotification notification = optional.get();
            notification.setDeleted(false);
            return notificationRepository.save(notification);
        }
        return null;
    }

    /**
     * Delete all notifications for a customer (soft delete)
     */
    @Transactional
    public int deleteAllNotifications(String customerId) {
        List<CustomerNotification> notifications = notificationRepository.findByCustomerIdAndDeletedFalseOrderByCreatedAtDesc(customerId);
        int count = 0;
        
        for (CustomerNotification notification : notifications) {
            notification.setDeleted(true);
            notificationRepository.save(notification);
            count++;
        }
        
        return count;
    }

    /**
     * Cleanup old read notifications (runs periodically)
     */
    @Transactional
    public long cleanupOldNotifications(String customerId, int daysOld) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysOld);
        return notificationRepository.deleteOldReadNotifications(customerId, cutoff);
    }

    /**
     * Delete expired notifications
     */
    @Transactional
    public int deleteExpiredNotifications(String customerId) {
        List<CustomerNotification> expired = notificationRepository.findExpiredNotifications(customerId, LocalDateTime.now());
        int count = 0;
        
        for (CustomerNotification notification : expired) {
            notification.setDeleted(true);
            notificationRepository.save(notification);
            count++;
        }
        
        return count;
    }

    // ================================
    // Bulk Operations for System Events
    // ================================

    /**
     * Send notification to multiple customers (for promotions, announcements)
     */
    @Transactional
    public void sendBulkPromotion(List<String> customerIds, String title, String message, String actionLink, LocalDateTime expiresAt) {
        for (String customerId : customerIds) {
            CustomerNotification notification = CustomerNotification.promotionNotification(customerId, title, message, actionLink);
            notification.setExpiresAt(expiresAt);
            notificationRepository.save(notification);
        }
    }

    /**
     * Send system announcement to all customers
     */
    @Transactional
    public CustomerNotification sendSystemAnnouncement(String customerId, String title, String message) {
        return createSystemNotification(customerId, title, message);
    }

    // ================================
    // Utility Methods
    // ================================

    /**
     * Check if notification exists for order (to prevent duplicates)
     */
    public boolean notificationExistsForOrder(String customerId, String orderId, String type) {
        return notificationRepository.existsByCustomerIdAndOrderIdAndType(customerId, orderId, type);
    }

    /**
     * Get notifications related to a specific order
     */
    public List<CustomerNotification> getOrderNotifications(String customerId, String orderId) {
        return notificationRepository.findByCustomerIdAndOrderIdAndDeletedFalse(customerId, orderId);
    }

    /**
     * Get notifications related to a support ticket
     */
    public List<CustomerNotification> getTicketNotifications(String customerId, String ticketId) {
        return notificationRepository.findByCustomerIdAndTicketIdAndDeletedFalse(customerId, ticketId);
    }
}
