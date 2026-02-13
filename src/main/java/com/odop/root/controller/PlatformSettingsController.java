package com.odop.root.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.odop.root.models.settings.PlatformSettings;
import com.odop.root.services.PlatformSettingsService;

@RestController
@RequestMapping("odop/settings")
@CrossOrigin
public class PlatformSettingsController {

    @Autowired
    private PlatformSettingsService settingsService;

    // ==================== GET ALL SETTINGS ====================

    @GetMapping
    public ResponseEntity<PlatformSettings> getAllSettings() {
        return ResponseEntity.ok(settingsService.getSettings());
    }

    @PutMapping
    public ResponseEntity<PlatformSettings> updateAllSettings(@RequestBody PlatformSettings settings) {
        return ResponseEntity.ok(settingsService.updateAllSettings(settings));
    }

    // ==================== PUBLIC SETTINGS ====================

    @GetMapping("/public/general")
    public ResponseEntity<Map<String, Object>> getPublicGeneralSettings() {
        return ResponseEntity.ok(settingsService.getGeneralSettings());
    }

    // ==================== GENERAL SETTINGS ====================

    @GetMapping("/general")
    public ResponseEntity<Map<String, Object>> getGeneralSettings() {
        return ResponseEntity.ok(settingsService.getGeneralSettings());
    }

    @PutMapping("/general")
    public ResponseEntity<PlatformSettings> updateGeneralSettings(@RequestBody Map<String, Object> generalData) {
        return ResponseEntity.ok(settingsService.updateGeneralSettings(generalData));
    }

    // ==================== SECURITY SETTINGS ====================

    @GetMapping("/security")
    public ResponseEntity<Map<String, Object>> getSecuritySettings() {
        return ResponseEntity.ok(settingsService.getSecuritySettings());
    }

    @PutMapping("/security")
    public ResponseEntity<PlatformSettings> updateSecuritySettings(@RequestBody Map<String, Object> securityData) {
        return ResponseEntity.ok(settingsService.updateSecuritySettings(securityData));
    }

    // ==================== EMAIL SETTINGS ====================

    @GetMapping("/email")
    public ResponseEntity<Map<String, Object>> getEmailSettings() {
        return ResponseEntity.ok(settingsService.getEmailSettings());
    }

    @PutMapping("/email")
    public ResponseEntity<PlatformSettings> updateEmailSettings(@RequestBody Map<String, Object> emailData) {
        return ResponseEntity.ok(settingsService.updateEmailSettings(emailData));
    }

    // ==================== NOTIFICATION SETTINGS ====================

    @GetMapping("/notifications")
    public ResponseEntity<Map<String, Object>> getNotificationSettings() {
        return ResponseEntity.ok(settingsService.getNotificationSettings());
    }

    @PutMapping("/notifications")
    public ResponseEntity<PlatformSettings> updateNotificationSettings(@RequestBody Map<String, Object> notificationData) {
        return ResponseEntity.ok(settingsService.updateNotificationSettings(notificationData));
    }

    // ==================== MAINTENANCE SETTINGS ====================

    @GetMapping("/maintenance")
    public ResponseEntity<Map<String, Object>> getMaintenanceSettings() {
        return ResponseEntity.ok(settingsService.getMaintenanceSettings());
    }

    @PutMapping("/maintenance")
    public ResponseEntity<PlatformSettings> updateMaintenanceSettings(@RequestBody Map<String, Object> maintenanceData) {
        return ResponseEntity.ok(settingsService.updateMaintenanceSettings(maintenanceData));
    }

    @GetMapping("/maintenance/status")
    public ResponseEntity<Map<String, Object>> getMaintenanceStatus() {
        Map<String, Object> status = Map.of(
            "enabled", settingsService.isMaintenanceMode(),
            "message", settingsService.getMaintenanceSettings().get("message")
        );
        return ResponseEntity.ok(status);
    }

    // ==================== PAYMENT SETTINGS ====================

    @GetMapping("/payment")
    public ResponseEntity<Map<String, Object>> getPaymentSettings() {
        return ResponseEntity.ok(settingsService.getPaymentSettings());
    }

    @PutMapping("/payment")
    public ResponseEntity<PlatformSettings> updatePaymentSettings(@RequestBody Map<String, Object> paymentData) {
        return ResponseEntity.ok(settingsService.updatePaymentSettings(paymentData));
    }

    // ==================== SOCIAL MEDIA SETTINGS ====================

    @GetMapping("/social")
    public ResponseEntity<Map<String, Object>> getSocialSettings() {
        return ResponseEntity.ok(settingsService.getSocialSettings());
    }

    @PutMapping("/social")
    public ResponseEntity<PlatformSettings> updateSocialSettings(@RequestBody Map<String, Object> socialData) {
        return ResponseEntity.ok(settingsService.updateSocialSettings(socialData));
    }

    // ==================== CONTACT SETTINGS ====================

    @GetMapping("/contact")
    public ResponseEntity<Map<String, Object>> getContactSettings() {
        return ResponseEntity.ok(settingsService.getContactSettings());
    }

    @PutMapping("/contact")
    public ResponseEntity<PlatformSettings> updateContactSettings(@RequestBody Map<String, Object> contactData) {
        return ResponseEntity.ok(settingsService.updateContactSettings(contactData));
    }

    // ==================== PUBLIC ENDPOINTS ====================

    /**
     * Public endpoint for clients to check maintenance status
     */
    @GetMapping("/public/maintenance")
    public ResponseEntity<Map<String, Object>> getPublicMaintenanceStatus() {
        Map<String, Object> status = Map.of(
            "enabled", settingsService.isMaintenanceMode(),
            "message", settingsService.getMaintenanceSettings().get("message")
        );
        return ResponseEntity.ok(status);
    }

    /**
     * Public endpoint for clients to get social media links
     */
    @GetMapping("/public/social")
    public ResponseEntity<Map<String, Object>> getPublicSocialLinks() {
        return ResponseEntity.ok(settingsService.getSocialSettings());
    }

    /**
     * Public endpoint for clients to get contact information
     */
    @GetMapping("/public/contact")
    public ResponseEntity<Map<String, Object>> getPublicContactInfo() {
        return ResponseEntity.ok(settingsService.getContactSettings());
    }
}
