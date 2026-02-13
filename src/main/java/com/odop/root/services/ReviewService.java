package com.odop.root.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.odop.root.models.Review;
import com.odop.root.repository.ReviewRepository;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    private static final Logger logger = LogManager.getLogger(ReviewService.class);

    public Review createReview(Review review) {
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        // Auto-approve reviews - they will be shown immediately without admin verification
        // Admin can still moderate and remove inappropriate reviews later
        if (review.getStatus() == null) {
            review.setStatus("APPROVED");
        }
        if (review.getHelpfulCount() == null) {
            review.setHelpfulCount(0);
        }
        if (review.getNotHelpfulCount() == null) {
            review.setNotHelpfulCount(0);
        }
        return reviewRepository.save(review);
    }

    public Review getReviewById(String reviewId) {
        return reviewRepository.findByReviewId(reviewId);
    }

    public List<Review> getReviewsByProductId(String productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    public List<Review> getApprovedReviewsByProductId(String productId) {
        return reviewRepository.findByProductIdAndStatus(productId, "APPROVED");
    }

    public List<Review> getReviewsByVendorId(String vendorId) {
        return reviewRepository.findByVendorIdOrderByCreatedAtDesc(vendorId);
    }

    public List<Review> getApprovedReviewsByVendorId(String vendorId) {
        return reviewRepository.findByVendorIdAndStatus(vendorId, "APPROVED");
    }

    public List<Review> getReviewsByCustomerId(String customerId) {
        return reviewRepository.findByCustomerId(customerId);
    }

    public List<Review> getVerifiedReviewsByProductId(String productId) {
        return reviewRepository.findByProductIdAndIsVerifiedPurchaseTrue(productId);
    }

    public Review updateReviewStatus(String reviewId, String status) {
        Review review = reviewRepository.findByReviewId(reviewId);
        if (review != null) {
            review.setStatus(status);
            review.setUpdatedAt(LocalDateTime.now());
            return reviewRepository.save(review);
        }
        throw new RuntimeException("Review not found with id: " + reviewId);
    }

    public Review addVendorReply(String reviewId, String reply) {
        Review review = reviewRepository.findByReviewId(reviewId);
        if (review != null) {
            review.setVendorReply(reply);
            review.setVendorReplyDate(LocalDateTime.now());
            review.setUpdatedAt(LocalDateTime.now());
            return reviewRepository.save(review);
        }
        throw new RuntimeException("Review not found with id: " + reviewId);
    }

    public Review markHelpful(String reviewId) {
        Review review = reviewRepository.findByReviewId(reviewId);
        if (review != null) {
            review.setHelpfulCount(review.getHelpfulCount() + 1);
            return reviewRepository.save(review);
        }
        throw new RuntimeException("Review not found with id: " + reviewId);
    }

    public Review markNotHelpful(String reviewId) {
        Review review = reviewRepository.findByReviewId(reviewId);
        if (review != null) {
            review.setNotHelpfulCount(review.getNotHelpfulCount() + 1);
            return reviewRepository.save(review);
        }
        throw new RuntimeException("Review not found with id: " + reviewId);
    }

    public double getAverageRatingForProduct(String productId) {
        List<Review> reviews = reviewRepository.findByProductIdAndStatus(productId, "APPROVED");
        if (reviews.isEmpty()) return 0.0;
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    public double getAverageRatingForVendor(String vendorId) {
        List<Review> reviews = reviewRepository.findByVendorIdAndStatus(vendorId, "APPROVED");
        if (reviews.isEmpty()) return 0.0;
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    public long getReviewCountForProduct(String productId) {
        return reviewRepository.countByProductId(productId);
    }

    public long getReviewCountForVendor(String vendorId) {
        return reviewRepository.countByVendorId(vendorId);
    }

    public boolean deleteReview(String reviewId) {
        Review review = reviewRepository.findByReviewId(reviewId);
        if (review != null) {
            reviewRepository.delete(review);
            return true;
        }
        return false;
    }

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    // ============================================
    // REVIEW IMAGE METHODS
    // ============================================

    /**
     * Add images to an existing review
     */
    public Review addReviewImages(String reviewId, List<String> imageUrls) {
        Review review = reviewRepository.findByReviewId(reviewId);
        if (review == null) {
            throw new RuntimeException("Review not found with id: " + reviewId);
        }
        
        List<String> existingImages = review.getReviewImages();
        if (existingImages == null) {
            existingImages = new ArrayList<>();
        }
        existingImages.addAll(imageUrls);
        review.setReviewImages(existingImages);
        review.setUpdatedAt(LocalDateTime.now());
        
        logger.info("Added {} images to review {}. Total images: {}", 
                    imageUrls.size(), reviewId, existingImages.size());
        
        return reviewRepository.save(review);
    }

    /**
     * Remove an image from a review by index
     */
    public Review removeReviewImage(String reviewId, int imageIndex) {
        Review review = reviewRepository.findByReviewId(reviewId);
        if (review == null) {
            throw new RuntimeException("Review not found with id: " + reviewId);
        }
        
        List<String> images = review.getReviewImages();
        if (images != null && imageIndex >= 0 && imageIndex < images.size()) {
            String removedUrl = images.remove(imageIndex);
            review.setReviewImages(images);
            review.setUpdatedAt(LocalDateTime.now());
            logger.info("Removed image at index {} from review {}. URL: {}", 
                        imageIndex, reviewId, removedUrl);
            return reviewRepository.save(review);
        }
        
        throw new RuntimeException("Invalid image index: " + imageIndex);
    }

    /**
     * Get reviews that have images for a product
     */
    public List<Review> getReviewsWithImages(String productId) {
        List<Review> allReviews = reviewRepository.findByProductIdAndStatus(productId, "APPROVED");
        return allReviews.stream()
                .filter(r -> r.getReviewImages() != null && !r.getReviewImages().isEmpty())
                .toList();
    }

    /**
     * Count reviews with images for a product
     */
    public long countReviewsWithImages(String productId) {
        List<Review> allReviews = reviewRepository.findByProductIdAndStatus(productId, "APPROVED");
        return allReviews.stream()
                .filter(r -> r.getReviewImages() != null && !r.getReviewImages().isEmpty())
                .count();
    }

    // ============================================
    // ADMIN MODERATION METHODS
    // ============================================

    /**
     * Get all flagged reviews for admin moderation
     */
    public List<Review> getFlaggedReviews() {
        return reviewRepository.findByFlaggedTrue();
    }

    /**
     * Get reviews by status for admin
     */
    public List<Review> getReviewsByStatus(String status) {
        return reviewRepository.findByStatus(status);
    }

    /**
     * Get pending reviews for admin approval
     */
    public List<Review> getPendingReviews() {
        return reviewRepository.findByStatus("PENDING");
    }

    /**
     * Admin approve a review
     */
    public Review approveReview(String reviewId, String adminId) {
        Review review = reviewRepository.findByReviewId(reviewId);
        if (review == null) {
            throw new RuntimeException("Review not found with id: " + reviewId);
        }
        review.setStatus("APPROVED");
        review.setModeratedBy(adminId);
        review.setModeratedAt(LocalDateTime.now());
        review.setFlagged(false);
        review.setUpdatedAt(LocalDateTime.now());
        logger.info("Admin {} approved review {}", adminId, reviewId);
        return reviewRepository.save(review);
    }

    /**
     * Admin reject a review
     */
    public Review rejectReview(String reviewId, String adminId, String reason) {
        Review review = reviewRepository.findByReviewId(reviewId);
        if (review == null) {
            throw new RuntimeException("Review not found with id: " + reviewId);
        }
        review.setStatus("REJECTED");
        review.setModeratedBy(adminId);
        review.setModeratedAt(LocalDateTime.now());
        review.setFlagReason(reason);
        review.setUpdatedAt(LocalDateTime.now());
        logger.info("Admin {} rejected review {} with reason: {}", adminId, reviewId, reason);
        return reviewRepository.save(review);
    }

    /**
     * Admin flag a review
     */
    public Review flagReview(String reviewId, String adminId, String reason) {
        Review review = reviewRepository.findByReviewId(reviewId);
        if (review == null) {
            throw new RuntimeException("Review not found with id: " + reviewId);
        }
        review.setFlagged(true);
        review.setFlagReason(reason);
        review.setModeratedBy(adminId);
        review.setModeratedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        logger.info("Admin {} flagged review {} with reason: {}", adminId, reviewId, reason);
        return reviewRepository.save(review);
    }

    /**
     * Admin add reply to a review
     */
    public Review addAdminReply(String reviewId, String adminId, String reply) {
        Review review = reviewRepository.findByReviewId(reviewId);
        if (review == null) {
            throw new RuntimeException("Review not found with id: " + reviewId);
        }
        review.setAdminReply(reply);
        review.setAdminReplyDate(LocalDateTime.now());
        review.setModeratedBy(adminId);
        review.setUpdatedAt(LocalDateTime.now());
        logger.info("Admin {} replied to review {}", adminId, reviewId);
        return reviewRepository.save(review);
    }

    /**
     * Get review statistics for admin dashboard
     */
    public ReviewStats getReviewStats() {
        List<Review> allReviews = reviewRepository.findAll();
        
        long total = allReviews.size();
        long pending = allReviews.stream().filter(r -> "PENDING".equals(r.getStatus())).count();
        long approved = allReviews.stream().filter(r -> "APPROVED".equals(r.getStatus())).count();
        long rejected = allReviews.stream().filter(r -> "REJECTED".equals(r.getStatus())).count();
        long flagged = allReviews.stream().filter(r -> Boolean.TRUE.equals(r.getFlagged())).count();
        
        double avgRating = allReviews.stream()
                .filter(r -> "APPROVED".equals(r.getStatus()))
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
        
        return new ReviewStats(total, pending, approved, rejected, flagged, avgRating);
    }

    /**
     * Review statistics DTO
     */
    public record ReviewStats(long total, long pending, long approved, long rejected, long flagged, double averageRating) {}
}
