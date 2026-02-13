package com.exhaustedpigeon.ODOP.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Notification model for storing user notifications
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
public class Notification {
    
    @Id
    private String id;
    
    @Indexed
    private String userId;          // Customer or Vendor ID
    
    private String userType;        // CUSTOMER, VENDOR, ADMIN
    
    // Notification Content
    private String title;
    private String body;
    private String imageUrl;
    private String iconUrl;
    
    // Type and Category
    private NotificationType type;
    private String category;        // ORDER, PAYMENT, PROMOTION, SYSTEM, etc.
    
    // Action
    private String actionUrl;       // URL to navigate when clicked
    private String actionType;      // OPEN_URL, OPEN_ORDER, OPEN_PRODUCT, etc.
    private Map<String, String> actionData;  // Additional data for action
    
    // Status
    private boolean read;
    private boolean delivered;
    private LocalDateTime deliveredAt;
    private LocalDateTime readAt;
    
    // Channels sent
    private List<NotificationChannel> sentChannels;
    
    // Reference
    private String referenceId;     // Order ID, Product ID, etc.
    private String referenceType;   // ORDER, PRODUCT, SHIPMENT, etc.
    
    // Priority
    private NotificationPriority priority;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    
    // Template info
    private String templateId;
    private Map<String, String> templateData;
    
    /**
     * Notification Types
     */
    public enum NotificationType {
        // Order related
        ORDER_PLACED,
        ORDER_CONFIRMED,
        ORDER_SHIPPED,
        ORDER_DELIVERED,
        ORDER_CANCELLED,
        ORDER_RETURNED,
        
        // Payment related
        PAYMENT_SUCCESS,
        PAYMENT_FAILED,
        REFUND_INITIATED,
        REFUND_COMPLETED,
        
        // Shipment related
        SHIPMENT_DISPATCHED,
        SHIPMENT_IN_TRANSIT,
        SHIPMENT_OUT_FOR_DELIVERY,
        SHIPMENT_DELIVERED,
        SHIPMENT_DELAYED,
        
        // Promotion related
        PROMOTION,
        FLASH_SALE,
        PRICE_DROP,
        BACK_IN_STOCK,
        
        // Vendor related
        NEW_ORDER,
        LOW_STOCK_ALERT,
        REVIEW_RECEIVED,
        VERIFICATION_UPDATE,
        
        // System related
        SYSTEM_UPDATE,
        ACCOUNT_UPDATE,
        SECURITY_ALERT,
        REMINDER,
        
        // Social
        NEW_FOLLOWER,
        VENDOR_UPDATE
    }
    
    /**
     * Notification Channels
     */
    public enum NotificationChannel {
        PUSH,           // Browser/App push notification
        EMAIL,          // Email notification
        SMS,            // SMS notification
        WHATSAPP,       // WhatsApp notification
        IN_APP          // In-app notification (stored in DB)
    }
    
    /**
     * Notification Priority
     */
    public enum NotificationPriority {
        LOW,
        NORMAL,
        HIGH,
        URGENT
    }
}
