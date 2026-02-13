package com.odop.root.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import com.odop.root.models.Products;
import com.odop.root.services.ProductService;
import com.odop.root.dto.ProductDto;
import com.odop.root.dto.PageResponse;
import java.util.stream.Collectors;

@RestController
@RequestMapping("odop/product")
@CrossOrigin
public class ProductController {

    @Autowired
    private ProductService productService;

    private static final Logger logger = LogManager.getLogger(ProductController.class);

    @PostMapping("/save_product")
    public ProductDto saveProduct(
            @RequestPart("product") Products product,
            @RequestPart("file") MultipartFile file) {
        try {
            Products saved = this.productService.saveProduct(product, file);
            return toDto(saved);
        } catch (IOException e) {
            throw new RuntimeException("Error storing file: " + e.getMessage());
        }
    }

    @GetMapping("/get_all_products")
    public List<ProductDto> getAllProducts() {
        return this.productService.getAllProducts().stream().map(this::toDto).collect(Collectors.toList());
    }

    // --- Paginated Endpoints ---
    
    /**
     * Get all products with pagination.
     * @param page Page number (0-indexed, default 0)
     * @param size Page size (default 12)
     * @param sortBy Field to sort by (default: createdAt)
     * @param sortDir Sort direction ASC or DESC (default: DESC)
     */
    @GetMapping("/paginated")
    public PageResponse<ProductDto> getAllProductsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        Page<Products> productPage = productService.getAllProductsPaginated(page, size, sortBy, sortDir);
        List<ProductDto> dtos = productPage.getContent().stream().map(this::toDto).collect(Collectors.toList());
        return PageResponse.of(productPage, dtos);
    }

    @GetMapping("/vendor/{vendorId}/paginated")
    public PageResponse<ProductDto> getProductsByVendorPaginated(
            @PathVariable String vendorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Page<Products> productPage = productService.getProductsByVendorIdPaginated(vendorId, page, size);
        List<ProductDto> dtos = productPage.getContent().stream().map(this::toDto).collect(Collectors.toList());
        return PageResponse.of(productPage, dtos);
    }

    @GetMapping("/category/{categoryId}/paginated")
    public PageResponse<ProductDto> getProductsByCategoryPaginated(
            @PathVariable String categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Page<Products> productPage = productService.getProductsByCategoryIdPaginated(categoryId, page, size);
        List<ProductDto> dtos = productPage.getContent().stream().map(this::toDto).collect(Collectors.toList());
        return PageResponse.of(productPage, dtos);
    }

    @GetMapping("/gi_tagged/paginated")
    public PageResponse<ProductDto> getGiTaggedProductsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Page<Products> productPage = productService.getGiTaggedProductsPaginated(page, size);
        List<ProductDto> dtos = productPage.getContent().stream().map(this::toDto).collect(Collectors.toList());
        return PageResponse.of(productPage, dtos);
    }

    @GetMapping("/search/paginated")
    public PageResponse<ProductDto> searchProductsPaginated(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Page<Products> productPage = productService.searchProductsPaginated(keyword, page, size);
        List<ProductDto> dtos = productPage.getContent().stream().map(this::toDto).collect(Collectors.toList());
        return PageResponse.of(productPage, dtos);
    }

    @GetMapping("/get_product_id/{id}")
    public ProductDto getProductById(@PathVariable("id") String uid) {
        return toDto(this.productService.getProductById(uid));
    }

    @GetMapping("/get_product_by_vendor_id/{vendor_id}")
    public List<ProductDto> getProductsByVendorId(@PathVariable("vendor_id") String vendor_id) {
        List<Products> products = this.productService.getProductByVendorId(vendor_id);
        logger.debug("{} Size {}", products.size(), vendor_id);
        return products.stream().map(this::toDto).collect(Collectors.toList());
    }

    @GetMapping("/get_product_by_product_name/{productName}")
    public List<ProductDto> getProductsByProductName(@PathVariable("productName") String productName) {
        List<Products> products = this.productService.getProductByProductName(productName);
        logger.debug("{} Size {}", products.size(), productName);
        return products.stream().map(this::toDto).collect(Collectors.toList());
    }

    @GetMapping("/get_product_by_category_id/{category_id}")
    public List<ProductDto> getProductsByCategoryId(@PathVariable("category_id") String category_id) {
        List<Products> products = this.productService.getProductByCategoryId(category_id);
        logger.debug("{} Size {}", products.size(), category_id);
        return products.stream().map(this::toDto).collect(Collectors.toList());
    }


    @GetMapping("/search_by_location")
    public List<ProductDto> getProductsByLocation(@RequestParam String district, @RequestParam String state) {
        List<Products> products = productService.getProductsByOriginDistrictAndState(district, state);
        return products.stream().map(this::toDto).collect(Collectors.toList());
    }

    @GetMapping("/search_by_state")
    public List<ProductDto> getProductsByState(@RequestParam String state) {
        List<Products> products = productService.getProductsByOriginState(state);
        return products.stream().map(this::toDto).collect(Collectors.toList());
    }

    @DeleteMapping("/delete_by_id/{id}")
    public boolean deleteProductById(@PathVariable("id") String id) {
        return this.productService.deleteById(id);
    }

    @GetMapping("/get_image/{productId}")
    public ResponseEntity<byte[]> getImage(@PathVariable String productId) {
        return null;
    }

    // --- GI Tag Filtering Endpoints ---
    @GetMapping("/gi_tagged")
    public List<ProductDto> getGiTaggedProducts() {
        List<Products> products = productService.getGiTaggedProducts();
        return products.stream().map(this::toDto).collect(Collectors.toList());
    }

    @GetMapping("/gi_tagged_by_state")
    public List<ProductDto> getGiTaggedProductsByState(@RequestParam String state) {
        List<Products> products = productService.getGiTaggedProductsByState(state);
        return products.stream().map(this::toDto).collect(Collectors.toList());
    }

    @GetMapping("/gi_tagged_by_location")
    public List<ProductDto> getGiTaggedProductsByLocation(@RequestParam String district, @RequestParam String state) {
        List<Products> products = productService.getGiTaggedProductsByLocation(district, state);
        return products.stream().map(this::toDto).collect(Collectors.toList());
    }

    // --- Featured and Latest Products Endpoints ---
    @GetMapping("/featured")
    public List<ProductDto> getFeaturedProducts(@RequestParam(defaultValue = "6") int limit) {
        List<Products> products = productService.getFeaturedProducts(limit);
        return products.stream().map(this::toDto).collect(Collectors.toList());
    }

    @GetMapping("/latest")
    public List<ProductDto> getLatestProducts(@RequestParam(defaultValue = "6") int limit) {
        List<Products> products = productService.getLatestProducts(limit);
        return products.stream().map(this::toDto).collect(Collectors.toList());
    }

    // --- DTO/entity mapping helpers for Products ---
    private ProductDto toDto(Products product) {
        if (product == null) return null;
        ProductDto dto = new ProductDto();
        dto.setProductId(product.getProductId());
        dto.setProductName(product.getProductName());
        dto.setProductDescription(product.getProductDescription());
        dto.setCategoryId(product.getCategoryId());
        dto.setSubCategoryId(product.getSubCategoryId());
        dto.setPrice(product.getPrice());
        dto.setProductQuantity(product.getProductQuantity());
        dto.setProductImageURL(product.getProductImageURL());
        dto.setDiscount(product.getDiscount());
        dto.setPromotionEnabled(product.isPromotionEnabled());
        dto.setSpecification(product.getSpecification());
        dto.setWarranty(product.getWarranty());
        dto.setRating(product.getRating());
        dto.setVendorId(product.getVendorId());
        dto.setTags(product.getTags());
        dto.setStockStatus(product.getStockStatus());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        dto.setOriginDistrict(product.getOriginDistrict());
        dto.setOriginState(product.getOriginState());
        dto.setOriginPinCode(product.getOriginPinCode());
        dto.setLocalName(product.getLocalName());
        dto.setGiTagNumber(product.getGiTagNumber());
        dto.setGiTagCertified(product.getGiTagCertified());
        dto.setGiTagCertificateUrl(product.getGiTagCertificateUrl());
        dto.setOriginStory(product.getOriginStory());
        dto.setCraftType(product.getCraftType());
        dto.setMadeBy(product.getMadeBy());
        dto.setMaterialsUsed(product.getMaterialsUsed());
        dto.setPopularityScore(product.getPopularityScore());
        dto.setTotalSold(product.getTotalSold());
        // Admin approval fields
        dto.setApprovalStatus(product.getApprovalStatus());
        dto.setApprovedBy(product.getApprovedBy());
        dto.setApprovalDate(product.getApprovalDate());
        dto.setRejectionReason(product.getRejectionReason());
        dto.setIsActive(product.getIsActive());
        return dto;
    }

    // ========== Admin Product Approval Endpoints ==========

    /**
     * Get products by approval status (PENDING, APPROVED, REJECTED)
     */
    @GetMapping("/admin/status/{status}")
    public PageResponse<ProductDto> getProductsByApprovalStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Page<Products> productPage = productService.getProductsByApprovalStatus(status.toUpperCase(), page, size);
        List<ProductDto> dtos = productPage.getContent().stream().map(this::toDto).collect(Collectors.toList());
        return PageResponse.of(productPage, dtos);
    }

    /**
     * Get all pending products awaiting approval
     */
    @GetMapping("/admin/pending")
    public List<ProductDto> getPendingProducts() {
        return productService.getPendingProducts().stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Approve a product
     */
    @PostMapping("/admin/approve/{productId}")
    public ResponseEntity<ProductDto> approveProduct(
            @PathVariable String productId,
            @RequestParam String adminId) {
        Products approved = productService.approveProduct(productId, adminId);
        if (approved != null) {
            return ResponseEntity.ok(toDto(approved));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Reject a product with reason
     */
    @PostMapping("/admin/reject/{productId}")
    public ResponseEntity<ProductDto> rejectProduct(
            @PathVariable String productId,
            @RequestParam String adminId,
            @RequestParam String reason) {
        Products rejected = productService.rejectProduct(productId, adminId, reason);
        if (rejected != null) {
            return ResponseEntity.ok(toDto(rejected));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Toggle product active status (suspend/reactivate)
     */
    @PostMapping("/admin/toggle-status/{productId}")
    public ResponseEntity<ProductDto> toggleProductStatus(
            @PathVariable String productId,
            @RequestParam boolean isActive) {
        Products toggled = productService.toggleProductStatus(productId, isActive);
        if (toggled != null) {
            return ResponseEntity.ok(toDto(toggled));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Get product approval statistics for admin dashboard
     */
    @GetMapping("/admin/stats")
    public ResponseEntity<ProductService.ProductApprovalStats> getApprovalStats() {
        return ResponseEntity.ok(productService.getApprovalStats());
    }

    /**
     * Get active products only
     */
    @GetMapping("/admin/active")
    public PageResponse<ProductDto> getActiveProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Page<Products> productPage = productService.getActiveProducts(page, size);
        List<ProductDto> dtos = productPage.getContent().stream().map(this::toDto).collect(Collectors.toList());
        return PageResponse.of(productPage, dtos);
    }

    /**
     * Get inactive products (suspended or rejected)
     */
    @GetMapping("/admin/inactive")
    public PageResponse<ProductDto> getInactiveProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Page<Products> productPage = productService.getInactiveProducts(page, size);
        List<ProductDto> dtos = productPage.getContent().stream().map(this::toDto).collect(Collectors.toList());
        return PageResponse.of(productPage, dtos);
    }
}