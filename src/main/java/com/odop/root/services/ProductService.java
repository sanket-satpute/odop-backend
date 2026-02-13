package com.odop.root.services;

import java.io.IOException;
import java.util.List;

// import org.bson.types.Binary; // Commented out - not currently used
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.odop.root.models.Products;
import com.odop.root.repository.ProductRepository;

@Service
public class ProductService {

	@Autowired
	ProductRepository productRepository;
	
	public Products saveProduct(Products product, MultipartFile file) throws IOException {
		if (file != null && !file.isEmpty()) {
            // Binary binaryImage = new Binary(file.getBytes());
            // product.setProductImage(binaryImage); - for a while commented
        }
        return this.productRepository.save(product);
	}
	
	public List<Products> getAllProducts() {
		return this.productRepository.findAll();
	}

	// --- Pagination Support ---
	
	/**
	 * Get paginated products with sorting.
	 * @param page Page number (0-indexed)
	 * @param size Number of items per page
	 * @param sortBy Field to sort by (default: createdAt)
	 * @param sortDir Sort direction (ASC or DESC)
	 */
	public Page<Products> getAllProductsPaginated(int page, int size, String sortBy, String sortDir) {
		Sort sort = sortDir.equalsIgnoreCase("DESC") 
			? Sort.by(sortBy).descending() 
			: Sort.by(sortBy).ascending();
		Pageable pageable = PageRequest.of(page, size, sort);
		return this.productRepository.findAll(pageable);
	}

	public Page<Products> getProductsByVendorIdPaginated(String vendorId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		return this.productRepository.findByVendorId(vendorId, pageable);
	}

	public Page<Products> getProductsByCategoryIdPaginated(String categoryId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		return this.productRepository.findByCategoryId(categoryId, pageable);
	}

	public Page<Products> getProductsByStatePaginated(String state, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		return this.productRepository.findByOriginState(state, pageable);
	}

	public Page<Products> getGiTaggedProductsPaginated(int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		return this.productRepository.findByGiTagCertifiedTrue(pageable);
	}

	public Page<Products> searchProductsPaginated(String keyword, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		return this.productRepository.findByProductNameContainingIgnoreCase(keyword, pageable);
	}
	
	public Products getProductById(String productId) {
		return this.productRepository.findByProductId(productId);
	}
	
	public List<Products> getProductByVendorId(String vendorId) {
		return this.productRepository.findByVendorId(vendorId);
	}
	
	public List<Products> getProductByCategoryId(String categoryId) {
		return this.productRepository.findByCategoryId(categoryId);
	}
	
	public List<Products> getProductByProductName(String productName) {
		return this.productRepository.findByProductName(productName);
	}
	
	public boolean deleteById(String adminId) {
		if(this.getProductById(adminId) != null) {
			this.productRepository.deleteById(adminId);
			return (this.getProductById(adminId)!= null);
		}
		return false;
	}

	// --- Location-based Search Methods ---
	public List<Products> getProductsByOriginDistrictAndState(String district, String state) {
		return this.productRepository.findByOriginDistrictAndOriginState(district, state);
	}

	public List<Products> getProductsByOriginState(String state) {
		return this.productRepository.findByOriginState(state);
	}

	// --- GI Tag Filtering Methods ---
	public List<Products> getGiTaggedProducts() {
		return this.productRepository.findByGiTagCertifiedTrue();
	}

	public List<Products> getGiTaggedProductsByState(String state) {
		return this.productRepository.findByGiTagCertifiedTrueAndOriginState(state);
	}

	public List<Products> getGiTaggedProductsByLocation(String district, String state) {
		return this.productRepository.findByGiTagCertifiedTrueAndOriginDistrictAndOriginState(district, state);
	}

	// --- Featured Products (Top rated or top selling products) ---
	public List<Products> getFeaturedProducts(int limit) {
		List<Products> allProducts = this.productRepository.findAll();
		// Sort by rating (descending), then by totalSold (descending)
		return allProducts.stream()
			.sorted((p1, p2) -> {
				// Compare by rating first (rating is primitive int, always has a value)
				int ratingCompare = Integer.compare(p2.getRating(), p1.getRating());
				if (ratingCompare != 0) return ratingCompare;
				// Then compare by totalSold
				return Integer.compare(
					p2.getTotalSold() != null ? p2.getTotalSold() : 0,
					p1.getTotalSold() != null ? p1.getTotalSold() : 0
				);
			})
			.limit(limit)
			.collect(java.util.stream.Collectors.toList());
	}

	// --- Latest Products ---
	public List<Products> getLatestProducts(int limit) {
		List<Products> allProducts = this.productRepository.findAll();
		return allProducts.stream()
			.sorted((p1, p2) -> {
				if (p2.getCreatedAt() == null) return -1;
				if (p1.getCreatedAt() == null) return 1;
				return p2.getCreatedAt().compareTo(p1.getCreatedAt());
			})
			.limit(limit)
			.collect(java.util.stream.Collectors.toList());
	}

	// ========== Admin Product Approval System ==========

	/**
	 * Get products by approval status with pagination
	 */
	public Page<Products> getProductsByApprovalStatus(String status, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		return this.productRepository.findByApprovalStatus(status, pageable);
	}

	/**
	 * Get all pending products (awaiting approval)
	 */
	public List<Products> getPendingProducts() {
		return this.productRepository.findByApprovalStatusOrderByCreatedAtDesc("PENDING");
	}

	/**
	 * Approve a product
	 */
	public Products approveProduct(String productId, String adminId) {
		Products product = this.productRepository.findByProductId(productId);
		if (product != null) {
			product.setApprovalStatus("APPROVED");
			product.setApprovedBy(adminId);
			product.setApprovalDate(java.time.LocalDateTime.now());
			product.setIsActive(true);
			product.setRejectionReason(null);
			return this.productRepository.save(product);
		}
		return null;
	}

	/**
	 * Reject a product with reason
	 */
	public Products rejectProduct(String productId, String adminId, String reason) {
		Products product = this.productRepository.findByProductId(productId);
		if (product != null) {
			product.setApprovalStatus("REJECTED");
			product.setApprovedBy(adminId);
			product.setApprovalDate(java.time.LocalDateTime.now());
			product.setRejectionReason(reason);
			product.setIsActive(false);
			return this.productRepository.save(product);
		}
		return null;
	}

	/**
	 * Toggle product active status (admin can suspend/reactivate products)
	 */
	public Products toggleProductStatus(String productId, boolean isActive) {
		Products product = this.productRepository.findByProductId(productId);
		if (product != null) {
			product.setIsActive(isActive);
			return this.productRepository.save(product);
		}
		return null;
	}

	/**
	 * Get product approval statistics for admin dashboard
	 */
	public ProductApprovalStats getApprovalStats() {
		long total = this.productRepository.count();
		long pending = this.productRepository.countByApprovalStatus("PENDING");
		long approved = this.productRepository.countByApprovalStatus("APPROVED");
		long rejected = this.productRepository.countByApprovalStatus("REJECTED");
		long active = this.productRepository.countByIsActive(true);
		long inactive = this.productRepository.countByIsActive(false);
		return new ProductApprovalStats(total, pending, approved, rejected, active, inactive);
	}

	/**
	 * Get active products only (for public product listings)
	 */
	public Page<Products> getActiveProducts(int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		return this.productRepository.findByIsActive(true, pageable);
	}

	/**
	 * Get inactive products (suspended or rejected)
	 */
	public Page<Products> getInactiveProducts(int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		return this.productRepository.findByIsActive(false, pageable);
	}

	// Record for product approval statistics
	public record ProductApprovalStats(
		long totalProducts,
		long pendingApproval,
		long approved,
		long rejected,
		long activeProducts,
		long inactiveProducts
	) {}
}
