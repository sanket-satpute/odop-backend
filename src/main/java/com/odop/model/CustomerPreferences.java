package com.odop.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "customer_preferences")
public class CustomerPreferences {

    @Id
    private String preferencesId;

    @Indexed(unique = true)
    private String customerId;

    // Notification channels (global settings)
    private NotificationChannels notificationChannels = new NotificationChannels();

    // Notification preferences by type
    private Map<String, NotificationPreference> notificationPreferences = new HashMap<>();

    // Security settings
    private SecuritySettings securitySettings = new SecuritySettings();

    // Appearance settings
    private AppearanceSettings appearanceSettings = new AppearanceSettings();

    // Privacy settings
    private PrivacySettings privacySettings = new PrivacySettings();

    // Connected services
    private List<ConnectedService> connectedServices = new ArrayList<>();

    // Language and regional settings
    private String language = "en";
    private String timezone = "Asia/Kolkata";
    private String currency = "INR";

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Nested class for notification channels
    public static class NotificationChannels {
        private boolean app = true;
        private boolean email = true;
        private boolean sms = false;
        private boolean whatsapp = true;
        private boolean push = true;

        public NotificationChannels() {}

        // Getters and Setters
        public boolean isApp() { return app; }
        public void setApp(boolean app) { this.app = app; }

        public boolean isEmail() { return email; }
        public void setEmail(boolean email) { this.email = email; }

        public boolean isSms() { return sms; }
        public void setSms(boolean sms) { this.sms = sms; }

        public boolean isWhatsapp() { return whatsapp; }
        public void setWhatsapp(boolean whatsapp) { this.whatsapp = whatsapp; }

        public boolean isPush() { return push; }
        public void setPush(boolean push) { this.push = push; }
    }

    // Nested class for specific notification preferences
    public static class NotificationPreference {
        private String key;
        private String label;
        private String description;
        private boolean email = true;
        private boolean push = false;
        private boolean sms = false;

        public NotificationPreference() {}

        public NotificationPreference(String key, String label, String description) {
            this.key = key;
            this.label = label;
            this.description = description;
        }

        // Getters and Setters
        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public boolean isEmail() { return email; }
        public void setEmail(boolean email) { this.email = email; }

        public boolean isPush() { return push; }
        public void setPush(boolean push) { this.push = push; }

        public boolean isSms() { return sms; }
        public void setSms(boolean sms) { this.sms = sms; }
    }

    // Nested class for security settings
    public static class SecuritySettings {
        private boolean twoFactorEnabled = false;
        private String twoFactorMethod = "authenticator"; // authenticator, sms, email
        private String twoFactorSecret;
        private boolean loginAlerts = true;
        private boolean suspiciousActivityAlerts = true;
        private List<String> trustedDevices = new ArrayList<>();
        private List<ActiveSession> activeSessions = new ArrayList<>();
        private LocalDateTime lastPasswordChange;

        public SecuritySettings() {}

        // Getters and Setters
        public boolean isTwoFactorEnabled() { return twoFactorEnabled; }
        public void setTwoFactorEnabled(boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; }

        public String getTwoFactorMethod() { return twoFactorMethod; }
        public void setTwoFactorMethod(String twoFactorMethod) { this.twoFactorMethod = twoFactorMethod; }

        public String getTwoFactorSecret() { return twoFactorSecret; }
        public void setTwoFactorSecret(String twoFactorSecret) { this.twoFactorSecret = twoFactorSecret; }

        public boolean isLoginAlerts() { return loginAlerts; }
        public void setLoginAlerts(boolean loginAlerts) { this.loginAlerts = loginAlerts; }

        public boolean isSuspiciousActivityAlerts() { return suspiciousActivityAlerts; }
        public void setSuspiciousActivityAlerts(boolean suspiciousActivityAlerts) { this.suspiciousActivityAlerts = suspiciousActivityAlerts; }

        public List<String> getTrustedDevices() { return trustedDevices; }
        public void setTrustedDevices(List<String> trustedDevices) { this.trustedDevices = trustedDevices; }

        public List<ActiveSession> getActiveSessions() { return activeSessions; }
        public void setActiveSessions(List<ActiveSession> activeSessions) { this.activeSessions = activeSessions; }

        public LocalDateTime getLastPasswordChange() { return lastPasswordChange; }
        public void setLastPasswordChange(LocalDateTime lastPasswordChange) { this.lastPasswordChange = lastPasswordChange; }
    }

    // Nested class for active sessions
    public static class ActiveSession {
        private String sessionId;
        private String deviceName;
        private String ipAddress;
        private String userAgent;
        private LocalDateTime loginTime;
        private LocalDateTime lastActive;
        private boolean current;

        public ActiveSession() {}

        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }

        public String getDeviceName() { return deviceName; }
        public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

        public String getUserAgent() { return userAgent; }
        public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

        public LocalDateTime getLoginTime() { return loginTime; }
        public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }

        public LocalDateTime getLastActive() { return lastActive; }
        public void setLastActive(LocalDateTime lastActive) { this.lastActive = lastActive; }

        public boolean isCurrent() { return current; }
        public void setCurrent(boolean current) { this.current = current; }
    }

    // Nested class for appearance settings
    public static class AppearanceSettings {
        private String themeMode = "light"; // light, dark, system
        private String fontSize = "medium"; // small, medium, large
        private boolean highContrast = false;
        private String accentColor = "#FF6B35"; // ODOP brand color
        private boolean compactMode = false;
        private boolean animations = true;

        public AppearanceSettings() {}

        // Getters and Setters
        public String getThemeMode() { return themeMode; }
        public void setThemeMode(String themeMode) { this.themeMode = themeMode; }

        public String getFontSize() { return fontSize; }
        public void setFontSize(String fontSize) { this.fontSize = fontSize; }

        public boolean isHighContrast() { return highContrast; }
        public void setHighContrast(boolean highContrast) { this.highContrast = highContrast; }

        public String getAccentColor() { return accentColor; }
        public void setAccentColor(String accentColor) { this.accentColor = accentColor; }

        public boolean isCompactMode() { return compactMode; }
        public void setCompactMode(boolean compactMode) { this.compactMode = compactMode; }

        public boolean isAnimations() { return animations; }
        public void setAnimations(boolean animations) { this.animations = animations; }
    }

    // Nested class for privacy settings
    public static class PrivacySettings {
        private String profileVisibility = "public"; // public, private, friends
        private boolean showPurchaseHistory = false;
        private boolean showWishlist = false;
        private boolean allowDataAnalytics = true;
        private boolean personalizedAds = true;
        private boolean shareDataWithPartners = false;
        private boolean showOnlineStatus = true;
        private boolean allowReviewsOnProfile = true;

        public PrivacySettings() {}

        // Getters and Setters
        public String getProfileVisibility() { return profileVisibility; }
        public void setProfileVisibility(String profileVisibility) { this.profileVisibility = profileVisibility; }

        public boolean isShowPurchaseHistory() { return showPurchaseHistory; }
        public void setShowPurchaseHistory(boolean showPurchaseHistory) { this.showPurchaseHistory = showPurchaseHistory; }

        public boolean isShowWishlist() { return showWishlist; }
        public void setShowWishlist(boolean showWishlist) { this.showWishlist = showWishlist; }

        public boolean isAllowDataAnalytics() { return allowDataAnalytics; }
        public void setAllowDataAnalytics(boolean allowDataAnalytics) { this.allowDataAnalytics = allowDataAnalytics; }

        public boolean isPersonalizedAds() { return personalizedAds; }
        public void setPersonalizedAds(boolean personalizedAds) { this.personalizedAds = personalizedAds; }

        public boolean isShareDataWithPartners() { return shareDataWithPartners; }
        public void setShareDataWithPartners(boolean shareDataWithPartners) { this.shareDataWithPartners = shareDataWithPartners; }

        public boolean isShowOnlineStatus() { return showOnlineStatus; }
        public void setShowOnlineStatus(boolean showOnlineStatus) { this.showOnlineStatus = showOnlineStatus; }

        public boolean isAllowReviewsOnProfile() { return allowReviewsOnProfile; }
        public void setAllowReviewsOnProfile(boolean allowReviewsOnProfile) { this.allowReviewsOnProfile = allowReviewsOnProfile; }
    }

    // Nested class for connected services
    public static class ConnectedService {
        private String id;
        private String name;
        private String icon;
        private boolean connected = false;
        private String externalId;
        private LocalDateTime connectedAt;
        private LocalDateTime lastUsed;

        public ConnectedService() {}

        public ConnectedService(String id, String name, String icon) {
            this.id = id;
            this.name = name;
            this.icon = icon;
        }

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }

        public boolean isConnected() { return connected; }
        public void setConnected(boolean connected) { this.connected = connected; }

        public String getExternalId() { return externalId; }
        public void setExternalId(String externalId) { this.externalId = externalId; }

        public LocalDateTime getConnectedAt() { return connectedAt; }
        public void setConnectedAt(LocalDateTime connectedAt) { this.connectedAt = connectedAt; }

        public LocalDateTime getLastUsed() { return lastUsed; }
        public void setLastUsed(LocalDateTime lastUsed) { this.lastUsed = lastUsed; }
    }

    // Default constructor
    public CustomerPreferences() {
        initializeDefaultNotificationPreferences();
        initializeDefaultConnectedServices();
    }

    // Constructor with customerId
    public CustomerPreferences(String customerId) {
        this.customerId = customerId;
        initializeDefaultNotificationPreferences();
        initializeDefaultConnectedServices();
    }

    // Initialize default notification preferences
    private void initializeDefaultNotificationPreferences() {
        notificationPreferences.put("orders", new NotificationPreference(
            "orders", "Order Updates", 
            "Get notified about order status changes, shipping updates, and delivery confirmations"
        ));
        notificationPreferences.get("orders").setEmail(true);
        notificationPreferences.get("orders").setPush(true);
        notificationPreferences.get("orders").setSms(false);

        notificationPreferences.put("promotions", new NotificationPreference(
            "promotions", "Promotions & Offers",
            "Receive special offers, discounts, and promotional announcements"
        ));
        notificationPreferences.get("promotions").setEmail(true);

        notificationPreferences.put("wishlist", new NotificationPreference(
            "wishlist", "Wishlist Alerts",
            "Get notified when items in your wishlist go on sale or are back in stock"
        ));
        notificationPreferences.get("wishlist").setEmail(true);
        notificationPreferences.get("wishlist").setPush(true);

        notificationPreferences.put("reviews", new NotificationPreference(
            "reviews", "Review Reminders",
            "Reminders to review products you have purchased"
        ));
        notificationPreferences.get("reviews").setEmail(true);

        notificationPreferences.put("newsletter", new NotificationPreference(
            "newsletter", "Newsletter",
            "Weekly digest of new products, artisan stories, and ODOP updates"
        ));
        notificationPreferences.get("newsletter").setEmail(true);

        notificationPreferences.put("security", new NotificationPreference(
            "security", "Security Alerts",
            "Important security notifications about your account"
        ));
        notificationPreferences.get("security").setEmail(true);
        notificationPreferences.get("security").setPush(true);
        notificationPreferences.get("security").setSms(true);
    }

    // Initialize default connected services for Indian context
    private void initializeDefaultConnectedServices() {
        connectedServices.add(new ConnectedService("aadhaar", "Aadhaar", "aadhaar-icon"));
        connectedServices.add(new ConnectedService("digilocker", "DigiLocker", "digilocker-icon"));
        connectedServices.add(new ConnectedService("umang", "UMANG", "umang-icon"));
        connectedServices.add(new ConnectedService("cowin", "CoWIN", "cowin-icon"));
        connectedServices.add(new ConnectedService("google", "Google", "fa-google"));
        connectedServices.add(new ConnectedService("facebook", "Facebook", "fa-facebook-f"));
    }

    // Main getters and setters
    public String getPreferencesId() { return preferencesId; }
    public void setPreferencesId(String preferencesId) { this.preferencesId = preferencesId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public NotificationChannels getNotificationChannels() { return notificationChannels; }
    public void setNotificationChannels(NotificationChannels notificationChannels) { this.notificationChannels = notificationChannels; }

    public Map<String, NotificationPreference> getNotificationPreferences() { return notificationPreferences; }
    public void setNotificationPreferences(Map<String, NotificationPreference> notificationPreferences) { this.notificationPreferences = notificationPreferences; }

    public SecuritySettings getSecuritySettings() { return securitySettings; }
    public void setSecuritySettings(SecuritySettings securitySettings) { this.securitySettings = securitySettings; }

    public AppearanceSettings getAppearanceSettings() { return appearanceSettings; }
    public void setAppearanceSettings(AppearanceSettings appearanceSettings) { this.appearanceSettings = appearanceSettings; }

    public PrivacySettings getPrivacySettings() { return privacySettings; }
    public void setPrivacySettings(PrivacySettings privacySettings) { this.privacySettings = privacySettings; }

    public List<ConnectedService> getConnectedServices() { return connectedServices; }
    public void setConnectedServices(List<ConnectedService> connectedServices) { this.connectedServices = connectedServices; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
