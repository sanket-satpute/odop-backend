package com.odop.root.certifications.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for vendor certification summary
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificationSummaryDto {
    
    private String vendorId;
    
    // Counts
    private int totalCertifications;
    private int activeCertifications;
    private int pendingCertifications;
    private int expiredCertifications;
    private int expiringSoonCount;
    
    // GI Tag specific
    private boolean hasGiTag;
    private CertificationDto giTagCertification;
    
    // By type
    private int giTagCount;
    private int qualityCount;
    private int sustainabilityCount;
    private int organicCount;
    private int handloomCount;
    private int otherCount;
    
    // Products coverage
    private int certifiedProductsCount;
    private int totalProductsCount;
    private double certificationCoveragePercent;
    
    // Certifications list
    private List<CertificationDto> certifications;
    
    // Alerts
    private List<CertificationAlertDto> alerts;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CertificationAlertDto {
        private String certificationId;
        private String certificationName;
        private String type;        // EXPIRING, EXPIRED, PENDING_REVIEW
        private String message;
        private int daysRemaining;  // For EXPIRING type
    }
}
