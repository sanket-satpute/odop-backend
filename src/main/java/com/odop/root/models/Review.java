package com.odop.root.models;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Review entity for product and vendor reviews.
 * Includes ODOP-specific quality indicators for craftsmanship.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "reviews")
public class Review {

    @Id
    private String reviewId;
    private String customerId;          // reference to Customer
    private String customerName;        // denormalized for display
    private String productId;           // reference to Products (nullable if vendor review)
    private String vendorId;            // reference to Vendor
    private String orderId;             // reference to Order (to ensure verified purchase)
    
    private String reviewType;          // PRODUCT, VENDOR
    private int rating;                 // 1-5 stars
    private String title;
    private String reviewText;
    private List<String> reviewImages;  // customer uploaded images
    
    // Quality indicators for ODOP products
    private Integer authenticityRating; // 1-5 - Is the product authentic?
    private Integer qualityRating;      // 1-5 - Quality of craftsmanship
    private Integer valueForMoneyRating;// 1-5 - Worth the price?
    private Integer packagingRating;    // 1-5 - How well was it packed?
    
    private Boolean isVerifiedPurchase;
    private Boolean isRecommended;
    private String status;              // PENDING, APPROVED, REJECTED
    
    // Vendor response
    private String vendorReply;
    private LocalDateTime vendorReplyDate;
    
    // Admin moderation
    private Boolean flagged;
    private String flagReason;
    private String adminReply;
    private LocalDateTime adminReplyDate;
    private String moderatedBy;         // adminId who moderated
    private LocalDateTime moderatedAt;
    
    // Helpfulness
    @Builder.Default
    private Integer helpfulCount = 0;
    @Builder.Default
    private Integer notHelpfulCount = 0;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
