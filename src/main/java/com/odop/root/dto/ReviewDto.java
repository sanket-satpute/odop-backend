package com.odop.root.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReviewDto {
    private String reviewId;
    private String customerId;
    private String customerName;
    private String productId;
    private String vendorId;
    private String orderId;
    
    private String reviewType;
    private int rating;
    private String title;
    private String reviewText;
    private List<String> reviewImages;
    
    // Quality indicators for ODOP products
    private Integer authenticityRating;
    private Integer qualityRating;
    private Integer valueForMoneyRating;
    private Integer packagingRating;
    
    private Boolean isVerifiedPurchase;
    private Boolean isRecommended;
    private String status;
    
    // Vendor response
    private String vendorReply;
    private LocalDateTime vendorReplyDate;
    
    // Admin moderation
    private Boolean flagged;
    private String flagReason;
    private String adminReply;
    private LocalDateTime adminReplyDate;
    private String moderatedBy;
    private LocalDateTime moderatedAt;
    
    // Helpfulness
    private Integer helpfulCount;
    private Integer notHelpfulCount;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
