package com.odop.service;

import com.odop.model.CustomerPreferences;
import com.odop.model.CustomerPreferences.*;
import com.odop.repository.CustomerPreferencesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CustomerPreferencesService {

    @Autowired
    private CustomerPreferencesRepository preferencesRepository;

    /**
     * Get or create customer preferences
     */
    public CustomerPreferences getOrCreatePreferences(String customerId) {
        return preferencesRepository.findByCustomerId(customerId)
            .orElseGet(() -> {
                CustomerPreferences preferences = new CustomerPreferences(customerId);
                preferences.setCreatedAt(LocalDateTime.now());
                preferences.setUpdatedAt(LocalDateTime.now());
                return preferencesRepository.save(preferences);
            });
    }

    /**
     * Get customer preferences by customer ID
     */
    public Optional<CustomerPreferences> getPreferencesByCustomerId(String customerId) {
        return preferencesRepository.findByCustomerId(customerId);
    }

    /**
     * Update entire preferences
     */
    @Transactional
    public CustomerPreferences updatePreferences(String customerId, CustomerPreferences updatedPreferences) {
        CustomerPreferences existing = getOrCreatePreferences(customerId);
        
        // Update all fields except system fields
        existing.setNotificationChannels(updatedPreferences.getNotificationChannels());
        existing.setNotificationPreferences(updatedPreferences.getNotificationPreferences());
        existing.setSecuritySettings(updatedPreferences.getSecuritySettings());
        existing.setAppearanceSettings(updatedPreferences.getAppearanceSettings());
        existing.setPrivacySettings(updatedPreferences.getPrivacySettings());
        existing.setConnectedServices(updatedPreferences.getConnectedServices());
        existing.setLanguage(updatedPreferences.getLanguage());
        existing.setTimezone(updatedPreferences.getTimezone());
        existing.setCurrency(updatedPreferences.getCurrency());
        existing.setUpdatedAt(LocalDateTime.now());

        return preferencesRepository.save(existing);
    }

    // ================================
    // Notification Channel Operations
    // ================================

    /**
     * Update notification channels
     */
    @Transactional
    public CustomerPreferences updateNotificationChannels(String customerId, NotificationChannels channels) {
        CustomerPreferences preferences = getOrCreatePreferences(customerId);
        preferences.setNotificationChannels(channels);
        preferences.setUpdatedAt(LocalDateTime.now());
        return preferencesRepository.save(preferences);
    }

    /**
     * Toggle specific notification channel
     */
    @Transactional
    public CustomerPreferences toggleNotificationChannel(String customerId, String channel, boolean enabled) {
        CustomerPreferences preferences = getOrCreatePreferences(customerId);
        NotificationChannels channels = preferences.getNotificationChannels();

        switch (channel.toLowerCase()) {
            case "app": channels.setApp(enabled); break;
            case "email": channels.setEmail(enabled); break;
            case "sms": channels.setSms(enabled); break;
            case "whatsapp": channels.setWhatsapp(enabled); break;
            case "push": channels.setPush(enabled); break;
        }

        preferences.setUpdatedAt(LocalDateTime.now());
        return preferencesRepository.save(preferences);
    }

    // ================================
    // Notification Preferences Operations
    // ================================

    /**
     * Update notification preferences for specific type
     */
    @Transactional
    public CustomerPreferences updateNotificationPreference(String customerId, String key, NotificationPreference preference) {
        CustomerPreferences preferences = getOrCreatePreferences(customerId);
        preferences.getNotificationPreferences().put(key, preference);
        preferences.setUpdatedAt(LocalDateTime.now());
        return preferencesRepository.save(preferences);
    }

    /**
     * Update all notification preferences
     */
    @Transactional
    public CustomerPreferences updateAllNotificationPreferences(String customerId, Map<String, NotificationPreference> notificationPreferences) {
        CustomerPreferences preferences = getOrCreatePreferences(customerId);
        preferences.setNotificationPreferences(notificationPreferences);
        preferences.setUpdatedAt(LocalDateTime.now());
        return preferencesRepository.save(preferences);
    }

    // ================================
    // Security Settings Operations
    // ================================

    /**
     * Update security settings
     */
    @Transactional
    public CustomerPreferences updateSecuritySettings(String customerId, SecuritySettings securitySettings) {
        CustomerPreferences preferences = getOrCreatePreferences(customerId);
        preferences.setSecuritySettings(securitySettings);
        preferences.setUpdatedAt(LocalDateTime.now());
        return preferencesRepository.save(preferences);
    }

    /**
     * Enable two-factor authentication
     */
    @Transactional
    public CustomerPreferences enableTwoFactor(String customerId, String method, String secret) {
        CustomerPreferences preferences = getOrCreatePreferences(customerId);
        SecuritySettings security = preferences.getSecuritySettings();
        security.setTwoFactorEnabled(true);
        security.setTwoFactorMethod(method);
        security.setTwoFactorSecret(secret);
        preferences.setUpdatedAt(LocalDateTime.now());
        return preferencesRepository.save(preferences);
    }

    /**
     * Disable two-factor authentication
     */
    @Transactional
    public CustomerPreferences disableTwoFactor(String customerId) {
        CustomerPreferences preferences = getOrCreatePreferences(customerId);
        SecuritySettings security = preferences.getSecuritySettings();
        security.setTwoFactorEnabled(false);
        security.setTwoFactorSecret(null);
        preferences.setUpdatedAt(LocalDateTime.now());
        return preferencesRepository.save(preferences);
    }

    /**
     * Add trusted device
     */
    @Transactional
    public CustomerPreferences addTrustedDevice(String customerId, String deviceId) {
        CustomerPreferences preferences = getOrCreatePreferences(customerId);
        List<String> devices = preferences.getSecuritySettings().getTrustedDevices();
        if (!devices.contains(deviceId)) {
            devices.add(deviceId);
        }
        preferences.setUpdatedAt(LocalDateTime.now());
        return preferencesRepository.save(preferences);
    }

    /**
     * Remove trusted device
     */
    @Transactional
    public CustomerPreferences removeTrustedDevice(String customerId, String deviceId) {
        CustomerPreferences preferences = getOrCreatePreferences(customerId);
        preferences.getSecuritySettings().getTrustedDevices().remove(deviceId);
        preferences.setUpdatedAt(LocalDateTime.now());
        return preferencesRepository.save(preferences);
    }

    /**
     * Update password change timestamp
     */
    @Transactional
    public void updatePasswordChangeTime(String customerId) {
        CustomerPreferences preferences = getOrCreatePreferences(customerId);
        preferences.getSecuritySettings().setLastPasswordChange(LocalDateTime.now());
        preferences.setUpdatedAt(LocalDateTime.now());
        preferencesRepository.save(preferences);
    }

    /**
     * Register or update customer login session
     */
    @Transactional
    public CustomerPreferences registerSession(String customerId, String sessionId, String userAgent, String ipAddress) {
        CustomerPreferences preferences = getOrCreatePreferences(customerId);
        SecuritySettings security = preferences.getSecuritySettings();

        if (security.getActiveSessions() == null) {
            security.setActiveSessions(new ArrayList<>());
        }

        for (ActiveSession session : security.getActiveSessions()) {
            session.setCurrent(false);
        }

        ActiveSession activeSession = new ActiveSession();
        activeSession.setSessionId(sessionId);
        activeSession.setUserAgent(userAgent);
        activeSession.setIpAddress(ipAddress);
        activeSession.setDeviceName(parseDeviceName(userAgent));
        activeSession.setLoginTime(LocalDateTime.now());
        activeSession.setLastActive(LocalDateTime.now());
        activeSession.setCurrent(true);

        security.getActiveSessions().add(0, activeSession);
        if (security.getActiveSessions().size() > 10) {
            security.setActiveSessions(new ArrayList<>(security.getActiveSessions().subList(0, 10)));
        }

        preferences.setUpdatedAt(LocalDateTime.now());
        return preferencesRepository.save(preferences);
    }

    /**
     * Get active sessions for a customer
     */
    public List<ActiveSession> getActiveSessions(String customerId) {
        CustomerPreferences preferences = getOrCreatePreferences(customerId);
        if (preferences.getSecuritySettings().getActiveSessions() == null) {
            return List.of();
        }
        return preferences.getSecuritySettings().getActiveSessions();
    }

    private String parseDeviceName(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Unknown Device";
        }
        String ua = userAgent.toLowerCase();
        if (ua.contains("windows")) return "Windows Device";
        if (ua.contains("android")) return "Android Device";
        if (ua.contains("iphone") || ua.contains("ios")) return "iPhone";
        if (ua.contains("mac")) return "Mac Device";
        if (ua.contains("linux")) return "Linux Device";
        return "Web Browser";
    }

    // ================================
    // Appearance Settings Operations
    // ================================

    /**
     * Update appearance settings
     */
    @Transactional
    public CustomerPreferences updateAppearanceSettings(String customerId, AppearanceSettings appearanceSettings) {
        CustomerPreferences preferences = getOrCreatePreferences(customerId);
        preferences.setAppearanceSettings(appearanceSettings);
        preferences.setUpdatedAt(LocalDateTime.now());
        return preferencesRepository.save(preferences);
    }

    /**
     * Set theme mode
     */
    @Transactional
    public CustomerPreferences setThemeMode(String customerId, String themeMode) {
        CustomerPreferences preferences = getOrCreatePreferences(customerId);
        preferences.getAppearanceSettings().setThemeMode(themeMode);
        preferences.setUpdatedAt(LocalDateTime.now());
        return preferencesRepository.save(preferences);
    }

    /**
     * Set font size
     */
    @Transactional
    public CustomerPreferences setFontSize(String customerId, String fontSize) {
        CustomerPreferences preferences = getOrCreatePreferences(customerId);
        preferences.getAppearanceSettings().setFontSize(fontSize);
        preferences.setUpdatedAt(LocalDateTime.now());
        return preferencesRepository.save(preferences);
    }

    /**
     * Toggle high contrast
     */
    @Transactional
    public CustomerPreferences toggleHighContrast(String customerId, boolean enabled) {
        CustomerPreferences preferences = getOrCreatePreferences(customerId);
        preferences.getAppearanceSettings().setHighContrast(enabled);
        preferences.setUpdatedAt(LocalDateTime.now());
        return preferencesRepository.save(preferences);
    }

    // ================================
    // Privacy Settings Operations
    // ================================

    /**
     * Update privacy settings
     */
    @Transactional
    public CustomerPreferences updatePrivacySettings(String customerId, PrivacySettings privacySettings) {
        CustomerPreferences preferences = getOrCreatePreferences(customerId);
        preferences.setPrivacySettings(privacySettings);
        preferences.setUpdatedAt(LocalDateTime.now());
        return preferencesRepository.save(preferences);
    }

    /**
     * Update profile visibility
     */
    @Transactional
    public CustomerPreferences setProfileVisibility(String customerId, String visibility) {
        CustomerPreferences preferences = getOrCreatePreferences(customerId);
        preferences.getPrivacySettings().setProfileVisibility(visibility);
        preferences.setUpdatedAt(LocalDateTime.now());
        return preferencesRepository.save(preferences);
    }

    /**
     * Toggle data analytics opt-in
     */
    @Transactional
    public CustomerPreferences toggleDataAnalytics(String customerId, boolean enabled) {
        CustomerPreferences preferences = getOrCreatePreferences(customerId);
        preferences.getPrivacySettings().setAllowDataAnalytics(enabled);
        preferences.setUpdatedAt(LocalDateTime.now());
        return preferencesRepository.save(preferences);
    }

    /**
     * Toggle personalized ads
     */
    @Transactional
    public CustomerPreferences togglePersonalizedAds(String customerId, boolean enabled) {
        CustomerPreferences preferences = getOrCreatePreferences(customerId);
        preferences.getPrivacySettings().setPersonalizedAds(enabled);
        preferences.setUpdatedAt(LocalDateTime.now());
        return preferencesRepository.save(preferences);
    }

    // ================================
    // Connected Services Operations
    // ================================

    /**
     * Connect a service
     */
    @Transactional
    public CustomerPreferences connectService(String customerId, String serviceId, String externalId) {
        CustomerPreferences preferences = getOrCreatePreferences(customerId);
        
        for (ConnectedService service : preferences.getConnectedServices()) {
            if (service.getId().equals(serviceId)) {
                service.setConnected(true);
                service.setExternalId(externalId);
                service.setConnectedAt(LocalDateTime.now());
                service.setLastUsed(LocalDateTime.now());
                break;
            }
        }
        
        preferences.setUpdatedAt(LocalDateTime.now());
        return preferencesRepository.save(preferences);
    }

    /**
     * Disconnect a service
     */
    @Transactional
    public CustomerPreferences disconnectService(String customerId, String serviceId) {
        CustomerPreferences preferences = getOrCreatePreferences(customerId);
        
        for (ConnectedService service : preferences.getConnectedServices()) {
            if (service.getId().equals(serviceId)) {
                service.setConnected(false);
                service.setExternalId(null);
                service.setConnectedAt(null);
                service.setLastUsed(null);
                break;
            }
        }
        
        preferences.setUpdatedAt(LocalDateTime.now());
        return preferencesRepository.save(preferences);
    }

    /**
     * Update last used time for a service
     */
    @Transactional
    public void updateServiceLastUsed(String customerId, String serviceId) {
        CustomerPreferences preferences = getOrCreatePreferences(customerId);
        
        for (ConnectedService service : preferences.getConnectedServices()) {
            if (service.getId().equals(serviceId) && service.isConnected()) {
                service.setLastUsed(LocalDateTime.now());
                break;
            }
        }
        
        preferences.setUpdatedAt(LocalDateTime.now());
        preferencesRepository.save(preferences);
    }

    // ================================
    // Regional Settings Operations
    // ================================

    /**
     * Update language setting
     */
    @Transactional
    public CustomerPreferences setLanguage(String customerId, String language) {
        CustomerPreferences preferences = getOrCreatePreferences(customerId);
        preferences.setLanguage(language);
        preferences.setUpdatedAt(LocalDateTime.now());
        return preferencesRepository.save(preferences);
    }

    /**
     * Update timezone setting
     */
    @Transactional
    public CustomerPreferences setTimezone(String customerId, String timezone) {
        CustomerPreferences preferences = getOrCreatePreferences(customerId);
        preferences.setTimezone(timezone);
        preferences.setUpdatedAt(LocalDateTime.now());
        return preferencesRepository.save(preferences);
    }

    /**
     * Update currency setting
     */
    @Transactional
    public CustomerPreferences setCurrency(String customerId, String currency) {
        CustomerPreferences preferences = getOrCreatePreferences(customerId);
        preferences.setCurrency(currency);
        preferences.setUpdatedAt(LocalDateTime.now());
        return preferencesRepository.save(preferences);
    }

    // ================================
    // Admin/Analytics Operations
    // ================================

    /**
     * Get customers who opted in for promotions
     */
    public List<CustomerPreferences> getPromotionOptedInCustomers() {
        return preferencesRepository.findCustomersOptedInToPromotions();
    }

    /**
     * Get newsletter subscriber count
     */
    public long getNewsletterSubscriberCount() {
        return preferencesRepository.countNewsletterSubscribers();
    }

    /**
     * Get customers with two-factor enabled
     */
    public List<CustomerPreferences> getTwoFactorEnabledCustomers() {
        return preferencesRepository.findCustomersWithTwoFactorEnabled();
    }

    /**
     * Delete customer preferences (for account deletion)
     */
    @Transactional
    public void deleteCustomerPreferences(String customerId) {
        preferencesRepository.deleteByCustomerId(customerId);
    }
}
