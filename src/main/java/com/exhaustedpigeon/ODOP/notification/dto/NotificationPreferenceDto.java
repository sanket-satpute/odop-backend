package com.exhaustedpigeon.ODOP.notification.dto;

import com.exhaustedpigeon.ODOP.notification.model.NotificationPreference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for notification preferences
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceDto {
    
    private String userId;
    private String userType;
    
    private ChannelPreferencesDto push;
    private ChannelPreferencesDto email;
    private ChannelPreferencesDto sms;
    private ChannelPreferencesDto whatsapp;
    
    private boolean globalEnabled;
    private boolean quietHoursEnabled;
    private String quietHoursStart;
    private String quietHoursEnd;
    
    private String timezone;
    private String preferredLanguage;
    
    private int maxDailyNotifications;
    private int maxWeeklyPromotions;
    
    // Contact info
    private String phoneNumber;
    private String emailAddress;
    private boolean phoneVerified;
    private boolean emailVerified;
    
    // Device info
    private List<DeviceInfoDto> devices;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChannelPreferencesDto {
        private boolean enabled;
        private boolean orderUpdates;
        private boolean paymentUpdates;
        private boolean shipmentUpdates;
        private boolean promotions;
        private boolean priceDropAlerts;
        private boolean backInStockAlerts;
        private boolean securityAlerts;
        private boolean accountUpdates;
        private boolean reviewResponses;
        private boolean newOrders;
        private boolean lowStockAlerts;
        private boolean reviewNotifications;
        private boolean verificationUpdates;
        private String promotionFrequency;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceInfoDto {
        private String deviceId;
        private String deviceName;
        private String platform;
        private boolean active;
    }
    
    /**
     * Convert from entity to DTO
     */
    public static NotificationPreferenceDto fromEntity(NotificationPreference preference) {
        return NotificationPreferenceDto.builder()
                .userId(preference.getUserId())
                .userType(preference.getUserType())
                .push(convertChannel(preference.getPush()))
                .email(convertChannel(preference.getEmail()))
                .sms(convertChannel(preference.getSms()))
                .whatsapp(convertChannel(preference.getWhatsapp()))
                .globalEnabled(preference.isGlobalEnabled())
                .quietHoursEnabled(preference.isQuietHoursEnabled())
                .quietHoursStart(preference.getQuietHoursStart())
                .quietHoursEnd(preference.getQuietHoursEnd())
                .timezone(preference.getTimezone())
                .preferredLanguage(preference.getPreferredLanguage())
                .maxDailyNotifications(preference.getMaxDailyNotifications())
                .maxWeeklyPromotions(preference.getMaxWeeklyPromotions())
                .phoneNumber(preference.getPhoneNumber())
                .emailAddress(preference.getEmailAddress())
                .phoneVerified(preference.isPhoneVerified())
                .emailVerified(preference.isEmailVerified())
                .devices(convertDevices(preference.getDeviceTokens()))
                .build();
    }
    
    private static ChannelPreferencesDto convertChannel(NotificationPreference.ChannelPreferences channel) {
        if (channel == null) return null;
        return ChannelPreferencesDto.builder()
                .enabled(channel.isEnabled())
                .orderUpdates(channel.isOrderUpdates())
                .paymentUpdates(channel.isPaymentUpdates())
                .shipmentUpdates(channel.isShipmentUpdates())
                .promotions(channel.isPromotions())
                .priceDropAlerts(channel.isPriceDropAlerts())
                .backInStockAlerts(channel.isBackInStockAlerts())
                .securityAlerts(channel.isSecurityAlerts())
                .accountUpdates(channel.isAccountUpdates())
                .reviewResponses(channel.isReviewResponses())
                .newOrders(channel.isNewOrders())
                .lowStockAlerts(channel.isLowStockAlerts())
                .reviewNotifications(channel.isReviewNotifications())
                .verificationUpdates(channel.isVerificationUpdates())
                .promotionFrequency(channel.getPromotionFrequency())
                .build();
    }
    
    private static List<DeviceInfoDto> convertDevices(List<NotificationPreference.DeviceToken> tokens) {
        if (tokens == null) return null;
        return tokens.stream()
                .map(token -> DeviceInfoDto.builder()
                        .deviceId(token.getDeviceId())
                        .deviceName(token.getDeviceName())
                        .platform(token.getPlatform())
                        .active(token.isActive())
                        .build())
                .toList();
    }
}
