package com.exhaustedpigeon.ODOP.verification.controller;

import com.exhaustedpigeon.ODOP.verification.dto.VerificationStatusDto;
import com.exhaustedpigeon.ODOP.verification.model.VendorVerification;
import com.exhaustedpigeon.ODOP.verification.model.VendorVerification.DocumentType;
import com.exhaustedpigeon.ODOP.verification.service.VendorVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Vendor Verification operations
 */
@RestController
@RequestMapping("/odop/verification")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class VendorVerificationController {
    
    private final VendorVerificationService verificationService;
    
    // ============================================
    // VENDOR ENDPOINTS
    // ============================================
    
    /**
     * Start verification process for a vendor
     * POST /odop/verification/start/{vendorId}
     */
    @PostMapping("/start/{vendorId}")
    public ResponseEntity<VerificationStatusDto> startVerification(@PathVariable String vendorId) {
        log.info("Starting verification for vendor: {}", vendorId);
        
        VendorVerification verification = verificationService.startVerification(vendorId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(VerificationStatusDto.fromEntity(verification));
    }
    
    /**
     * Get verification status for a vendor
     * GET /odop/verification/status/{vendorId}
     */
    @GetMapping("/status/{vendorId}")
    public ResponseEntity<VerificationStatusDto> getVerificationStatus(@PathVariable String vendorId) {
        log.info("Getting verification status for vendor: {}", vendorId);
        
        VerificationStatusDto status = verificationService.getVerificationStatus(vendorId);
        return ResponseEntity.ok(status);
    }
    
    /**
     * Upload a verification document
     * POST /odop/verification/{vendorId}/document
     */
    @PostMapping("/{vendorId}/document")
    public ResponseEntity<VerificationStatusDto> uploadDocument(
            @PathVariable String vendorId,
            @RequestParam("documentType") String documentType,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "documentNumber", required = false) String documentNumber) {
        
        log.info("Uploading document {} for vendor: {}", documentType, vendorId);
        
        try {
            DocumentType type = DocumentType.valueOf(documentType.toUpperCase());
            VendorVerification verification = verificationService.uploadDocument(
                    vendorId, type, file, documentNumber);
            return ResponseEntity.ok(VerificationStatusDto.fromEntity(verification));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    /**
     * Submit verification for review
     * POST /odop/verification/{vendorId}/submit
     */
    @PostMapping("/{vendorId}/submit")
    public ResponseEntity<?> submitForReview(@PathVariable String vendorId) {
        log.info("Submitting verification for review: vendor {}", vendorId);
        
        try {
            VendorVerification verification = verificationService.submitForReview(vendorId);
            return ResponseEntity.ok(VerificationStatusDto.fromEntity(verification));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get required documents list
     * GET /odop/verification/required-documents
     */
    @GetMapping("/required-documents")
    public ResponseEntity<List<Map<String, Object>>> getRequiredDocuments() {
        List<Map<String, Object>> documents = java.util.Arrays.stream(DocumentType.values())
                .map(type -> Map.<String, Object>of(
                        "type", type.name(),
                        "displayName", type.getDisplayName(),
                        "required", type.isRequired()
                ))
                .toList();
        
        return ResponseEntity.ok(documents);
    }
    
    // ============================================
    // ADMIN ENDPOINTS
    // ============================================
    
    /**
     * Get all pending verifications (Admin)
     * GET /odop/verification/admin/pending
     */
    @GetMapping("/admin/pending")
    public ResponseEntity<List<VerificationStatusDto>> getPendingVerifications() {
        log.info("Getting pending verifications for admin");
        
        List<VerificationStatusDto> pending = verificationService.getPendingVerifications();
        return ResponseEntity.ok(pending);
    }
    
    /**
     * Get verification statistics (Admin)
     * GET /odop/verification/admin/stats
     */
    @GetMapping("/admin/stats")
    public ResponseEntity<VendorVerificationService.VerificationStats> getStats() {
        log.info("Getting verification stats");
        
        VendorVerificationService.VerificationStats stats = verificationService.getVerificationStats();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Start reviewing a verification (Admin)
     * POST /odop/verification/admin/review/{vendorId}
     */
    @PostMapping("/admin/review/{vendorId}")
    public ResponseEntity<VerificationStatusDto> startReview(
            @PathVariable String vendorId,
            @RequestBody Map<String, String> body) {
        
        String adminId = body.get("adminId");
        String adminName = body.get("adminName");
        
        log.info("Admin {} starting review for vendor: {}", adminId, vendorId);
        
        VendorVerification verification = verificationService.startReview(vendorId, adminId, adminName);
        return ResponseEntity.ok(VerificationStatusDto.fromEntity(verification));
    }
    
    /**
     * Verify a specific document (Admin)
     * POST /odop/verification/admin/verify-document/{vendorId}/{documentId}
     */
    @PostMapping("/admin/verify-document/{vendorId}/{documentId}")
    public ResponseEntity<VerificationStatusDto> verifyDocument(
            @PathVariable String vendorId,
            @PathVariable String documentId,
            @RequestBody Map<String, Object> body) {
        
        boolean approved = (Boolean) body.get("approved");
        String rejectionReason = (String) body.get("rejectionReason");
        String adminId = (String) body.get("adminId");
        
        log.info("Admin {} verifying document {} for vendor: {}", adminId, documentId, vendorId);
        
        VendorVerification verification = verificationService.verifyDocument(
                vendorId, documentId, approved, rejectionReason, adminId);
        return ResponseEntity.ok(VerificationStatusDto.fromEntity(verification));
    }
    
    /**
     * Approve vendor verification (Admin)
     * POST /odop/verification/admin/approve/{vendorId}
     */
    @PostMapping("/admin/approve/{vendorId}")
    public ResponseEntity<VerificationStatusDto> approveVerification(
            @PathVariable String vendorId,
            @RequestBody Map<String, Object> body) {
        
        String adminId = (String) body.get("adminId");
        String notes = (String) body.get("notes");
        Integer qualityScore = body.get("qualityScore") != null ? 
                ((Number) body.get("qualityScore")).intValue() : null;
        Integer trustScore = body.get("trustScore") != null ? 
                ((Number) body.get("trustScore")).intValue() : null;
        
        log.info("Admin {} approving verification for vendor: {}", adminId, vendorId);
        
        VendorVerification verification = verificationService.approveVerification(
                vendorId, adminId, notes, qualityScore, trustScore);
        return ResponseEntity.ok(VerificationStatusDto.fromEntity(verification));
    }
    
    /**
     * Reject vendor verification (Admin)
     * POST /odop/verification/admin/reject/{vendorId}
     */
    @PostMapping("/admin/reject/{vendorId}")
    public ResponseEntity<VerificationStatusDto> rejectVerification(
            @PathVariable String vendorId,
            @RequestBody Map<String, Object> body) {
        
        String adminId = (String) body.get("adminId");
        String reason = (String) body.get("reason");
        @SuppressWarnings("unchecked")
        List<String> documentIssues = (List<String>) body.get("documentIssues");
        
        log.info("Admin {} rejecting verification for vendor: {} - Reason: {}", adminId, vendorId, reason);
        
        VendorVerification verification = verificationService.rejectVerification(
                vendorId, adminId, reason, documentIssues);
        return ResponseEntity.ok(VerificationStatusDto.fromEntity(verification));
    }
    
    /**
     * Request additional information (Admin)
     * POST /odop/verification/admin/request-info/{vendorId}
     */
    @PostMapping("/admin/request-info/{vendorId}")
    public ResponseEntity<VerificationStatusDto> requestAdditionalInfo(
            @PathVariable String vendorId,
            @RequestBody Map<String, Object> body) {
        
        String adminId = (String) body.get("adminId");
        @SuppressWarnings("unchecked")
        List<String> requiredItems = (List<String>) body.get("requiredItems");
        
        log.info("Admin {} requesting additional info for vendor: {}", adminId, vendorId);
        
        VendorVerification verification = verificationService.requestAdditionalInfo(
                vendorId, adminId, requiredItems);
        return ResponseEntity.ok(VerificationStatusDto.fromEntity(verification));
    }
    
    // ============================================
    // EXCEPTION HANDLERS
    // ============================================
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        log.error("Verification error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }
    
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalStateException(IllegalStateException ex) {
        log.error("Verification state error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
}
