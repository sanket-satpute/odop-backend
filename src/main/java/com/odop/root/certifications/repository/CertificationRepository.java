package com.odop.root.certifications.repository;

import com.odop.root.certifications.model.Certification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Certification entity
 */
@Repository
public interface CertificationRepository extends MongoRepository<Certification, String> {
    
    // Find by vendor
    List<Certification> findByVendorId(String vendorId);
    
    List<Certification> findByVendorIdAndStatus(String vendorId, String status);
    
    List<Certification> findByVendorIdOrderByCreatedAtDesc(String vendorId);
    
    // Find by type
    List<Certification> findByVendorIdAndType(String vendorId, String type);
    
    List<Certification> findByTypeAndStatus(String type, String status);
    
    // Find expiring
    List<Certification> findByVendorIdAndExpiryDateBetweenAndStatus(
        String vendorId, LocalDateTime startDate, LocalDateTime endDate, String status);
    
    List<Certification> findByExpiryDateBeforeAndStatus(LocalDateTime date, String status);
    
    // Find for products
    List<Certification> findByApplicableProductIdsContaining(String productId);
    
    List<Certification> findByApplicableCategoriesContaining(String category);
    
    // Verification queries
    List<Certification> findByVerifiedFalseAndStatus(String status);
    
    List<Certification> findByVendorIdAndVerified(String vendorId, boolean verified);
    
    // Count queries
    long countByVendorIdAndStatus(String vendorId, String status);
    
    long countByVendorIdAndType(String vendorId, String type);
    
    long countByVendorIdAndVerified(String vendorId, boolean verified);
    
    // Check existence
    boolean existsByVendorIdAndTypeAndStatus(String vendorId, String type, String status);
}
