package com.odop.controller;

import com.odop.model.CustomerPreferences;
import com.odop.model.CustomerPreferences.*;
import com.odop.root.models.Customer;
import com.odop.root.repository.CustomerRepository;
import com.odop.service.CustomerPreferencesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/odop/customer/{customerId}/preferences")
@PreAuthorize("hasRole('CUSTOMER')")
@CrossOrigin(origins = "*")
public class CustomerPreferencesController {

    @Autowired
    private CustomerPreferencesService preferencesService;

    @Autowired
    private CustomerRepository customerRepository;

    // ================================
    // Main Preferences Endpoints
    // ================================

    /**
     * Get all preferences for a customer
     */
    @GetMapping
    public ResponseEntity<?> getPreferences(@PathVariable String customerId) {
        try {
            CustomerPreferences preferences = preferencesService.getOrCreatePreferences(customerId);
            return ResponseEntity.ok(preferences);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch preferences: " + e.getMessage()));
        }
    }

    /**
     * Update all preferences
     */
    @PutMapping
    public ResponseEntity<?> updatePreferences(
            @PathVariable String customerId,
            @RequestBody CustomerPreferences preferences) {
        try {
            CustomerPreferences updated = preferencesService.updatePreferences(customerId, preferences);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update preferences: " + e.getMessage()));
        }
    }

    // ================================
    // Notification Channel Endpoints
    // ================================

    /**
     * Get notification channels
     */
    @GetMapping("/notifications/channels")
    public ResponseEntity<?> getNotificationChannels(@PathVariable String customerId) {
        try {
            CustomerPreferences preferences = preferencesService.getOrCreatePreferences(customerId);
            return ResponseEntity.ok(preferences.getNotificationChannels());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch notification channels: " + e.getMessage()));
        }
    }

    /**
     * Update notification channels
     */
    @PutMapping("/notifications/channels")
    public ResponseEntity<?> updateNotificationChannels(
            @PathVariable String customerId,
            @RequestBody NotificationChannels channels) {
        try {
            CustomerPreferences updated = preferencesService.updateNotificationChannels(customerId, channels);
            return ResponseEntity.ok(updated.getNotificationChannels());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update notification channels: " + e.getMessage()));
        }
    }

    /**
     * Toggle specific notification channel
     */
    @PatchMapping("/notifications/channels/{channel}")
    public ResponseEntity<?> toggleNotificationChannel(
            @PathVariable String customerId,
            @PathVariable String channel,
            @RequestBody Map<String, Boolean> request) {
        try {
            boolean enabled = request.getOrDefault("enabled", false);
            CustomerPreferences updated = preferencesService.toggleNotificationChannel(customerId, channel, enabled);
            return ResponseEntity.ok(updated.getNotificationChannels());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to toggle notification channel: " + e.getMessage()));
        }
    }

    // ================================
    // Notification Preferences Endpoints
    // ================================

    /**
     * Get notification preferences (by type)
     */
    @GetMapping("/notifications/preferences")
    public ResponseEntity<?> getNotificationPreferences(@PathVariable String customerId) {
        try {
            CustomerPreferences preferences = preferencesService.getOrCreatePreferences(customerId);
            return ResponseEntity.ok(preferences.getNotificationPreferences());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch notification preferences: " + e.getMessage()));
        }
    }

    /**
     * Update notification preferences
     */
    @PutMapping("/notifications/preferences")
    public ResponseEntity<?> updateNotificationPreferences(
            @PathVariable String customerId,
            @RequestBody Map<String, NotificationPreference> preferences) {
        try {
            CustomerPreferences updated = preferencesService.updateAllNotificationPreferences(customerId, preferences);
            return ResponseEntity.ok(updated.getNotificationPreferences());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update notification preferences: " + e.getMessage()));
        }
    }

    /**
     * Update specific notification preference
     */
    @PatchMapping("/notifications/preferences/{key}")
    public ResponseEntity<?> updateNotificationPreference(
            @PathVariable String customerId,
            @PathVariable String key,
            @RequestBody NotificationPreference preference) {
        try {
            CustomerPreferences updated = preferencesService.updateNotificationPreference(customerId, key, preference);
            return ResponseEntity.ok(updated.getNotificationPreferences().get(key));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update notification preference: " + e.getMessage()));
        }
    }

    // ================================
    // Security Settings Endpoints
    // ================================

    /**
     * Get security settings
     */
    @GetMapping("/security")
    public ResponseEntity<?> getSecuritySettings(@PathVariable String customerId) {
        try {
            CustomerPreferences preferences = preferencesService.getOrCreatePreferences(customerId);
            // Don't expose the 2FA secret
            SecuritySettings settings = preferences.getSecuritySettings();
            Map<String, Object> response = new HashMap<>();
            response.put("twoFactorEnabled", settings.isTwoFactorEnabled());
            response.put("twoFactorMethod", settings.getTwoFactorMethod());
            response.put("loginAlerts", settings.isLoginAlerts());
            response.put("suspiciousActivityAlerts", settings.isSuspiciousActivityAlerts());
            response.put("trustedDevices", settings.getTrustedDevices().size());
            response.put("lastPasswordChange", settings.getLastPasswordChange());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch security settings: " + e.getMessage()));
        }
    }

    /**
     * Update security settings
     */
    @PutMapping("/security")
    public ResponseEntity<?> updateSecuritySettings(
            @PathVariable String customerId,
            @RequestBody SecuritySettings securitySettings) {
        try {
            preferencesService.updateSecuritySettings(customerId, securitySettings);
            return ResponseEntity.ok(Map.of("message", "Security settings updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update security settings: " + e.getMessage()));
        }
    }

    /**
     * Enable two-factor authentication
     */
    @PostMapping("/security/2fa/enable")
    public ResponseEntity<?> enableTwoFactor(
            @PathVariable String customerId,
            @RequestBody Map<String, String> request) {
        try {
            String method = request.getOrDefault("method", "authenticator");
            String secret = request.get("secret"); // In real app, generate this server-side
            preferencesService.enableTwoFactor(customerId, method, secret);
            return ResponseEntity.ok(Map.of(
                "message", "Two-factor authentication enabled",
                "method", method
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to enable 2FA: " + e.getMessage()));
        }
    }

    /**
     * Disable two-factor authentication
     */
    @PostMapping("/security/2fa/disable")
    public ResponseEntity<?> disableTwoFactor(@PathVariable String customerId) {
        try {
            preferencesService.disableTwoFactor(customerId);
            return ResponseEntity.ok(Map.of("message", "Two-factor authentication disabled"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to disable 2FA: " + e.getMessage()));
        }
    }

    /**
     * Add trusted device
     */
    @PostMapping("/security/devices")
    public ResponseEntity<?> addTrustedDevice(
            @PathVariable String customerId,
            @RequestBody Map<String, String> request) {
        try {
            String deviceId = request.get("deviceId");
            CustomerPreferences updated = preferencesService.addTrustedDevice(customerId, deviceId);
            return ResponseEntity.ok(Map.of(
                "message", "Device added to trusted list",
                "totalDevices", updated.getSecuritySettings().getTrustedDevices().size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to add trusted device: " + e.getMessage()));
        }
    }

    /**
     * Remove trusted device
     */
    @DeleteMapping("/security/devices/{deviceId}")
    public ResponseEntity<?> removeTrustedDevice(
            @PathVariable String customerId,
            @PathVariable String deviceId) {
        try {
            preferencesService.removeTrustedDevice(customerId, deviceId);
            return ResponseEntity.ok(Map.of("message", "Device removed from trusted list"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to remove trusted device: " + e.getMessage()));
        }
    }

    /**
     * Get active sessions
     */
    @GetMapping("/security/sessions")
    public ResponseEntity<?> getActiveSessions(@PathVariable String customerId) {
        try {
            return ResponseEntity.ok(preferencesService.getActiveSessions(customerId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch active sessions: " + e.getMessage()));
        }
    }

    /**
     * Export customer data
     */
    @GetMapping("/export")
    public ResponseEntity<?> exportCustomerData(@PathVariable String customerId) {
        try {
            CustomerPreferences preferences = preferencesService.getOrCreatePreferences(customerId);
            Customer customer = customerRepository.findByCustomerId(customerId);

            Map<String, Object> profile = new HashMap<>();
            if (customer != null) {
                profile.put("customerId", customer.getCustomerId());
                profile.put("fullName", customer.getFullName());
                profile.put("emailAddress", customer.getEmailAddress());
                profile.put("contactNumber", customer.getContactNumber());
                profile.put("city", customer.getCity());
                profile.put("state", customer.getState());
                profile.put("status", customer.getStatus());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("exportedAt", java.time.LocalDateTime.now());
            response.put("profile", profile);
            response.put("preferences", preferences);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to export customer data: " + e.getMessage()));
        }
    }

    /**
     * Deactivate customer account
     */
    @PostMapping("/deactivate")
    public ResponseEntity<?> deactivateAccount(@PathVariable String customerId) {
        try {
            Customer customer = customerRepository.findByCustomerId(customerId);
            if (customer == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Customer not found"));
            }

            customer.setStatus("inactive");
            customerRepository.save(customer);

            return ResponseEntity.ok(Map.of(
                "message", "Account deactivated successfully",
                "customerId", customerId,
                "status", "inactive"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to deactivate account: " + e.getMessage()));
        }
    }

    // ================================
    // Appearance Settings Endpoints
    // ================================

    /**
     * Get appearance settings
     */
    @GetMapping("/appearance")
    public ResponseEntity<?> getAppearanceSettings(@PathVariable String customerId) {
        try {
            CustomerPreferences preferences = preferencesService.getOrCreatePreferences(customerId);
            return ResponseEntity.ok(preferences.getAppearanceSettings());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch appearance settings: " + e.getMessage()));
        }
    }

    /**
     * Update appearance settings
     */
    @PutMapping("/appearance")
    public ResponseEntity<?> updateAppearanceSettings(
            @PathVariable String customerId,
            @RequestBody AppearanceSettings appearanceSettings) {
        try {
            CustomerPreferences updated = preferencesService.updateAppearanceSettings(customerId, appearanceSettings);
            return ResponseEntity.ok(updated.getAppearanceSettings());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update appearance settings: " + e.getMessage()));
        }
    }

    /**
     * Set theme mode
     */
    @PatchMapping("/appearance/theme")
    public ResponseEntity<?> setThemeMode(
            @PathVariable String customerId,
            @RequestBody Map<String, String> request) {
        try {
            String theme = request.get("theme");
            CustomerPreferences updated = preferencesService.setThemeMode(customerId, theme);
            return ResponseEntity.ok(Map.of("theme", updated.getAppearanceSettings().getThemeMode()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to set theme: " + e.getMessage()));
        }
    }

    /**
     * Set font size
     */
    @PatchMapping("/appearance/font-size")
    public ResponseEntity<?> setFontSize(
            @PathVariable String customerId,
            @RequestBody Map<String, String> request) {
        try {
            String fontSize = request.get("fontSize");
            CustomerPreferences updated = preferencesService.setFontSize(customerId, fontSize);
            return ResponseEntity.ok(Map.of("fontSize", updated.getAppearanceSettings().getFontSize()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to set font size: " + e.getMessage()));
        }
    }

    // ================================
    // Privacy Settings Endpoints
    // ================================

    /**
     * Get privacy settings
     */
    @GetMapping("/privacy")
    public ResponseEntity<?> getPrivacySettings(@PathVariable String customerId) {
        try {
            CustomerPreferences preferences = preferencesService.getOrCreatePreferences(customerId);
            return ResponseEntity.ok(preferences.getPrivacySettings());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch privacy settings: " + e.getMessage()));
        }
    }

    /**
     * Update privacy settings
     */
    @PutMapping("/privacy")
    public ResponseEntity<?> updatePrivacySettings(
            @PathVariable String customerId,
            @RequestBody PrivacySettings privacySettings) {
        try {
            CustomerPreferences updated = preferencesService.updatePrivacySettings(customerId, privacySettings);
            return ResponseEntity.ok(updated.getPrivacySettings());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update privacy settings: " + e.getMessage()));
        }
    }

    /**
     * Set profile visibility
     */
    @PatchMapping("/privacy/visibility")
    public ResponseEntity<?> setProfileVisibility(
            @PathVariable String customerId,
            @RequestBody Map<String, String> request) {
        try {
            String visibility = request.get("visibility");
            CustomerPreferences updated = preferencesService.setProfileVisibility(customerId, visibility);
            return ResponseEntity.ok(Map.of("visibility", updated.getPrivacySettings().getProfileVisibility()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to set profile visibility: " + e.getMessage()));
        }
    }

    /**
     * Toggle data analytics opt-in
     */
    @PatchMapping("/privacy/analytics")
    public ResponseEntity<?> toggleDataAnalytics(
            @PathVariable String customerId,
            @RequestBody Map<String, Boolean> request) {
        try {
            boolean enabled = request.getOrDefault("enabled", true);
            CustomerPreferences updated = preferencesService.toggleDataAnalytics(customerId, enabled);
            return ResponseEntity.ok(Map.of("allowDataAnalytics", updated.getPrivacySettings().isAllowDataAnalytics()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to toggle data analytics: " + e.getMessage()));
        }
    }

    // ================================
    // Connected Services Endpoints
    // ================================

    /**
     * Get connected services
     */
    @GetMapping("/services")
    public ResponseEntity<?> getConnectedServices(@PathVariable String customerId) {
        try {
            CustomerPreferences preferences = preferencesService.getOrCreatePreferences(customerId);
            return ResponseEntity.ok(preferences.getConnectedServices());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch connected services: " + e.getMessage()));
        }
    }

    /**
     * Connect a service
     */
    @PostMapping("/services/{serviceId}/connect")
    public ResponseEntity<?> connectService(
            @PathVariable String customerId,
            @PathVariable String serviceId,
            @RequestBody Map<String, String> request) {
        try {
            String externalId = request.get("externalId");
            preferencesService.connectService(customerId, serviceId, externalId);
            return ResponseEntity.ok(Map.of(
                "message", "Service connected successfully",
                "serviceId", serviceId
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to connect service: " + e.getMessage()));
        }
    }

    /**
     * Disconnect a service
     */
    @PostMapping("/services/{serviceId}/disconnect")
    public ResponseEntity<?> disconnectService(
            @PathVariable String customerId,
            @PathVariable String serviceId) {
        try {
            preferencesService.disconnectService(customerId, serviceId);
            return ResponseEntity.ok(Map.of(
                "message", "Service disconnected successfully",
                "serviceId", serviceId
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to disconnect service: " + e.getMessage()));
        }
    }

    // ================================
    // Regional Settings Endpoints
    // ================================

    /**
     * Get regional settings
     */
    @GetMapping("/regional")
    public ResponseEntity<?> getRegionalSettings(@PathVariable String customerId) {
        try {
            CustomerPreferences preferences = preferencesService.getOrCreatePreferences(customerId);
            Map<String, String> regional = new HashMap<>();
            regional.put("language", preferences.getLanguage());
            regional.put("timezone", preferences.getTimezone());
            regional.put("currency", preferences.getCurrency());
            return ResponseEntity.ok(regional);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch regional settings: " + e.getMessage()));
        }
    }

    /**
     * Update regional settings
     */
    @PutMapping("/regional")
    public ResponseEntity<?> updateRegionalSettings(
            @PathVariable String customerId,
            @RequestBody Map<String, String> request) {
        try {
            if (request.containsKey("language")) {
                preferencesService.setLanguage(customerId, request.get("language"));
            }
            if (request.containsKey("timezone")) {
                preferencesService.setTimezone(customerId, request.get("timezone"));
            }
            if (request.containsKey("currency")) {
                preferencesService.setCurrency(customerId, request.get("currency"));
            }
            
            CustomerPreferences updated = preferencesService.getOrCreatePreferences(customerId);
            Map<String, String> regional = new HashMap<>();
            regional.put("language", updated.getLanguage());
            regional.put("timezone", updated.getTimezone());
            regional.put("currency", updated.getCurrency());
            return ResponseEntity.ok(regional);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update regional settings: " + e.getMessage()));
        }
    }
}
