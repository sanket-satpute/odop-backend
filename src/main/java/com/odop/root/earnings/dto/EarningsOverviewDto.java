package com.odop.root.earnings.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for vendor earnings overview
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EarningsOverviewDto {
    
    // Current period summary
    private double totalEarnings;
    private double pendingPayouts;
    private double availableBalance;
    private double lifetimeEarnings;
    
    // Change percentages (compared to previous period)
    private double earningsChangePercent;
    private double ordersChangePercent;
    
    // Order statistics
    private int totalOrders;
    private int completedOrders;
    private int pendingOrders;
    private int cancelledOrders;
    
    // Platform fees
    private double platformFee;
    private double platformFeeRate; // percentage
    private double netEarnings; // totalEarnings - platformFee
    
    // Period info
    private String period; // WEEK, MONTH, QUARTER, YEAR, ALL_TIME
    private String startDate;
    private String endDate;
    
    // Breakdown
    private List<EarningsBreakdownDto> breakdown;
    private List<EarningsByProductDto> topProducts;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EarningsBreakdownDto {
        private String date;
        private double earnings;
        private int orders;
        private double avgOrderValue;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EarningsByProductDto {
        private String productId;
        private String productName;
        private String productImage;
        private int unitsSold;
        private double totalEarnings;
        private double percentage; // of total earnings
    }
}
