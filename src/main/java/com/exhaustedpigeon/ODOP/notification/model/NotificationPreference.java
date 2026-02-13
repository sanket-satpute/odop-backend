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
 * User notification preferences
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notification_preferences")
public class NotificationPreference {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String userId;
    
    private String userType;        // CUSTOMER, VENDOR, ADMIN
    
    // Channel preferences
    private ChannelPreferences push;
    private ChannelPreferences email;
    private ChannelPreferences sms;
    private ChannelPreferences whatsapp;
    
    // Global settings
    private boolean globalEnabled;
    private boolean quietHoursEnabled;
    private String quietHoursStart;     // HH:mm format
    private String quietHoursEnd;       // HH:mm format
    
    // Timezone
    private String timezone;
    
    // Device tokens for push notifications
    private List<DeviceToken> deviceTokens;
    
    // Contact info (for SMS/WhatsApp)
    private String phoneNumber;
    private String emailAddress;
    private boolean phoneVerified;
    private boolean emailVerified;
    
    // Language preference
    private String preferredLanguage;
    
    // Frequency limits
    private int maxDailyNotifications;
    private int maxWeeklyPromotions;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Channel-specific preferences
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChannelPreferences {
        private boolean enabled;
        
        // Category preferences
        private boolean orderUpdates;
        private boolean paymentUpdates;
        private boolean shipmentUpdates;
        private boolean promotions;
        private boolean priceDropAlerts;
        private boolean backInStockAlerts;
        private boolean securityAlerts;
        private boolean accountUpdates;
        private boolean reviewResponses;
        
        // Vendor specific
        private boolean newOrders;
        private boolean lowStockAlerts;
        private boolean reviewNotifications;
        private boolean verificationUpdates;
        
        // Frequency
        private String promotionFrequency;  // INSTANT, DAILY_DIGEST, WEEKLY_DIGEST
    }
    
    /**
     * Device token for push notifications
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceToken {
        private String token;
        private String platform;        // WEB, ANDROID, IOS
        private String deviceId;
        private String deviceName;
        private LocalDateTime registeredAt;
        private LocalDateTime lastUsedAt;
        private boolean active;
    }
    
    /**
     * Create default preferences for a new user
     */
    public static NotificationPreference createDefault(String userId, String userType, String email, String phone) {
        ChannelPreferences defaultPush = ChannelPreferences.builder()
                .enabled(true)
                .orderUpdates(true)
                .paymentUpdates(true)
                .shipmentUpdates(true)
                .promotions(true)
                .priceDropAlerts(true)
                .backInStockAlerts(true)
                .securityAlerts(true)
                .accountUpdates(true)
                .reviewResponses(true)
                .newOrders(true)
                .lowStockAlerts(true)
                .reviewNotifications(true)
                .verificationUpdates(true)
                .promotionFrequency("DAILY_DIGEST")
                .build();
        
        ChannelPreferences defaultEmail = ChannelPreferences.builder()
                .enabled(true)
                .orderUpdates(true)
                .paymentUpdates(true)
                .shipmentUpdates(true)
                .promotions(false)
                .priceDropAlerts(false)
                .backInStockAlerts(true)
                .securityAlerts(true)
                .accountUpdates(true)
                .reviewResponses(true)
                .newOrders(true)
                .lowStockAlerts(true)
                .reviewNotifications(true)
                .verificationUpdates(true)
                .promotionFrequency("WEEKLY_DIGEST")
                .build();
        
        ChannelPreferences defaultSms = ChannelPreferences.builder()
                .enabled(false)
                .orderUpdates(true)
                .paymentUpdates(true)
                .shipmentUpdates(true)
                .promotions(false)
                .priceDropAlerts(false)
                .backInStockAlerts(false)
                .securityAlerts(true)
                .accountUpdates(false)
                .reviewResponses(false)
                .newOrders(true)
                .lowStockAlerts(false)
                .reviewNotifications(false)
                .verificationUpdates(true)
                .promotionFrequency("WEEKLY_DIGEST")
                .build();
        
        return NotificationPreference.builder()
                .userId(userId)
                .userType(userType)
                .push(defaultPush)
                .email(defaultEmail)
                .sms(defaultSms)
                .whatsapp(defaultSms)  // Same as SMS by default
                .globalEnabled(true)
                .quietHoursEnabled(false)
                .quietHoursStart("22:00")
                .quietHoursEnd("08:00")
                .timezone("Asia/Kolkata")
                .emailAddress(email)
                .phoneNumber(phone)
                .emailVerified(email != null)
                .phoneVerified(false)
                .preferredLanguage("en")
                .maxDailyNotifications(50)
                .maxWeeklyPromotions(10)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
