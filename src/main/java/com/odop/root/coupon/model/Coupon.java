package com.odop.root.coupon.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "coupons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    private String couponId;
    private String code;                    // SAVE20, FREESHIP
    private String description;
    
    // Discount Type
    private String discountType;            // PERCENTAGE, FIXED_AMOUNT, FREE_SHIPPING
    private double discountValue;           // 20 for 20%, 100 for ₹100
    private double maxDiscountAmount;       // Cap for percentage discounts
    
    // Validity
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private boolean isActive;
    
    // Usage Limits
    private int totalUsageLimit;            // Max total uses (0 = unlimited)
    private int usagePerCustomer;           // Max per customer (0 = unlimited)
    private int currentUsageCount;          // Track total uses
    
    // Conditions
    private double minOrderAmount;          // Minimum cart value
    private List<String> applicableCategories;   // null = all categories
    private List<String> applicableProducts;     // null = all products
    private List<String> applicableStates;       // null = all states
    private boolean firstOrderOnly;         // New customers only
    
    // Metadata
    private String createdBy;               // Admin who created
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
