package com.odop.root.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.odop.root.dto.ImageUploadResponse;
import com.odop.root.dto.MultiImageUploadResponse;
import com.odop.root.dto.ReviewDto;
import com.odop.root.models.Review;
import com.odop.root.services.ImageUploadService;
import com.odop.root.services.ReviewService;

@RestController
@RequestMapping("odop/review")
@CrossOrigin
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ImageUploadService imageUploadService;

    private static final Logger logger = LogManager.getLogger(ReviewController.class);
    private static final int MAX_REVIEW_IMAGES = 5;

    // ============================================
    // REVIEW IMAGE UPLOAD ENDPOINTS
    // ============================================

    /**
     * Upload images for a review
     * POST /odop/review/{reviewId}/images
     */
    @PostMapping("/{reviewId}/images")
    public ResponseEntity<?> uploadReviewImages(
            @PathVariable String reviewId,
            @RequestParam("images") List<MultipartFile> images,
            @RequestParam(value = "customerId", required = false) String customerId) {
        
        logger.info("Uploading {} images for review: {}", images.size(), reviewId);
        
        // Validate review exists
        Review review = reviewService.getReviewById(reviewId);
        if (review == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Check image count limit
        int existingImageCount = review.getReviewImages() != null ? review.getReviewImages().size() : 0;
        if (existingImageCount + images.size() > MAX_REVIEW_IMAGES) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Maximum " + MAX_REVIEW_IMAGES + " images allowed per review. " +
                         "Current: " + existingImageCount + ", Trying to add: " + images.size()
            ));
        }
        
        // Upload images
        MultiImageUploadResponse uploadResponse = imageUploadService.uploadMultipleImages(
                images.toArray(new MultipartFile[0]),
                "review",
                reviewId,
                customerId != null ? customerId : review.getCustomerId(),
                "CUSTOMER"
        );
        
        if (!uploadResponse.isSuccess()) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to upload images",
                "failedCount", uploadResponse.getFailedCount(),
                "errors", uploadResponse.getFailedFiles() != null ? uploadResponse.getFailedFiles() : List.of()
            ));
        }
        
        // Update review with new image URLs
        List<String> newImageUrls = uploadResponse.getUploadedImages().stream()
                .map(ImageUploadResponse::getImageUrl)
                .collect(Collectors.toList());
        
        Review updatedReview = reviewService.addReviewImages(reviewId, newImageUrls);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "uploadedCount", uploadResponse.getSuccessCount(),
            "images", updatedReview.getReviewImages(),
            "review", toDto(updatedReview)
        ));
    }

    /**
     * Delete a specific image from a review
     * DELETE /odop/review/{reviewId}/images/{imageIndex}
     */
    @DeleteMapping("/{reviewId}/images/{imageIndex}")
    public ResponseEntity<?> deleteReviewImage(
            @PathVariable String reviewId,
            @PathVariable int imageIndex) {
        
        logger.info("Deleting image at index {} from review: {}", imageIndex, reviewId);
        
        Review review = reviewService.getReviewById(reviewId);
        if (review == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (review.getReviewImages() == null || imageIndex < 0 || 
            imageIndex >= review.getReviewImages().size()) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Invalid image index"
            ));
        }
        
        // Remove from Cloudinary (optional - just log for now)
        String imageUrl = review.getReviewImages().get(imageIndex);
        logger.info("Removing image from review: {}", imageUrl);
        
        // Remove from review
        Review updatedReview = reviewService.removeReviewImage(reviewId, imageIndex);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "remainingImages", updatedReview.getReviewImages(),
            "review", toDto(updatedReview)
        ));
    }

    /**
     * Create review with images (multipart form)
     * POST /odop/review/create-with-images
     */
    @PostMapping("/create-with-images")
    public ResponseEntity<?> createReviewWithImages(
            @RequestPart("review") ReviewDto reviewDto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        
        logger.info("Creating review with {} images for product: {}", 
                    images != null ? images.size() : 0, reviewDto.getProductId());
        
        // Check image count
        if (images != null && images.size() > MAX_REVIEW_IMAGES) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Maximum " + MAX_REVIEW_IMAGES + " images allowed per review"
            ));
        }
        
        // Create review first
        Review review = toEntity(reviewDto);
        Review savedReview = reviewService.createReview(review);
        
        // Upload images if provided
        if (images != null && !images.isEmpty()) {
            MultiImageUploadResponse uploadResponse = imageUploadService.uploadMultipleImages(
                    images.toArray(new MultipartFile[0]),
                    "review",
                    savedReview.getReviewId(),
                    savedReview.getCustomerId(),
                    "CUSTOMER"
            );
            
            if (uploadResponse.getSuccessCount() > 0) {
                List<String> imageUrls = uploadResponse.getUploadedImages().stream()
                        .map(ImageUploadResponse::getImageUrl)
                        .collect(Collectors.toList());
                savedReview = reviewService.addReviewImages(savedReview.getReviewId(), imageUrls);
            }
        }
        
        return ResponseEntity.ok(toDto(savedReview));
    }

    /**
     * Get reviews with images for a product (filter to show only reviews with photos)
     * GET /odop/review/product/{productId}/with-images
     */
    @GetMapping("/product/{productId}/with-images")
    public ResponseEntity<List<ReviewDto>> getReviewsWithImages(@PathVariable String productId) {
        List<Review> reviews = reviewService.getReviewsWithImages(productId);
        return ResponseEntity.ok(reviews.stream().map(this::toDto).collect(Collectors.toList()));
    }

    // ============================================
    // EXISTING REVIEW ENDPOINTS
    // ============================================

    @PostMapping("/create")
    public ResponseEntity<ReviewDto> createReview(@RequestBody ReviewDto reviewDto) {
        Review review = toEntity(reviewDto);
        Review savedReview = reviewService.createReview(review);
        return ResponseEntity.ok(toDto(savedReview));
    }

    @GetMapping("/get/{reviewId}")
    public ResponseEntity<ReviewDto> getReviewById(@PathVariable String reviewId) {
        Review review = reviewService.getReviewById(reviewId);
        if (review == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toDto(review));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewDto>> getReviewsByProductId(@PathVariable String productId) {
        List<Review> reviews = reviewService.getApprovedReviewsByProductId(productId);
        return ResponseEntity.ok(reviews.stream().map(this::toDto).collect(Collectors.toList()));
    }

    @GetMapping("/product/{productId}/all")
    public ResponseEntity<List<ReviewDto>> getAllReviewsByProductId(@PathVariable String productId) {
        List<Review> reviews = reviewService.getReviewsByProductId(productId);
        return ResponseEntity.ok(reviews.stream().map(this::toDto).collect(Collectors.toList()));
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<ReviewDto>> getReviewsByVendorId(@PathVariable String vendorId) {
        List<Review> reviews = reviewService.getApprovedReviewsByVendorId(vendorId);
        return ResponseEntity.ok(reviews.stream().map(this::toDto).collect(Collectors.toList()));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<ReviewDto>> getReviewsByCustomerId(@PathVariable String customerId) {
        List<Review> reviews = reviewService.getReviewsByCustomerId(customerId);
        return ResponseEntity.ok(reviews.stream().map(this::toDto).collect(Collectors.toList()));
    }

    @GetMapping("/product/{productId}/verified")
    public ResponseEntity<List<ReviewDto>> getVerifiedReviewsByProductId(@PathVariable String productId) {
        List<Review> reviews = reviewService.getVerifiedReviewsByProductId(productId);
        return ResponseEntity.ok(reviews.stream().map(this::toDto).collect(Collectors.toList()));
    }

    @PatchMapping("/update-status/{reviewId}")
    public ResponseEntity<ReviewDto> updateReviewStatus(
            @PathVariable String reviewId,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        Review updatedReview = reviewService.updateReviewStatus(reviewId, status);
        return ResponseEntity.ok(toDto(updatedReview));
    }

    @PostMapping("/vendor-reply/{reviewId}")
    public ResponseEntity<ReviewDto> addVendorReply(
            @PathVariable String reviewId,
            @RequestBody Map<String, String> body) {
        String reply = body.get("reply");
        Review updatedReview = reviewService.addVendorReply(reviewId, reply);
        return ResponseEntity.ok(toDto(updatedReview));
    }

    @PostMapping("/helpful/{reviewId}")
    public ResponseEntity<ReviewDto> markHelpful(@PathVariable String reviewId) {
        Review updatedReview = reviewService.markHelpful(reviewId);
        return ResponseEntity.ok(toDto(updatedReview));
    }

    @PostMapping("/not-helpful/{reviewId}")
    public ResponseEntity<ReviewDto> markNotHelpful(@PathVariable String reviewId) {
        Review updatedReview = reviewService.markNotHelpful(reviewId);
        return ResponseEntity.ok(toDto(updatedReview));
    }

    @GetMapping("/product/{productId}/rating")
    public ResponseEntity<Map<String, Object>> getProductRating(@PathVariable String productId) {
        double avgRating = reviewService.getAverageRatingForProduct(productId);
        long reviewCount = reviewService.getReviewCountForProduct(productId);
        return ResponseEntity.ok(Map.of(
                "averageRating", avgRating,
                "reviewCount", reviewCount
        ));
    }

    @GetMapping("/vendor/{vendorId}/rating")
    public ResponseEntity<Map<String, Object>> getVendorRating(@PathVariable String vendorId) {
        double avgRating = reviewService.getAverageRatingForVendor(vendorId);
        long reviewCount = reviewService.getReviewCountForVendor(vendorId);
        return ResponseEntity.ok(Map.of(
                "averageRating", avgRating,
                "reviewCount", reviewCount
        ));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ReviewDto>> getAllReviews() {
        List<Review> reviews = reviewService.getAllReviews();
        return ResponseEntity.ok(reviews.stream().map(this::toDto).collect(Collectors.toList()));
    }

    @DeleteMapping("/delete/{reviewId}")
    public ResponseEntity<Boolean> deleteReview(@PathVariable String reviewId) {
        return ResponseEntity.ok(reviewService.deleteReview(reviewId));
    }

    // ============================================
    // ADMIN MODERATION ENDPOINTS
    // ============================================

    /**
     * Get all pending reviews for admin moderation
     */
    @GetMapping("/admin/pending")
    public ResponseEntity<List<ReviewDto>> getPendingReviews() {
        List<Review> reviews = reviewService.getPendingReviews();
        return ResponseEntity.ok(reviews.stream().map(this::toDto).collect(Collectors.toList()));
    }

    /**
     * Get all flagged reviews
     */
    @GetMapping("/admin/flagged")
    public ResponseEntity<List<ReviewDto>> getFlaggedReviews() {
        List<Review> reviews = reviewService.getFlaggedReviews();
        return ResponseEntity.ok(reviews.stream().map(this::toDto).collect(Collectors.toList()));
    }

    /**
     * Get reviews by status
     */
    @GetMapping("/admin/status/{status}")
    public ResponseEntity<List<ReviewDto>> getReviewsByStatus(@PathVariable String status) {
        List<Review> reviews = reviewService.getReviewsByStatus(status);
        return ResponseEntity.ok(reviews.stream().map(this::toDto).collect(Collectors.toList()));
    }

    /**
     * Get review statistics for admin dashboard
     */
    @GetMapping("/admin/stats")
    public ResponseEntity<ReviewService.ReviewStats> getReviewStats() {
        return ResponseEntity.ok(reviewService.getReviewStats());
    }

    /**
     * Admin approve a review
     */
    @PostMapping("/admin/approve/{reviewId}")
    public ResponseEntity<ReviewDto> approveReview(
            @PathVariable String reviewId,
            @RequestBody Map<String, String> body) {
        String adminId = body.get("adminId");
        Review review = reviewService.approveReview(reviewId, adminId);
        return ResponseEntity.ok(toDto(review));
    }

    /**
     * Admin reject a review
     */
    @PostMapping("/admin/reject/{reviewId}")
    public ResponseEntity<ReviewDto> rejectReview(
            @PathVariable String reviewId,
            @RequestBody Map<String, String> body) {
        String adminId = body.get("adminId");
        String reason = body.get("reason");
        Review review = reviewService.rejectReview(reviewId, adminId, reason);
        return ResponseEntity.ok(toDto(review));
    }

    /**
     * Admin flag a review
     */
    @PostMapping("/admin/flag/{reviewId}")
    public ResponseEntity<ReviewDto> flagReview(
            @PathVariable String reviewId,
            @RequestBody Map<String, String> body) {
        String adminId = body.get("adminId");
        String reason = body.get("reason");
        Review review = reviewService.flagReview(reviewId, adminId, reason);
        return ResponseEntity.ok(toDto(review));
    }

    /**
     * Admin reply to a review
     */
    @PostMapping("/admin/reply/{reviewId}")
    public ResponseEntity<ReviewDto> adminReplyToReview(
            @PathVariable String reviewId,
            @RequestBody Map<String, String> body) {
        String adminId = body.get("adminId");
        String reply = body.get("reply");
        Review review = reviewService.addAdminReply(reviewId, adminId, reply);
        return ResponseEntity.ok(toDto(review));
    }

    // DTO/Entity mapping helpers
    private ReviewDto toDto(Review review) {
        if (review == null) return null;
        ReviewDto dto = new ReviewDto();
        dto.setReviewId(review.getReviewId());
        dto.setCustomerId(review.getCustomerId());
        dto.setCustomerName(review.getCustomerName());
        dto.setProductId(review.getProductId());
        dto.setVendorId(review.getVendorId());
        dto.setOrderId(review.getOrderId());
        dto.setReviewType(review.getReviewType());
        dto.setRating(review.getRating());
        dto.setTitle(review.getTitle());
        dto.setReviewText(review.getReviewText());
        dto.setReviewImages(review.getReviewImages());
        dto.setAuthenticityRating(review.getAuthenticityRating());
        dto.setQualityRating(review.getQualityRating());
        dto.setValueForMoneyRating(review.getValueForMoneyRating());
        dto.setPackagingRating(review.getPackagingRating());
        dto.setIsVerifiedPurchase(review.getIsVerifiedPurchase());
        dto.setIsRecommended(review.getIsRecommended());
        dto.setStatus(review.getStatus());
        dto.setVendorReply(review.getVendorReply());
        dto.setVendorReplyDate(review.getVendorReplyDate());
        // Admin moderation fields
        dto.setFlagged(review.getFlagged());
        dto.setFlagReason(review.getFlagReason());
        dto.setAdminReply(review.getAdminReply());
        dto.setAdminReplyDate(review.getAdminReplyDate());
        dto.setModeratedBy(review.getModeratedBy());
        dto.setModeratedAt(review.getModeratedAt());
        dto.setHelpfulCount(review.getHelpfulCount());
        dto.setNotHelpfulCount(review.getNotHelpfulCount());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());
        return dto;
    }

    private Review toEntity(ReviewDto dto) {
        Review review = new Review();
        review.setReviewId(dto.getReviewId());
        review.setCustomerId(dto.getCustomerId());
        review.setCustomerName(dto.getCustomerName());
        review.setProductId(dto.getProductId());
        review.setVendorId(dto.getVendorId());
        review.setOrderId(dto.getOrderId());
        review.setReviewType(dto.getReviewType());
        review.setRating(dto.getRating());
        review.setTitle(dto.getTitle());
        review.setReviewText(dto.getReviewText());
        review.setReviewImages(dto.getReviewImages());
        review.setAuthenticityRating(dto.getAuthenticityRating());
        review.setQualityRating(dto.getQualityRating());
        review.setValueForMoneyRating(dto.getValueForMoneyRating());
        review.setPackagingRating(dto.getPackagingRating());
        review.setIsVerifiedPurchase(dto.getIsVerifiedPurchase());
        review.setIsRecommended(dto.getIsRecommended());
        return review;
    }
}
