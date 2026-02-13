package com.exhaustedpigeon.ODOP.verification.dto;

import com.exhaustedpigeon.ODOP.verification.model.VendorVerification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for Vendor Verification Status Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationStatusDto {
    
    private String vendorId;
    private String vendorName;
    
    // Current Status
    private String status;
    private String statusDisplayName;
    private String statusMessage;
    
    // Progress
    private int completionPercentage;
    private List<StepStatus> steps;
    
    // Documents
    private List<DocumentStatusDto> documents;
    private List<String> missingDocuments;
    
    // GI Tag
    private GiTagStatusDto giTagStatus;
    
    // Timeline
    private LocalDateTime submittedAt;
    private LocalDateTime lastUpdatedAt;
    private LocalDateTime estimatedCompletionDate;
    
    // If rejected
    private String rejectionReason;
    private List<String> actionRequired;
    
    /**
     * Step status for UI display
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepStatus {
        private String stepName;
        private String stepDisplayName;
        private boolean completed;
        private boolean current;
        private LocalDateTime completedAt;
        private String icon;
    }
    
    /**
     * Document status for UI
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentStatusDto {
        private String documentType;
        private String documentDisplayName;
        private boolean required;
        private boolean uploaded;
        private String status;
        private String statusDisplayName;
        private String documentUrl;
        private String rejectionReason;
    }
    
    /**
     * GI Tag status for UI
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GiTagStatusDto {
        private boolean applicable;
        private boolean submitted;
        private boolean verified;
        private String giTagNumber;
        private String productCategory;
        private LocalDateTime expiryDate;
    }
    
    /**
     * Convert from entity to DTO
     */
    public static VerificationStatusDto fromEntity(VendorVerification verification) {
        VerificationStatusDtoBuilder builder = VerificationStatusDto.builder()
                .vendorId(verification.getVendorId())
                .vendorName(verification.getVendorName())
                .status(verification.getStatus().name())
                .statusDisplayName(verification.getStatus().getDisplayName())
                .statusMessage(verification.getStatusMessage())
                .submittedAt(verification.getSubmittedAt())
                .lastUpdatedAt(verification.getLastUpdatedAt());
        
        // Add steps progress
        if (verification.getSteps() != null) {
            builder.completionPercentage(verification.getSteps().getCompletionPercentage());
            builder.steps(buildStepsList(verification.getSteps()));
        }
        
        // Add documents status
        if (verification.getDocuments() != null) {
            builder.documents(verification.getDocuments().stream()
                    .map(VerificationStatusDto::toDocumentDto)
                    .toList());
        }
        
        // Add missing documents
        builder.missingDocuments(getMissingDocuments(verification));
        
        // Add GI tag status
        if (verification.getGiTagVerification() != null) {
            builder.giTagStatus(GiTagStatusDto.builder()
                    .applicable(true)
                    .submitted(verification.getGiTagVerification().getCertificateUrl() != null)
                    .verified(verification.getGiTagVerification().isVerified())
                    .giTagNumber(verification.getGiTagVerification().getGiTagNumber())
                    .productCategory(verification.getGiTagVerification().getProductCategory())
                    .expiryDate(verification.getGiTagVerification().getCertificateExpiryDate())
                    .build());
        }
        
        // Add rejection info if applicable
        if (verification.getStatus() == VendorVerification.VerificationStatus.REJECTED ||
            verification.getStatus() == VendorVerification.VerificationStatus.ADDITIONAL_INFO_REQUIRED) {
            builder.actionRequired(getActionRequired(verification));
        }
        
        return builder.build();
    }
    
    private static List<StepStatus> buildStepsList(VendorVerification.VerificationSteps steps) {
        return List.of(
                StepStatus.builder()
                        .stepName("basicInfo")
                        .stepDisplayName("Basic Information")
                        .completed(steps.isBasicInfoCompleted())
                        .completedAt(steps.getBasicInfoCompletedAt())
                        .icon("person")
                        .build(),
                StepStatus.builder()
                        .stepName("businessDocuments")
                        .stepDisplayName("Business Documents")
                        .completed(steps.isBusinessDocumentsUploaded())
                        .completedAt(steps.getBusinessDocumentsUploadedAt())
                        .icon("description")
                        .build(),
                StepStatus.builder()
                        .stepName("identity")
                        .stepDisplayName("Identity Verification")
                        .completed(steps.isIdentityVerified())
                        .completedAt(steps.getIdentityVerifiedAt())
                        .icon("badge")
                        .build(),
                StepStatus.builder()
                        .stepName("address")
                        .stepDisplayName("Address Verification")
                        .completed(steps.isAddressVerified())
                        .completedAt(steps.getAddressVerifiedAt())
                        .icon("location_on")
                        .build(),
                StepStatus.builder()
                        .stepName("bankDetails")
                        .stepDisplayName("Bank Details")
                        .completed(steps.isBankDetailsVerified())
                        .completedAt(steps.getBankDetailsVerifiedAt())
                        .icon("account_balance")
                        .build(),
                StepStatus.builder()
                        .stepName("giTag")
                        .stepDisplayName("GI Tag Verification")
                        .completed(steps.isGiTagVerified())
                        .completedAt(steps.getGiTagVerifiedAt())
                        .icon("verified")
                        .build(),
                StepStatus.builder()
                        .stepName("agreement")
                        .stepDisplayName("Agreement Signed")
                        .completed(steps.isAgreementSigned())
                        .completedAt(steps.getAgreementSignedAt())
                        .icon("handshake")
                        .build()
        );
    }
    
    private static DocumentStatusDto toDocumentDto(VendorVerification.VerificationDocument doc) {
        return DocumentStatusDto.builder()
                .documentType(doc.getDocumentType().name())
                .documentDisplayName(doc.getDocumentType().getDisplayName())
                .required(doc.getDocumentType().isRequired())
                .uploaded(doc.getDocumentUrl() != null)
                .status(doc.getStatus().name())
                .statusDisplayName(doc.getStatus().getDisplayName())
                .documentUrl(doc.getDocumentUrl())
                .rejectionReason(doc.getRejectionReason())
                .build();
    }
    
    private static List<String> getMissingDocuments(VendorVerification verification) {
        List<VendorVerification.DocumentType> requiredTypes = java.util.Arrays.stream(VendorVerification.DocumentType.values())
                .filter(VendorVerification.DocumentType::isRequired)
                .toList();
        
        List<String> uploadedTypes = verification.getDocuments() != null 
                ? verification.getDocuments().stream()
                        .map(d -> d.getDocumentType().name())
                        .toList()
                : List.of();
        
        return requiredTypes.stream()
                .filter(t -> !uploadedTypes.contains(t.name()))
                .map(VendorVerification.DocumentType::getDisplayName)
                .toList();
    }
    
    private static List<String> getActionRequired(VendorVerification verification) {
        List<String> actions = new java.util.ArrayList<>();
        
        // Check for rejected documents
        if (verification.getDocuments() != null) {
            verification.getDocuments().stream()
                    .filter(d -> d.getStatus() == VendorVerification.DocumentStatus.REJECTED)
                    .forEach(d -> actions.add("Re-upload " + d.getDocumentType().getDisplayName() + 
                                             ": " + d.getRejectionReason()));
        }
        
        // Check missing required documents
        actions.addAll(getMissingDocuments(verification).stream()
                .map(d -> "Upload required document: " + d)
                .toList());
        
        return actions;
    }
}
