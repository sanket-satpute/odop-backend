package com.odop.root.services;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.odop.root.models.settings.PlatformSettings;
import com.odop.root.repository.settings.PlatformSettingsRepository;

@Service
public class PlatformSettingsService {

    @Autowired
    private PlatformSettingsRepository settingsRepository;

    /**
     * Get all platform settings (singleton)
     */
    public PlatformSettings getSettings() {
        List<PlatformSettings> all = settingsRepository.findAll();
        if (all.isEmpty()) {
            return createDefaultSettings();
        }
        return all.get(0);
    }

    /**
     * Get only general settings
     */
    public Map<String, Object> getGeneralSettings() {
        PlatformSettings settings = getSettings();
        Map<String, Object> general = new HashMap<>();
        general.put("platformName", settings.getPlatformName());
        general.put("primaryEmail", settings.getPrimaryEmail());
        general.put("supportEmail", settings.getSupportEmail());
        general.put("timezone", settings.getTimezone());
        general.put("language", settings.getLanguage());
        general.put("accentColor", settings.getAccentColor());
        general.put("logoUrl", settings.getLogoUrl());
        general.put("faviconUrl", settings.getFaviconUrl());
        return general;
    }

    /**
     * Update general settings
     */
    public PlatformSettings updateGeneralSettings(Map<String, Object> generalData) {
        PlatformSettings settings = getSettings();
        if (generalData.containsKey("platformName")) settings.setPlatformName((String) generalData.get("platformName"));
        if (generalData.containsKey("primaryEmail")) settings.setPrimaryEmail((String) generalData.get("primaryEmail"));
        if (generalData.containsKey("supportEmail")) settings.setSupportEmail((String) generalData.get("supportEmail"));
        if (generalData.containsKey("timezone")) settings.setTimezone((String) generalData.get("timezone"));
        if (generalData.containsKey("language")) settings.setLanguage((String) generalData.get("language"));
        if (generalData.containsKey("accentColor")) settings.setAccentColor((String) generalData.get("accentColor"));
        if (generalData.containsKey("logoUrl")) settings.setLogoUrl((String) generalData.get("logoUrl"));
        if (generalData.containsKey("faviconUrl")) settings.setFaviconUrl((String) generalData.get("faviconUrl"));
        return settingsRepository.save(settings);
    }

    /**
     * Get only security settings
     */
    public Map<String, Object> getSecuritySettings() {
        PlatformSettings settings = getSettings();
        Map<String, Object> security = new HashMap<>();
        security.put("twoFactorEnabled", settings.getTwoFactorEnabled());
        security.put("minPasswordLength", settings.getMinPasswordLength());
        security.put("sessionTimeout", settings.getSessionTimeout());
        security.put("loginAttemptLimit", settings.getLoginAttemptLimit());
        security.put("requireSpecialChars", settings.getRequireSpecialChars());
        security.put("captchaEnabled", settings.getCaptchaEnabled());
        return security;
    }

    /**
     * Update security settings
     */
    public PlatformSettings updateSecuritySettings(Map<String, Object> securityData) {
        PlatformSettings settings = getSettings();
        if (securityData.containsKey("twoFactorEnabled")) settings.setTwoFactorEnabled((Boolean) securityData.get("twoFactorEnabled"));
        if (securityData.containsKey("minPasswordLength")) settings.setMinPasswordLength((Integer) securityData.get("minPasswordLength"));
        if (securityData.containsKey("sessionTimeout")) settings.setSessionTimeout((Integer) securityData.get("sessionTimeout"));
        if (securityData.containsKey("loginAttemptLimit")) settings.setLoginAttemptLimit((Integer) securityData.get("loginAttemptLimit"));
        if (securityData.containsKey("requireSpecialChars")) settings.setRequireSpecialChars((Boolean) securityData.get("requireSpecialChars"));
        if (securityData.containsKey("captchaEnabled")) settings.setCaptchaEnabled((Boolean) securityData.get("captchaEnabled"));
        return settingsRepository.save(settings);
    }

    /**
     * Get only email settings
     */
    public Map<String, Object> getEmailSettings() {
        PlatformSettings settings = getSettings();
        Map<String, Object> email = new HashMap<>();
        email.put("smtpHost", settings.getSmtpHost());
        email.put("smtpPort", settings.getSmtpPort());
        email.put("smtpUsername", settings.getSmtpUsername());
        // Don't return password for security
        email.put("senderEmail", settings.getSenderEmail());
        email.put("senderName", settings.getSenderName());
        email.put("encryptionType", settings.getEncryptionType());
        return email;
    }

    /**
     * Update email settings
     */
    public PlatformSettings updateEmailSettings(Map<String, Object> emailData) {
        PlatformSettings settings = getSettings();
        if (emailData.containsKey("smtpHost")) settings.setSmtpHost((String) emailData.get("smtpHost"));
        if (emailData.containsKey("smtpPort")) settings.setSmtpPort((Integer) emailData.get("smtpPort"));
        if (emailData.containsKey("smtpUsername")) settings.setSmtpUsername((String) emailData.get("smtpUsername"));
        if (emailData.containsKey("smtpPassword") && emailData.get("smtpPassword") != null && !((String) emailData.get("smtpPassword")).isEmpty()) {
            settings.setSmtpPassword((String) emailData.get("smtpPassword"));
        }
        if (emailData.containsKey("senderEmail")) settings.setSenderEmail((String) emailData.get("senderEmail"));
        if (emailData.containsKey("senderName")) settings.setSenderName((String) emailData.get("senderName"));
        if (emailData.containsKey("encryptionType")) settings.setEncryptionType((String) emailData.get("encryptionType"));
        return settingsRepository.save(settings);
    }

    /**
     * Get only notification settings
     */
    public Map<String, Object> getNotificationSettings() {
        PlatformSettings settings = getSettings();
        Map<String, Object> notifications = new HashMap<>();
        notifications.put("systemEmailAlerts", settings.getSystemEmailAlerts());
        notifications.put("pushNotifications", settings.getPushNotifications());
        notifications.put("orderNotifications", settings.getOrderNotifications());
        notifications.put("userMessageAlerts", settings.getUserMessageAlerts());
        notifications.put("soundAlerts", settings.getSoundAlerts());
        notifications.put("vibrationAlerts", settings.getVibrationAlerts());
        return notifications;
    }

    /**
     * Update notification settings
     */
    public PlatformSettings updateNotificationSettings(Map<String, Object> notificationData) {
        PlatformSettings settings = getSettings();
        if (notificationData.containsKey("systemEmailAlerts")) settings.setSystemEmailAlerts((Boolean) notificationData.get("systemEmailAlerts"));
        if (notificationData.containsKey("pushNotifications")) settings.setPushNotifications((Boolean) notificationData.get("pushNotifications"));
        if (notificationData.containsKey("orderNotifications")) settings.setOrderNotifications((Boolean) notificationData.get("orderNotifications"));
        if (notificationData.containsKey("userMessageAlerts")) settings.setUserMessageAlerts((Boolean) notificationData.get("userMessageAlerts"));
        if (notificationData.containsKey("soundAlerts")) settings.setSoundAlerts((Boolean) notificationData.get("soundAlerts"));
        if (notificationData.containsKey("vibrationAlerts")) settings.setVibrationAlerts((Boolean) notificationData.get("vibrationAlerts"));
        return settingsRepository.save(settings);
    }

    /**
     * Get only maintenance settings
     */
    public Map<String, Object> getMaintenanceSettings() {
        PlatformSettings settings = getSettings();
        Map<String, Object> maintenance = new HashMap<>();
        maintenance.put("enabled", settings.getMaintenanceEnabled());
        maintenance.put("message", settings.getMaintenanceMessage());
        maintenance.put("scheduledStart", settings.getMaintenanceScheduledStart());
        maintenance.put("scheduledEnd", settings.getMaintenanceScheduledEnd());
        return maintenance;
    }

    /**
     * Update maintenance settings
     */
    public PlatformSettings updateMaintenanceSettings(Map<String, Object> maintenanceData) {
        PlatformSettings settings = getSettings();
        if (maintenanceData.containsKey("enabled")) settings.setMaintenanceEnabled((Boolean) maintenanceData.get("enabled"));
        if (maintenanceData.containsKey("message")) settings.setMaintenanceMessage((String) maintenanceData.get("message"));
        if (maintenanceData.containsKey("scheduledStart")) settings.setMaintenanceScheduledStart((String) maintenanceData.get("scheduledStart"));
        if (maintenanceData.containsKey("scheduledEnd")) settings.setMaintenanceScheduledEnd((String) maintenanceData.get("scheduledEnd"));
        return settingsRepository.save(settings);
    }

    /**
     * Get payment settings
     */
    public Map<String, Object> getPaymentSettings() {
        PlatformSettings settings = getSettings();
        Map<String, Object> payment = new HashMap<>();
        payment.put("razorpayKeyId", settings.getRazorpayKeyId());
        // Don't expose secret key
        payment.put("razorpayTestMode", settings.getRazorpayTestMode());
        payment.put("platformCommissionPercent", settings.getPlatformCommissionPercent());
        return payment;
    }

    /**
     * Update payment settings
     */
    public PlatformSettings updatePaymentSettings(Map<String, Object> paymentData) {
        PlatformSettings settings = getSettings();
        if (paymentData.containsKey("razorpayKeyId")) settings.setRazorpayKeyId((String) paymentData.get("razorpayKeyId"));
        if (paymentData.containsKey("razorpayKeySecret") && paymentData.get("razorpayKeySecret") != null && !((String) paymentData.get("razorpayKeySecret")).isEmpty()) {
            settings.setRazorpayKeySecret((String) paymentData.get("razorpayKeySecret"));
        }
        if (paymentData.containsKey("razorpayTestMode")) settings.setRazorpayTestMode((Boolean) paymentData.get("razorpayTestMode"));
        if (paymentData.containsKey("platformCommissionPercent")) settings.setPlatformCommissionPercent((Double) paymentData.get("platformCommissionPercent"));
        return settingsRepository.save(settings);
    }

    /**
     * Get social media settings
     */
    public Map<String, Object> getSocialSettings() {
        PlatformSettings settings = getSettings();
        Map<String, Object> social = new HashMap<>();
        social.put("facebookUrl", settings.getFacebookUrl());
        social.put("twitterUrl", settings.getTwitterUrl());
        social.put("instagramUrl", settings.getInstagramUrl());
        social.put("linkedinUrl", settings.getLinkedinUrl());
        social.put("youtubeUrl", settings.getYoutubeUrl());
        return social;
    }

    /**
     * Update social media settings
     */
    public PlatformSettings updateSocialSettings(Map<String, Object> socialData) {
        PlatformSettings settings = getSettings();
        if (socialData.containsKey("facebookUrl")) settings.setFacebookUrl((String) socialData.get("facebookUrl"));
        if (socialData.containsKey("twitterUrl")) settings.setTwitterUrl((String) socialData.get("twitterUrl"));
        if (socialData.containsKey("instagramUrl")) settings.setInstagramUrl((String) socialData.get("instagramUrl"));
        if (socialData.containsKey("linkedinUrl")) settings.setLinkedinUrl((String) socialData.get("linkedinUrl"));
        if (socialData.containsKey("youtubeUrl")) settings.setYoutubeUrl((String) socialData.get("youtubeUrl"));
        return settingsRepository.save(settings);
    }

    /**
     * Get contact settings
     */
    public Map<String, Object> getContactSettings() {
        PlatformSettings settings = getSettings();
        Map<String, Object> contact = new HashMap<>();
        contact.put("contactAddress", settings.getContactAddress());
        contact.put("contactPhone", settings.getContactPhone());
        contact.put("contactWhatsapp", settings.getContactWhatsapp());
        return contact;
    }

    /**
     * Update contact settings
     */
    public PlatformSettings updateContactSettings(Map<String, Object> contactData) {
        PlatformSettings settings = getSettings();
        if (contactData.containsKey("contactAddress")) settings.setContactAddress((String) contactData.get("contactAddress"));
        if (contactData.containsKey("contactPhone")) settings.setContactPhone((String) contactData.get("contactPhone"));
        if (contactData.containsKey("contactWhatsapp")) settings.setContactWhatsapp((String) contactData.get("contactWhatsapp"));
        return settingsRepository.save(settings);
    }

    /**
     * Update all settings at once
     */
    public PlatformSettings updateAllSettings(PlatformSettings newSettings) {
        PlatformSettings settings = getSettings();
        // Copy ID from existing to ensure we update, not create new
        newSettings.setId(settings.getId());
        return settingsRepository.save(newSettings);
    }

    /**
     * Check if maintenance mode is enabled
     */
    public boolean isMaintenanceMode() {
        PlatformSettings settings = getSettings();
        return Boolean.TRUE.equals(settings.getMaintenanceEnabled());
    }

    /**
     * Create default settings if none exist
     */
    private PlatformSettings createDefaultSettings() {
        PlatformSettings settings = new PlatformSettings();
        
        // General
        settings.setPlatformName("ODOP - One District One Product");
        settings.setPrimaryEmail("admin@odop.in");
        settings.setSupportEmail("support@odop.in");
        settings.setTimezone("Asia/Kolkata");
        settings.setLanguage("en");
        settings.setAccentColor("#FF6B00");
        settings.setLogoUrl("");
        settings.setFaviconUrl("");
        
        // Security
        settings.setTwoFactorEnabled(false);
        settings.setMinPasswordLength(8);
        settings.setSessionTimeout(30);
        settings.setLoginAttemptLimit(5);
        settings.setRequireSpecialChars(true);
        settings.setCaptchaEnabled(false);
        
        // Email
        settings.setSmtpHost("smtp.gmail.com");
        settings.setSmtpPort(587);
        settings.setSenderEmail("noreply@odop.in");
        settings.setSenderName("ODOP Platform");
        settings.setEncryptionType("tls");
        
        // Notifications
        settings.setSystemEmailAlerts(true);
        settings.setPushNotifications(true);
        settings.setOrderNotifications(true);
        settings.setUserMessageAlerts(true);
        settings.setSoundAlerts(false);
        settings.setVibrationAlerts(false);
        
        // Maintenance
        settings.setMaintenanceEnabled(false);
        settings.setMaintenanceMessage("We are currently performing scheduled maintenance. Please check back shortly.");
        
        // Payment
        settings.setRazorpayTestMode(true);
        settings.setPlatformCommissionPercent(5.0);
        
        // Social
        settings.setFacebookUrl("");
        settings.setTwitterUrl("");
        settings.setInstagramUrl("");
        settings.setLinkedinUrl("");
        settings.setYoutubeUrl("");
        
        // Contact
        settings.setContactAddress("");
        settings.setContactPhone("");
        settings.setContactWhatsapp("");
        
        return settingsRepository.save(settings);
    }
}
