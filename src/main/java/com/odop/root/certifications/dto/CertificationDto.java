package com.odop.root.certifications.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for certification data transfer
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificationDto {
    
    private String certificationId;
    private String vendorId;
    
    // Basic info
    private String name;
    private String type;
    private String description;
    private String issuingAuthority;
    private String certificateNumber;
    
    // Validity
    private LocalDateTime issuedDate;
    private LocalDateTime expiryDate;
    private String status;
    private int daysUntilExpiry;
    private boolean isExpiringSoon; // Expires within 30 days
    
    // Documents
    private List<String> documentUrls;
    private String verificationUrl;
    
    // Products
    private List<String> applicableProductIds;
    private List<String> applicableCategories;
    private int productCount;
    
    // Verification
    private boolean verified;
    private String rejectionReason;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
