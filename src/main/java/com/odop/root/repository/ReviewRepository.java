package com.odop.root.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.odop.root.models.Review;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {

    Review findByReviewId(String reviewId);

    List<Review> findByProductId(String productId);

    List<Review> findByVendorId(String vendorId);

    List<Review> findByCustomerId(String customerId);

    List<Review> findByOrderId(String orderId);

    List<Review> findByProductIdAndStatus(String productId, String status);

    List<Review> findByVendorIdAndStatus(String vendorId, String status);

    List<Review> findByProductIdOrderByCreatedAtDesc(String productId);

    List<Review> findByVendorIdOrderByCreatedAtDesc(String vendorId);

    List<Review> findByReviewType(String reviewType);

    List<Review> findByProductIdAndIsVerifiedPurchaseTrue(String productId);

    // Average rating queries would typically use aggregation, but Spring Data can help
    Long countByProductId(String productId);

    Long countByVendorId(String vendorId);

    // Admin moderation queries
    List<Review> findByFlaggedTrue();

    List<Review> findByStatus(String status);

    List<Review> findByStatusOrderByCreatedAtDesc(String status);

    Long countByStatus(String status);

    Long countByFlaggedTrue();
}
