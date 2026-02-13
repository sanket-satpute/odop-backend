package com.odop.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "customer_notifications")
public class CustomerNotification {

    @Id
    private String id;

    @Indexed
    private String customerId;

    private String title;
    private String message;
    
    // Types: order, promotion, wallet, security, review, system, wishlist, support
    private String type;
    
    // Category for grouping (Orders, Promotions, Security, etc.)
    private String category;

    private boolean read = false;
    private boolean deleted = false;

    // Optional action
    private String actionText;
    private String actionLink;

    // Display properties
    private String icon;
    private String color;

    // Related entity references
    private String orderId;
    private String productId;
    private String ticketId;

    // Additional metadata
    private Map<String, Object> metadata = new HashMap<>();

    // Priority for sorting (higher = more important)
    private int priority = 0;

    // Expiry date (optional)
    private LocalDateTime expiresAt;

    @CreatedDate
    @Indexed
    private LocalDateTime createdAt;

    private LocalDateTime readAt;

    // Constructors
    public CustomerNotification() {}

    public CustomerNotification(String customerId, String title, String message, String type) {
        this.customerId = customerId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.category = getCategoryFromType(type);
        this.createdAt = LocalDateTime.now();
    }

    // Helper method to derive category from type
    private String getCategoryFromType(String type) {
        switch (type) {
            case "order": return "Orders";
            case "promotion": return "Promotions";
            case "wallet": return "Wallet";
            case "security": return "Security";
            case "review": return "Reviews";
            case "wishlist": return "Wishlist";
            case "support": return "Support";
            default: return "System";
        }
    }

    // Factory methods for common notification types
    public static CustomerNotification orderNotification(String customerId, String orderId, String title, String message) {
        CustomerNotification notification = new CustomerNotification(customerId, title, message, "order");
        notification.setOrderId(orderId);
        notification.setActionText("View Order");
        notification.setActionLink("/customer-dashboard/cust-orders");
        notification.setIcon("fa-shopping-bag");
        notification.setColor("#4CAF50");
        notification.setPriority(7);
        return notification;
    }

    public static CustomerNotification promotionNotification(String customerId, String title, String message, String actionLink) {
        CustomerNotification notification = new CustomerNotification(customerId, title, message, "promotion");
        notification.setActionText("Shop Now");
        notification.setActionLink(actionLink != null ? actionLink : "/shop");
        notification.setIcon("fa-tags");
        notification.setColor("#FF9800");
        notification.setPriority(5);
        return notification;
    }

    public static CustomerNotification walletNotification(String customerId, String title, String message) {
        CustomerNotification notification = new CustomerNotification(customerId, title, message, "wallet");
        notification.setActionText("View Wallet");
        notification.setActionLink("/customer-dashboard/cust-wallet");
        notification.setIcon("fa-wallet");
        notification.setColor("#2196F3");
        notification.setPriority(6);
        return notification;
    }

    public static CustomerNotification securityNotification(String customerId, String title, String message) {
        CustomerNotification notification = new CustomerNotification(customerId, title, message, "security");
        notification.setActionText("Review");
        notification.setActionLink("/customer-dashboard/cust-settings");
        notification.setIcon("fa-shield-alt");
        notification.setColor("#F44336");
        notification.setPriority(9);
        return notification;
    }

    public static CustomerNotification reviewNotification(String customerId, String orderId, String productId, String title, String message) {
        CustomerNotification notification = new CustomerNotification(customerId, title, message, "review");
        notification.setOrderId(orderId);
        notification.setProductId(productId);
        notification.setActionText("Write Review");
        notification.setActionLink("/customer-dashboard/cust-orders");
        notification.setIcon("fa-star");
        notification.setColor("#FFC107");
        notification.setPriority(4);
        return notification;
    }

    public static CustomerNotification wishlistNotification(String customerId, String productId, String title, String message) {
        CustomerNotification notification = new CustomerNotification(customerId, title, message, "wishlist");
        notification.setProductId(productId);
        notification.setActionText("View Product");
        notification.setActionLink("/product/" + productId);
        notification.setIcon("fa-heart");
        notification.setColor("#E91E63");
        notification.setPriority(5);
        return notification;
    }

    public static CustomerNotification supportNotification(String customerId, String ticketId, String title, String message) {
        CustomerNotification notification = new CustomerNotification(customerId, title, message, "support");
        notification.setTicketId(ticketId);
        notification.setActionText("View Ticket");
        notification.setActionLink("/customer-dashboard/cust-support");
        notification.setIcon("fa-headset");
        notification.setColor("#9C27B0");
        notification.setPriority(6);
        return notification;
    }

    public static CustomerNotification systemNotification(String customerId, String title, String message) {
        CustomerNotification notification = new CustomerNotification(customerId, title, message, "system");
        notification.setIcon("fa-info-circle");
        notification.setColor("#607D8B");
        notification.setPriority(3);
        return notification;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { 
        this.type = type;
        this.category = getCategoryFromType(type);
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { 
        this.read = read;
        if (read && this.readAt == null) {
            this.readAt = LocalDateTime.now();
        }
    }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }

    public String getActionText() { return actionText; }
    public void setActionText(String actionText) { this.actionText = actionText; }

    public String getActionLink() { return actionLink; }
    public void setActionLink(String actionLink) { this.actionLink = actionLink; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }

    // Utility methods
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}
