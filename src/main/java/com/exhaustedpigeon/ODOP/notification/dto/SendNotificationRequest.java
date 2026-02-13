package com.exhaustedpigeon.ODOP.notification.dto;

import com.exhaustedpigeon.ODOP.notification.model.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

/**
 * Request DTO for sending notifications
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Body is required")
    private String body;
    
    private String imageUrl;
    private String iconUrl;
    
    @NotBlank(message = "Type is required")
    private Notification.NotificationType type;
    
    private String category;
    
    // Target users
    private String userId;                          // Single user
    private List<String> userIds;                   // Multiple users
    private String userType;                        // All users of type (CUSTOMER, VENDOR)
    private boolean sendToAll;                      // Broadcast to all
    
    // Channels
    private List<Notification.NotificationChannel> channels;
    
    // Action
    private String actionUrl;
    private String actionType;
    private Map<String, String> actionData;
    
    // Reference
    private String referenceId;
    private String referenceType;
    
    // Priority
    private Notification.NotificationPriority priority;
    
    // Template
    private String templateId;
    private Map<String, String> templateData;
    
    // Schedule
    private String scheduledTime;   // ISO DateTime for scheduled notifications
}
