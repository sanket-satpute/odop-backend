package com.odop.root.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import com.odop.root.models.Products;

@Repository
public interface ProductRepository extends MongoRepository<Products, String> {

    Products findByProductId(String productId);

    List<Products> findByProductName(String productName);

    List<Products> findByVendorId(String vendorId);

    List<Products> findByCategoryId(String categoryId);

    // Location-based search
    List<Products> findByOriginDistrictAndOriginState(String originDistrict, String originState);
    List<Products> findByOriginState(String originState);

    // GI tag filtering
    List<Products> findByGiTagCertifiedTrue();
    List<Products> findByGiTagCertifiedTrueAndOriginState(String originState);
    List<Products> findByGiTagCertifiedTrueAndOriginDistrictAndOriginState(String originDistrict, String originState);

    // --- Count Methods ---
    long countByVendorId(String vendorId);
    
    // --- Pagination Support ---
    
    Page<Products> findAll(Pageable pageable);
    
    Page<Products> findByVendorId(String vendorId, Pageable pageable);
    
    Page<Products> findByCategoryId(String categoryId, Pageable pageable);
    
    Page<Products> findByOriginState(String originState, Pageable pageable);
    
    Page<Products> findByOriginDistrictAndOriginState(String originDistrict, String originState, Pageable pageable);
    
    Page<Products> findByGiTagCertifiedTrue(Pageable pageable);
    
    Page<Products> findByGiTagCertifiedTrueAndOriginState(String originState, Pageable pageable);
    
    // Full-text search with pagination (using product name or description)
    Page<Products> findByProductNameContainingIgnoreCase(String keyword, Pageable pageable);

    // ========== Admin Approval System ==========
    List<Products> findByApprovalStatus(String approvalStatus);
    Page<Products> findByApprovalStatus(String approvalStatus, Pageable pageable);
    List<Products> findByApprovalStatusOrderByCreatedAtDesc(String approvalStatus);
    long countByApprovalStatus(String approvalStatus);
    
    // Active/Inactive products
    List<Products> findByIsActiveTrue();
    List<Products> findByIsActiveFalse();
    Page<Products> findByIsActive(Boolean isActive, Pageable pageable);
    long countByIsActive(Boolean isActive);
    
    // Combined filters for admin dashboard
    Page<Products> findByApprovalStatusAndVendorId(String approvalStatus, String vendorId, Pageable pageable);
    Page<Products> findByApprovalStatusAndCategoryId(String approvalStatus, String categoryId, Pageable pageable);
}
