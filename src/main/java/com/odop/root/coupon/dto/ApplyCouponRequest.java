package com.odop.root.coupon.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplyCouponRequest {
    private String couponCode;
    private String cartId;
    private double cartTotal;
    private String shippingState;
}
