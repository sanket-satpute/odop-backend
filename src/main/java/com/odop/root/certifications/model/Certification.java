package com.odop.root.certifications.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Model for vendor certifications (GI Tag, Quality, Sustainability, etc.)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "certifications")
public class Certification {
    
    @Id
    private String certificationId;
    private String vendorId;
    
    // Certification details
    private String name;                    // e.g., "GI Tag Certificate", "ISO 9001"
    private String type;                    // GI_TAG, QUALITY, SUSTAINABILITY, ORGANIC, HANDLOOM
    private String description;
    private String issuingAuthority;        // Who issued the certification
    private String certificateNumber;        // Certificate ID/number
    
    // Validity
    private LocalDateTime issuedDate;
    private LocalDateTime expiryDate;
    private String status;                  // ACTIVE, EXPIRED, PENDING, REJECTED
    
    // Documents
    private List<String> documentUrls;      // Uploaded certificate documents
    private String verificationUrl;         // URL to verify certificate
    
    // Product associations
    private List<String> applicableProductIds;  // Products this certification applies to
    private List<String> applicableCategories;  // Categories this applies to
    
    // Verification
    private boolean verified;               // Admin verified
    private String verifiedBy;              // Admin who verified
    private LocalDateTime verifiedAt;
    private String rejectionReason;
    
    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
