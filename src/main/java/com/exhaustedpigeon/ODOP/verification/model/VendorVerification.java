package com.exhaustedpigeon.ODOP.verification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Vendor Verification Request model
 * Tracks the complete verification workflow for vendors
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "vendor_verifications")
public class VendorVerification {

    @Id
    private String id;
    
    @Indexed(unique = true)
    private String vendorId;
    
    private String vendorName;
    private String vendorEmail;
    
    // Current Status
    private VerificationStatus status;
    private String statusMessage;
    
    // Verification Steps
    private VerificationSteps steps;
    
    // Documents
    private List<VerificationDocument> documents;
    
    // GI Tag Verification
    private GiTagVerification giTagVerification;
    
    // Admin Review
    private AdminReview adminReview;
    
    // Timeline
    private LocalDateTime submittedAt;
    private LocalDateTime lastUpdatedAt;
    private LocalDateTime completedAt;
    
    // Rejection History (if rejected and resubmitted)
    private List<RejectionRecord> rejectionHistory;
    
    /**
     * Verification Status Enum
     */
    public enum VerificationStatus {
        NOT_STARTED("Not Started"),
        DOCUMENTS_PENDING("Documents Pending"),
        DOCUMENTS_SUBMITTED("Documents Submitted"),
        UNDER_REVIEW("Under Review"),
        ADDITIONAL_INFO_REQUIRED("Additional Information Required"),
        GI_TAG_PENDING("GI Tag Verification Pending"),
        APPROVED("Approved"),
        REJECTED("Rejected"),
        SUSPENDED("Suspended");
        
        private final String displayName;
        
        VerificationStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Verification Steps tracking
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerificationSteps {
        private boolean basicInfoCompleted;
        private boolean businessDocumentsUploaded;
        private boolean identityVerified;
        private boolean addressVerified;
        private boolean bankDetailsVerified;
        private boolean giTagVerified;
        private boolean agreementSigned;
        
        // Timestamps for each step
        private LocalDateTime basicInfoCompletedAt;
        private LocalDateTime businessDocumentsUploadedAt;
        private LocalDateTime identityVerifiedAt;
        private LocalDateTime addressVerifiedAt;
        private LocalDateTime bankDetailsVerifiedAt;
        private LocalDateTime giTagVerifiedAt;
        private LocalDateTime agreementSignedAt;
        
        /**
         * Calculate overall completion percentage
         */
        public int getCompletionPercentage() {
            int completed = 0;
            if (basicInfoCompleted) completed++;
            if (businessDocumentsUploaded) completed++;
            if (identityVerified) completed++;
            if (addressVerified) completed++;
            if (bankDetailsVerified) completed++;
            if (giTagVerified) completed++;
            if (agreementSigned) completed++;
            return (completed * 100) / 7;
        }
    }
    
    /**
     * Document Model
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerificationDocument {
        private String documentId;
        private DocumentType documentType;
        private String documentName;
        private String documentUrl;
        private String documentNumber;     // e.g., PAN number, GSTIN
        private DocumentStatus status;
        private String rejectionReason;
        private LocalDateTime uploadedAt;
        private LocalDateTime verifiedAt;
        private String verifiedBy;
    }
    
    /**
     * Document Types
     */
    public enum DocumentType {
        PAN_CARD("PAN Card", true),
        AADHAAR_CARD("Aadhaar Card", true),
        GSTIN_CERTIFICATE("GSTIN Certificate", true),
        BUSINESS_LICENSE("Business License", false),
        SHOP_ESTABLISHMENT_LICENSE("Shop Establishment License", false),
        FSSAI_LICENSE("FSSAI License", false),  // For food products
        GI_TAG_CERTIFICATE("GI Tag Certificate", false),
        ARTISAN_CARD("Artisan Card", false),
        BANK_STATEMENT("Bank Statement", true),
        CANCELLED_CHEQUE("Cancelled Cheque", true),
        ADDRESS_PROOF("Address Proof", true),
        SHOP_PHOTO("Shop Photo", true),
        PRODUCT_SAMPLES("Product Sample Photos", false);
        
        private final String displayName;
        private final boolean required;
        
        DocumentType(String displayName, boolean required) {
            this.displayName = displayName;
            this.required = required;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public boolean isRequired() {
            return required;
        }
    }
    
    /**
     * Document Status
     */
    public enum DocumentStatus {
        PENDING("Pending Review"),
        VERIFIED("Verified"),
        REJECTED("Rejected"),
        EXPIRED("Expired");
        
        private final String displayName;
        
        DocumentStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * GI Tag Verification Details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GiTagVerification {
        private String giTagNumber;
        private String productCategory;     // e.g., "Banarasi Silk", "Pashmina"
        private String geographicalIndication;  // e.g., "Varanasi, UP"
        private String certificateUrl;
        private LocalDateTime certificateExpiryDate;
        private boolean verified;
        private String verificationNotes;
    }
    
    /**
     * Admin Review Details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminReview {
        private String reviewedBy;          // Admin ID
        private String reviewerName;
        private LocalDateTime reviewedAt;
        private String reviewNotes;
        private Integer qualityScore;       // 1-10 rating for vendor quality
        private Integer trustScore;         // 1-10 rating for trustworthiness
        private List<String> concerns;      // Any concerns noted
        private List<String> recommendations;   // Recommendations for vendor
    }
    
    /**
     * Rejection Record
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RejectionRecord {
        private LocalDateTime rejectedAt;
        private String rejectedBy;
        private String reason;
        private List<String> documentIssues;
        private LocalDateTime resubmittedAt;
    }
}
