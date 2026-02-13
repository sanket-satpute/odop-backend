package com.odop.root.certifications.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * DTO for creating/updating certification applications
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificationRequestDto {
    
    private String vendorId;
    
    // Certification details
    private String name;
    private String type;        // GI_TAG, QUALITY, SUSTAINABILITY, ORGANIC, HANDLOOM
    private String description;
    private String issuingAuthority;
    private String certificateNumber;
    
    // Dates (ISO format strings)
    private String issuedDate;
    private String expiryDate;
    
    // Documents (URLs if already uploaded)
    private List<String> documentUrls;
    private String verificationUrl;
    
    // Product associations
    private List<String> applicableProductIds;
    private List<String> applicableCategories;
}
