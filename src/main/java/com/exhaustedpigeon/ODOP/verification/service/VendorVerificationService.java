package com.exhaustedpigeon.ODOP.verification.service;

import com.exhaustedpigeon.ODOP.verification.dto.VerificationStatusDto;
import com.exhaustedpigeon.ODOP.verification.model.VendorVerification;
import com.exhaustedpigeon.ODOP.verification.model.VendorVerification.*;
import com.exhaustedpigeon.ODOP.verification.repository.VendorVerificationRepository;
import com.odop.root.dto.ImageUploadResponse;
import com.odop.root.models.Vendor;
import com.odop.root.repository.VendorRepository;
import com.odop.root.services.ImageUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing vendor verification workflow
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VendorVerificationService {
    
    private final VendorVerificationRepository verificationRepository;
    private final VendorRepository vendorRepository;
    private final ImageUploadService imageUploadService;
    
    /**
     * Start verification process for a vendor
     */
    @Transactional
    public VendorVerification startVerification(String vendorId) {
        // Check if vendor exists
        Vendor vendor = vendorRepository.findByVendorId(vendorId);
        if (vendor == null) {
            throw new RuntimeException("Vendor not found: " + vendorId);
        }
        
        // Check if verification already exists
        Optional<VendorVerification> existing = verificationRepository.findByVendorId(vendorId);
        if (existing.isPresent()) {
            return existing.get();
        }
        
        // Create new verification record
        VendorVerification verification = VendorVerification.builder()
                .vendorId(vendorId)
                .vendorName(vendor.getShoppeeName())
                .vendorEmail(vendor.getEmailAddress())
                .status(VerificationStatus.DOCUMENTS_PENDING)
                .statusMessage("Please upload required documents to begin verification")
                .steps(VerificationSteps.builder()
                        .basicInfoCompleted(false)
                        .businessDocumentsUploaded(false)
                        .identityVerified(false)
                        .addressVerified(false)
                        .bankDetailsVerified(false)
                        .giTagVerified(false)
                        .agreementSigned(false)
                        .build())
                .documents(new ArrayList<>())
                .submittedAt(LocalDateTime.now())
                .lastUpdatedAt(LocalDateTime.now())
                .build();
        
        VendorVerification saved = verificationRepository.save(verification);
        log.info("Started verification for vendor: {}", vendorId);
        
        return saved;
    }
    
    /**
     * Get verification status for a vendor
     */
    public VerificationStatusDto getVerificationStatus(String vendorId) {
        VendorVerification verification = verificationRepository.findByVendorId(vendorId)
                .orElseThrow(() -> new RuntimeException("Verification not found for vendor: " + vendorId));
        
        return VerificationStatusDto.fromEntity(verification);
    }
    
    /**
     * Upload a verification document
     */
    @Transactional
    public VendorVerification uploadDocument(String vendorId, DocumentType documentType,
                                             MultipartFile file, String documentNumber) {
        VendorVerification verification = verificationRepository.findByVendorId(vendorId)
                .orElseGet(() -> {
                    log.info("Verification record missing for vendor {}. Auto-starting verification before upload.", vendorId);
                    return startVerification(vendorId);
                });
        
        // Upload to Cloudinary
        ImageUploadResponse uploadResponse = imageUploadService.uploadImage(
                file, "verification", vendorId, documentType.name(), vendorId, "VENDOR");
        
        if (!uploadResponse.isSuccess()) {
            throw new RuntimeException("Failed to upload document: " + uploadResponse.getMessage());
        }
        
        // Create document record
        VerificationDocument document = VerificationDocument.builder()
                .documentId(java.util.UUID.randomUUID().toString())
                .documentType(documentType)
                .documentName(file.getOriginalFilename())
                .documentUrl(uploadResponse.getImageUrl())
                .documentNumber(documentNumber)
                .status(DocumentStatus.PENDING)
                .uploadedAt(LocalDateTime.now())
                .build();
        
        // Add or replace document
        List<VerificationDocument> documents = verification.getDocuments();
        if (documents == null) {
            documents = new ArrayList<>();
        }
        
        // Remove existing document of same type
        documents.removeIf(d -> d.getDocumentType() == documentType);
        documents.add(document);
        
        verification.setDocuments(documents);
        verification.setLastUpdatedAt(LocalDateTime.now());
        
        // Update steps based on documents
        updateStepsBasedOnDocuments(verification);
        
        // Update status if all required documents are uploaded
        if (areAllRequiredDocumentsUploaded(verification)) {
            verification.setStatus(VerificationStatus.DOCUMENTS_SUBMITTED);
            verification.setStatusMessage("All documents uploaded. Awaiting admin review.");
        }
        
        VendorVerification saved = verificationRepository.save(verification);
        log.info("Uploaded document {} for vendor {}", documentType, vendorId);
        
        return saved;
    }
    
    /**
     * Submit verification for review (after all documents uploaded)
     */
    @Transactional
    public VendorVerification submitForReview(String vendorId) {
        VendorVerification verification = verificationRepository.findByVendorId(vendorId)
                .orElseThrow(() -> new RuntimeException("Verification not found for vendor: " + vendorId));
        
        // Check if all required documents are uploaded
        if (!areAllRequiredDocumentsUploaded(verification)) {
            throw new IllegalStateException("All required documents must be uploaded before submission");
        }
        
        verification.setStatus(VerificationStatus.DOCUMENTS_SUBMITTED);
        verification.setStatusMessage("Verification submitted for admin review");
        verification.setLastUpdatedAt(LocalDateTime.now());
        
        VendorVerification saved = verificationRepository.save(verification);
        log.info("Submitted verification for review: vendor {}", vendorId);
        
        // TODO: Send notification to admin
        
        return saved;
    }
    
    /**
     * Admin: Start reviewing a verification
     */
    @Transactional
    public VendorVerification startReview(String vendorId, String adminId, String adminName) {
        VendorVerification verification = verificationRepository.findByVendorId(vendorId)
                .orElseThrow(() -> new RuntimeException("Verification not found"));
        
        verification.setStatus(VerificationStatus.UNDER_REVIEW);
        verification.setStatusMessage("Verification is being reviewed by admin");
        verification.setAdminReview(AdminReview.builder()
                .reviewedBy(adminId)
                .reviewerName(adminName)
                .reviewedAt(LocalDateTime.now())
                .build());
        verification.setLastUpdatedAt(LocalDateTime.now());
        
        return verificationRepository.save(verification);
    }
    
    /**
     * Admin: Verify a specific document
     */
    @Transactional
    public VendorVerification verifyDocument(String vendorId, String documentId, 
                                             boolean approved, String rejectionReason,
                                             String adminId) {
        VendorVerification verification = verificationRepository.findByVendorId(vendorId)
                .orElseThrow(() -> new RuntimeException("Verification not found"));
        
        // Find and update document
        verification.getDocuments().stream()
                .filter(d -> d.getDocumentId().equals(documentId))
                .findFirst()
                .ifPresent(doc -> {
                    doc.setStatus(approved ? DocumentStatus.VERIFIED : DocumentStatus.REJECTED);
                    doc.setRejectionReason(approved ? null : rejectionReason);
                    doc.setVerifiedAt(LocalDateTime.now());
                    doc.setVerifiedBy(adminId);
                });
        
        verification.setLastUpdatedAt(LocalDateTime.now());
        
        // Update steps if applicable
        updateStepsBasedOnDocuments(verification);
        
        return verificationRepository.save(verification);
    }
    
    /**
     * Admin: Approve vendor verification
     */
    @Transactional
    public VendorVerification approveVerification(String vendorId, String adminId, 
                                                   String notes, Integer qualityScore,
                                                   Integer trustScore) {
        VendorVerification verification = verificationRepository.findByVendorId(vendorId)
                .orElseThrow(() -> new RuntimeException("Verification not found"));
        
        // Update status
        verification.setStatus(VerificationStatus.APPROVED);
        verification.setStatusMessage("Congratulations! Your verification has been approved.");
        verification.setCompletedAt(LocalDateTime.now());
        verification.setLastUpdatedAt(LocalDateTime.now());
        
        // Update admin review
        AdminReview review = verification.getAdminReview();
        if (review == null) {
            review = new AdminReview();
        }
        review.setReviewedBy(adminId);
        review.setReviewedAt(LocalDateTime.now());
        review.setReviewNotes(notes);
        review.setQualityScore(qualityScore);
        review.setTrustScore(trustScore);
        verification.setAdminReview(review);
        
        // Update all steps to complete
        VerificationSteps steps = verification.getSteps();
        steps.setBasicInfoCompleted(true);
        steps.setBusinessDocumentsUploaded(true);
        steps.setIdentityVerified(true);
        steps.setAddressVerified(true);
        steps.setBankDetailsVerified(true);
        steps.setAgreementSigned(true);
        
        VendorVerification saved = verificationRepository.save(verification);
        
        // Update vendor status
        updateVendorVerificationStatus(vendorId, true);
        
        log.info("Approved verification for vendor: {}", vendorId);
        
        // TODO: Send approval notification to vendor
        
        return saved;
    }
    
    /**
     * Admin: Reject vendor verification
     */
    @Transactional
    public VendorVerification rejectVerification(String vendorId, String adminId,
                                                  String reason, List<String> documentIssues) {
        VendorVerification verification = verificationRepository.findByVendorId(vendorId)
                .orElseThrow(() -> new RuntimeException("Verification not found"));
        
        // Update status
        verification.setStatus(VerificationStatus.REJECTED);
        verification.setStatusMessage("Verification rejected: " + reason);
        verification.setLastUpdatedAt(LocalDateTime.now());
        
        // Add rejection record
        List<RejectionRecord> history = verification.getRejectionHistory();
        if (history == null) {
            history = new ArrayList<>();
        }
        history.add(RejectionRecord.builder()
                .rejectedAt(LocalDateTime.now())
                .rejectedBy(adminId)
                .reason(reason)
                .documentIssues(documentIssues)
                .build());
        verification.setRejectionHistory(history);
        
        VendorVerification saved = verificationRepository.save(verification);
        
        // Update vendor status
        updateVendorVerificationStatus(vendorId, false);
        
        log.info("Rejected verification for vendor: {} - Reason: {}", vendorId, reason);
        
        // TODO: Send rejection notification to vendor
        
        return saved;
    }
    
    /**
     * Admin: Request additional information
     */
    @Transactional
    public VendorVerification requestAdditionalInfo(String vendorId, String adminId,
                                                     List<String> requiredItems) {
        VendorVerification verification = verificationRepository.findByVendorId(vendorId)
                .orElseThrow(() -> new RuntimeException("Verification not found"));
        
        verification.setStatus(VerificationStatus.ADDITIONAL_INFO_REQUIRED);
        verification.setStatusMessage("Additional information required: " + String.join(", ", requiredItems));
        verification.setLastUpdatedAt(LocalDateTime.now());
        
        return verificationRepository.save(verification);
    }
    
    /**
     * Get all pending verifications for admin dashboard
     */
    public List<VerificationStatusDto> getPendingVerifications() {
        return verificationRepository.findPendingReviews().stream()
                .map(VerificationStatusDto::fromEntity)
                .toList();
    }
    
    /**
     * Get verification statistics for admin dashboard
     */
    public VerificationStats getVerificationStats() {
        return new VerificationStats(
                verificationRepository.countByStatus(VerificationStatus.DOCUMENTS_PENDING),
                verificationRepository.countByStatus(VerificationStatus.DOCUMENTS_SUBMITTED),
                verificationRepository.countByStatus(VerificationStatus.UNDER_REVIEW),
                verificationRepository.countByStatus(VerificationStatus.APPROVED),
                verificationRepository.countByStatus(VerificationStatus.REJECTED)
        );
    }
    
    // ============================================
    // HELPER METHODS
    // ============================================
    
    private boolean areAllRequiredDocumentsUploaded(VendorVerification verification) {
        List<DocumentType> requiredTypes = java.util.Arrays.stream(DocumentType.values())
                .filter(DocumentType::isRequired)
                .toList();
        
        List<DocumentType> uploadedTypes = verification.getDocuments() != null
                ? verification.getDocuments().stream()
                        .map(VerificationDocument::getDocumentType)
                        .toList()
                : List.of();
        
        return uploadedTypes.containsAll(requiredTypes);
    }
    
    private void updateStepsBasedOnDocuments(VendorVerification verification) {
        List<VerificationDocument> docs = verification.getDocuments();
        if (docs == null) return;
        
        VerificationSteps steps = verification.getSteps();
        LocalDateTime now = LocalDateTime.now();
        
        // Check identity documents
        boolean hasIdentity = docs.stream().anyMatch(d -> 
                d.getDocumentType() == DocumentType.PAN_CARD || 
                d.getDocumentType() == DocumentType.AADHAAR_CARD);
        if (hasIdentity && !steps.isIdentityVerified()) {
            steps.setIdentityVerified(true);
            steps.setIdentityVerifiedAt(now);
        }
        
        // Check business documents
        boolean hasBusiness = docs.stream().anyMatch(d ->
                d.getDocumentType() == DocumentType.GSTIN_CERTIFICATE ||
                d.getDocumentType() == DocumentType.BUSINESS_LICENSE);
        if (hasBusiness && !steps.isBusinessDocumentsUploaded()) {
            steps.setBusinessDocumentsUploaded(true);
            steps.setBusinessDocumentsUploadedAt(now);
        }
        
        // Check bank details
        boolean hasBank = docs.stream().anyMatch(d ->
                d.getDocumentType() == DocumentType.CANCELLED_CHEQUE ||
                d.getDocumentType() == DocumentType.BANK_STATEMENT);
        if (hasBank && !steps.isBankDetailsVerified()) {
            steps.setBankDetailsVerified(true);
            steps.setBankDetailsVerifiedAt(now);
        }
        
        // Check address
        boolean hasAddress = docs.stream().anyMatch(d ->
                d.getDocumentType() == DocumentType.ADDRESS_PROOF);
        if (hasAddress && !steps.isAddressVerified()) {
            steps.setAddressVerified(true);
            steps.setAddressVerifiedAt(now);
        }
        
        // Check GI tag
        boolean hasGiTag = docs.stream().anyMatch(d ->
                d.getDocumentType() == DocumentType.GI_TAG_CERTIFICATE);
        if (hasGiTag && !steps.isGiTagVerified()) {
            steps.setGiTagVerified(true);
            steps.setGiTagVerifiedAt(now);
        }
    }
    
    private void updateVendorVerificationStatus(String vendorId, boolean verified) {
        Vendor vendor = vendorRepository.findByVendorId(vendorId);
        if (vendor != null) {
            vendor.setIsVerified(verified);
            vendor.setVerified(verified);
            vendor.setStatus(verified ? "verified" : "rejected");
            vendor.setUpdatedAt(LocalDateTime.now());
            vendorRepository.save(vendor);
        }
    }
    
    /**
     * Stats record for dashboard
     */
    public record VerificationStats(
            long pending,
            long submitted,
            long underReview,
            long approved,
            long rejected
    ) {}
}
