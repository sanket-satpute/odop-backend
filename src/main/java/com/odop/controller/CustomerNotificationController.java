package com.odop.controller;

import com.odop.model.CustomerNotification;
import com.odop.service.CustomerNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/odop/customer/{customerId}/notifications")
@PreAuthorize("hasRole('CUSTOMER')")
@CrossOrigin(origins = "*")
public class CustomerNotificationController {

    @Autowired
    private CustomerNotificationService notificationService;

    // ================================
    // Fetch Endpoints
    // ================================

    /**
     * Get all notifications for a customer
     */
    @GetMapping
    public ResponseEntity<?> getNotifications(@PathVariable String customerId) {
        try {
            List<CustomerNotification> notifications = notificationService.getNotifications(customerId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch notifications: " + e.getMessage()));
        }
    }

    /**
     * Get notifications with pagination
     */
    @GetMapping("/paginated")
    public ResponseEntity<?> getNotificationsPaginated(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<CustomerNotification> notifications = notificationService.getNotificationsPaginated(customerId, page, size);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch notifications: " + e.getMessage()));
        }
    }

    /**
     * Get unread notifications
     */
    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadNotifications(@PathVariable String customerId) {
        try {
            List<CustomerNotification> notifications = notificationService.getUnreadNotifications(customerId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch unread notifications: " + e.getMessage()));
        }
    }

    /**
     * Get unread count
     */
    @GetMapping("/unread/count")
    public ResponseEntity<?> getUnreadCount(@PathVariable String customerId) {
        try {
            long count = notificationService.getUnreadCount(customerId);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get unread count: " + e.getMessage()));
        }
    }

    /**
     * Get notification statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getNotificationStats(@PathVariable String customerId) {
        try {
            Map<String, Object> stats = notificationService.getNotificationStats(customerId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get notification stats: " + e.getMessage()));
        }
    }

    /**
     * Get notifications by type
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<?> getNotificationsByType(
            @PathVariable String customerId,
            @PathVariable String type) {
        try {
            List<CustomerNotification> notifications = notificationService.getNotificationsByType(customerId, type);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch notifications by type: " + e.getMessage()));
        }
    }

    /**
     * Get single notification by ID
     */
    @GetMapping("/{notificationId}")
    public ResponseEntity<?> getNotification(
            @PathVariable String customerId,
            @PathVariable String notificationId) {
        try {
            Optional<CustomerNotification> notification = notificationService.getNotificationById(notificationId);
            if (notification.isPresent()) {
                return ResponseEntity.ok(notification.get());
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Notification not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch notification: " + e.getMessage()));
        }
    }

    // ================================
    // Create Endpoints
    // ================================

    /**
     * Create a custom notification (for testing/internal use)
     */
    @PostMapping
    public ResponseEntity<?> createNotification(
            @PathVariable String customerId,
            @RequestBody CustomerNotification notification) {
        try {
            notification.setCustomerId(customerId);
            CustomerNotification created = notificationService.createNotification(notification);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to create notification: " + e.getMessage()));
        }
    }

    // ================================
    // Update Endpoints
    // ================================

    /**
     * Mark notification as read
     */
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable String customerId,
            @PathVariable String notificationId) {
        try {
            CustomerNotification notification = notificationService.markAsRead(notificationId);
            if (notification != null) {
                return ResponseEntity.ok(notification);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Notification not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to mark as read: " + e.getMessage()));
        }
    }

    /**
     * Mark all notifications as read
     */
    @PatchMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(@PathVariable String customerId) {
        try {
            int count = notificationService.markAllAsRead(customerId);
            return ResponseEntity.ok(Map.of(
                "message", count + " notifications marked as read",
                "count", count
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to mark all as read: " + e.getMessage()));
        }
    }

    /**
     * Mark notifications of a specific type as read
     */
    @PatchMapping("/type/{type}/read")
    public ResponseEntity<?> markTypeAsRead(
            @PathVariable String customerId,
            @PathVariable String type) {
        try {
            int count = notificationService.markTypeAsRead(customerId, type);
            return ResponseEntity.ok(Map.of(
                "message", count + " " + type + " notifications marked as read",
                "count", count
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to mark type as read: " + e.getMessage()));
        }
    }

    // ================================
    // Delete Endpoints
    // ================================

    /**
     * Delete (soft) a notification
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteNotification(
            @PathVariable String customerId,
            @PathVariable String notificationId) {
        try {
            boolean deleted = notificationService.deleteNotification(notificationId);
            if (deleted) {
                return ResponseEntity.ok(Map.of(
                    "message", "Notification deleted",
                    "notificationId", notificationId
                ));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Notification not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to delete notification: " + e.getMessage()));
        }
    }

    /**
     * Restore a deleted notification
     */
    @PostMapping("/{notificationId}/restore")
    public ResponseEntity<?> restoreNotification(
            @PathVariable String customerId,
            @PathVariable String notificationId) {
        try {
            CustomerNotification restored = notificationService.restoreNotification(notificationId);
            if (restored != null) {
                return ResponseEntity.ok(restored);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Notification not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to restore notification: " + e.getMessage()));
        }
    }

    /**
     * Delete all notifications
     */
    @DeleteMapping("/all")
    public ResponseEntity<?> deleteAllNotifications(@PathVariable String customerId) {
        try {
            int count = notificationService.deleteAllNotifications(customerId);
            return ResponseEntity.ok(Map.of(
                "message", count + " notifications deleted",
                "count", count
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to delete notifications: " + e.getMessage()));
        }
    }

    /**
     * Cleanup old notifications
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<?> cleanupNotifications(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "30") int daysOld) {
        try {
            long deleted = notificationService.cleanupOldNotifications(customerId, daysOld);
            int expired = notificationService.deleteExpiredNotifications(customerId);
            return ResponseEntity.ok(Map.of(
                "message", "Cleanup completed",
                "oldDeleted", deleted,
                "expiredDeleted", expired
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to cleanup notifications: " + e.getMessage()));
        }
    }

    // ================================
    // Related Notifications Endpoints
    // ================================

    /**
     * Get notifications for a specific order
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getOrderNotifications(
            @PathVariable String customerId,
            @PathVariable String orderId) {
        try {
            List<CustomerNotification> notifications = notificationService.getOrderNotifications(customerId, orderId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch order notifications: " + e.getMessage()));
        }
    }

    /**
     * Get notifications for a support ticket
     */
    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<?> getTicketNotifications(
            @PathVariable String customerId,
            @PathVariable String ticketId) {
        try {
            List<CustomerNotification> notifications = notificationService.getTicketNotifications(customerId, ticketId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch ticket notifications: " + e.getMessage()));
        }
    }
}
