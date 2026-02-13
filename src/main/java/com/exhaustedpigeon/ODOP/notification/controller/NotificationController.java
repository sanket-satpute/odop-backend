package com.exhaustedpigeon.ODOP.notification.controller;

import com.exhaustedpigeon.ODOP.notification.dto.*;
import com.exhaustedpigeon.ODOP.notification.model.Notification;
import com.exhaustedpigeon.ODOP.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * Controller for notification endpoints
 */
@RestController
@RequestMapping("/odop/notifications")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class NotificationController {
    
    private final NotificationService notificationService;
    
    // ==================== USER NOTIFICATIONS ====================
    
    /**
     * Get notifications for authenticated user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<NotificationDto>> getUserNotifications(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Getting notifications for user: {}", userId);
        Page<NotificationDto> notifications = notificationService.getUserNotifications(userId, page, size);
        return ResponseEntity.ok(notifications);
    }
    
    /**
     * Get unread notifications count
     */
    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@PathVariable String userId) {
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }
    
    /**
     * Get unread notifications
     */
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<NotificationDto>> getUnreadNotifications(@PathVariable String userId) {
        List<NotificationDto> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }
    
    /**
     * Get notifications by type
     */
    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<NotificationDto>> getNotificationsByType(
            @PathVariable String userId,
            @PathVariable Notification.NotificationType type) {
        
        List<NotificationDto> notifications = notificationService.getNotificationsByType(userId, type);
        return ResponseEntity.ok(notifications);
    }
    
    // ==================== MARK AS READ ====================
    
    /**
     * Mark notification as read
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable String notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Mark all notifications as read
     */
    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<Void> markAllAsRead(@PathVariable String userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }
    
    // ==================== DELETE NOTIFICATIONS ====================
    
    /**
     * Delete a notification
     */
    @DeleteMapping("/{notificationId}/user/{userId}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable String notificationId,
            @PathVariable String userId) {
        
        notificationService.deleteNotification(notificationId, userId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Clear all notifications
     */
    @DeleteMapping("/user/{userId}/clear-all")
    public ResponseEntity<Void> clearAllNotifications(@PathVariable String userId) {
        notificationService.clearAllNotifications(userId);
        return ResponseEntity.ok().build();
    }
    
    // ==================== PREFERENCES ====================
    
    /**
     * Get notification preferences
     */
    @GetMapping("/preferences/{userId}")
    public ResponseEntity<NotificationPreferenceDto> getPreferences(@PathVariable String userId) {
        NotificationPreferenceDto preferences = notificationService.getPreferences(userId);
        if (preferences == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(preferences);
    }
    
    /**
     * Update notification preferences
     */
    @PutMapping("/preferences/{userId}")
    public ResponseEntity<NotificationPreferenceDto> updatePreferences(
            @PathVariable String userId,
            @RequestBody NotificationPreferenceDto preferences) {
        
        log.info("Updating notification preferences for user: {}", userId);
        NotificationPreferenceDto updated = notificationService.updatePreferences(userId, preferences);
        return ResponseEntity.ok(updated);
    }
    
    // ==================== DEVICE REGISTRATION ====================
    
    /**
     * Register device for push notifications
     */
    @PostMapping("/devices/{userId}")
    public ResponseEntity<Void> registerDevice(
            @PathVariable String userId,
            @Valid @RequestBody RegisterDeviceRequest request) {
        
        log.info("Registering device for user: {}", userId);
        notificationService.registerDevice(userId, request);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Unregister device
     */
    @DeleteMapping("/devices/{userId}")
    public ResponseEntity<Void> unregisterDevice(
            @PathVariable String userId,
            @RequestParam String deviceToken) {
        
        log.info("Unregistering device for user: {}", userId);
        notificationService.unregisterDevice(userId, deviceToken);
        return ResponseEntity.ok().build();
    }
    
    // ==================== ADMIN ENDPOINTS ====================
    
    /**
     * Send notification to a specific user (Admin only)
     */
    @PostMapping("/admin/send")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationDto> sendNotification(
            @Valid @RequestBody SendNotificationRequest request) {
        
        log.info("Admin sending notification to user: {}", request.getUserId());
        NotificationDto notification = notificationService.sendToUser(request.getUserId(), request);
        return ResponseEntity.ok(notification);
    }
    
    /**
     * Send notification to multiple users (Admin only)
     */
    @PostMapping("/admin/send-bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> sendBulkNotification(
            @Valid @RequestBody SendNotificationRequest request) {
        
        log.info("Admin sending bulk notification");
        
        if (request.isSendToAll()) {
            notificationService.broadcast(request);
            return ResponseEntity.ok(Map.of("message", "Broadcasting to all users"));
        } else if (request.getUserType() != null) {
            notificationService.sendToUserType(request.getUserType(), request);
            return ResponseEntity.ok(Map.of("message", "Sending to all " + request.getUserType() + " users"));
        } else if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
            notificationService.sendToUsers(request.getUserIds(), request);
            return ResponseEntity.ok(Map.of("message", "Sending to " + request.getUserIds().size() + " users"));
        }
        
        return ResponseEntity.badRequest().body(Map.of("error", "No target specified"));
    }
    
    /**
     * Send promotional notification (Admin only)
     */
    @PostMapping("/admin/promotion")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> sendPromotion(
            @RequestParam(required = false) String userId,
            @RequestParam String title,
            @RequestParam String body,
            @RequestParam String actionUrl) {
        
        log.info("Admin sending promotional notification");
        
        if (userId != null) {
            notificationService.sendPromotion(userId, title, body, actionUrl);
        } else {
            // Broadcast to all users who have promotions enabled
            SendNotificationRequest request = SendNotificationRequest.builder()
                    .title(title)
                    .body(body)
                    .type(Notification.NotificationType.PROMOTION)
                    .category("PROMOTION")
                    .actionUrl(actionUrl)
                    .actionType("OPEN_URL")
                    .sendToAll(true)
                    .build();
            notificationService.broadcast(request);
        }
        
        return ResponseEntity.ok(Map.of("message", "Promotion sent successfully"));
    }
    
    // ==================== TEST ENDPOINTS ====================
    
    /**
     * Test push notification (Development only)
     */
    @PostMapping("/test/push/{userId}")
    public ResponseEntity<NotificationDto> testPushNotification(@PathVariable String userId) {
        log.info("Testing push notification for user: {}", userId);
        
        SendNotificationRequest request = SendNotificationRequest.builder()
                .title("Test Notification 🔔")
                .body("This is a test notification from ODOP. If you see this, notifications are working!")
                .type(Notification.NotificationType.SYSTEM_UPDATE)
                .category("TEST")
                .actionUrl("/")
                .actionType("OPEN_URL")
                .priority(Notification.NotificationPriority.NORMAL)
                .build();
        
        NotificationDto notification = notificationService.sendToUser(userId, request);
        return ResponseEntity.ok(notification);
    }
}
