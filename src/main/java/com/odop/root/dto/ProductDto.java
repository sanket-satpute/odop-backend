package com.odop.root.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductDto {
    private String productId;
    private String productName;
    private String productDescription;
    private String categoryId;
    private String subCategoryId;
    private double price;
    private long productQuantity;
    private String productImageURL;
    private int discount;
    private boolean promotionEnabled;
    private String specification;
    private String warranty;
    private int rating;
    private String vendorId;
    private List<String> tags;
    private String stockStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ODOP Location Fields
    private String originDistrict;
    private String originState;
    private String originPinCode;
    private String localName;

    // GI Tag
    private String giTagNumber;
    private Boolean giTagCertified;
    private String giTagCertificateUrl;

    // Product Origin Story
    private String originStory;
    private String craftType;
    private String madeBy;
    private String materialsUsed;

    // Search & Discovery
    private Integer popularityScore;
    private Integer totalSold;

    // Admin Approval System
    private String approvalStatus;       // PENDING, APPROVED, REJECTED
    private String approvedBy;           // adminId who approved/rejected
    private LocalDateTime approvalDate;
    private String rejectionReason;      // If rejected, why?
    private Boolean isActive;            // Product visibility toggle
}
