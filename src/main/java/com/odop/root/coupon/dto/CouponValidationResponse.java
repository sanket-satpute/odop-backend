package com.odop.root.coupon.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponValidationResponse {
    private boolean valid;
    private String couponCode;
    private String message;
    private String discountType;
    private double discountValue;
    private double discountAmount;
    private double finalAmount;
}
