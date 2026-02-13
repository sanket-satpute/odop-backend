package com.exhaustedpigeon.ODOP.notification.dto;

import com.exhaustedpigeon.ODOP.notification.model.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO for notification responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    
    private String id;
    private String title;
    private String body;
    private String imageUrl;
    private String iconUrl;
    
    private Notification.NotificationType type;
    private String category;
    
    private String actionUrl;
    private String actionType;
    private Map<String, String> actionData;
    
    private boolean read;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
    
    private String referenceId;
    private String referenceType;
    
    private Notification.NotificationPriority priority;
    
    /**
     * Convert from entity to DTO
     */
    public static NotificationDto fromEntity(Notification notification) {
        return NotificationDto.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .body(notification.getBody())
                .imageUrl(notification.getImageUrl())
                .iconUrl(notification.getIconUrl())
                .type(notification.getType())
                .category(notification.getCategory())
                .actionUrl(notification.getActionUrl())
                .actionType(notification.getActionType())
                .actionData(notification.getActionData())
                .read(notification.isRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .referenceId(notification.getReferenceId())
                .referenceType(notification.getReferenceType())
                .priority(notification.getPriority())
                .build();
    }
}
