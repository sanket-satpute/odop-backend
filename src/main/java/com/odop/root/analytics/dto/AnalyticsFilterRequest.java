package com.odop.root.analytics.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsFilterRequest {
    
    private LocalDate startDate;
    private LocalDate endDate;
    private String period;          // TODAY, WEEK, MONTH, QUARTER, YEAR, CUSTOM
    private String state;
    private String district;
    private String categoryId;
    private String vendorId;
    private Boolean giTagOnly;
    private String orderStatus;
    private String paymentStatus;
    private Integer limit;          // For top N queries
    
    public static AnalyticsFilterRequest defaultFilter() {
        return AnalyticsFilterRequest.builder()
                .period("MONTH")
                .limit(10)
                .build();
    }
}
