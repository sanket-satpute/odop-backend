package com.odop.root.certifications.service;

import com.odop.root.certifications.dto.*;
import com.odop.root.certifications.model.Certification;
import com.odop.root.certifications.repository.CertificationRepository;
import com.odop.root.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing vendor certifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CertificationService {

    private final CertificationRepository certificationRepository;
    private final ProductRepository productRepository;
    
    private static final int EXPIRING_SOON_DAYS = 30;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Get certification summary for a vendor
     */
    public CertificationSummaryDto getCertificationSummary(String vendorId) {
        log.info("Fetching certification summary for vendor: {}", vendorId);
        
        List<Certification> certifications = certificationRepository.findByVendorIdOrderByCreatedAtDesc(vendorId);
        List<CertificationDto> certDtos = certifications.stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
        
        // Count by status
        int active = (int) certifications.stream().filter(c -> "ACTIVE".equals(c.getStatus())).count();
        int pending = (int) certifications.stream().filter(c -> "PENDING".equals(c.getStatus())).count();
        int expired = (int) certifications.stream().filter(c -> "EXPIRED".equals(c.getStatus())).count();
        
        // Count expiring soon (within 30 days)
        LocalDateTime thirtyDaysLater = LocalDateTime.now().plusDays(EXPIRING_SOON_DAYS);
        int expiringSoon = (int) certifications.stream()
            .filter(c -> "ACTIVE".equals(c.getStatus()))
            .filter(c -> c.getExpiryDate() != null && c.getExpiryDate().isBefore(thirtyDaysLater))
            .count();
        
        // Count by type
        Map<String, Long> typeCounts = certifications.stream()
            .collect(Collectors.groupingBy(c -> c.getType() != null ? c.getType() : "OTHER", Collectors.counting()));
        
        // Check GI Tag
        CertificationDto giTagCert = certDtos.stream()
            .filter(c -> "GI_TAG".equals(c.getType()) && "ACTIVE".equals(c.getStatus()))
            .findFirst()
            .orElse(null);
        
        // Build alerts
        List<CertificationSummaryDto.CertificationAlertDto> alerts = buildAlerts(certifications);
        
        // Product coverage
        long totalProducts = productRepository.countByVendorId(vendorId);
        Set<String> certifiedProductIds = certifications.stream()
            .filter(c -> "ACTIVE".equals(c.getStatus()))
            .filter(c -> c.getApplicableProductIds() != null)
            .flatMap(c -> c.getApplicableProductIds().stream())
            .collect(Collectors.toSet());
        
        double coveragePercent = totalProducts > 0 
            ? (certifiedProductIds.size() / (double) totalProducts) * 100 
            : 0;
        
        return CertificationSummaryDto.builder()
            .vendorId(vendorId)
            .totalCertifications(certifications.size())
            .activeCertifications(active)
            .pendingCertifications(pending)
            .expiredCertifications(expired)
            .expiringSoonCount(expiringSoon)
            .hasGiTag(giTagCert != null)
            .giTagCertification(giTagCert)
            .giTagCount(typeCounts.getOrDefault("GI_TAG", 0L).intValue())
            .qualityCount(typeCounts.getOrDefault("QUALITY", 0L).intValue())
            .sustainabilityCount(typeCounts.getOrDefault("SUSTAINABILITY", 0L).intValue())
            .organicCount(typeCounts.getOrDefault("ORGANIC", 0L).intValue())
            .handloomCount(typeCounts.getOrDefault("HANDLOOM", 0L).intValue())
            .otherCount(typeCounts.getOrDefault("OTHER", 0L).intValue())
            .certifiedProductsCount(certifiedProductIds.size())
            .totalProductsCount((int) totalProducts)
            .certificationCoveragePercent(coveragePercent)
            .certifications(certDtos)
            .alerts(alerts)
            .build();
    }

    /**
     * Get all certifications for a vendor
     */
    public List<CertificationDto> getVendorCertifications(String vendorId, String status) {
        log.info("Fetching certifications for vendor: {}, status: {}", vendorId, status);
        
        List<Certification> certifications;
        if (status != null && !status.isEmpty()) {
            certifications = certificationRepository.findByVendorIdAndStatus(vendorId, status);
        } else {
            certifications = certificationRepository.findByVendorIdOrderByCreatedAtDesc(vendorId);
        }
        
        return certifications.stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    /**
     * Get a single certification by ID
     */
    public CertificationDto getCertificationById(String certificationId) {
        log.info("Fetching certification: {}", certificationId);
        
        return certificationRepository.findById(certificationId)
            .map(this::mapToDto)
            .orElseThrow(() -> new RuntimeException("Certification not found: " + certificationId));
    }

    /**
     * Create new certification application
     */
    public CertificationDto createCertification(CertificationRequestDto request) {
        log.info("Creating certification for vendor: {}, type: {}", request.getVendorId(), request.getType());
        
        Certification certification = Certification.builder()
            .certificationId(UUID.randomUUID().toString())
            .vendorId(request.getVendorId())
            .name(request.getName())
            .type(request.getType())
            .description(request.getDescription())
            .issuingAuthority(request.getIssuingAuthority())
            .certificateNumber(request.getCertificateNumber())
            .issuedDate(parseDate(request.getIssuedDate()))
            .expiryDate(parseDate(request.getExpiryDate()))
            .status("PENDING")
            .documentUrls(request.getDocumentUrls())
            .verificationUrl(request.getVerificationUrl())
            .applicableProductIds(request.getApplicableProductIds())
            .applicableCategories(request.getApplicableCategories())
            .verified(false)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        Certification saved = certificationRepository.save(certification);
        log.info("Certification created: {}", saved.getCertificationId());
        
        return mapToDto(saved);
    }

    /**
     * Update existing certification
     */
    public CertificationDto updateCertification(String certificationId, CertificationRequestDto request) {
        log.info("Updating certification: {}", certificationId);
        
        Certification existing = certificationRepository.findById(certificationId)
            .orElseThrow(() -> new RuntimeException("Certification not found: " + certificationId));
        
        // Update fields
        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setIssuingAuthority(request.getIssuingAuthority());
        existing.setCertificateNumber(request.getCertificateNumber());
        existing.setIssuedDate(parseDate(request.getIssuedDate()));
        existing.setExpiryDate(parseDate(request.getExpiryDate()));
        existing.setDocumentUrls(request.getDocumentUrls());
        existing.setVerificationUrl(request.getVerificationUrl());
        existing.setApplicableProductIds(request.getApplicableProductIds());
        existing.setApplicableCategories(request.getApplicableCategories());
        existing.setUpdatedAt(LocalDateTime.now());
        
        // Reset verification if documents changed
        existing.setVerified(false);
        existing.setStatus("PENDING");
        
        Certification saved = certificationRepository.save(existing);
        return mapToDto(saved);
    }

    /**
     * Delete a certification
     */
    public void deleteCertification(String certificationId) {
        log.info("Deleting certification: {}", certificationId);
        certificationRepository.deleteById(certificationId);
    }

    /**
     * Renew a certification (create renewal application)
     */
    public CertificationDto renewCertification(String certificationId) {
        log.info("Renewing certification: {}", certificationId);
        
        Certification existing = certificationRepository.findById(certificationId)
            .orElseThrow(() -> new RuntimeException("Certification not found: " + certificationId));
        
        // Mark old as expired
        existing.setStatus("EXPIRED");
        certificationRepository.save(existing);
        
        // Create renewal
        Certification renewal = Certification.builder()
            .certificationId(UUID.randomUUID().toString())
            .vendorId(existing.getVendorId())
            .name(existing.getName() + " (Renewal)")
            .type(existing.getType())
            .description(existing.getDescription())
            .issuingAuthority(existing.getIssuingAuthority())
            .status("PENDING")
            .applicableProductIds(existing.getApplicableProductIds())
            .applicableCategories(existing.getApplicableCategories())
            .verified(false)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        Certification saved = certificationRepository.save(renewal);
        return mapToDto(saved);
    }

    /**
     * Get certifications for a specific product
     */
    public List<CertificationDto> getCertificationsForProduct(String productId) {
        log.info("Fetching certifications for product: {}", productId);
        
        return certificationRepository.findByApplicableProductIdsContaining(productId).stream()
            .filter(c -> "ACTIVE".equals(c.getStatus()))
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    // ==================== ADMIN METHODS ====================

    /**
     * Verify a certification (Admin only)
     */
    public CertificationDto verifyCertification(String certificationId, String adminId, boolean approved, String reason) {
        log.info("Verifying certification: {}, approved: {}", certificationId, approved);
        
        Certification certification = certificationRepository.findById(certificationId)
            .orElseThrow(() -> new RuntimeException("Certification not found: " + certificationId));
        
        if (approved) {
            certification.setVerified(true);
            certification.setStatus("ACTIVE");
            certification.setVerifiedBy(adminId);
            certification.setVerifiedAt(LocalDateTime.now());
        } else {
            certification.setVerified(false);
            certification.setStatus("REJECTED");
            certification.setRejectionReason(reason);
        }
        
        certification.setUpdatedAt(LocalDateTime.now());
        Certification saved = certificationRepository.save(certification);
        
        return mapToDto(saved);
    }

    /**
     * Get all pending certifications (Admin only)
     */
    public List<CertificationDto> getPendingCertifications() {
        log.info("Fetching all pending certifications");
        
        return certificationRepository.findByVerifiedFalseAndStatus("PENDING").stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    // ==================== HELPER METHODS ====================

    private CertificationDto mapToDto(Certification cert) {
        int daysUntilExpiry = 0;
        boolean expiringSoon = false;
        
        if (cert.getExpiryDate() != null) {
            daysUntilExpiry = (int) ChronoUnit.DAYS.between(LocalDateTime.now(), cert.getExpiryDate());
            expiringSoon = daysUntilExpiry >= 0 && daysUntilExpiry <= EXPIRING_SOON_DAYS;
        }
        
        int productCount = cert.getApplicableProductIds() != null ? cert.getApplicableProductIds().size() : 0;
        
        return CertificationDto.builder()
            .certificationId(cert.getCertificationId())
            .vendorId(cert.getVendorId())
            .name(cert.getName())
            .type(cert.getType())
            .description(cert.getDescription())
            .issuingAuthority(cert.getIssuingAuthority())
            .certificateNumber(cert.getCertificateNumber())
            .issuedDate(cert.getIssuedDate())
            .expiryDate(cert.getExpiryDate())
            .status(cert.getStatus())
            .daysUntilExpiry(Math.max(daysUntilExpiry, 0))
            .isExpiringSoon(expiringSoon)
            .documentUrls(cert.getDocumentUrls())
            .verificationUrl(cert.getVerificationUrl())
            .applicableProductIds(cert.getApplicableProductIds())
            .applicableCategories(cert.getApplicableCategories())
            .productCount(productCount)
            .verified(cert.isVerified())
            .rejectionReason(cert.getRejectionReason())
            .createdAt(cert.getCreatedAt())
            .updatedAt(cert.getUpdatedAt())
            .build();
    }

    private List<CertificationSummaryDto.CertificationAlertDto> buildAlerts(List<Certification> certifications) {
        List<CertificationSummaryDto.CertificationAlertDto> alerts = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (Certification cert : certifications) {
            // Expired alert
            if ("EXPIRED".equals(cert.getStatus())) {
                alerts.add(CertificationSummaryDto.CertificationAlertDto.builder()
                    .certificationId(cert.getCertificationId())
                    .certificationName(cert.getName())
                    .type("EXPIRED")
                    .message(cert.getName() + " has expired. Please renew to maintain your certification.")
                    .daysRemaining(0)
                    .build());
            }
            // Expiring soon alert
            else if ("ACTIVE".equals(cert.getStatus()) && cert.getExpiryDate() != null) {
                int daysRemaining = (int) ChronoUnit.DAYS.between(now, cert.getExpiryDate());
                if (daysRemaining > 0 && daysRemaining <= EXPIRING_SOON_DAYS) {
                    alerts.add(CertificationSummaryDto.CertificationAlertDto.builder()
                        .certificationId(cert.getCertificationId())
                        .certificationName(cert.getName())
                        .type("EXPIRING")
                        .message(cert.getName() + " expires in " + daysRemaining + " days.")
                        .daysRemaining(daysRemaining)
                        .build());
                }
            }
            // Pending review alert
            else if ("PENDING".equals(cert.getStatus())) {
                alerts.add(CertificationSummaryDto.CertificationAlertDto.builder()
                    .certificationId(cert.getCertificationId())
                    .certificationName(cert.getName())
                    .type("PENDING_REVIEW")
                    .message(cert.getName() + " is pending verification.")
                    .daysRemaining(-1)
                    .build());
            }
        }
        
        // Sort by urgency (expired first, then by days remaining)
        alerts.sort((a, b) -> {
            if ("EXPIRED".equals(a.getType())) return -1;
            if ("EXPIRED".equals(b.getType())) return 1;
            return Integer.compare(a.getDaysRemaining(), b.getDaysRemaining());
        });
        
        return alerts;
    }

    private LocalDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            // Try ISO format first
            return LocalDateTime.parse(dateStr, dateFormatter);
        } catch (Exception e) {
            // Try date-only format
            try {
                return LocalDateTime.parse(dateStr + "T00:00:00", dateFormatter);
            } catch (Exception e2) {
                log.warn("Could not parse date: {}", dateStr);
                return null;
            }
        }
    }
}
