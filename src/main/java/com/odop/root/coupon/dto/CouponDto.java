package com.odop.root.coupon.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponDto {
    private String code;
    private String description;
    private String discountType;
    private double discountValue;
    private double maxDiscountAmount;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private boolean isActive;
    private int totalUsageLimit;
    private int usagePerCustomer;
    private double minOrderAmount;
    private List<String> applicableCategories;
    private List<String> applicableStates;
    private boolean firstOrderOnly;
}
