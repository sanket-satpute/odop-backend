package com.odop.root.analytics.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesAnalyticsDto {
    
    private double totalRevenue;
    private long totalOrders;
    private double averageOrderValue;
    
    // Revenue breakdown
    private Map<String, Double> revenueByCategory;
    private Map<String, Double> revenueByState;
    private Map<String, Double> revenueByPaymentMethod;
    
    // Order status breakdown
    private Map<String, Long> ordersByStatus;
    private Map<String, Long> ordersByPaymentStatus;
    
    // Time series data
    private List<TimeSeriesDataDto> dailyRevenue;
    private List<TimeSeriesDataDto> dailyOrders;
    
    // Top performers
    private List<TopSellingProductDto> topProducts;
    private List<TopVendorDto> topVendors;
    
    // Period info
    private String period;
    private String startDate;
    private String endDate;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopSellingProductDto {
        private String productId;
        private String productName;
        private String categoryName;
        private String vendorName;
        private long unitsSold;
        private double revenue;
        private String imageUrl;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopVendorDto {
        private String vendorId;
        private String shoppeeName;
        private String location;
        private double revenue;
        private long orders;
        private double rating;
    }
}
