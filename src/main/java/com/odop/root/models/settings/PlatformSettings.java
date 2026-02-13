package com.odop.root.models.settings;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "platform_settings")
public class PlatformSettings {
    
    @Id
    private String id;
    
    // ==================== GENERAL SETTINGS ====================
    private String platformName;
    private String primaryEmail;
    private String supportEmail;
    private String timezone;
    private String language;
    private String accentColor;
    private String logoUrl;
    private String faviconUrl;
    
    // ==================== SECURITY SETTINGS ====================
    private Boolean twoFactorEnabled;
    private Integer minPasswordLength;
    private Integer sessionTimeout;      // in minutes
    private Integer loginAttemptLimit;
    private Boolean requireSpecialChars;
    private Boolean captchaEnabled;
    
    // ==================== EMAIL SETTINGS ====================
    private String smtpHost;
    private Integer smtpPort;
    private String smtpUsername;
    private String smtpPassword;
    private String senderEmail;
    private String senderName;
    private String encryptionType;       // tls, ssl, none
    
    // ==================== NOTIFICATION SETTINGS ====================
    private Boolean systemEmailAlerts;
    private Boolean pushNotifications;
    private Boolean orderNotifications;
    private Boolean userMessageAlerts;
    private Boolean soundAlerts;
    private Boolean vibrationAlerts;
    
    // ==================== MAINTENANCE SETTINGS ====================
    private Boolean maintenanceEnabled;
    private String maintenanceMessage;
    private String maintenanceScheduledStart;
    private String maintenanceScheduledEnd;
    
    // ==================== PAYMENT SETTINGS ====================
    private String razorpayKeyId;
    private String razorpayKeySecret;
    private Boolean razorpayTestMode;
    private Double platformCommissionPercent;
    
    // ==================== SOCIAL SETTINGS ====================
    private String facebookUrl;
    private String twitterUrl;
    private String instagramUrl;
    private String linkedinUrl;
    private String youtubeUrl;
    
    // ==================== CONTACT SETTINGS ====================
    private String contactAddress;
    private String contactPhone;
    private String contactWhatsapp;
}
