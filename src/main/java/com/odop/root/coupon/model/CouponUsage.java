package com.odop.root.coupon.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "coupon_usages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponUsage {

    @Id
    private String usageId;
    private String couponCode;
    private String customerId;
    private String orderId;
    private double discountApplied;
    private LocalDateTime usedAt;
}
