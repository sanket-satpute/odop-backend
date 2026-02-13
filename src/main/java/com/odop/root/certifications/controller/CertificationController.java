package com.odop.root.certifications.controller;

import com.odop.root.certifications.dto.*;
import com.odop.root.certifications.service.CertificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for vendor certifications management.
 * Provides endpoints for GI tags, quality certifications, and other compliance documents.
 */
@RestController
@RequestMapping("/odop/certifications")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CertificationController {

    private final CertificationService certificationService;

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Certifications API");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    // ==================== VENDOR ENDPOINTS ====================

    /**
     * Get certification summary for a vendor
     */
    @GetMapping("/vendor/{vendorId}/summary")
    public ResponseEntity<CertificationSummaryDto> getCertificationSummary(
            @PathVariable String vendorId) {
        
        log.info("Fetching certification summary for vendor: {}", vendorId);
        CertificationSummaryDto summary = certificationService.getCertificationSummary(vendorId);
        return ResponseEntity.ok(summary);
    }

    /**
     * Get all certifications for a vendor
     */
    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<CertificationDto>> getVendorCertifications(
            @PathVariable String vendorId,
            @RequestParam(required = false) String status) {
        
        log.info("Fetching certifications for vendor: {}, status: {}", vendorId, status);
        List<CertificationDto> certifications = certificationService.getVendorCertifications(vendorId, status);
        return ResponseEntity.ok(certifications);
    }

    /**
     * Get a single certification by ID
     */
    @GetMapping("/{certificationId}")
    public ResponseEntity<CertificationDto> getCertification(
            @PathVariable String certificationId) {
        
        log.info("Fetching certification: {}", certificationId);
        CertificationDto certification = certificationService.getCertificationById(certificationId);
        return ResponseEntity.ok(certification);
    }

    /**
     * Create/apply for a new certification
     */
    @PostMapping("/vendor/{vendorId}")
    public ResponseEntity<CertificationDto> createCertification(
            @PathVariable String vendorId,
            @RequestBody CertificationRequestDto request) {
        
        request.setVendorId(vendorId);
        log.info("Creating certification for vendor: {}, type: {}", vendorId, request.getType());
        CertificationDto created = certificationService.createCertification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update an existing certification
     */
    @PutMapping("/{certificationId}")
    public ResponseEntity<CertificationDto> updateCertification(
            @PathVariable String certificationId,
            @RequestBody CertificationRequestDto request) {
        
        log.info("Updating certification: {}", certificationId);
        CertificationDto updated = certificationService.updateCertification(certificationId, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a certification
     */
    @DeleteMapping("/{certificationId}")
    public ResponseEntity<Void> deleteCertification(
            @PathVariable String certificationId) {
        
        log.info("Deleting certification: {}", certificationId);
        certificationService.deleteCertification(certificationId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Renew an expired or expiring certification
     */
    @PostMapping("/{certificationId}/renew")
    public ResponseEntity<CertificationDto> renewCertification(
            @PathVariable String certificationId) {
        
        log.info("Renewing certification: {}", certificationId);
        CertificationDto renewed = certificationService.renewCertification(certificationId);
        return ResponseEntity.ok(renewed);
    }

    /**
     * Get certifications for a specific product
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<CertificationDto>> getProductCertifications(
            @PathVariable String productId) {
        
        log.info("Fetching certifications for product: {}", productId);
        List<CertificationDto> certifications = certificationService.getCertificationsForProduct(productId);
        return ResponseEntity.ok(certifications);
    }

    // ==================== ADMIN ENDPOINTS ====================

    /**
     * Get all pending certifications (Admin only)
     */
    @GetMapping("/admin/pending")
    public ResponseEntity<List<CertificationDto>> getPendingCertifications() {
        log.info("Fetching all pending certifications");
        List<CertificationDto> pending = certificationService.getPendingCertifications();
        return ResponseEntity.ok(pending);
    }

    /**
     * Verify/approve a certification (Admin only)
     */
    @PostMapping("/admin/{certificationId}/verify")
    public ResponseEntity<CertificationDto> verifyCertification(
            @PathVariable String certificationId,
            @RequestParam String adminId,
            @RequestParam boolean approved,
            @RequestParam(required = false) String reason) {
        
        log.info("Verifying certification: {}, approved: {}", certificationId, approved);
        CertificationDto verified = certificationService.verifyCertification(certificationId, adminId, approved, reason);
        return ResponseEntity.ok(verified);
    }

    // ==================== CERTIFICATION TYPES ====================

    /**
     * Get available certification types
     */
    @GetMapping("/types")
    public ResponseEntity<List<Map<String, String>>> getCertificationTypes() {
        List<Map<String, String>> types = List.of(
            Map.of("code", "GI_TAG", "name", "GI Tag Certificate", "description", "Geographical Indication certification for authentic regional products"),
            Map.of("code", "QUALITY", "name", "Quality Certification", "description", "ISO or other quality management certifications"),
            Map.of("code", "SUSTAINABILITY", "name", "Sustainability Certificate", "description", "Environmental sustainability and eco-friendly certifications"),
            Map.of("code", "ORGANIC", "name", "Organic Certification", "description", "Organic product certification"),
            Map.of("code", "HANDLOOM", "name", "Handloom Mark", "description", "Handloom Board certification for authentic handwoven products"),
            Map.of("code", "HANDICRAFT", "name", "Handicraft Mark", "description", "Handicraft certification for authentic handcrafted products"),
            Map.of("code", "OTHER", "name", "Other Certification", "description", "Other relevant certifications")
        );
        return ResponseEntity.ok(types);
    }
}
