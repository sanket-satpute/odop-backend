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
 * Products entity for ODOP e-commerce platform.
 * Includes ODOP-specific fields like GI Tag, Origin Story, Craft Type.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class Products {

    @Id 
    private String productId;
    private String productName;
    private String productDescription;
    private String categoryId;           // reference to ProductCategory
    private String subCategoryId;        // reference to ProductCategory if subcategory
    private double price;
    private long productQuantity;
    private String productImageURL;      // URL
    private int discount;
    private boolean promotionEnabled;
    private String specification;
    private String warranty;
    private int rating;
    private String vendorId;             // reference to Vendor
    private List<String> tags;
    private String stockStatus;          // In Stock, Out of Stock
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ========== ODOP Location Fields ==========
    private String originDistrict;       // District where product originates
    private String originState;          // State where product originates
    private String originPinCode;        // PIN code of origin area
    private String localName;            // Product name in local language

    // ========== GI Tag (Geographical Indication) ==========
    private String giTagNumber;          // Official GI tag registration number
    private Boolean giTagCertified;      // Is product GI certified?
    private String giTagCertificateUrl;  // URL to GI certificate image

    // ========== Product Origin Story ==========
    private String originStory;          // Cultural significance & history
    private String craftType;            // Type of craft/product
    private String madeBy;               // Artisan/maker name
    private String materialsUsed;        // Raw materials used

    // ========== Search & Discovery ==========
    private Integer popularityScore;     // For "popular in area" ranking
    private Integer totalSold;           // Units sold (for popularity)

    // ========== Admin Approval System ==========
    private String approvalStatus;       // PENDING, APPROVED, REJECTED
    private String approvedBy;           // adminId who approved/rejected
    private LocalDateTime approvalDate;
    private String rejectionReason;      // If rejected, why?
    private Boolean isActive;            // Product visibility toggle
}
