package com.exhaustedpigeon.ODOP.verification.repository;

import com.exhaustedpigeon.ODOP.verification.model.VendorVerification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Vendor Verification operations
 */
@Repository
public interface VendorVerificationRepository extends MongoRepository<VendorVerification, String> {
    
    /**
     * Find verification by vendor ID
     */
    Optional<VendorVerification> findByVendorId(String vendorId);
    
    /**
     * Find by status
     */
    List<VendorVerification> findByStatus(VendorVerification.VerificationStatus status);
    
    /**
     * Find verifications pending review (documents submitted, awaiting admin review)
     */
    @Query("{ 'status': { $in: ['DOCUMENTS_SUBMITTED', 'UNDER_REVIEW'] } }")
    List<VendorVerification> findPendingReviews();
    
    /**
     * Find verifications needing additional info
     */
    List<VendorVerification> findByStatusOrderByLastUpdatedAtDesc(
            VendorVerification.VerificationStatus status);
    
    /**
     * Find recently submitted (last 24 hours)
     */
    @Query("{ 'submittedAt': { $gte: ?0 }, 'status': 'DOCUMENTS_SUBMITTED' }")
    List<VendorVerification> findRecentlySubmitted(LocalDateTime since);
    
    /**
     * Find stale reviews (under review for more than X days)
     */
    @Query("{ 'status': 'UNDER_REVIEW', 'lastUpdatedAt': { $lt: ?0 } }")
    List<VendorVerification> findStaleReviews(LocalDateTime cutoffDate);
    
    /**
     * Count by status
     */
    long countByStatus(VendorVerification.VerificationStatus status);
    
    /**
     * Check if vendor has submitted verification
     */
    boolean existsByVendorId(String vendorId);
    
    /**
     * Find approved vendors with GI tag
     */
    @Query("{ 'status': 'APPROVED', 'giTagVerification.verified': true }")
    List<VendorVerification> findApprovedWithGiTag();
    
    /**
     * Find by admin reviewer
     */
    @Query("{ 'adminReview.reviewedBy': ?0 }")
    List<VendorVerification> findByReviewer(String adminId);
}
